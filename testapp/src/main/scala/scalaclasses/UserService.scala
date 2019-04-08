package scalaclasses

import java.io.File
import java.sql.DriverManager
import java.util.Properties

import com.vermeg.testapp.services.TestService
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.{XPath, XPathConstants}
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.SparkSession
import org.json.simple.parser.JSONParser
import org.springframework.cglib.beans.BeanMap
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.w3c.dom.{Document, Element, Node, NodeList}

class UserService {



  def convertValues(context: StandardEvaluationContext, richnode: Node, parsers: SpelExpressionParser) = {
    val converter= richnode.getAttributes.getNamedItem("converter").getNodeValue

    if(converter == "convertDateTime")
    {
      val pattern= richnode.getAttributes.getNamedItem("pattern").getNodeValue
      val valuenode=richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
      var value= parsers.parseExpression(valuenode).getValue(context).asInstanceOf[String]
      val formattedvalue = TestService.FormatDate(value,pattern)
      var updatedvalue= valuenode.split("#").apply(1)
      context.setVariable(updatedvalue,formattedvalue)
    }
    else if(converter == "NumberConverter")
      {
        val pattern= richnode.getAttributes.getNamedItem("pattern").getNodeValue
        val valuenode=richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
        var value= parsers.parseExpression(valuenode).getValue(context).asInstanceOf[java.lang.Double]
        val formattedvalue = TestService.formatNumber(value,pattern)
        var updatedvalue= valuenode.split("#").apply(1)
        context.setVariable(updatedvalue,formattedvalue)

      }

  }


  def DateConverter(context: StandardEvaluationContext, richnode: Node, parsers: SpelExpressionParser): String={

    val pattern= richnode.getAttributes.getNamedItem("pattern").getNodeValue
    val valuenode=richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
    var value= parsers.parseExpression(valuenode).getValue(context).asInstanceOf[String]
    val formattedvalue = TestService.FormatDate(value,pattern)

    return formattedvalue;
  }

  def NumberCOnverter(context: StandardEvaluationContext, richnode: Node, parsers: SpelExpressionParser): String={

    val pattern= richnode.getAttributes.getNamedItem("pattern").getNodeValue
    val valuenode=richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
    var value= parsers.parseExpression(valuenode).getValue(context).asInstanceOf[java.lang.Double]
    val formattedvalue = TestService.formatNumber(value,pattern)
    return formattedvalue;
  }





  def setattributerowRollback(colfamily: String, colname: String, targettable: String, rowdom: Element, rollbackdoc: Document) = {
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



  def UserFunctionsetGlobalVariablesIntoContext(xpath :XPath, document:Document, spark:SparkSession, parsers: SpelExpressionParser, context: StandardEvaluationContext)
  = {

    var VariablesGlobales=xpath.evaluate("//vg", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    var g=0

    while(g < VariablesGlobales.getLength)
    {
      var vgformula = VariablesGlobales.item(g).getAttributes.getNamedItem("value").getNodeValue
      var nom=VariablesGlobales.item(g).getAttributes.getNamedItem("nom").getNodeValue
      var vgexpression = parsers.parseExpression(vgformula)
      var vgvalue = vgexpression.getValue(context).asInstanceOf[Double]
      context.setVariable(nom, vgvalue)
      g=g+1
    }

  }


  def UserFunctionsetClientFunctionsIntoContext(xpath: XPath, context: StandardEvaluationContext, document: Document) = {
    var classes = xpath.evaluate("//Fonction", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    var nc = 0;
    while (nc < classes.getLength) {
      var   classname = classes.item(nc).getAttributes.getNamedItem("nomclasse").getNodeValue
      var functionname = classes.item(nc).getAttributes.getNamedItem("nomfonction").getNodeValue
      var inputclass = classes.item(nc).getAttributes.getNamedItem("InputClass").getNodeValue

      context.registerFunction(functionname, Class.forName(classname).getDeclaredMethod(functionname, Class.forName(inputclass)))

      nc = nc + 1;
    }
  }


  def createrowdom(id: String,racine: Element, rollbackdoc: Document, targettable: String):Element = {


    var rowdom=rollbackdoc.createElement("row")
    racine.appendChild(rowdom)

    var idattribute=rollbackdoc.createAttribute("idrow")
    idattribute.setValue(id)
    rowdom.setAttributeNode(idattribute)

    var talecibleattribute=rollbackdoc.createAttribute("table")
    talecibleattribute.setValue(targettable)
    rowdom.setAttributeNode(talecibleattribute)
    return rowdom

  }



  def UserFunctiongetHBaseConnection(xpath: XPath, document: Document):Connection = {
    val hbaseoptionsconf= xpath.evaluate("//cible", document, XPathConstants.NODE).asInstanceOf[Element]

    val HbaseConf = HBaseConfiguration.create()
    HbaseConf.set("hbase.master", hbaseoptionsconf.getAttribute("hbase.master"))
    HbaseConf.set("hbase.zookeeper.quorum", hbaseoptionsconf.getAttribute("hbase.zookeeper.quorum"))
    HbaseConf.set("hbase.zookeeper.property.clientPort", hbaseoptionsconf.getAttribute("hbase.zookeeper.property.clientPort"))
    val connection = ConnectionFactory.createConnection(HbaseConf)
    return connection

  }


  def UserFunctionCreateHbaseTbalesIfNotExists(document: Document,xpath: XPath, connection: Connection) = {

    var HbaseTables = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element] // get  tables in hbase to work on

    var stciblelength = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").getLength

    var   s = 0
    while (s < stciblelength) {
      var stciblename = HbaseTables.getElementsByTagName("Table").item(s).getAttributes.getNamedItem("nom").getNodeValue
      var cfnames = HbaseTables.getElementsByTagName("Table").item(s).getAttributes.getNamedItem("CF").getNodeValue
      val tableDescriptor = new HTableDescriptor(TableName.valueOf(stciblename))
      val nbcf = cfnames.split(";").length
      var   c = 0
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

  }


  def UserFunctionCreatTempViewOfSOurceTbales(document:Document, xpath: XPath, spark: SparkSession) = {
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
    var nbtablessources = xpath.evaluate("//StructureSource", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").getLength
    var tb=0
    while(tb<nbtablessources)
    {
      var tablename = xpath.evaluate("//StructureSource", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").item(tb).getAttributes.getNamedItem("nom").getNodeValue
      var source_table_1 = spark.read.jdbc(jdbcUrl, tablename, connectionProperties)

      source_table_1.createTempView(tablename)
      tb = tb + 1;

    }

  }

  def UserFunctionCreateSparkSession():SparkSession ={

    val spark = SparkSession
      .builder()
      .appName("TestappApplication")
      .config("spark.master", "local[*]")
      .getOrCreate(); // [1]
    return spark
  }


}
