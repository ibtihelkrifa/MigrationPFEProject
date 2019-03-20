package scalaclasses
import java.text.SimpleDateFormat
import java.sql.Date

import org.json.simple.JSONObject
import org.springframework.cglib.beans.{BeanGenerator, BeanMap}

import scala.reflect.macros.ParseException

class BeanGenerators(var json: JSONObject) {

  def setBeanSchema():BeanMap=
  {
    var generator = new BeanGenerator
    var keys = json.keySet()


    keys.toArray().foreach(k => {
      val key = k
      val value = json.get(key).asInstanceOf[Object]
      val keyClass = guessValueClass(value)
      generator.addProperty(key.toString, keyClass)
    }
    )


    def guessValueClass(value: Object): Class[_] = {

      try {
        if (value.isInstanceOf[Long])
          return classOf[Long]
      }
      catch {
        case e1: NumberFormatException =>
      }
      try{
         if(value.isInstanceOf[Int])
         return classOf[Integer]}
      catch {
        case e1: NumberFormatException =>
      }
      try{
        if(value.isInstanceOf[Double])
         return classOf[Double]}
      catch {
        case e1: NumberFormatException =>
      }
      classOf[String]
      }




    var result = generator.create()
    var bean = BeanMap.create(result)
    return bean

  }


  def getBean(bean :BeanMap):BeanMap={
    var keys = json.keySet()

    var it=keys.iterator()

    while(it.hasNext){
      var key = it.next()
      var value = json.get(key)
      bean.put(key, value)
    }


    return bean

  }


}
