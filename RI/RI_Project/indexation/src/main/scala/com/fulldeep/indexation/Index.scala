package com.fulldeep.indexation;



import scala.collection.JavaConverters._
import com.fulldeep.indexation._
import java.io.RandomAccessFile
import java.io.File

object Index {

  class Index(val name:String, val parser:Parser, val textRepresenter:Stemmer) extends Serializable {

    val file_index : File = new File("src/resources/"+name+"_index.csv")
    val index: RandomAccessFile = new RandomAccessFile(file_index, "rw")

    val file_inverted : File = new File("src/resources/"+name+"_inverted.csv")
    val inverted: RandomAccessFile = new RandomAccessFile(file_inverted, "rw")

    val docs: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()
    val stems: scala.collection.mutable.Map[String,(Int,Int)]=scala.collection.mutable.Map()
    val docFrom: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()


    def indexation_index(filename:String):Unit={
      parser.init(filename)
      index.seek(0)

      var doc = parser.nextDocument()
      while(doc!=null){
        val curF:Int=index.getFilePointer().toInt
        val line:String = doc.getId()+":"+createStringFromMap(getMapWordOccurFromString(doc.getText()))
        val sizeLine:Int = line.size+2

        index.writeChars(line+"\n")
        docs.put(doc.getId().toInt ,(curF, sizeLine))
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
        doc = parser.nextDocument()
      }

      inverted.seek(0)
      mapWordDoc.map(e=>{
      val curF:Int=inverted.getFilePointer().toInt
      val line:String = e._1+":"+createStringFromMapIntInt(e._2.toMap)
      val sizeLine:Int = line.size+2
      inverted.writeChars(line+"\n")
      stems.put(e._1 ,(curF, sizeLine))
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
    def createSeqFromString(myString: String ): Seq[(String,Int)]={
      myString.split(";").toList.map(elem=> (elem.split(",")(0),elem.split(",")(1).toInt)).toSeq
    }
     def readRAF(begin:Int,size:Int, file: RandomAccessFile):String={
       file.seek(begin)
       List.range(1,size).map(_=>file.readChar()).toArray.mkString("")
     }

    //TODO--------------
    def getTfsForDoc(doc:Int):Option[Seq[(String,Int)]]={
      docs.get(doc) match{
        case Some((begin,size))=> Option(createSeqFromString(readRAF(begin,size,index).split(":")(1)))
        case None => None
      }
    }
    // def getTfsForStem(stem:String):Option[Seq[(Int,Int)]] = {
    //   stems.get(stem) match{
    //     case Some(str)=> createSeqFromString(str.split(":")(1)).map(a=>(a._1.toInt,a._2))
    //     case None => None
    //   }
    // }
    def getStrDoc(id:Int):String={
      return ""
    }
  }


}
