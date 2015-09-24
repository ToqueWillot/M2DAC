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


    def indexation_index(filename:String):Unit={
      parser.init(filename)
      index.seek(0)

      var doc = parser.nextDocument()
      while(doc!=null){
        val curF:Int=index.getFilePointer().toInt
        val line:String = doc.getId()+";"+createStringFromMap(getMapWordOccurFromString(doc.getText()))
        val sizeLine:Int = line.size

        index.writeChars(line+"\n")
        docs.put(doc.getId().toInt ,(curF, sizeLine-1))
        doc = parser.nextDocument()
      }
    }

    def indexation_inverted(filename:String):Unit={
      parser.init(filename)

      val mapWordDoc: scala.collection.mutable.Map[String,scala.collection.mutable.Map[Int,Int]]=scala.collection.mutable.Map()
      var doc = parser.nextDocument()
      while(doc!=null){
        val listWordDoc: List[(String,(Int,Int))] = getMapWordOccurFromString(doc.getText()).map(e=> (e._1,(doc.getId().toInt,e._2))).toList

        listWordDoc.foreach(t =>
          if( mapWordDoc.keySet.exists(_ == t._1) )
            mapWordDoc(t._1).put(t._2._1,t._2._2)
          else
            mapWordDoc.put(t._1, scala.collection.mutable.Map((t._2._1,t._2._2)))

        )
      }

      inverted.seek(0)
      mapWordDoc.map(e=>{
        val curF:Int=inverted.getFilePointer().toInt
        val line:String = e._1+";"+createStringFromMapIntInt(e._2.toMap)
        val sizeLine:Int = line.size
        inverted.writeChars(line+"\n")
        stems.put(doc.getId().toInt ,(curF, sizeLine-1))
      })
    }

    //function utils
    def createStringFromMapIntInt(myMap: Map[Int,Int]):String={
      myMap.map(tuple=> tuple._1.toString+","+tuple._2.toString).toArray.mkString(";")
    }
    def getMapWordOccurFromString(text : String,textRepresenter:Stemmer=textRepresenter): Map[String,Int]={
      textRepresenter.porterStemmerHash(text).asScala.mapValues(_.intValue).toMap - " * "
    }
    def createStringFromMap(myMap: Map[String,Int]):String={
      myMap.map(tuple=> tuple._1+","+tuple._2.toString).toArray.mkString(";")
    }
    def createMapFromString(myString: String ): Seq[(String,Int)]={
      myString.split(";").toList.map(elem=> (elem.split(",")(0),elem.split(",")(1).toInt)).toSeq
    }
    //TODO--------------
    // def getTfsForDoc(doc:Int):Option[Map[Int,Seq[(String,Int)]]]= {
    //   docs
    //   return Map()
    // }
    def getTfsForStem(index:RandomAccessFile):Map[String,List[(Int,Int)]] = {
      return Map()
    }
    def getStrDoc(id:Int):String={
      return ""
    }
  }


}
