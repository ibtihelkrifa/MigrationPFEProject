package scala.vermeg.sevices

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

      try{
        if(value.isInstanceOf[java.lang.Integer])
          return classOf[java.lang.Integer]}
      catch {
        case e1: NumberFormatException =>
      }

      try {
        if (value.isInstanceOf[java.lang.Long])
          return classOf[java.lang.Long]
      }
      catch {
        case e1: NumberFormatException =>
      }

      try{
        if(value.isInstanceOf[java.lang.Double])
         return classOf[java.lang.Double]}
      catch {
        case e1: NumberFormatException =>
      }
      return  classOf[java.lang.String]
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
