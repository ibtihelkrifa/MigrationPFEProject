package scalaclasses
import org.apache.hadoop.hbase.HColumnDescriptor
import java.sql.DriverManager
import java.util
import java.util.{Date, Properties}

import com.databricks.spark.xml._
import com.vermeg.testapp.services.TestService
import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.spark.sql.SparkSession
import javax.script.ScriptException
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client._
import org.apache.hadoop.hbase.util.Bytes
import org.json.simple.JSONObject
import org.springframework.expression.spel.support.StandardEvaluationContext

import scala.collection.JavaConverters._
import org.json.simple.parser.JSONParser
import org.springframework.expression.spel.standard.SpelExpressionParser

class GreetingInScala {



  def configure(path: String): String = {


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
    //connection à une base de donnée mysql

    //recupertaion des options de connection à mysql
    val source = spark.sql("""select xmltable.source.ip, xmltable.source.port,xmltable.source.name,xmltable.source.user,xmltable.source.password from xmltable """);
    //stocker les variables de communication dans des varibales scala
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




    //connection à la base de donnée hbase
    var testtest=""
    var colname=""

    var colfamily=""
    try {

      val HbaseConf = HBaseConfiguration.create()
      HbaseConf.set("hbase.master", "localhost")
      HbaseConf.set("hbase.zookeeper.quorum", "localhost")
      HbaseConf.set("hbase.zookeeper.property.clientPort", "2181")
      val connection = ConnectionFactory.createConnection(HbaseConf)
      val table = connection.getTable(TableName.valueOf("emp"))

      //selectionner le nom de la base de donnee cible

      val Bdcibledf = spark.sql("""select xmltable.StructureCible.name from xmltable """);
      val Bdciblename = Bdcibledf.collectAsList().get(0).get(0).toString()

      //créer le descripteur de la base de donnée cible

      val tableDescriptor = new HTableDescriptor(TableName.valueOf(Bdciblename))

      //sélectionnes les noms de famille de colonne de la base de donnée cible

      var cols = new util.ArrayList[String]()
      val df = spark.read
        .option("rowTag", "cols")
        .xml(path)

      df.select("_CF").collectAsList().get(0).get(0).toString.split(";")
        .foreach(s => {
          cols.add(s)
        })

      //parcoutir la liste des colonnes et complete la creation de descrpteur de la table
      var age=""
      var v= new util.ArrayList[(String,String)]
      var types=""
      var ColumnQualifier=""
      var mappingformula=""
      for (c: String <- cols.asScala) {
        val family = new HColumnDescriptor(Bytes.toBytes(c))
        tableDescriptor.addFamily(family)
      }

      // se connecter à la base HBase et création de la table

      val admin = connection.getAdmin

      if (!admin.tableExists(tableDescriptor.getTableName))
        admin.createTable(tableDescriptor)

      //commencer les transformation

      val dftransformation= spark.read
        .option("rowTag", "RichKeyMapping")
        .xml(path)

      var trans=spark.read.option("rowTag","Transformation").xml(path)
      var test=""

     //test=trans.collectAsList().get(0).getAs[String]("_att")


      var a= new util.ArrayList[String]()


      var  parser = new JSONParser


      val LesRichKeyMapping = dftransformation.collectAsList().forEach(t=>{

          types =t.getAs[String]("_type")

          val hTable = new HTable(HbaseConf, Bdciblename)
          var table_source_name=t.getAs[String]("_tablesource")
          ColumnQualifier =t.getAs[String]("_columnqualifier")
          colfamily= ColumnQualifier.split(":").apply(0)
          colname=ColumnQualifier.split(":").apply(1)
          mappingformula=t.getAs[String]("_mappingformula")
          val context = new StandardEvaluationContext()
          val parsers = new SpelExpressionParser
          var exp = parsers.parseExpression(mappingformula)
          var employees_table = spark.read.jdbc(jdbcUrl, table_source_name, connectionProperties)
          var valueexp= t.getAs[String]("_value")
          var ignore= t.getAs[Boolean]("_ignore")

          employees_table.toJSON.collectAsList().forEach(row => {

            var json = parser.parse(row).asInstanceOf[org.json.simple.JSONObject]


            var bean = new BeanGenerators(json).setBeanSchema()
            bean = new BeanGenerators(json).getBean(bean)
            var keys = json.keySet()
            context.registerFunction("getAge", classOf[TestService].getDeclaredMethod("getAge", classOf[String]))
            context.registerFunction("formatNumber", classOf[TestService].getDeclaredMethod("formatNumber", classOf[Double]))
            context.registerFunction("getcurrency",classOf[TestService].getDeclaredMethod("getcurrency",classOf[String]))
            keys.forEach(key => {
              context.setVariable(key.toString, bean.get(key.toString))
            })
            var put = new Put(Bytes.toBytes("row" + bean.toString))

            if(types=="Formule") {

            var expression = exp.getValue(context).asInstanceOf[String]
              var put = new Put(Bytes.toBytes("row" + bean.toString))

            put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(expression))
            hTable.put(put)
            }

            else if(types=="condition")
            {
              var expression=exp.getValue(context).asInstanceOf[Boolean]
              if(expression)
                {
                  var expp= parsers.parseExpression(valueexp)
                  var value=expp.getValue(context).asInstanceOf[String]

                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes(value))
                  test=colfamily

                }
              else if(ignore==false)
                {
                  test="i am here 2"

                  put.addColumn(Bytes.toBytes(colfamily), Bytes.toBytes(colname), Bytes.toBytes("null"))

                }
              hTable.put(put)

            }
          })



    })



    return  "done"


    }

     catch {
         case e: ScriptException => e.printStackTrace
           return "false"
       }
}}