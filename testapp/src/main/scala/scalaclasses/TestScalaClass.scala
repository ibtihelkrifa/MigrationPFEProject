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
import org.apache.hadoop.hbase.client.{ConnectionFactory, HTable, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SparkSession
import org.json.simple.parser.JSONParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext

import scala.collection.JavaConverters._

class TestScalaClass {


  def configure2(path: String): String = {

    val spark = SparkSession
      .builder()
      .appName("TestappApplication")
      .config("spark.master", "local[*]")
      .getOrCreate(); // [1]


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
    val df2 = spark.read
      .option("rowTag", "transformation")
      .format("com.databricks.spark.xml")
      .load(path)

 try{

    val HbaseConf = HBaseConfiguration.create()
    HbaseConf.set("hbase.master", "localhost")
    HbaseConf.set("hbase.zookeeper.quorum", "localhost")
    HbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
    val connection = ConnectionFactory.createConnection(HbaseConf)

    val Bdcibledf = spark.sql("""select xmltable.StructureCible.name from xmltable """);
    val Bdciblename = Bdcibledf.collectAsList().get(0).get(0).toString()

    //créer le descripteur de la base de donnée cible

    val tableDescriptor = new HTableDescriptor(TableName.valueOf(Bdciblename))
    var cols = new util.ArrayList[String]()
    val df3 = spark.read
      .option("rowTag", "cols")
      .xml(path)

    df3.select("_CF").collectAsList().get(0).get(0).toString.split(";")
      .foreach(s => {
        cols.add(s)
      })


    for (c: String <- cols.asScala) {
      val family = new HColumnDescriptor(Bytes.toBytes(c))
      tableDescriptor.addFamily(family)
    }

    // se connecter à la base HBase et création de la table

    val admin = connection.getAdmin

    if (!admin.tableExists(tableDescriptor.getTableName))
      admin.createTable(tableDescriptor)
    var parser = new JSONParser
    val context = new StandardEvaluationContext()
    val parsers = new SpelExpressionParser


    var VariablesGlobales= spark.read.option("rootTag","VariablesGlobales")
      .option("rowTag","vg")
      .format("com.databricks.spark.xml")
      .load(path)
    context.registerFunction("getcurrency", classOf[TestService].getDeclaredMethod("getcurrency", classOf[String]))


    VariablesGlobales.collectAsList().forEach(vg=>{
      var vgformula= vg.getAs[String]("_value")
      var vgexpression= parsers.parseExpression(vgformula)
      var vgvalue= vgexpression.getValue(context).asInstanceOf[Double]
      context.setVariable(vg.getAs[String]("_name"),vgvalue)
    })

    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))
    val nListlength = document.getElementsByTagName("transformation").getLength// get how many transformation in the config file
    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath

    var i=0;
    var table_target_name=""
    var table_source_name=""
    var ColumnQualifier=""
    var colfamily=""
    var colname=""
    var lengthrich=0
    var mappingformula=""
    while(i<nListlength)
    {
      var element = xpath.evaluate("//transformation[@id='"+i+"']", document, XPathConstants.NODE).asInstanceOf[Element]
      table_source_name=element.getAttribute("tablesource")


       lengthrich=element.getElementsByTagName("RichKeyMapping").getLength// get how many rich key mapping in the transformation
       var j=0;
      while(j<lengthrich)
        {

          table_target_name=element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("targettable").getNodeValue
          ColumnQualifier=element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("columnqualifier").getNodeValue
          colfamily = ColumnQualifier.split(":").apply(0)
          colname = ColumnQualifier.split(":").apply(1)
          mappingformula=element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("mappingformula").getNodeValue

          var idrow=element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("idrow").getNodeValue
          val hTable = new HTable(HbaseConf, table_target_name)

          var exp = parsers.parseExpression(mappingformula)
          var current_table = spark.read.jdbc(jdbcUrl, table_source_name, connectionProperties)
          var condition= element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("condition").getNodeValue
          var ignore= element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("ignore").getNodeValue
          var expp = parsers.parseExpression(condition)
          var idexp=parsers.parseExpression(idrow)
          current_table.toJSON.collectAsList().forEach(row => {

            var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]

            var bean = new BeanGenerators(json).setBeanSchema()
            bean = new BeanGenerators(json).getBean(bean)
            var keys = json.keySet()
            context.registerFunction("getAge", classOf[TestService].getDeclaredMethod("getAge", classOf[String]))
            context.registerFunction("formatNumber", classOf[TestService].getDeclaredMethod("formatNumber", classOf[Double]))
            keys.forEach(key => {
              context.setVariable(key.toString, bean.get(key.toString))
            })
            var id= idexp.getValue(context).asInstanceOf[Long]
            var put = new Put(Bytes.toBytes("row" + id.toString))
            var valueCondition = expp.getValue(context).asInstanceOf[Boolean]
            if(valueCondition)
            {
              var expression = exp.getValue(context).asInstanceOf[String]
              put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(expression))
              hTable.put(put)
            }
            else if(ignore=="false")
            {
              put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes("null"))
              hTable.put(put)
            }
        })
            j=j+1
        }

            i=i+1
    }

  }

 catch {
   case e: ScriptException => e.printStackTrace
     return "false"
 }
    return "done"
}}
