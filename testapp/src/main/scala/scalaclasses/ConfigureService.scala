package scalaclasses

import java.io.File
import java.sql.DriverManager
import java.util.Properties

import com.vermeg.testapp.services.TestService
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPath, XPathConstants, XPathFactory}
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Delete, Put}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.json.simple.parser.JSONParser
import org.springframework.cglib.beans.BeanMap
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.w3c.dom.{Document, Element, Node, NodeList}

import scala.util.control.Breaks.{break, breakable}

class ConfigureService {


  def Configurer(path: String): String = {
    // enregistrer le temps de debut du programme
    val debut = System.currentTimeMillis
    //create an instance of user srevice that contains functions called in this class
    var userservice = new UserService
    var spark = userservice.UserFunctionCreateSparkSession()
    //lire le fichier de configuration
    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath

    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))


    // connection à HBase
    val connection = userservice.UserFunctiongetHBaseConnection(xpath, document)
    // create rollback file
    val rollbackdoc = docBuilder.newDocument()
    val racine = rollbackdoc.createElement("Rollback")
    rollbackdoc.appendChild(racine)

    val context = new StandardEvaluationContext
    val parsers = new SpelExpressionParser
    // creer le stbales hbases
   // userservice.UserFunctionCreateHbaseTbalesIfNotExists(document, xpath, connection)
    // crere  des temp view pour les table sources
    userservice.UserFunctionCreatTempViewOfSOurceTbales(document, xpath, spark)
    // enregistre les fonctiosn client dans SPEL COntext
    userservice.UserFunctionsetClientFunctionsIntoContext(xpath, context, document)
    // enregistre dans le SPEL les variable globales et leurs valeurs
    userservice.UserFunctionsetGlobalVariablesIntoContext(xpath, document, spark, parsers, context)

    // recuperer les transformation racines et commence le traitement
    var listparent = xpath.evaluate("//Transformation", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    implicit def pimpNodeList(nl: NodeList): Seq[Node] = 0 until nl.getLength map (nl item _)

    var TablesHbasedom = rollbackdoc.createElement("TablesHBase")

    racine.appendChild(TablesHbasedom);
    pimpNodeList(listparent).foreach(node => {

      var id = node.getAttributes.getNamedItem("id").getNodeValue
      var idrow = node.getAttributes.getNamedItem("idLigne").getNodeValue
      var typeidrow = node.getAttributes.getNamedItem("typeidLigne").getNodeValue
      var tablesource = node.getAttributes.getNamedItem("tablesource").getNodeValue
      var targettable = node.getAttributes.getNamedItem("tablecible").getNodeValue
      var keyjoin = ""
      var TableHbasedom = rollbackdoc.createElement("Table")

      TablesHbasedom.appendChild(TableHbasedom);

      var tablename = rollbackdoc.createAttribute("nom")
      tablename.setValue(targettable)
      TableHbasedom.setAttributeNode(tablename)
      aggregation("transformation", id.toInt, idrow, typeidrow, tablesource, targettable, keyjoin)


    })

    userservice.createrollbackfile(rollbackdoc, xpath, document, rollbackdoc: Document)


    def aggregation(typetransformation: String, incrementaggrega: Int, idrow: String, typeidRow: String, tablesource: String, targettable: String, keyjoin: String): Unit = {


      var hTable = connection.getTable(TableName.valueOf(targettable))

      var RichKeyList = xpath.compile("//Transformation[@id='" + incrementaggrega + "']/CleRicheDeMappage").evaluate(document, XPathConstants.NODESET).asInstanceOf[NodeList]
      var colonnesRichKeysourcesWithTableNames = ""
      var condition = ""
      var colonnesRichKeySource = ""

      RichKeyList.foreach(node => {

        var colname = node.getAttributes.getNamedItem("colonnesource").getNodeValue
        colonnesRichKeysourcesWithTableNames = colonnesRichKeysourcesWithTableNames + "," + tablesource + "." + colname
        colonnesRichKeySource = colonnesRichKeySource + "," + colname
        if (node.getAttributes.getNamedItem("condition") != null) {
          condition = condition + " and " + node.getAttributes.getNamedItem("condition").getNodeValue
        }

      })
      colonnesRichKeysourcesWithTableNames = colonnesRichKeysourcesWithTableNames + "," + tablesource + "." + idrow.substring(1);
      colonnesRichKeySource = colonnesRichKeySource + "," + idrow.substring(1);


      if (condition.length != 0) {
        condition = condition.substring(4);
      }
      else {
        condition = "true"
      }


      colonnesRichKeysourcesWithTableNames = colonnesRichKeysourcesWithTableNames.substring(1)


      var subaggregation = xpath.evaluate("//Transformation[@id='" + incrementaggrega + "']/Document", document, XPathConstants.NODESET).asInstanceOf[NodeList]

      var subinc = 0
      var tables = tablesource
      var colonnesTransformationSource = colonnesRichKeysourcesWithTableNames
      var cleJointure = ""
      var cleJointureComplete = ""

      while (subinc < subaggregation.getLength) {
        var sousggregation = subaggregation.item(subinc)
        var tablesourcee = sousggregation.getAttributes.getNamedItem("tablesource").getNodeValue

        var colonnessource = sousggregation.getAttributes.getNamedItem("colonnessources").getNodeValue
        var newcols = ""

        colonnessource.split(",").foreach(data => newcols = newcols + "," + tablesourcee + "." + data)
        colonnesTransformationSource = colonnesTransformationSource + newcols
        tables = tables + "," + tablesourcee
        cleJointure = sousggregation.getAttributes.getNamedItem("clejointure").getNodeValue
        var typejointure = sousggregation.getAttributes.getNamedItem("typejointure").getNodeValue
        cleJointureComplete = cleJointureComplete + "  " + typejointure + " " + tablesourcee + " on  " + cleJointure
        subinc = subinc + 1
      }


      colonnesRichKeySource = colonnesRichKeySource.substring(1)
      var df = spark.sql("select " + colonnesTransformationSource + " from " + tablesource + " " + cleJointureComplete + " where " + condition)


      df.createOrReplaceTempView("tempView")

      var df2 = spark.sql("select " + colonnesRichKeySource + " from tempView")

      var g = 0;
      df2.toJSON.collectAsList().forEach(row => {

        var g = 0

        var bean = userservice.savebeanrowincontext(context, row)

        var idexp = parsers.parseExpression(idrow)
        var id = String.valueOf(Class.forName(typeidRow).cast(idexp.getValue(context)))
        var put = new Put(Bytes.toBytes("row" + id))

        while (g < RichKeyList.getLength) {

          var richnode = RichKeyList.item(g)
          var condition = true


          var finalvalue = ""

          if (richnode.getAttributes.getNamedItem("converter") != null) {


            if (richnode.getAttributes.getNamedItem("converter").getNodeValue == "convertDateTime") {
              finalvalue = String.valueOf(userservice.DateConverter(context, richnode, parsers))

            }
            else if (richnode.getAttributes.getNamedItem("converter").getNodeValue == "NumberConverter") {
              finalvalue = String.valueOf(userservice.NumberCOnverter(context, richnode, parsers))

            }
          }

          else {

            var valueexp = richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue

            var valueparse = parsers.parseExpression(valueexp)
            var typeformule = richnode.getAttributes.getNamedItem("type").getNodeValue

            finalvalue = String.valueOf(Class.forName(typeformule).cast(valueparse.getValue(context)))

          }

          var colfamily = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(0)

          var colname = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(1)

          var rowdom = userservice.createrowdom(id, racine, rollbackdoc, targettable)
          put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
          hTable.put(put)
          userservice.setattributerowRollback(colfamily, colname, targettable, rowdom, rollbackdoc)


          g = g + 1

        }


        subaggregation.par.foreach(sousggregation2 => {
          var colonnessource = sousggregation2.getAttributes.getNamedItem("colonnessources").getNodeValue
          var colonnecible = sousggregation2.getAttributes.getNamedItem("colonnescibles").getNodeValue

          var sousggregation = subaggregation.item(subinc)


          var df3 = spark.sql("select " + colonnessource + " from tempview where " + idrow.substring(1) + " = " + id)
          var r = 0

          if (!df3.rdd.isEmpty()) {
            df3.toJSON.collectAsList().forEach(row => {
              var bean = userservice.savebeanrowincontext(context, row)
              var idexp = parsers.parseExpression(idrow)

              var id = String.valueOf(Class.forName(typeidRow).cast(idexp.getValue(context)))
              var put = new Put(Bytes.toBytes("row" + id))
              var colfamily = colonnecible.split(":").apply(0)
              var colname = colonnecible.split(":").apply(1)
              var rowdom = userservice.createrowdom(id, racine, rollbackdoc, targettable)

              put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(bean.toString))
              userservice.setattributerowRollback(colfamily, colname + " " + r, targettable, rowdom, rollbackdoc)
              hTable.put(put)
              r = r + 1

            })
          }

        })


      })

    }


    return "time" + (System.currentTimeMillis() - debut).toString
  }


}
