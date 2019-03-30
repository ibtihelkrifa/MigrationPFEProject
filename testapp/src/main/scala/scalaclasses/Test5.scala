package scalaclasses

import java.io.File
import java.sql.DriverManager
import java.util.Properties

import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPath, XPathConstants, XPathFactory}
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Delete, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.json.simple.parser.JSONParser
import org.springframework.cglib.beans.BeanMap
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.w3c.dom.{Document, Element, Node, NodeList}

import scala.util.control.Breaks.breakable

class Test5 {





  def setattributerow(colfamily: String, colname: String, targettable: String, rowdom: Element, rollbackdoc: Document) = {
    var colfamilyattribute=rollbackdoc.createAttribute("Famillecolonne")
    colfamilyattribute.setValue(colfamily)

    rowdom.setAttributeNode(colfamilyattribute)
    var colnameattribute=rollbackdoc.createAttribute("nomcolonne")
    colnameattribute.setValue(colname)
    rowdom.setAttributeNode(colnameattribute)

    var talecibleattribute=rollbackdoc.createAttribute("table")
    talecibleattribute.setValue(targettable)
    rowdom.setAttributeNode(talecibleattribute)
  }


  def savebeanrowincontext(context:StandardEvaluationContext,row:String): BeanMap =
  {
    var parser = new JSONParser

    var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]

    var bean = new BeanGenerators(json).setBeanSchema()
    bean = new BeanGenerators(json).getBean(bean)
    var keys2 = json.keySet()

    keys2.forEach(key => {
      context.setVariable(key.toString, bean.get(key.toString))
    })
    return bean
  }

  def createrollbackfile(rollbackdoc: Document,xpath: XPath, document: Document, document1: Document) = {

      val transformerFactory = TransformerFactory.newInstance
      val transformer = transformerFactory.newTransformer
      val sources = new DOMSource(rollbackdoc)
      var Configuration=  xpath.evaluate("//Configuration", document, XPathConstants.NODE).asInstanceOf[Element]
      val resultat = new StreamResult(new File(Configuration.getAttribute("rollbackfile")))
      transformer.transform(sources, resultat)
  }

  def setGlobalVariablesIntoContext(VariablesGlobales: DataFrame, parsers: SpelExpressionParser, context: StandardEvaluationContext)
  = {
    VariablesGlobales.collectAsList().forEach(vg => {
      var vgformula = vg.getAs[String]("_value")
      var vgexpression = parsers.parseExpression(vgformula)
      var vgvalue = vgexpression.getValue(context).asInstanceOf[Double]
      context.setVariable(vg.getAs[String]("_nom"), vgvalue)
    })
  }


  def setClientFunctionsIntoContext(xpath: XPath, context: StandardEvaluationContext, document: Document) = {
    var numbersofclasses = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").getLength


    var nc = 0;
    var classname = ""
    while (nc < numbersofclasses) {
      classname = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").item(nc).getAttributes.getNamedItem("nomclasse").getNodeValue
      var functionname = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").item(nc).getAttributes.getNamedItem("nomfonction").getNodeValue
      var inputclass = xpath.evaluate("//FonctionClient", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Fonction").item(nc).getAttributes.getNamedItem("InputClass").getNodeValue

      context.registerFunction(functionname, Class.forName(classname).getDeclaredMethod(functionname, Class.forName(inputclass)))

      nc = nc + 1;
    }
  }


  def createrowdom(id: Long,racine: Element, rollbackdoc: Document, toString: String, targettable: String):Element = {


      var rowdom=rollbackdoc.createElement("row")
      racine.appendChild(rowdom)

      var idattribute=rollbackdoc.createAttribute("idrow")
      idattribute.setValue(id.toString)
      rowdom.setAttributeNode(idattribute)

      var talecibleattribute=rollbackdoc.createAttribute("table")
      talecibleattribute.setValue(targettable)
      rowdom.setAttributeNode(talecibleattribute)
      return rowdom

  }






  def Configurer(path:String): String =
  {

    val debut = System.currentTimeMillis

    val spark = SparkSession
      .builder()
      .appName("TestappApplication")
      .config("spark.master", "local[*]")
      .getOrCreate(); // [1]


    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath
    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))


    //read du config file et recuperation des options de connection à la base de donnée



    val source = xpath.evaluate("//source", document, XPathConstants.NODE).asInstanceOf[Element]
    val ip = source.getAttribute("ip")
    val port = source.getAttribute("port")
    val nameBd = source.getAttribute("nomBase")
    val user = source.getAttribute("utilisateur")
    val password = source.getAttribute("motdepasse")


    val jdbcUrl = s"jdbc:mysql://${ip}:${port}/${nameBd}"
    val connectionProperties = new Properties()
    connectionProperties.put("user", s"${user}")
    connectionProperties.put("password", s"${password}")



    DriverManager.getConnection(jdbcUrl, connectionProperties)
    // connection à HBase


    val hbaseoptionsconf= xpath.evaluate("//cible", document, XPathConstants.NODE).asInstanceOf[Element]

    val HbaseConf = HBaseConfiguration.create()
    HbaseConf.set("hbase.master", hbaseoptionsconf.getAttribute("hbase.master"))
    HbaseConf.set("hbase.zookeeper.quorum", hbaseoptionsconf.getAttribute("hbase.zookeeper.quorum"))
    HbaseConf.set("hbase.zookeeper.property.clientPort", hbaseoptionsconf.getAttribute("hbase.zookeeper.property.clientPort"))
    val connection = ConnectionFactory.createConnection(HbaseConf)


    val rollbackdoc= docBuilder.newDocument()
    val racine = rollbackdoc.createElement("Rollback")
    rollbackdoc.appendChild(racine)


    var s = 0
    var c = 0
    var nc = 0
    var test=0
    var keyjoinvalu=""
    val context = new StandardEvaluationContext
    val parsers = new SpelExpressionParser


    var HbaseTables = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element] // get  tables in hbase to work on

    var stciblelength = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").getLength

    s = 0
    while (s < stciblelength) {
      var stciblename = HbaseTables.getElementsByTagName("Table").item(s).getAttributes.getNamedItem("nom").getNodeValue
      var cfnames = HbaseTables.getElementsByTagName("Table").item(s).getAttributes.getNamedItem("CF").getNodeValue
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
    var nbtablessources = xpath.evaluate("//StructureSource", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").getLength
    var tb=0
    while(tb<nbtablessources)
    {
      var tablename = xpath.evaluate("//StructureSource", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").item(tb).getAttributes.getNamedItem("nom").getNodeValue
      var source_table_1 = spark.read.jdbc(jdbcUrl, tablename, connectionProperties)
      source_table_1.createTempView(tablename)
      tb = tb + 1;

    }



    //lire les classes clients et enregistrer les fonctions à utiliser dans le contexte

    setClientFunctionsIntoContext(xpath,context,document)

    // enregistre dans le contexte les variable globales et leurs valeurs
    var VariablesGlobales = spark.read.option("rootTag", "VariablesGlobales")
      .option("rowTag", "vg")
      .format("com.databricks.spark.xml")
      .load(path)

    setGlobalVariablesIntoContext(VariablesGlobales,parsers,context)


    var listparent=  xpath.evaluate("//Transformation[@type='racine']", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    implicit def pimpNodeList(nl: NodeList): Seq[Node] = 0 until nl.getLength map (nl item _)

    var TablesHbasedom=rollbackdoc.createElement("TablesHBase")

    racine.appendChild(TablesHbasedom);

    pimpNodeList(listparent).map(node =>{

      var id= node.getAttributes.getNamedItem("id").getNodeValue
      var idrow=node.getAttributes.getNamedItem("idLigne").getNodeValue
      var tablesource=node.getAttributes.getNamedItem("tablesource").getNodeValue
      var colonnessources=node.getAttributes.getNamedItem("structuresource").getNodeValue
      var targettable=node.getAttributes.getNamedItem("tablecible").getNodeValue
      var keyjoin=""
      var TableHbasedom=rollbackdoc.createElement("Table")

      TablesHbasedom.appendChild(TableHbasedom);

      var tablename=rollbackdoc.createAttribute("nom")
      tablename.setValue(targettable)
      TableHbasedom.setAttributeNode(tablename)
      aggregation("transformation",id.toInt,idrow,tablesource,colonnessources,targettable, keyjoin)

    })

    createrollbackfile(rollbackdoc,xpath,document,rollbackdoc: Document)


    def aggregation(typetransformation:String,incrementaggrega: Int,idrow : String, tablesource: String,colonnessources: String,targettable: String, keyjoin: String): Unit =
    {
      val conn = ConnectionFactory.createConnection(HbaseConf)
      val hAdmin = conn.getAdmin

      var hTable=connection.getTable(TableName.valueOf(targettable))


      if(typetransformation=="transformation")
        {
          var dfaggrega = spark.sql("select " + colonnessources + " from " + tablesource )
          var RichKeyList=xpath.compile("//Transformation[@id='"+incrementaggrega+"']/CartographieCle[@idpere='" + incrementaggrega + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList]
          var g=0;
          dfaggrega.toJSON.collectAsList().forEach(row => {

            var g=0

            var bean=savebeanrowincontext(context,row)

            var idexp = parsers.parseExpression(idrow)
            var id = idexp.getValue(context).asInstanceOf[Long]
            var put = new Put(Bytes.toBytes("row" + id.toString))

            breakable{
              while(g < RichKeyList.getLength) {

                var richnode = RichKeyList.item(g)

                var valueexp = richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
                var colfamily = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(0)

                var colname = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(1)

                var rowdom= createrowdom(id,racine,rollbackdoc,id.toString,targettable)


                  var valueparse = parsers.parseExpression(valueexp)
                  var finalvalue = valueparse.getValue(context).asInstanceOf[String]
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
                  hTable.put(put)

                  setattributerow(colfamily,colname,targettable,rowdom,rollbackdoc)
                g = g + 1

              }}
            soustransformationfunction(context,xpath,parsers,incrementaggrega)
          })
        }



            else if(typetransformation=="soustransformation")
              {
                var dfaggrega=spark.sql("select " + colonnessources + " from " + tablesource + " where " + keyjoin)
                var DocumenList=xpath.compile("//SousTransformation[@id='"+incrementaggrega+"']/Document[@idpere='" + incrementaggrega + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList]

                var r=0

                dfaggrega.toJSON.collectAsList().forEach(row => {
                  var bean=savebeanrowincontext(context,row)
                  var idexp = parsers.parseExpression(idrow)
                  var id = idexp.getValue(context).asInstanceOf[Long]
                  var put = new Put(Bytes.toBytes("row" + id.toString))
                  var d=0
                  breakable{

                while(d< DocumenList.getLength )
                {

                  var richnode = DocumenList.item(d)

                  var valueexp = richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
                  var colfamily = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(0)
                  var colname = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(1)
                  var rowdom= createrowdom(id,racine,rollbackdoc,id.toString,targettable)
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(bean.toString))
                  setattributerow(colfamily,colname,targettable,rowdom,rollbackdoc)
                  hTable.put(put)
                  r=r+1
                  d=d+1
                }

                    soustransformationfunction(context,xpath,parsers,incrementaggrega)

                  }})

              }

      }


    def soustransformationfunction(context: StandardEvaluationContext, xpath: XPath, parsers: SpelExpressionParser, incrementaggrega: Int): Unit = {


      var subaggregation = xpath.evaluate("//SousTransformation[@idpere='"+incrementaggrega+"']", document, XPathConstants.NODESET).asInstanceOf[NodeList]

      var subinc=0

      while(subinc < subaggregation.getLength) {

        var subaggregation = xpath.evaluate("//SousTransformation[@idpere='" + incrementaggrega + "']", document, XPathConstants.NODESET).asInstanceOf[NodeList].item(subinc)
        var id = subaggregation.getAttributes.getNamedItem("id").getNodeValue
        var idrow = subaggregation.getAttributes.getNamedItem("idLigne").getNodeValue
        var tablesource = subaggregation.getAttributes.getNamedItem("tablesource").getNodeValue
        var colonnessources = subaggregation.getAttributes.getNamedItem("structuresource").getNodeValue
        var targettable = subaggregation.getAttributes.getNamedItem("tablecible").getNodeValue

        var keyjoin = subaggregation.getAttributes.getNamedItem("CleJointure").getNodeValue
        var keyparse = parsers.parseExpression(keyjoin)
        var keyjoinvalue = keyparse.getValue(context).asInstanceOf[String]

        subinc = subinc + 1
        aggregation("soustransformation", id.toInt, idrow, tablesource, colonnessources, targettable, keyjoinvalue)
        keyjoin = ""
      }


    }


    return (System.currentTimeMillis()-debut).toString
  }





}
