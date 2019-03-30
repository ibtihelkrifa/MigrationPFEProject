package scalaclasses

import java.io.File
import java.util

import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.{XPathConstants, XPathFactory}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Delete}
import org.apache.hadoop.hbase.util.Bytes
import org.w3c.dom.{Element, NodeList}

class Rollback {

  def rollbackfunction(path: String):String=
  {



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

    val tables=xpath.evaluate("//Table", document, XPathConstants.NODESET).asInstanceOf[NodeList]

    var t=0
    while(t< tables.getLength)
      {
        var table= tables.item(t).getAttributes.getNamedItem("nom").getNodeValue
        var rows = xpath.evaluate("//row[@table='"+table+"']", document, XPathConstants.NODESET).asInstanceOf[NodeList]
        var r=0
        var hTable=connection.getTable(TableName.valueOf(table))

        while(r< rows.getLength)
        {

          var row= rows.item(r)
          var idrow= row.getAttributes.getNamedItem("idrow").getNodeValue
          var colfamily= row.getAttributes.getNamedItem("Famillecolonne").getNodeValue
          var colname= row.getAttributes.getNamedItem("nomcolonne").getNodeValue

          var delete = new Delete(Bytes.toBytes("row" + idrow))
          delete.deleteColumns(Bytes.toBytes(colfamily),Bytes.toBytes(colname))
          hTable.delete(delete)

          r=r+1
        }
        t=t+1
      }


    return "done"

  }


}
