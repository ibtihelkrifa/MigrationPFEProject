package scalaclasses

import java.sql.{DriverManager, Struct}
import java.util
import java.util.Properties

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.StringType
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}

import scala.collection.mutable

class TestScalaClass {




  def configure2(path: String): String = {
    val spark = SparkSession
      .builder()
      .appName("TestappApplication")
      .config("spark.master", "local[*]")
      .getOrCreate(); // [1]


    val df = spark.read
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

   var L= List("user","address")
    var list= new util.ArrayList[(String,DataFrame)]()

    L.map(s=>(s,spark.read.jdbc(jdbcUrl, s, connectionProperties))).foreach(m=>{
       list.add(m)
    })



   // val df=  spark.sqlContext.createDataFrame(list)

return "done"


  }
}
