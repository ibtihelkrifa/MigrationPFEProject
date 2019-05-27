package scalaclasses

import java.io.File
import java.sql.DriverManager
import java.util

import javax.xml.xpath.{XPathConstants, XPathFactory}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put, Scan}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, HColumnDescriptor, HTableDescriptor, KeyValue, TableName}
import org.apache.spark.sql.SparkSession
import org.json.simple.parser.JSONParser
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.w3c.dom.{Element, Node, NodeList}
import java.sql.DatabaseMetaData
import java.sql.Connection
import java.sql.DriverManager

import org.apache.hadoop.hbase.util.Bytes
import java.util

import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.client.Result

import scala.util.control.Breaks.breakable

class GestionConnection {



 class Column (nomcolonne:String,typecolonne:String){

   def getcolumnname(): String={
     return nomcolonne
   }

   def gettypecolumn():String={
     return typecolonne
   }

   override def toString: String =
     s"($nomcolonne, $typecolonne)"
 }



  def getColumnNamesMysqlTable(jdbcConnection :Connection ,tablename:String):java.util.ArrayList[Column] =
  {
   try{
    val md = jdbcConnection.getMetaData

    val rs = md.getColumns(jdbcConnection.getCatalog, null,tablename, null)
    var nomcolonne=""
    var typecolonne=""
    var list= new util.ArrayList[Column]()
    while(rs.next())
    {
      nomcolonne=rs.getString("COLUMN_NAME")
      typecolonne=rs.getString("TYPE_NAME")

      list.add(new Column(nomcolonne,typecolonne))
    }


    return list
   }
    catch
      {
        case e: Exception =>
          print(e)
          return null

      }
  }


  def getMysqlTables(jdbcConnection: Connection): util.ArrayList[String]={

    val md = jdbcConnection.getMetaData

    val rs = md.getTables(jdbcConnection.getCatalog, null,null, Array[String]("TABLE"))
    var a=""
    var list = new util.ArrayList[String]()

    while (rs.next())
    {
      a = rs.getString(3)
      list.add(a)

    }

    return list
  }


  def checkHbaseConnection( master: String,quorum:String, port: String): org.apache.hadoop.hbase.client.Connection =
  {

    try{
    val HbaseConf = HBaseConfiguration.create()
    HbaseConf.set("hbase.master", master)
    HbaseConf.set("hbase.zookeeper.quorum", quorum)
    HbaseConf.set("hbase.zookeeper.property.clientPort",port)

    return ConnectionFactory.createConnection(HbaseConf)
    }
    catch
      {
        case e: Exception =>
          print("connection habse failed")
          return null
      }

  }

    def getHbasetablesname(connection: org.apache.hadoop.hbase.client.Connection): util.ArrayList[String]=
    {

    val admin = connection.getAdmin

    var tab = new util.ArrayList[String]()
    var r = 0

    while(r< admin.listTables().length)
      {
    var l = admin.listTables().apply(r).getTableName.toString


        tab.add(l)
        r=r+1
      }
    return tab

    }


    def getColumnFamilies(connection: org.apache.hadoop.hbase.client.Connection,HbaseTableName: String): util.ArrayList[String] =
  {


    val admin = connection.getAdmin
    var colfamiliesList= admin.getTableDescriptor(TableName.valueOf(HbaseTableName)).getColumnFamilies.toList
    var c=0
    var list= new util.ArrayList[String]()
    while(c<colfamiliesList.length)
    {
      list.add(colfamiliesList.apply(c).getNameAsString)
      c=c+1
    }

    return list
  }



  def getColumnsHabseOfColumnFamily(connection: org.apache.hadoop.hbase.client.Connection, nomtable: String,nomFamille: String): util.List[String] =
  {

    val admin = connection.getAdmin
    var htable= connection.getTable(TableName.valueOf(nomtable))
    var scan = new Scan()

    var results=htable.getScanner(scan)

    val columnList = new util.ArrayList[String]()

    val i=results.iterator();
    while(i.hasNext)
      {
        val familymap=i.next().getFamilyMap(Bytes.toBytes(nomFamille))

        for (entry <- familymap.entrySet) {
          columnList.add(Bytes.toString(entry.getKey))
        }
      }




    return columnList

  }





  def getColumnTypesMysqlTable(jdbcConnection :Connection ,tablename:String): util.ArrayList[String] =
  {
    try{
      val md = jdbcConnection.getMetaData

      val rs = md.getColumns(jdbcConnection.getCatalog, null,tablename, null)
      var a=""

      var list= new util.ArrayList[String]()
      while(rs.next())
      {
        a=rs.getString("TYPE_NAME")
        list.add(a)
      }


      return list
    }
    catch
      {
        case e: Exception =>
          print(e)
          return null

      }
  }

}