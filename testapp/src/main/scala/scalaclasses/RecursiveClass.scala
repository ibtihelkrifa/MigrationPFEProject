package scalaclasses

import java.io.File
import java.sql.DriverManager
import java.util
import java.util.Properties

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import com.databricks.spark.xml._
import com.vermeg.testapp.services.TestService
import javax.script.ScriptException
import org.w3c.dom.{Element, Node, NodeList}
import javax.xml.xpath.XPathFactory
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Delete, HTable, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SparkSession
import org.json.simple.parser.JSONParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.client.Connection
import org.apache.hadoop.hbase.client.ConnectionFactory
import scala.util.control.Breaks._

class RecursiveClass {


  def configure3(path: String): String = {



    try{

      //create a spark session
      val spark = SparkSession
        .builder()
        .appName("TestappApplication")
        .config("spark.master", "local[*]")
        .getOrCreate(); // [1]

      //read du config file et recuperation des options de connection à la base de donnée
      var df = spark.read
        .option("rowTag", "debut")
        .format("com.databricks.spark.xml")
        .load(path)


      df.createOrReplaceTempView("xmltable");

      val source = spark.sql("""select xmltable.source.ip, xmltable.source.port,xmltable.source.name,xmltable.source.user,xmltable.source.password from xmltable """);
      val ip = source.collectAsList().get(0).get(0).toString
      val port = source.collectAsList().get(0).get(1).toString
      val nameBd = source.collectAsList().get(0).get(2).toString
      val user = source.collectAsList().get(0).get(3).toString
      val password = source.collectAsList().get(0).get(4).toString
      val jdbcUrl = s"jdbc:mysql://${ip}:${port}/${nameBd}"
      val connectionProperties = new Properties()
      connectionProperties.put("user", s"${user}")
      connectionProperties.put("password", s"${password}")
      DriverManager.getConnection(jdbcUrl, connectionProperties)
      // connection à HBase
      val HbaseConf = HBaseConfiguration.create()
      HbaseConf.set("hbase.master", "localhost")
      HbaseConf.set("hbase.zookeeper.quorum", "localhost")
      HbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
      val connection = ConnectionFactory.createConnection(HbaseConf)




      // pour lire le fichier de config on a construti un document builder




      //déclaration des variables
      var valueexp=""
      var tablesources = ""
      var selectedcolumns = ""
      var s = 0
      var c = 0
      var nc = 0
      var test=0
      var parser = new JSONParser
      val context = new StandardEvaluationContext
    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))

    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath
      val parsers = new SpelExpressionParser

      var HbaseTables = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element] // get  tables in hbase to work on

      var stciblelength = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("StructureCible").getLength

      s = 0
      while (s < stciblelength) {
        var stciblename = HbaseTables.getElementsByTagName("StructureCible").item(s).getAttributes.getNamedItem("name").getNodeValue
        var cfnames = HbaseTables.getElementsByTagName("StructureCible").item(s).getAttributes.getNamedItem("CF").getNodeValue
        val tableDescriptor = new HTableDescriptor(TableName.valueOf(stciblename))
        val nbcf = cfnames.split(";").length
        c = 0
        while (c < nbcf) {
          val family = new HColumnDescriptor(Bytes.toBytes(cfnames.split(";").apply(c)))
          tableDescriptor.addFamily(family)

          c = c + 1
        }


        val admin = connection.getAdmin

        if (!admin.tableExists(tableDescriptor.getTableName))
          admin.createTable(tableDescriptor)


        s = s + 1;
      }

      //lire les tables sources  qu'on va utiliser
      var nbtablessources = xpath.evaluate("//TablesSources", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").getLength
      var tb=0
      while(tb<nbtablessources)
      {
        var tablename = xpath.evaluate("//TablesSources", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").item(tb).getAttributes.getNamedItem("name").getNodeValue
        var source_table_1 = spark.read.jdbc(jdbcUrl, tablename, connectionProperties)
        source_table_1.createTempView(tablename)
        tb = tb + 1;

      }



      //lire les classes clients et enregistrer les fonctions à utiliser dans le contexte


      var numbersofclasses = xpath.evaluate("//ClientClass", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Function").getLength


      nc = 0;
      var classname = ""
      while (nc < numbersofclasses) {
        classname = xpath.evaluate("//ClientClass", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Function").item(nc).getAttributes.getNamedItem("NameClass").getNodeValue
        var functionname = xpath.evaluate("//ClientClass", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Function").item(nc).getAttributes.getNamedItem("NameFunction").getNodeValue
        var inputclass = xpath.evaluate("//ClientClass", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Function").item(nc).getAttributes.getNamedItem("InputClass").getNodeValue

        context.registerFunction(functionname, Class.forName(classname).getDeclaredMethod(functionname, Class.forName(inputclass)))

        nc = nc + 1;
      }

      // enregistre dans le contexte les variable globales et leurs valeurs
      var VariablesGlobales = spark.read.option("rootTag", "VariablesGlobales")
        .option("rowTag", "vg")
        .format("com.databricks.spark.xml")
        .load(path)


      VariablesGlobales.collectAsList().forEach(vg => {
        var vgformula = vg.getAs[String]("_value")
        var vgexpression = parsers.parseExpression(vgformula)
        var vgvalue = vgexpression.getValue(context).asInstanceOf[Double]
        context.setVariable(vg.getAs[String]("_name"), vgvalue)
      })





      var numberOfAggrigations = xpath.evaluate("//aggregation", document, XPathConstants.NODESET).asInstanceOf[NodeList].getLength

      var agg=0
      var LengthOfRichKey=0
      while(agg < numberOfAggrigations) {

        var RichKeyList=xpath.compile("//aggregation[@id='"+agg+"']/RichKeyMapping[@id='" + agg + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList]

        var currentaggregation= xpath.compile("//aggregation[@id='"+agg+"']").evaluate(document,XPathConstants.NODE).asInstanceOf[Node]
        var tablesources=currentaggregation.getAttributes.getNamedItem("tablesource").getNodeValue
        var idrow = currentaggregation.getAttributes.getNamedItem("idrow").getNodeValue

         selectedcolumns = currentaggregation.getAttributes.getNamedItem("colonnessources").getNodeValue
        var targettable = currentaggregation.getAttributes.getNamedItem("targettable").getNodeValue




        val conn = ConnectionFactory.createConnection(HbaseConf)
        val hAdmin = conn.getAdmin

       var hTable=connection.getTable(TableName.valueOf(targettable))

        val dfaggrega = spark.sql("select " + selectedcolumns + " from " + tablesources )

        dfaggrega.toJSON.collectAsList().forEach(row => {

          var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]

          var bean = new BeanGenerators(json).setBeanSchema()
          bean = new BeanGenerators(json).getBean(bean)
          var keys2 = json.keySet()

          keys2.forEach(key => {
            context.setVariable(key.toString, bean.get(key.toString))
          })

          var idexp = parsers.parseExpression(idrow)
          var id = idexp.getValue(context).asInstanceOf[String]
          var put = new Put(Bytes.toBytes("row" + id))

          var g = 0
          breakable{

            while (g < RichKeyList.getLength) {


              var richnode=xpath.compile("//aggregation[@id='"+agg+"']/RichKeyMapping[@id='" + agg + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList].item(g)

               valueexp = richnode.getAttributes.getNamedItem("mappingformula").getNodeValue
              var colfamily=richnode.getAttributes.getNamedItem("columnqualifier").getNodeValue.split(":").apply(0)

              var colname=richnode.getAttributes.getNamedItem("columnqualifier").getNodeValue.split(":").apply(1)

              if (richnode.getAttributes.getNamedItem("type") == null) {

                var valueparse = parsers.parseExpression(valueexp)
                var finalvalue = valueparse.getValue(context).asInstanceOf[String]
                put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))

                hTable.put(put)

              }

              else if(richnode.getAttributes.getNamedItem("type") == "Document")
                {

                  var keyjoin = currentaggregation.getAttributes.getNamedItem("KeyJoin").getNodeValue
                  var exppkey = parsers.parseExpression(keyjoin)

                  var keyvalue = exppkey.getValue(context).asInstanceOf[String]

                  var dfaggrega = spark.sql("select " + valueexp + " from " + tablesources + " where " + keyvalue)
                  var r = 0;
                  dfaggrega.toJSON.collectAsList().forEach(row => {

                    var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]
                    put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(json.toJSONString))
                    hTable.put(put)

                    r = r + 1;
                  })
                }





                g=g+1
            }}


        })

            agg=agg+1

      }




 ///////   var nodelist=xpath.compile("//aggregation[@id='"+1+"']/RichKeyMapping[@id='" + 1 + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList]









      return valueexp


    }

    catch
      {
        case e: ScriptException => e.printStackTrace
          return "false"      }
  }




}
