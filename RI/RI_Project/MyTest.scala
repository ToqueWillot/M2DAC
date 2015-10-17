//Fichier de test et de nimp


  import com.fulldeep.indexation._
  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._
  import java.io.RandomAccessFile
  import scala.io
  import java.io.File

  val filename :String = "../../data/cacm/cacm.txt"
  //val filename :String = "../cacmTaille2Test.txt"

  val parser = new ParserCISI_CACM()
  val stemmer = new Stemmer()

  val indexer = new Index.Index("cacm",parser,stemmer)
  indexer.docs
  indexer.indexation(filename)

  val weighter = new Weighter.WeighterTF(indexer)

  val query=" What articles exist which deal with TSS (Time Sharing System), an operating system for IBM computers?"
  val mapquery :Map[String,Int]= indexer.getMapWordOccurFromString(query)
  weighter.getWeightsForQuery(mapquery)
  indexer.getTfsForStem("articl")


  //----------------------------------------------------

//deux manière de sauvegarder docs 1-json lire dun bloc 2-txt lire en stream
//Json
// 1-lire le fichier json => ressortir un string str
// 2-read[Map[String,(String,String)]](str).map(a=>(a._1.toInt,(a._2._1.toInt,a._2._2.toInt))) on a notre map
// pour ecrire:
// 1- map b = m.map(a=>(a._1.toString,(a._2._1.toString,a._2._2.toString))).toMap
// 2- str = Serialization.write(b)
// 3- ecrire str dans un fichier.
//txt


// val docs: scala.collection.mutable.Map[Int,(Int,Int)]=scala.collection.mutable.Map()
// if(new java.io.File("src/resources/"+name+"_docs.csv").exists ){
//     val file = scala.io.Source.fromFile("src/resources/"+name+"_docs.csv", "UTF-8").getLines.toList.map(line=>{
//       val tuple = lineToMapIntIntInt(line)
//       docs.put(tuple._1,(tuple._2,tuple._3))
//     })
// }

// import java.io._
// val pw = new PrintWriter(new File("hello.txt" ))
// docs.map(elem=>{
//   val txt:String=elem._1.toString+":"+elem._2._1.toString+":"+elem._2._2.toString
//   pw.write(txt)
// })
// pw.close



import com.fulldeep.indexation._
import com.fulldeep.modeles._
import com.fulldeep.evaluation._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.io.RandomAccessFile
import scala.io
import java.io.File

val filename :String = "../../data/cacm/cacm.txt"
//val filename :String = "../cacmTaille2Test.txt"

val parser = new ParserCISI_CACM()
val stemmer = new Stemmer()

val indexer = new Index.Index("cacm",parser,stemmer)
indexer.docs
indexer.indexation(filename)

val weighter = new Weighter.WeighterTF1(indexer)

val query=" What articles exist which deal with TSS (Time Sharing System), an operating system for IBM computers?"
val mapquery :Map[String,Int]= indexer.getMapWordOccurFromString(query)
weighter.getWeightsForQuery(mapquery)
indexer.getTfsForStem("techniqu")

// test du query parser
val fileQuery:String="../../data/cacm/cacm.qry"
val fileRel:String="../../data/cacm/cacm.rel"
val qp=new QueryParser(fileQuery,fileRel)
val query1:Query=qp.nextQuery()

val categorizer = new IRmodele.Vectoriel(indexer,weighter)
categorizer.getRanking(mapquery)

val parserr= new ParserCISI_CACM()
parserr.init(filename)
val d = parserr.nextDocument()



/* ----- Test TME1 ---- */

import com.fulldeep.indexation._
import java.io.FileInputStream
val filename :String = "../../data/cacm/cacm.txt"
//val filename :String = "../cacmTaille2Test.txt"

val parser = new ParserCISI_CACM()
val stemmer = new Stemmer()

val indexer = new Index.Index("cacm",parser,stemmer)
indexer.createInverted(indexer.index)

indexer.createLinksSucc(filename)
indexer.createLinksPred(indexer.linksSucc)
indexer.indexation_index(filename)

indexer.docs
indexer.stems
indexer.docFrom
indexer.linksSucc
indexer.linksPred

indexer.docs
indexer.stems
indexer.docFrom
indexer.linksSucc
indexer.linksPred
