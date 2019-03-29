package scalaclasses

import java.io.File
import java.sql.DriverManager
import java.util.Properties

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SparkSession
import org.json.simple.parser.JSONParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.w3c.dom.{Element, Node, NodeList}

import scala.util.control.Breaks.breakable

class Test5 {
  def Configurer(path:String): String =
  {

    val debut = System.currentTimeMillis

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

    val source = spark.sql("""select xmltable.source.ip, xmltable.source.port,xmltable.source.nom,xmltable.source.user,xmltable.source.password from xmltable """);
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




    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))

    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath

    var s = 0
    var c = 0
    var nc = 0
    var test=0
    var keyjoinvalu=""
    var parser = new JSONParser
    val context = new StandardEvaluationContext
    val parsers = new SpelExpressionParser

    var HbaseTables = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element] // get  tables in hbase to work on

    var stciblelength = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("StructureCible").getLength

    s = 0
    while (s < stciblelength) {
      var stciblename = HbaseTables.getElementsByTagName("StructureCible").item(s).getAttributes.getNamedItem("nom").getNodeValue
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
      var tablename = xpath.evaluate("//TablesSources", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").item(tb).getAttributes.getNamedItem("nom").getNodeValue
      var source_table_1 = spark.read.jdbc(jdbcUrl, tablename, connectionProperties)
      source_table_1.createTempView(tablename)
      tb = tb + 1;

    }



    //lire les classes clients et enregistrer les fonctions à utiliser dans le contexte


    var numbersofclasses = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").getLength


    nc = 0;
    var classname = ""
    while (nc < numbersofclasses) {
      classname = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").item(nc).getAttributes.getNamedItem("nomclasse").getNodeValue
      var functionname = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").item(nc).getAttributes.getNamedItem("nomfonction").getNodeValue
      var inputclass = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").item(nc).getAttributes.getNamedItem("InputClass").getNodeValue

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
      context.setVariable(vg.getAs[String]("_nom"), vgvalue)
    })









    var listparent=  xpath.evaluate("//Transformation[@type='racine']", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    implicit def pimpNodeList(nl: NodeList): Seq[Node] = 0 until nl.getLength map (nl item _)



    pimpNodeList(listparent).map(node =>{

      var id= node.getAttributes.getNamedItem("id").getNodeValue
      var idrow=node.getAttributes.getNamedItem("idLigne").getNodeValue
      var tablesource=node.getAttributes.getNamedItem("tablesource").getNodeValue
      var colonnessources=node.getAttributes.getNamedItem("structuresource").getNodeValue
      var targettable=node.getAttributes.getNamedItem("tablecible").getNodeValue
      var keyjoin=""


      aggregation(id.toInt,idrow,tablesource,colonnessources,targettable, keyjoin)

    })




    def aggregation(incrementaggrega: Int,idrow : String, tablesource: String,colonnessources: String,targettable: String, keyjoin: String): Unit =
    {


      var dfaggrega = spark.sql("select " + colonnessources + " from " + tablesource )

      if(keyjoin.length> " ".length)
      {
        dfaggrega=spark.sql("select " + colonnessources + " from " + tablesource + " where " + keyjoin)
      }

      val conn = ConnectionFactory.createConnection(HbaseConf)
      val hAdmin = conn.getAdmin

      var hTable=connection.getTable(TableName.valueOf(targettable))

      var RichKeyList=xpath.compile("//Transformation[@id='"+incrementaggrega+"']/CartographieCle[@pere='" + incrementaggrega + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList]

      var g=0;

      var r=0
      dfaggrega.toJSON.collectAsList().forEach(row => {

        var g=0

        var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]

        var bean = new BeanGenerators(json).setBeanSchema()
        bean = new BeanGenerators(json).getBean(bean)
        var keys2 = json.keySet()

        keys2.forEach(key => {
          context.setVariable(key.toString, bean.get(key.toString))
        })

        var idexp = parsers.parseExpression(idrow)
        var id = idexp.getValue(context).asInstanceOf[Long]
        var put = new Put(Bytes.toBytes("row" + id.toString))

        breakable{

          while(g < RichKeyList.getLength) {

            var richnode = RichKeyList.item(g)

            var valueexp = richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
            var colfamily = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(0)

            var colname = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(1)


            if (richnode.getAttributes.getNamedItem("type") == null) {

              var valueparse = parsers.parseExpression(valueexp)
              var finalvalue = valueparse.getValue(context).asInstanceOf[String]
              put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))

              hTable.put(put)
            }

            else if(richnode.getAttributes.getNamedItem("type").getNodeValue == "Document" )
            {

              put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(bean.toString))
              hTable.put(put)

              r=r+1
            }


            g = g + 1

          }
          var subaggregation = xpath.evaluate("//Transformation[@pere='"+incrementaggrega+"']", document, XPathConstants.NODESET).asInstanceOf[NodeList]

          var subinc=0

          while(subinc < subaggregation.getLength)
          {

            var subaggregation=xpath.evaluate("//Transformation[@pere='"+incrementaggrega+"']", document, XPathConstants.NODESET).asInstanceOf[NodeList].item(subinc)
            var id= subaggregation.getAttributes.getNamedItem("id").getNodeValue
            var idrow=subaggregation.getAttributes.getNamedItem("idLigne").getNodeValue
            var tablesource=subaggregation.getAttributes.getNamedItem("tablesource").getNodeValue
            var colonnessources=subaggregation.getAttributes.getNamedItem("structuresource").getNodeValue
            var targettable=subaggregation.getAttributes.getNamedItem("tablecible").getNodeValue

            var keyjoin=subaggregation.getAttributes.getNamedItem("CleJointure").getNodeValue
            var keyparse=parsers.parseExpression(keyjoin)
            var keyjoinvalue = keyparse.getValue(context).asInstanceOf[String]

            subinc=subinc+1
            aggregation(id.toInt,idrow,tablesource,colonnessources,targettable,keyjoinvalue)
            keyjoin=""

          }

        }


      })


    }




    return (System.currentTimeMillis()-debut).toString
  }
}
