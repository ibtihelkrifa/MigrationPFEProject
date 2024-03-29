package scala.vermeg.sevices

import java.io.File
import java.sql.DriverManager
import java.util.Properties

import com.vermeg.app.exceptions.PatternNotFoundException
import com.vermeg.app.services.TestService
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

import scala.util.control.Breaks.{break, breakable}

class UserService {





  def DateConverter(context: StandardEvaluationContext, richnode: Node, parsers: SpelExpressionParser): String={

    var pattern=""
    var formattedvalue=""

    if(richnode.getAttributes.getNamedItem("pattern") != null)
      {
       pattern= richnode.getAttributes.getNamedItem("pattern").getNodeValue
        val valuenode=richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
        var value= parsers.parseExpression(valuenode).getValue(context).asInstanceOf[String]

        formattedvalue = TestService.FormatDate(value,pattern)
      }
    else
      {
        throw new PatternNotFoundException("pattern is null when using converter; set your pattern please")
      }


    return formattedvalue;
  }

  def NumberCOnverter(context: StandardEvaluationContext, richnode: Node, parsers: SpelExpressionParser): String={

  var pattern=""
    var formattedvalue=""

    if(richnode.getAttributes.getNamedItem("pattern") != null)
    {
     pattern= richnode.getAttributes.getNamedItem("pattern").getNodeValue
      val valuenode=richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
      var value= parsers.parseExpression(valuenode).getValue(context).asInstanceOf[java.lang.Double]
       formattedvalue = TestService.formatNumber(value,pattern)

    }
    else
    {
      throw new PatternNotFoundException("pattern is null when using converter; set your pattern please")
    }


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
    //var Configuration=  xpath.evaluate("//Configuration", document, XPathConstants.NODE).asInstanceOf[Element]
    val resultat = new StreamResult(new File("rollbackconf.xml"))
    transformer.transform(sources, resultat)

  }



  def UserFunctionsetGlobalVariablesIntoContext(xpath :XPath, document:Document, spark:SparkSession, parsers: SpelExpressionParser, context: StandardEvaluationContext)
  = {


    var vgexpression=parsers.parseExpression("#getcurrency('http://demo1525832.mockable.io/currency','tauxr')")
    var vgvalue= vgexpression.getValue(context).asInstanceOf[Double]
    context.setVariable("currency",vgvalue)

    vgexpression=parsers.parseExpression("#getcurrency('https://free.currconv.com/api/v7/convert?q=EUR_TND&compact=ultra&apiKey=04f0757040a62121e421','EUR_TND')")
    vgvalue=vgexpression.getValue(context).asInstanceOf[Double]
    context.setVariable("EUR_TND",vgvalue)


    vgexpression=parsers.parseExpression("#getcurrency('https://free.currconv.com/api/v7/convert?q=TND_EUR&compact=ultra&apiKey=04f0757040a62121e421','TND_EUR')")
    vgvalue=vgexpression.getValue(context).asInstanceOf[Double]
    context.setVariable("TND_EUR",vgvalue)


  }


  def UserFunctionsetClientFunctionsIntoContext(xpath: XPath, context: StandardEvaluationContext, document: Document) = {
   /* var classes = xpath.evaluate("//Fonction", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    var nc = 0;
    while (nc < classes.getLength) {
      var   classname = classes.item(nc).getAttributes.getNamedItem("nomclasse").getNodeValue
      var functionname = classes.item(nc).getAttributes.getNamedItem("nomfonction").getNodeValue
      var inputclass = classes.item(nc).getAttributes.getNamedItem("InputClass").getNodeValue

      context.registerFunction(functionname, Class.forName(classname).getDeclaredMethod(functionname, Class.forName(inputclass)))

      nc = nc + 1;
    }*/

    context.registerFunction("CONCAT", Class.forName("com.vermeg.app.services.TestService").getDeclaredMethod("CONCAT", Class.forName("java.lang.String"),Class.forName("java.lang.String")))

    context.registerFunction("LOWER", Class.forName("com.vermeg.app.services.TestService").getDeclaredMethod("LOWER", Class.forName("java.lang.String")))
    context.registerFunction("UPPER", Class.forName("com.vermeg.app.services.TestService").getDeclaredMethod("UPPER", Class.forName("java.lang.String")))
    context.registerFunction("getAge", Class.forName("com.vermeg.app.services.TestService").getDeclaredMethod("getAge", Class.forName("java.lang.String")))
    context.registerFunction("getcurrency", Class.forName("com.vermeg.app.services.TestService").getDeclaredMethod("getcurrency", Class.forName("java.lang.String"), Class.forName("java.lang.String")))
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

    var element1= xpath.evaluate("//Transformation",document,XPathConstants.NODESET).asInstanceOf[NodeList]
    var tablesources=""

    if(element1.getLength != 0)
    {
      tablesources=element1.item(0).getAttributes.getNamedItem("tablesource").getNodeValue
    }

    var el1=1
    while(el1 < element1.getLength)
    {

      tablesources=tablesources+","+element1.item(el1).getAttributes.getNamedItem("tablesource").getNodeValue
      el1=el1+1
    }

    var element2= xpath.evaluate("//Document",document,XPathConstants.NODESET).asInstanceOf[NodeList]

    var el2=0
    /*if(element2.getLength != 0)
      {
        tablesources=tablesources+","
      }*/
    while(el2 < element2.getLength)
    {
      tablesources=tablesources+","+element2.item(el2).getAttributes.getNamedItem("tablesource").getNodeValue
      el2=el2+1
    }

    var nbtablessources= tablesources.split(",").length

    // var nbtablessources = xpath.evaluate("//StructureSource", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").getLength
    var tb=0
    while(tb<nbtablessources)
    {
      var tablename= tablesources.split(",").apply(tb)
      //var tablename = xpath.evaluate("//StructureSource", document, XPathConstants.NODE).asInstanceOf[Element].getElementsByTagName("Table").item(tb).getAttributes.getNamedItem("nom").getNodeValue
      var source_table_1 = spark.read.jdbc(jdbcUrl, tablename, connectionProperties)
      try{

        source_table_1.createTempView(tablename)
        tb=tb+1
      }

      catch
        {
          case e: Exception=> tb=tb+1
        }

    }

  }

  def UserFunctionCreateSparkSession():SparkSession ={

    val spark = SparkSession
      .builder()
      .appName("TestappApplication")
      .config("spark.master", "local[*]").config("memory","AVAILABLE_MEMORY_MB")
      .getOrCreate(); // [1]
    return spark
  }



}
