package com.fulldeep.indexation;



import scala.collection.JavaConverters._
import com.fulldeep.indexation._
import java.io.RandomAccessFile
import java.io.File

object index {

  class index(val name:String, val parser:Parser, val textRepresenter:Stemmer) extends Serializable {

    val file_index : File = new File(name+"_index.csv")
    val index: RandomAccessFile = new RandomAccessFile(file_index, "rw")

    val file_inverted : File = new File(name+"_inverted.csv")
    val inverted: RandomAccessFile = new RandomAccessFile(file_inverted, "rw")

    val docs: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()
    val stems: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()
    val docFrom: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()


    def indexation(filename:String):Unit={
      parser.init(filename)

    }

    //function utils
    def getMapWordOccurFromString(text : String,textRepresenter:Stemmer=textRepresenter): Map[String,Int]={
      textRepresenter.porterStemmerHash(text).asScala.mapValues(_.intValue).toMap - " * "
    }
    def createStringFromMap(myMap: Map[String,Int]):String={
      myMap.map(tuple=> tuple._1+","+tuple._2.toString).toArray.mkString(";")
    }
    def createMapFromString(myString: String ): Map[String,Int]={
      myString.split(";").toList.map(elem=> (elem.split(",")(0),elem.split(",")(1).toInt)).toMap
    }
    //TODO--------------
    def getTfsForDoc(index:RandomAccessFile):Map[Int,List[(String,Int)]] = {
      return Map()
    }
    def getTfsForStem(index:RandomAccessFile):Map[String,List[(Int,Int)]] = {
      return Map()
    }
    def getStrDoc(id:Int):String={
      return ""
    }
  }


}
