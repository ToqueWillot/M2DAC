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

    //Big File
    //index contains idDoc=>[(word=>tf)]
    //inverted contains word=>[(idDoc=>tf)]

    val file_index_name:String="../indexation/src/resources/cacm_index.csv"
    val file_index : File = new File("../indexation/src/resources/"+name+"_index.csv")
    val index: RandomAccessFile = new RandomAccessFile(file_index, "rw")

    val file_inverted_name:String="../indexation/src/resources/"+name+"_inverted.csv"
    val file_inverted : File = new File("../indexation/src/resources/"+name+"_inverted.csv")
    val inverted: RandomAccessFile = new RandomAccessFile(file_inverted, "rw")

    //Little File
    //Contains the position for each word or doc in the big file
    val filenameDocs:String="../indexation/src/resources/"+name+"_docs.csv"
    val filenameStems:String="../indexation/src/resources/"+name+"_stems.csv"

    //file contains
    //links_succ contains idDoc=>[idDocS]
    //links_pred contains idDoc=>[idDocP]
    val filenameDocFrom:String="../indexation/src/resources/"+name+"_docFrom.csv"
    val filenameLinksSucc:String="../indexation/src/resources/"+name+"_linksSucc.csv"
    val filenameLinksPred:String="../indexation/src/resources/"+name+"_linksPred.csv"


    //hashMap contains the positions

    val docs: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()
    if(new java.io.File(filenameDocs).exists ){
        val file = scala.io.Source.fromFile(filenameDocs, "UTF-8").getLines.toList.map(line=>{
          val tuple = lineToMapIntIntInt(line)
          docs.put(tuple._1,(tuple._2,tuple._3))
        })
    }
    println("indeex _docs ok")
    val stems: scala.collection.mutable.Map[String,(Int,Int)]=scala.collection.mutable.Map()
    if(new java.io.File(filenameStems).exists ){
        val file = scala.io.Source.fromFile(filenameStems, "UTF-8").getLines.toList.map(line=>{
          val tuple = lineToMapStringIntInt(line)
          stems.put(tuple._1,(tuple._2,tuple._3))
        })
    }
    println("indeex _stems ok")

    val docFrom: scala.collection.mutable.Map[Int,(String,Int,Int)]=scala.collection.mutable.Map()
    if(new java.io.File(filenameDocFrom).exists ){
        val file = scala.io.Source.fromFile(filenameDocFrom, "UTF-8").getLines.toList.map(line=>{
          val tuple = lineToMapIntStringIntInt(line)
          docFrom.put(tuple._1,(tuple._2,tuple._3,tuple._4))
        })
    }

    println("indeex _docFrom ok")
    val linksSucc: scala.collection.mutable.Map[Int,Option[Seq[Int]]]=scala.collection.mutable.Map()
    if(new java.io.File(filenameLinksSucc).exists ){
        val file = scala.io.Source.fromFile(filenameLinksSucc, "UTF-8").getLines.toList.map(line=>{
          val tuple = line.split(":")
          val list :Option[Seq[Int]]= tuple.size match{
            case 1 => None
            case _ => Option(tuple(1).split(";").map(_.toInt).toSeq)
          }
          linksSucc.put(tuple(0).toInt,list)
        })
    }

    println("indeex linkssucc ok")
    val linksPred: scala.collection.mutable.Map[Int,Option[Seq[Int]]]=scala.collection.mutable.Map()
    if(new java.io.File(filenameLinksPred).exists) {
        val file = scala.io.Source.fromFile(filenameLinksPred, "UTF-8").getLines.toList.map(line=>{
          val tuple = line.split(":")
          val list :Option[Seq[Int]]= tuple.size match{
            case 1 => None
            case _ => Option(tuple(1).split(";").map(_.toInt).toSeq)
          }
          linksPred.put(tuple(0).toInt,list)
        })
    }
    println("indeex linksPred ok")

    //indexation : create files:  index inverted  and
    def indexation(filename:String):Unit={
      val s =System.nanoTime
      if(!(new java.io.File(filenameDocs).exists)){ indexation_index(filename)}
      println("docs terminé, time="+(System.nanoTime-s)/1e9+"s")
      if(!(new java.io.File(filenameStems).exists)){ indexation_inverted2(filename)}
      println("stems terminé, time="+(System.nanoTime-s)/1e9+"s")
      if(!(new java.io.File(filenameDocFrom).exists)){ createDocFrom(filename)}
      println("docFrom terminé, time="+(System.nanoTime-s)/1e9+"s")
      if(!(new java.io.File(filenameLinksSucc).exists)){ createLinksSucc(filename)}
      println("linksSucc terminé, time="+(System.nanoTime-s)/1e9+"s")
      if(!(new java.io.File(filenameLinksPred).exists)){ createLinksPred(linksSucc)}
      println("linksPred terminé, time="+(System.nanoTime-s)/1e9+"s")
    }

    // functions create 3 file docfrom, LinksSucc, LinksPred
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


    def createLinksSucc(filename:String):Unit={
      parser.init(filename)
      val pw = new PrintWriter(new File(filenameLinksSucc))
      var doc = parser.nextDocument()
      while(doc!=null){
        val links:String = doc.get("links")
        val line:String = doc.getId()+":"+links
        var l=""

        val list :Option[Seq[Int]] =  links.size match{
          case 0 => None
          case _ => {
            try {
              l=links
              Option(links.split(";").map(_.toInt).toSeq)
            }
            catch{
              case nfe:NumberFormatException => {
                println("Error with doc"+doc.getId())
                l=""
                None
              }
            }

          }
        }
        pw.write(doc.getId()+":"+l+"\n")
        linksSucc.put(doc.getId().toInt ,list)
        doc = parser.nextDocument()
      }
      pw.close
    }


    def createLinksPred(linksSucc:scala.collection.mutable.Map[Int,Option[Seq[Int]]]):Unit={
       val listTmp:scala.collection.mutable.Map[Int,scala.collection.mutable.ListBuffer[Int]]=linksSucc.map(e=>(e._1,new scala.collection.mutable.ListBuffer[Int]))

       linksSucc.toSeq.foreach(succ=>{
          succ._2 match{
            case Some(list)=>list.map(e=>listTmp(e)+=succ._1)
            case None=> //nothing
          }
        })

       val pw = new PrintWriter(new File(filenameLinksPred))
       listTmp.foreach(e=>{
           e._2.size match {
            case 0=> {
              linksPred.put(e._1,None)
              val line:String = e._1.toString+":"
              pw.write(line+"\n")
            }
            case _=> linksPred.put(e._1,Option(e._2.toSeq))
            val line:String = e._1.toString+":"+e._2.mkString(";")
            pw.write(line+"\n")
           }
         }
       )
       pw.close

    }
    def createInverted(index: RandomAccessFile):Unit={

       //create the list that contains word (idDoc,tf)
       val listTmp:scala.collection.mutable.Map[String,scala.collection.mutable.ListBuffer[(Int,Int)]]=scala.collection.mutable.Map()
       println("ooook1")
       var cpt=1
       docs.foreach{u=>{
         val line = readRAF2(u._2._1,u._2._2,index)
         val split=line.split(":")
         val id:Int=split(0).toInt
         split.size match{
           case 1 =>
           case _ => split(1).split(";").foreach(t=>{
             val word=t.split(",")(0)
             val nb = t.split(",")(1).toInt
             listTmp.keySet.exists(_ == word) match{
               case true => listTmp(word)+=(id->nb)
               case false => listTmp+=(word->scala.collection.mutable.ListBuffer((id,nb)))
             }
           })
         }
         print("line number "+cpt)
         cpt+=1
       }
     }

       println("ooook2")
       //write in stems and in the file inverted
       val pw = new PrintWriter(new File(filenameStems))
       inverted.setLength(0)
       inverted.seek(0)
       listTmp.foreach(word=>{
            val curF:Int=inverted.getFilePointer().toInt
            val line= word._1+":"+word._2.map(tup=>tup._1.toString+","+tup._2.toString).mkString(";")
            val sizeLine:Int = line.size
            inverted.writeBytes(line+"\n")
            stems.put(word._1 ,(curF, sizeLine))
            pw.write(word._1+":"+curF.toString+":"+sizeLine.toString+"\n")
           }
       )
       println("ooook3")
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

        index.writeBytes(line+"\n")
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

    // def indexation_links(filename:String):Unit={
    //   links_succ.setLength(0);
    //   parser.init(filename)
    //   links_succ.seek(0)
    //
    //   var doc = parser.nextDocument()
    //   while(doc!=null){
    //     val curF:Int=links_succ.getFilePointer().toInt
    //     val line:String = doc.getId()+":"+doc.get("links_succ")
    //     val sizeLine:Int = line.size
    //
    //     links_succ.writeChars(line+"\n")
    //     linksPosition.put(doc.getId().toInt ,(curF, sizeLine))
    //     doc = parser.nextDocument()
    //   }
    //   //write in file docs
    //   val pw = new PrintWriter(new File(filenameLinks))
    //   linksPosition.map(elem=>{
    //     val txt:String=elem._1.toString+":"+elem._2._1.toString+":"+elem._2._2.toString+"\n"
    //     pw.write(txt)
    //   })
    //   pw.close
    // }

    //TODO
    //inverted en partant des index=> lire lignes une par une et ajouter dans la hashmapmotnb


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
      val pw = new PrintWriter(new File(filenameStems))
      mapWordDoc.foreach(e=>{
      val curF:Int=inverted.getFilePointer().toInt
      val line:String = e._1+":"+createStringFromMapIntInt(e._2.toMap)
      val sizeLine:Int = line.size
      inverted.writeBytes(line+"\n")
      stems.put(e._1 ,(curF, sizeLine))
      val txt:String=e._1.toString+":"+curF.toString+":"+sizeLine.toString+"\n"
      pw.write(txt)
      })
      pw.close
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
            mapWordSize.put(t._1, t._2+(t._1+":").size)
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
       val s=System.nanoTime
       file.seek(begin)
       println("temsfile seek "+(System.nanoTime-s)/1e9+"s")
       val s2=System.nanoTime
       val r = file.readLine()
       println("temsfile readline "+(System.nanoTime-s2)/1e9+"s")
       r
       //List.range(0,size).map(_=>file.readChar()).toArray.mkString("")
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
    // def getLinksSuccForDoc(doc:Int):Option[Seq[Int]] = {
    //   linksPosition.get(doc) match{
    //     case Some((begin,size))=> {
    //       val str = readRAF2(begin,size,links_succ).split(":")
    //       if(str.size<=1){
    //         None
    //       }
    //       else{
    //         Option(str(1).split(";").map(_.toInt).toSeq)
    //       }
    //     }
    //     case None => None
    //   }
    // }
    // val AllLinksSucc:Map[Int,Option[Seq[Int]]]=  indexer.linksPosition.map(elem=> elem._1, getLinksSuccForDoc(elem._1))
    //
    // val AllLinksPrec:Map[Int,Option[Seq[Int]]]={
    //   val mapSucc  = AllLinksSucc.map(elem=> elem.map(id=>(id._1,id._2)))
    //
    //
    // }

    //TODO--------------
    def getStrDoc(id:Int):Option[String]={
      docFrom.get(id) match{
        case Some((f,begin,size))=> Option(readRAF(begin,size/2,(new RandomAccessFile(f, "r"))))
        case None => None
      }
    }
  }


}
