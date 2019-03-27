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
import org.w3c.dom.Element
import javax.xml.xpath.XPathFactory
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Delete, HTable, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SparkSession
import org.json.simple.parser.JSONParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.apache.hadoop.hbase.util.Bytes
import scala.util.control.Breaks._
import scala.collection.JavaConverters._

class TestScalaClass {


  def configure2(path: String): String = {
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

    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))

    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath


    //déclaration des variables

    var tablesources = ""
    var selectedcolumns = ""
    var s = 0
    var c = 0
    var nc = 0
     var test=""
    var parser = new JSONParser
    val context = new StandardEvaluationContext()
    val parsers = new SpelExpressionParser

    // commençer le traitement des transformations
    try {

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


      val aggregListlength = document.getElementsByTagName("aggregation").getLength
      // get how many aggregation in aggregations
      var k = 0
      while (k < aggregListlength) {

        var elementaggrega = xpath.evaluate("//aggregation[@id='" + k + "']", document, XPathConstants.NODE).asInstanceOf[Element]
        var idrow = elementaggrega.getAttribute("idrow")
        tablesources = elementaggrega.getAttribute("tablesource")
        selectedcolumns = elementaggrega.getAttribute("colonnessources")
        var targettable = elementaggrega.getAttribute("targettable")
        val hTable = new HTable(HbaseConf, targettable)


        val dfaggrega = spark.sql("select " + selectedcolumns + " from " + tablesources)

        dfaggrega.toJSON.collectAsList().forEach(row => {

          var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]

          var bean = new BeanGenerators(json).setBeanSchema()
          bean = new BeanGenerators(json).getBean(bean)
          var keys2 = json.keySet()

          keys2.forEach(key => {
            context.setVariable(key.toString, bean.get(key.toString))
          })

          var targetcolonneslength = elementaggrega.getElementsByTagName("RichKeyMapping").getLength

          var idexp = parsers.parseExpression(idrow)
          var id = idexp.getValue(context).asInstanceOf[String]
          var put = new Put(Bytes.toBytes("row" + id))

          var g = 0
          breakable{

          while (g < targetcolonneslength) {
            var valueexp = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("mappingformula").getNodeValue
            var colfamily = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("columnqualifier").getNodeValue.split(":").apply(0)
            var colname = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("columnqualifier").getNodeValue.split(":").apply(1)




            var pattern = ""
            var patternboolean = true
            var valueCondition=true


            if (elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("type") == null) {


                if (elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("condition") != null) {
                  var condition = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("condition").getNodeValue
                  var expp = parsers.parseExpression(condition)
                  valueCondition = expp.getValue(context).asInstanceOf[Boolean]
                }

                  if (valueCondition) {


                    if (elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("formatters") != null) {
                      var formatters = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("formatters").getNodeValue
                      var parseformat = parsers.parseExpression(formatters)
                      parseformat.getValue(context).asInstanceOf[java.lang.String]
                    }



                    if (elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("pattern") != null) {

                      pattern = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("pattern").getNodeValue
                      var parsepattern = parsers.parseExpression(pattern).getValue(context).asInstanceOf[Boolean]
                      if (!parsepattern ) {
                        patternboolean = false
                      }
                    }



                    if (patternboolean == true) {

                      var valueparse = parsers.parseExpression(valueexp)
                      var finalvalue = valueparse.getValue(context).asInstanceOf[String]
                      put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
                      hTable.put(put)
                    }

                  }
                  else  {

                    val d = new Delete(Bytes.toBytes("row" + id))
                    hTable.delete(d)
                    break

                  }

              }

            else {
              var typemap = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("type").getNodeValue

              if (typemap == "Document") {
                var tablesourcename = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("tablesource").getNodeValue


                var keyjoin = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("keyJoin").getNodeValue
                var exppkey = parsers.parseExpression(keyjoin)

                var keyvalue = exppkey.getValue(context).asInstanceOf[String]

                var dfaggrega = spark.sql("select " + valueexp + " from " + tablesourcename + " where " + keyvalue)
                var r = 0;
                dfaggrega.toJSON.collectAsList().forEach(row => {

                  var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(json.toJSONString))
                  hTable.put(put)

                  r = r + 1;
                })
              }




            }
            g = g + 1
            patternboolean=true
          }

        }
          keys2.forEach(key => {
            context.setVariable(key.toString, null)
          })

        })


        k = k + 1
      }
      return test
    }

    catch {
      case e: ScriptException => e.printStackTrace
        return "false"
    }

  }
}
