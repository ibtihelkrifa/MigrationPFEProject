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


      // commencer à traiter les simples transformations   (chaque trasnformation est sur une table)

      val nListlength = document.getElementsByTagName("transformation").getLength // get how many transformation in the config file


      var i = 0;
      var table_target_name = ""
      var table_source_name = ""
      var ColumnQualifier = ""
      var colfamily = ""
      var colname = ""
      var lengthrich = 0
      var mappingformula = ""

      //recupérer toutes les tarnsformations
      val Transfromations = xpath.evaluate("//StructuresCibles", document, XPathConstants.NODE).asInstanceOf[Element]

      //dans cette boucle on va travailler sur chaque transformation qui va contenir des rich key mapping sur le table
      while (i < nListlength) {

        var element = xpath.evaluate("//transformation[@id='" + i + "']", document, XPathConstants.NODE).asInstanceOf[Element]
        //nom de la tbale source
        table_source_name = element.getAttribute("tablesource")
        //colonne sources
        var colonnessources=element.getAttribute("colonnessources")
        var current_table=spark.sql("select "+ colonnessources + " from "+ table_source_name)

        // nombre de richkeymapping
        lengthrich = element.getElementsByTagName("RichKeyMapping").getLength
        var j = 0;
        //parcourir les richkeymapping
        while (j < lengthrich) {
          //recuperer le nom du table, et le nom de colonne family et colonne hbase target
          table_target_name = element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("targettable").getNodeValue
          ColumnQualifier = element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("columnqualifier").getNodeValue
          colfamily = ColumnQualifier.split(":").apply(0)
          colname = ColumnQualifier.split(":").apply(1)
          // recuperer la valuer qu'o va enregistrer
          mappingformula = element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("mappingformula").getNodeValue
          //recuperer l'id row de habse
          var idrow = element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("idrow").getNodeValue
          // pointer sur la table hbase ou on va inserer
          val hTable = new HTable(HbaseConf, table_target_name)
          //parser la formule à inserer
          var exp = parsers.parseExpression(mappingformula)
          //lire la table source


          var idexp = parsers.parseExpression(idrow)
          //parcourir la table source
          current_table.toJSON.collectAsList().forEach(row => {
            //faire un acst à chauqe row de la tbale source dans un jsononject
            var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]
            //generer un bean pour chaque json object
            var bean = new BeanGenerators(json).setBeanSchema()
            bean = new BeanGenerators(json).getBean(bean)
            //enregistrer les attricuts du bean et ses valeurs dans le contexte
            var keys = json.keySet()
            keys.forEach(key => {
              context.setVariable(key.toString, bean.get(key.toString))
            })



            if(element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("formatters")!= null)
            {
              var formatters=element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("formatters").getNodeValue
              var parseformat= parsers.parseExpression(formatters)
              parseformat.getValue(context).asInstanceOf[java.lang.String]
            }

            var pattern=""
            var patternboolean=true
            var strictpattern="true"
            if(element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("pattern")!=null)
            {
              strictpattern= element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("strictpattern").getNodeValue

              pattern = element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("pattern").getNodeValue
              var parsepattern=parsers.parseExpression(pattern)
              if(!parsepattern.getValue(context).asInstanceOf[Boolean] && strictpattern=="false")
              {
                patternboolean=false
              }


            }

            var id = idexp.getValue(context).asInstanceOf[Long]
            // construire le put de hbase
            var put = new Put(Bytes.toBytes("row" + id.toString))

            if(patternboolean==true)
              {

                // verifier si'il ya une condition sur le put
                if (element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("condition") != null) {
                  //recuperer la condition et faire son evaluation
                  var condition = element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("condition").getNodeValue
                  var expp = parsers.parseExpression(condition)
                  var valueCondition = expp.getValue(context).asInstanceOf[Boolean]

                  if (valueCondition) {
                    //evaluer l'expression à mettre dans habse
                    var expression = exp.getValue(context).asInstanceOf[java.lang.String]
                    put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(expression))
                    hTable.put(put)
                  }
                  //si la condition ,n'est pas verifié on doit verifier si cette condition est stricte ou pas
                  else if (element.getElementsByTagName("RichKeyMapping").item(j).getAttributes.getNamedItem("strict").getNodeValue == "true") {
                    put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes("null"))
                    hTable.put(put)
                  }

                }
                //si n'a pas de condition
                else {
                  var expression = exp.getValue(context).asInstanceOf[String]
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(expression))
                  hTable.put(put)

                }
              }

          })
          j = j + 1
        }
        i = i + 1
      }


      var aggregationslength = document.getElementsByTagName("aggregations").getLength
      val aggregListlength = document.getElementsByTagName("aggregation").getLength
      // get how many aggregation in aggregations
      var k = 0
      while (k < aggregationslength) {
        var elementaggrega = xpath.evaluate("//aggregation[@id='" + k + "']", document, XPathConstants.NODE).asInstanceOf[Element]
        var idrow = elementaggrega.getAttribute("idrow")
        tablesources = elementaggrega.getAttribute("tablesource")
        selectedcolumns = elementaggrega.getAttribute("colonnessources")
        var targettable = elementaggrega.getAttribute("targettable")
        val hTable = new HTable(HbaseConf, targettable)

        var idexp = parsers.parseExpression(idrow)


        val dfaggrega = spark.sql("select " + selectedcolumns + " from " + tablesources)
        print(dfaggrega.collectAsList().get(0))
        dfaggrega.toJSON.collectAsList().forEach(row => {

          var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]

          var bean = new BeanGenerators(json).setBeanSchema()
          bean = new BeanGenerators(json).getBean(bean)
          var keys2 = json.keySet()
          keys2.forEach(key => {
            context.setVariable(key.toString, bean.get(key.toString))
          })

          var targetcolonneslength = elementaggrega.getElementsByTagName("RichKeyMapping").getLength
          var g = 0
          var id = idexp.getValue(context).asInstanceOf[String]
          var put = new Put(Bytes.toBytes("row" + id))

          while (g < targetcolonneslength) {
            var valueexp = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("mappingformula").getNodeValue
            var colfamily = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("columnqualifier").getNodeValue.split(":").apply(0)
            var colname = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("columnqualifier").getNodeValue.split(":").apply(1)




            if(elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("formatters")!= null)
            {
              var formatters=elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("formatters").getNodeValue
              var parseformat= parsers.parseExpression(formatters)
               parseformat.getValue(context).asInstanceOf[java.lang.String]
            }

            var pattern=""
            var patternboolean=true
            var strictpattern="true"
            if(elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("pattern")!=null)
            {
              strictpattern= elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("strictpattern").getNodeValue

              pattern = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("pattern").getNodeValue
              var parsepattern=parsers.parseExpression(pattern)
              if(!parsepattern.getValue(context).asInstanceOf[Boolean] && strictpattern=="false")
              {
                patternboolean=false
              }


            }



            if(elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("type")==null) {


              if (patternboolean == true ){

              var valueparse = parsers.parseExpression(valueexp)
              var finalvalue = valueparse.getValue(context).asInstanceOf[String]


              if (elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("condition") != null) {
                var condition = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("condition").getNodeValue
                var expp = parsers.parseExpression(condition)
                var valueCondition = expp.getValue(context).asInstanceOf[Boolean]

                if (valueCondition) {
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
                  hTable.put(put)

                }
                else if (elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("strict").getNodeValue == "true") {
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes("null"))
                  hTable.put(put)

                }
              }
              else {
                put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
                hTable.put(put)
              }
            }
            }
            else
            {
              var typemap= elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("type").getNodeValue
              if(typemap=="document")
              {
                var tablesourcename = elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("tablesource").getNodeValue


                var keyjoin=elementaggrega.getElementsByTagName("RichKeyMapping").item(g).getAttributes.getNamedItem("keyJoin").getNodeValue
                var exppkey = parsers.parseExpression(keyjoin)

                var keyvalue=exppkey.getValue(context).asInstanceOf[String]

                var dfaggrega = spark.sql("select " + valueexp + " from " + tablesourcename + " where " + keyvalue)
                var r=0;
                dfaggrega.toJSON.collectAsList().forEach(row=>{
                  var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname+" "+ r), Bytes.toBytes(json.toJSONString))
                  hTable.put(put)

                  r=r+1;
                })
              }
            }
            g = g + 1
          }
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
