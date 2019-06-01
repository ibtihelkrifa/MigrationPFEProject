package scala.vermeg.sevices

import java.io.File
import java.sql.DriverManager
import java.util
import java.util.Properties

import com.vermeg.app.services.TestService
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


  def Configurer(path: String): String =  {
    // enregistrer le temps de debut du programme
    val debut = System.currentTimeMillis
    //create an instance of user srevice that contains functions called in this class
    var userservice = new UserService
    //creer le spark session
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

    //creer le context de SpEL
    val context = new StandardEvaluationContext
    //creer le parser de SpEL
    val parsers = new SpelExpressionParser


    // creer le stbales hbases
    //userservice.UserFunctionCreateHbaseTbalesIfNotExists(document, xpath, connection)


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
      //obtenir l'id de la balise transfortmation
      var id = node.getAttributes.getNamedItem("id").getNodeValue
      // obtenri la valeur de l'attribut id de la ligne dans le target
      var idrow = node.getAttributes.getNamedItem("idLigne").getNodeValue
      // obtenir le type de l'id ligne du target
      var typeidrow = node.getAttributes.getNamedItem("typeidLigne").getNodeValue
      // obtenir le nom de la table source
      var tablesource = node.getAttributes.getNamedItem("tablesource").getNodeValue
      // obtenir le nom de la table cible
      var targettable = node.getAttributes.getNamedItem("tablecible").getNodeValue
      var keyjoin = ""
      // creer un dom element dans le rollback file
      var TableHbasedom = rollbackdoc.createElement("Table")

      TablesHbasedom.appendChild(TableHbasedom);

      var tablename = rollbackdoc.createAttribute("nom")
      tablename.setValue(targettable)
      TableHbasedom.setAttributeNode(tablename)
      //pour chaque transformation on applique cette fonction aggregation
      aggregation("transformation", id.toInt, idrow, typeidrow, tablesource, targettable, keyjoin)


    })

    userservice.createrollbackfile(rollbackdoc, xpath, document, rollbackdoc: Document)

    //la fonction aggregation appliqué sur chaque transformation
    def aggregation(typetransformation: String, incrementaggrega: Int, idrow: String, typeidRow: String, tablesource: String, targettable: String, keyjoin: String): Unit = {

      // se connecter sur la table target hbase


      var hTable = connection.getTable(TableName.valueOf(targettable))

      // obtenir tous les rich key d'une transformation
      var RichKeyList = xpath.compile("//Transformation[@id='" + incrementaggrega + "']/CleRicheDeMappage").evaluate(document, XPathConstants.NODESET).asInstanceOf[NodeList]
      var colonnesRichKeysourcesWithTableNames = ""
      var condition = ""
      var colonnesRichKeySource = ""

      //paralleliser le traitment des richkey d'une tranformation
      RichKeyList.foreach(node => {
        // obtenir le nom des colonnes sources
        var colname = node.getAttributes.getNamedItem("colonnesource").getNodeValue
        colname.split(",").foreach(col => {
          colonnesRichKeysourcesWithTableNames = colonnesRichKeysourcesWithTableNames + "," + tablesource + "." + col
        })

        colonnesRichKeySource = colonnesRichKeySource + "," + colname

        // vérifier s'ilya une condition sur la sélection et constuire la conditon de sélection
        if (node.getAttributes.getNamedItem("condition") != null) {
          condition = condition + " and " + node.getAttributes.getNamedItem("condition").getNodeValue
        }

      })

      //concatener les noms de colonnes sources à leurs table names
      colonnesRichKeysourcesWithTableNames = colonnesRichKeysourcesWithTableNames + "," + tablesource + "." + idrow.substring(1);
      colonnesRichKeySource = colonnesRichKeySource + "," + idrow.substring(1);



      colonnesRichKeysourcesWithTableNames = colonnesRichKeysourcesWithTableNames.substring(1)

      //obtenri toutes les jointures au sein d'une même transformation
      var subaggregation = xpath.evaluate("//Transformation[@id='" + incrementaggrega + "']/Document", document, XPathConstants.NODESET).asInstanceOf[NodeList]

      var tables = tablesource
      var colonnesTransformationSource = colonnesRichKeysourcesWithTableNames
      var cleJointure = ""
      var cleJointureComplete = ""


      // constutire la requete de jointure finale
     subaggregation.foreach(sousaggregation=>{
       var tablesourcee = sousaggregation.getAttributes.getNamedItem("tablesource").getNodeValue

       var colonnessource = sousaggregation.getAttributes.getNamedItem("colonnessources").getNodeValue
       var newcols = ""

       colonnessource.split(",").foreach(data => newcols = newcols + "," + tablesourcee + "." + data)
       colonnesTransformationSource = colonnesTransformationSource + newcols
       tables = tables + "," + tablesourcee
       cleJointure = sousaggregation.getAttributes.getNamedItem("clejointure").getNodeValue
       var typejointure = sousaggregation.getAttributes.getNamedItem("typejointure").getNodeValue
       cleJointureComplete = cleJointureComplete + "  " + typejointure + " " + tablesourcee + " on  " + cleJointure
     })




      var requetesql = ""
      if (condition.length().>(5)) {
        condition = condition.substring(4);
        System.out.println(condition + "conditon")
        requetesql = "select " + colonnesTransformationSource + " from " + tablesource + " " + cleJointureComplete + " where " + condition
      }
      else {

        requetesql = "select " + colonnesTransformationSource + " from " + tablesource + " " + cleJointureComplete
      }


      colonnesRichKeySource = colonnesRichKeySource.substring(1)
      //executer la requete des mappages simples
      var df = spark.sql(requetesql)

      df.createOrReplaceTempView("tempView")
      //recupere les colonnes necessaires du dataframe
      var df2 = spark.sql("select " + colonnesRichKeySource + " from tempView")

      var g = 0;


      //parcourir le dataframe
      df2.toJSON.collectAsList().forEach(row => {

        // enregistrer les données d'un row dans un bean genrique
        var bean = userservice.savebeanrowincontext(context, row)
        // evaluer l'id de la ligne cible
        var idexp = parsers.parseExpression(idrow)
        var id = String.valueOf(Class.forName(typeidRow).cast(idexp.getValue(context)))
        // enregister l'id de la ligne cible
        var put = new Put(Bytes.toBytes("row" + id))
        // parcourir les rich key pour les executer

        RichKeyList.foreach(richnode=>{


            var richnode = RichKeyList.item(g)
            var condition = true


            var finalvalue = ""


            // verifier l'existence d'un convertisseur, si il existe evaluer et convertir
            if (richnode.getAttributes.getNamedItem("converter") != null) {


              if (richnode.getAttributes.getNamedItem("converter").getNodeValue == "convertDateTime") {
                finalvalue = String.valueOf(userservice.DateConverter(context, richnode, parsers))

              }
              else if (richnode.getAttributes.getNamedItem("converter").getNodeValue == "NumberConverter") {
                finalvalue = String.valueOf(userservice.NumberCOnverter(context, richnode, parsers))

              }
            }
            // si il n'a pas un convertisseur evaluer
            else {

              var valueexp = richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue

              var valueparse = parsers.parseExpression(valueexp)
              var typeformule = richnode.getAttributes.getNamedItem("type").getNodeValue

              finalvalue = String.valueOf(Class.forName(typeformule).cast(valueparse.getValue(context)))

            }

            //obtenir le nom de colonne de famille et nom de colonne cible
            var colfamily = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(0)

            var colname = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(1)

            var rowdom = userservice.createrowdom(id, racine, rollbackdoc, targettable)
            put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
            //enregistrer les données dans la cible
            userservice.setattributerowRollback(colfamily, colname, targettable, rowdom, rollbackdoc)




        })




        //list.add(Integer.valueOf(id))

        hTable.put(put)


      })

      var r = 0

      subaggregation.par.foreach(sousggregation2 => {
        var colonnessource = sousggregation2.getAttributes.getNamedItem("colonnessources").getNodeValue
        var colonnecible = sousggregation2.getAttributes.getNamedItem("colonnescibles").getNodeValue

    //    var sousggregation = subaggregation.item(subinc)

        var dftest = spark.sql("select " + colonnessource + "  , " + idrow.substring(1) + " from tempview ")


      //  val list = new util.ArrayList[Int]
        dftest.toJSON.collectAsList().forEach(row => {

          var bean = userservice.savebeanrowincontext(context, row)

          var idexp = parsers.parseExpression(idrow)
          var id = String.valueOf(Class.forName(typeidRow).cast(idexp.getValue(context)))



          // enregister l'id de la ligne cible
          var put = new Put(Bytes.toBytes("row" + id))

          var colfamily = colonnecible.split(":").apply(0)
          var colname = colonnecible.split(":").apply(1)
          var rowdom = userservice.createrowdom(id, racine, rollbackdoc, targettable)
          put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(bean.toString))
          userservice.setattributerowRollback(colfamily, colname + " " + r, targettable, rowdom, rollbackdoc)
          r=r+1

          hTable.put(put)
        })

      })






    }


      spark.close()
      return "time" + (System.currentTimeMillis() - debut).toString


  }
}
