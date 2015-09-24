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
      index.setLength(0);
      parser.init(filename)
      index.seek(0)

      var doc = parser.nextDocument()
      while(doc!=null){
        val curF:Int=index.getFilePointer().toInt
        val line:String = doc.getId()+":"+createStringFromMap(getMapWordOccurFromString(doc.getText()))
        val sizeLine:Int = line.size

        index.writeChars(line+"\n")
        docs.put(doc.getId().toInt ,(curF, sizeLine))
        doc = parser.nextDocument()
      }
    }

    def indexation_inverted2(filename:String):Unit={
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
      val sizeLine:Int = line.size
      inverted.writeChars(line+"\n")
      stems.put(e._1 ,(curF, sizeLine))
      })
    }

    def indexation_inverted(filename:String):Unit={

      //First reading of Documents, keep size of Words representation
      parser.init(filename)
      val mapWordSize: scala.collection.mutable.Map[String,Int]=scala.collection.mutable.Map()
      var doc = parser.nextDocument()
      while(doc!=null){
        val listWordDoc: List[(String,(Int,Int))] = getMapWordOccurFromString(doc.getText()).map(e=> (e._1,(doc.getId().toInt,e._2))).toList
        val wordSize: List[(String,Int)] = listWordDoc.map(elem=>(elem._1,(elem._2._1.toString+","+elem._2._2.toString+";").size))

        wordSize.foreach(t =>
          if( mapWordSize.keySet.exists(_ == t._1) )
            mapWordSize(t._1)+=t._2
          else
            mapWordSize.put(t._1, t._2+ (t._1+":").size)
        )
        doc = parser.nextDocument()
      }
      println("Partie 1/3 terminée ")
      //set buffer for all Words
      val mapTemp: scala.collection.mutable.Seq[(String,Int)] = scala.collection.mutable.Seq(mapWordSize.toSeq: _*)
      var buff:Int = 0
      val seqWordBuffer= mapTemp.map(e=>{
        val wordPlace=e._2
        buff+=wordPlace
        (e._1,buff - wordPlace)
      })
      val mapWordBuffer : scala.collection.mutable.Map[String,Int] = scala.collection.mutable.Map(seqWordBuffer: _*)
      mapWordBuffer.map(w=> stems.put(w._1,(w._2*2,mapWordSize(w._1))))
      println("Partie 2/3 terminée ")
      //Second reading of Documents, writting on inverted
      parser.init(filename)
      doc = parser.nextDocument()
      // PrintWriter writer = new PrintWriter("temp","UTF-8");
      // writer.flush();
      inverted.setLength(0);
      val wordWrite: scala.collection.mutable.Map[String,Boolean]=mapWordBuffer.map(e=> (e._1,false))
      while(doc!=null){
        val listWordDoc: List[(String,(Int,Int))] = getMapWordOccurFromString(doc.getText()).map(e=> (e._1,(doc.getId().toInt,e._2))).toList
        listWordDoc.map(w=>{
          if(wordWrite(w._1)==false){
            wordWrite(w._1)=true
            val curWord:Int = mapWordBuffer(w._1)*2
            inverted.seek(curWord)
            val toWrite :String= w._1+":"+w._2._1+","+w._2._1+";"
            val sizeLine:Int = toWrite.size
            inverted.writeChars(toWrite)
            mapWordBuffer(w._1)+=sizeLine
          }
          else{
            val curWord:Int = mapWordBuffer(w._1)*2
            inverted.seek(curWord)
            val toWrite :String= w._2._1+","+w._2._1+";"
            val sizeLine:Int = toWrite.size
            inverted.writeChars(toWrite)
            mapWordBuffer(w._1)+=sizeLine
          }

        })
        doc = parser.nextDocument()
      }
      println("Partie 3/3 terminée ")
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
    def createSeqIntIntFromString(myString: String ): Seq[(Int,Int)]={
      myString.split(";").toList.map(elem=> (elem.split(",")(0).toInt,elem.split(",")(1).toInt)).toSeq
    }
     def readRAF(begin:Int,size:Int, file: RandomAccessFile):String={
       file.seek(begin)
       List.range(0,size).map(_=>file.readChar()).toArray.mkString("")
     }

    //TODO--------------
    def getTfsForDoc(doc:Int):Option[Seq[(String,Int)]]={
      docs.get(doc) match{
        case Some((begin,size))=> Option(createSeqFromString(readRAF(begin,size,index).split(":")(1)))
        case None => None
      }
    }
    def getTfsForStem(stem:String):Option[Seq[(Int,Int)]] = {
      stems.get(stem) match{
        case Some((begin,size))=> Option(createSeqIntIntFromString(readRAF(begin,size,inverted).split(":")(1)))
        case None => None
      }
    }
    def getStrDoc(id:Int):String={
      return ""
    }
  }


}
