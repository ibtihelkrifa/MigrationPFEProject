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




  def Configurer(path:String): String =
  {
    // enregistrer le temps de debut du programme
    val debut = System.currentTimeMillis
//create an instance of user srevice that contains functions called in this class
    var userservice= new UserService
    var spark= userservice.UserFunctionCreateSparkSession()
    //lire le fichier de configuration
    val xpathFactory = XPathFactory.newInstance
    val xpath = xpathFactory.newXPath

    val docBuilderFactory = DocumentBuilderFactory.newInstance
    val docBuilder = docBuilderFactory.newDocumentBuilder
    val document = docBuilder.parse(new File(path))




    // connection à HBase
    val connection= userservice.UserFunctiongetHBaseConnection(xpath,document)
   // create rollback file
    val rollbackdoc= docBuilder.newDocument()
    val racine = rollbackdoc.createElement("Rollback")
    rollbackdoc.appendChild(racine)

    val context = new StandardEvaluationContext
    val parsers = new SpelExpressionParser
    // creer le stbales hbases
    userservice.UserFunctionCreateHbaseTbalesIfNotExists(document,xpath,connection)
    // crere  des temp view pour les table sources
    userservice.UserFunctionCreatTempViewOfSOurceTbales(document,xpath,spark)
    // enregistre les fonctiosn client dans SPEL COntext
    userservice.UserFunctionsetClientFunctionsIntoContext(xpath,context,document)
    // enregistre dans le SPEL les variable globales et leurs valeurs
    userservice.UserFunctionsetGlobalVariablesIntoContext(xpath,document,spark,parsers,context)

    // recuperer les transformation racines et commence le traitement
    var listparent=  xpath.evaluate("//Transformation", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    implicit def pimpNodeList(nl: NodeList): Seq[Node] = 0 until nl.getLength map (nl item _)

    var TablesHbasedom=rollbackdoc.createElement("TablesHBase")

    racine.appendChild(TablesHbasedom);

    pimpNodeList(listparent).map(node =>{

      var id= node.getAttributes.getNamedItem("id").getNodeValue
      var idrow=node.getAttributes.getNamedItem("idLigne").getNodeValue
      var tablesource=node.getAttributes.getNamedItem("tablesource").getNodeValue
      var colonnessources=node.getAttributes.getNamedItem("structuresource").getNodeValue
      var colonnescible=node.getAttributes.getNamedItem("structurecible").getNodeValue
      var targettable=node.getAttributes.getNamedItem("tablecible").getNodeValue
      var keyjoin=""
      var TableHbasedom=rollbackdoc.createElement("Table")

      TablesHbasedom.appendChild(TableHbasedom);

      var tablename=rollbackdoc.createAttribute("nom")
      tablename.setValue(targettable)
      TableHbasedom.setAttributeNode(tablename)
      aggregation("transformation",id.toInt,idrow,tablesource,colonnessources,colonnescible,targettable, keyjoin)

    })

    userservice.createrollbackfile(rollbackdoc,xpath,document,rollbackdoc: Document)






    def aggregation(typetransformation:String,incrementaggrega: Int,idrow : String, tablesource: String,colonnessources: String,colonnecible:String,targettable: String, keyjoin: String): Unit =
    {


      var hTable=connection.getTable(TableName.valueOf(targettable))


      if(typetransformation=="transformation")
        {
          var dfaggrega = spark.sql("select " + colonnessources + " from " + tablesource )
          var RichKeyList=xpath.compile("//Transformation[@id='"+incrementaggrega+"']/CartographieCle[@idpere='" + incrementaggrega + "']").evaluate(document,XPathConstants.NODESET).asInstanceOf[NodeList]
          var g=0;
          dfaggrega.toJSON.collectAsList().forEach(row => {

            var g=0

            var bean=userservice.savebeanrowincontext(context,row)

            var idexp = parsers.parseExpression(idrow)
            var id = idexp.getValue(context).asInstanceOf[Long]
            var put = new Put(Bytes.toBytes("row" + id.toString))

            breakable{
              while(g < RichKeyList.getLength) {

                var richnode = RichKeyList.item(g)
                var condition=true
                if(richnode.getAttributes.getNamedItem("condition") != null)
                  {
                    var conditonattribute=richnode.getAttributes.getNamedItem("condition").getNodeValue
                    condition= parsers.parseExpression(conditonattribute).getValue(context).asInstanceOf[Boolean]
                  }

                if(condition)
                  {

                    if(richnode.getAttributes.getNamedItem("converter")!= null)
                      {
                        userservice.convertValues(context,richnode,parsers)
                      }

                     var valueexp = richnode.getAttributes.getNamedItem("cartographieformule").getNodeValue
                     var colfamily = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(0)

                     var colname = richnode.getAttributes.getNamedItem("colonnecible").getNodeValue.split(":").apply(1)

                     var rowdom= userservice.createrowdom(id,racine,rollbackdoc,id.toString,targettable)


                  var valueparse = parsers.parseExpression(valueexp)
                  var finalvalue = valueparse.getValue(context).asInstanceOf[String]
                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(finalvalue))
                  hTable.put(put)
                 userservice.setattributerowRollback(colfamily,colname,targettable,rowdom,rollbackdoc)

                  }

                else
                {

                  val d = new Delete(Bytes.toBytes("row" + id))
                  hTable.delete(d)
                  break

                }

                g = g + 1

              }
              soustransformationfunction(context,xpath,parsers,incrementaggrega)

            }

          })
        }



            else if(typetransformation=="soustransformation")
              {
                var dfaggrega=spark.sql("select " + colonnessources + " from " + tablesource + " where " + keyjoin)

                var r=0

                dfaggrega.toJSON.collectAsList().forEach(row => {
                  var bean=userservice.savebeanrowincontext(context,row)
                  var idexp = parsers.parseExpression(idrow)
                  var id = idexp.getValue(context).asInstanceOf[Long]
                  var put = new Put(Bytes.toBytes("row" + id.toString))
                  var colfamily = colonnecible.split(":").apply(0)
                  var colname = colonnecible.split(":").apply(1)
                  var rowdom= userservice.createrowdom(id,racine,rollbackdoc,id.toString,targettable)

                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname + " " + r), Bytes.toBytes(bean.toString))
                  userservice.setattributerowRollback(colfamily,colname + " " + r,targettable,rowdom,rollbackdoc)
                  hTable.put(put)
                  r=r+1


                    soustransformationfunction(context,xpath,parsers,incrementaggrega)

                  })

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
        var colonnecible=subaggregation.getAttributes.getNamedItem("structurecible").getNodeValue
        var fieldfromparent=subaggregation.getAttributes.getNamedItem("FieldFromParent").getNodeValue

        var keyjoin = subaggregation.getAttributes.getNamedItem("CleJointure").getNodeValue
        var keyparse = parsers.parseExpression(keyjoin)
        var keyjoinvalue = keyparse.getValue(context).asInstanceOf[String]

        subinc = subinc + 1
        aggregation("soustransformation", id.toInt, idrow, tablesource, colonnessources, colonnecible,targettable, keyjoinvalue)
        keyjoin = ""
      }


    }


    return (System.currentTimeMillis()-debut).toString
  }





}
