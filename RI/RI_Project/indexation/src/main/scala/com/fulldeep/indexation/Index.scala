package com.fulldeep.indexation;



import scala.collection.JavaConverters._
import com.fulldeep.indexation._
import java.io.File
import java.io.FileInputStream
import java.io.RandomAccessFile
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.PrintWriter

object Index {

  class Index(val name:String, val parser:Parser, val textRepresenter:Stemmer) extends Serializable {

    val file_index : File = new File("src/resources/"+name+"_index.csv")
    val index: RandomAccessFile = new RandomAccessFile(file_index, "rw")

    val file_inverted : File = new File("src/resources/"+name+"_inverted.csv")
    val inverted: RandomAccessFile = new RandomAccessFile(file_inverted, "rw")

    val filenameDocs:String="src/resources/"+name+"_docs.csv"
    val filenameStems:String="src/resources/"+name+"_stems.csv"
    val filenameDocFrom:String="src/resources/"+name+"_docFrom.csv"

    val docs: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()
    if(new java.io.File(filenameDocs).exists ){
        val file = scala.io.Source.fromFile(filenameDocs, "UTF-8").getLines.toList.map(line=>{
          val tuple = lineToMapIntIntInt(line)
          docs.put(tuple._1,(tuple._2,tuple._3))
        })
    }

    val stems: scala.collection.mutable.Map[String,(Int,Int)]=scala.collection.mutable.Map()
    if(new java.io.File(filenameStems).exists ){
        val file = scala.io.Source.fromFile(filenameStems, "UTF-8").getLines.toList.map(line=>{
          val tuple = lineToMapStringIntInt(line)
          stems.put(tuple._1,(tuple._2,tuple._3))
        })
    }


    //TODO---
    val docFrom: scala.collection.mutable.Map[Int,(String,Int,Int)]=scala.collection.mutable.Map()
    if(new java.io.File(filenameDocFrom).exists ){
        val file = scala.io.Source.fromFile(filenameDocFrom, "UTF-8").getLines.toList.map(line=>{
          val tuple = lineToMapIntStringIntInt(line)
          docFrom.put(tuple._1,(tuple._2,tuple._3,tuple._4))
        })
    }


    //indexation : create files:  index inverted  and
    def indexation(filename:String):Unit={
      if(!(new java.io.File(filenameDocs).exists)){ indexation_index(filename)}
      if(!(new java.io.File(filenameStems).exists)){ indexation_inverted(filename)}
      if(!(new java.io.File(filenameDocFrom).exists)){ createDocFrom(filename)}
    }

    def createDocFrom(filename:String):Unit={
      parser.init(filename)
      val pw = new PrintWriter(new File(filenameDocFrom))
      var doc = parser.nextDocument()
      while(doc!=null){
        val line:String = doc.getId()+";"+doc.get("from")
        val split = line.split(";")
        pw.write(line+"\n")
        docFrom.put(doc.getId().toInt ,(split(1).toString, split(2).toInt, split(3).toInt))
        doc = parser.nextDocument()
      }
      pw.close
    }

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
      //write in file docs
      val pw = new PrintWriter(new File(filenameDocs))
      docs.map(elem=>{
        val txt:String=elem._1.toString+":"+elem._2._1.toString+":"+elem._2._2.toString+"\n"
        pw.write(txt)
      })
      pw.close
    }

    // def indexation_inverted2(filename:String):Unit={
    //   parser.init(filename)
    //
    //   val mapWordDoc: scala.collection.mutable.Map[String,scala.collection.mutable.Map[Int,Int]]=scala.collection.mutable.Map()
    //   var doc = parser.nextDocument()
    //   while(doc!=null){
    //     val listWordDoc: List[(String,(Int,Int))] = getMapWordOccurFromString(doc.getText()).map(e=> (e._1,(doc.getId().toInt,e._2))).toList
    //
    //     listWordDoc.foreach(t =>
    //       if( mapWordDoc.keySet.exists(_ == t._1) )
    //         mapWordDoc(t._1).put(t._2._1,t._2._2)
    //       else
    //         mapWordDoc.put(t._1, scala.collection.mutable.Map((t._2._1,t._2._2)))
    //     )
    //     doc = parser.nextDocument()
    //   }
    //
    //   inverted.seek(0)
    //   mapWordDoc.map(e=>{
    //   val curF:Int=inverted.getFilePointer().toInt
    //   val line:String = e._1+":"+createStringFromMapIntInt(e._2.toMap)
    //   val sizeLine:Int = line.size
    //   inverted.writeChars(line+"\n")
    //   stems.put(e._1 ,(curF, sizeLine))
    //   })
    // }

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
      Console.flush();
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
            val toWrite :String= w._1+":"+w._2._1+","+w._2._2+";"
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


      //write in file docs
      val pw = new PrintWriter(new File(filenameStems))
      stems.map(elem=>{
        val txt:String=elem._1.toString+":"+elem._2._1.toString+":"+elem._2._2.toString+"\n"
        pw.write(txt)
      })
      pw.close

    }



    //function utils

    def lineToMapIntIntInt(line:String):(Int,Int,Int)={
      val elem = line.split(":")
      (elem(0).toInt,elem(1).toInt,elem(2).toInt)
    }
    def lineToMapStringIntInt(line:String):(String,Int,Int)={
      val elem = line.split(":")
      (elem(0),elem(1).toInt,elem(2).toInt)
    }
    def lineToMapIntStringIntInt(line:String):(Int,String,Int,Int)={
      val elem = line.split(";")
      (elem(0).toInt,elem(1),elem(2).toInt,elem(3).toInt)
    }
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
       List.range(0,size).map(_=>file.readByte().asInstanceOf[Char]).toArray.mkString("")
     }
     def readRAF2(begin:Int,size:Int, file: RandomAccessFile):String={
       file.seek(begin)
       List.range(0,size).map(_=>file.readChar()).toArray.mkString("")
     }

    //TODO--------------
    def getTfsForDoc(doc:Int):Option[Seq[(String,Int)]]={
      docs.get(doc) match{
        case Some((begin,size))=> Option(createSeqFromString(readRAF2(begin,size,index).split(":")(1)))
        case None => None
      }
    }
    def getTfsForStem(stem:String):Option[Seq[(Int,Int)]] = {
      stems.get(stem) match{
        case Some((begin,size))=> Option(createSeqIntIntFromString(readRAF2(begin,size,inverted).split(":")(1)))
        case None => None
      }
    }

    //TODO--------------
    def getStrDoc(id:Int):Option[String]={
      docFrom.get(id) match{
        case Some((f,begin,size))=> Option(readRAF(begin,size/2,(new RandomAccessFile(f, "r"))))
        case None => None
      }
    }
  }


}
