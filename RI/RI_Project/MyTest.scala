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

//
// def e():Unit={
//   val s = System.nanoTime
//   indexer.getTfsForDoc(4192)
//   println("time"+(System.nanoTime-s)/1e9+"s")
// }
// indexer.getTfsForDoc(4192).get.size
//
// indexer.docs(4192)
// def g(a:Int):Unit={
//   val s = System.nanoTime
//   indexer.readRAF2(indexer.docs(a)._1,indexer.docs(4192)._2,indexer.index)
//   println("time"+(System.nanoTime-s)/1e9+"s")
// }
//
// def f(a:Int):Unit={
//   val myfile="/Users/floriantoque/Documents/UPMC/M2DAC_git/RI/RI_Project/indexation/src/resources/cacm_index.csv"
//   val reader = new BufferedReader(new FileReader(myfile));
//   val s = System.nanoTime
//   reader.seek(indexer.docs(a)._1.toLong)
//   println("time"+(System.nanoTime-s)/1e9+"s")
//   reader.readLine()
//
// }


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
//val filename :String = "../../data/cisi/cisi.txt"

val parser = new ParserCISI_CACM()
val stemmer = new Stemmer()

val indexer = new Index.Index("cacm",parser,stemmer)
//val indexer = new Index.Index("cisi",parser,stemmer)
// indexer.indexation_index(filename)
// indexer.indexation_inverted2(filename)
indexer.indexation(filename)
//todo make smart inverted
//indexer.createInverted(indexer.index)

// indexer.createLinksSucc(filename)
// indexer.createLinksPred(indexer.linksSucc)

indexer.docs
indexer.stems
indexer.docFrom
indexer.linksSucc
indexer.linksPred

/* ----- Test TME2 ---- */
import com.fulldeep.modeles._
import java.io.BufferedReader
import java.io.FileReader
val weighter = new Weighter.WeighterTF1(indexer)

val query=" What articles exist which deal with TSS (Time Sharing System), an operating system for IBM computers?"
val mapquery :Map[String,Int]= indexer.getMapWordOccurFromString(query)
weighter.getWeightsForQuery(mapquery)
indexer.getTfsForStem("techniqu")



/* ----- Test TME3 ---- */
import com.fulldeep.evaluation._



val categorizer = new IRmodele.Vectoriel(indexer,weighter)

// test du query parser
// val fileQuery:String="../../data/cisi/cisi.qry"
// val fileRel:String="../../data/cisi/cisi.rel"
// val qp=new QueryParser(fileQuery,fileRel)

val fileQuery:String="../../data/cacm/cacm.qry"
val fileRel:String="../../data/cacm/cacm.rel"
val qp=new QueryParser(fileQuery,fileRel)

val queries:scala.collection.mutable.ListBuffer[Query]=scala.collection.mutable.ListBuffer()
var q = qp.nextQuery()
while (q!=None){
  queries+=q.get
  q = qp.nextQuery()
}


val queriesList=queries.toList
//val measure: EvalMeasure= new Precision_Recall()
val measure: EvalMeasure= new Precision_Mean()
val evalIrmodele=new EvalIRModele(categorizer,measure,queriesList)
evalIrmodele.eval()
println(evalIrmodele.mean)


/* ----- Test TME4 ---- */
import com.fulldeep.evaluation._

//val categorizer = new IRmodele.LanguageModel(indexer,weighter,lambda=0.8f)
val categorizer = new IRmodele.OKAPI(indexer,weighter, k= 1.5f, b= 0.75f)
categorizer.getRanking(mapquery)

// test du query parser
// val fileQuery:String="../../data/cisi/cisi.qry"
// val fileRel:String="../../data/cisi/cisi.rel"
// val qp=new QueryParser(fileQuery,fileRel)

val fileQuery:String="../../data/cacm/cacm.qry"
val fileRel:String="../../data/cacm/cacm.rel"
val qp=new QueryParser(fileQuery,fileRel)

val queries:scala.collection.mutable.ListBuffer[Query]=scala.collection.mutable.ListBuffer()
var q = qp.nextQuery()
while (q!=None){
  queries+=q.get
  q = qp.nextQuery()
}


val queriesList=queries.toList
//val measure: EvalMeasure= new Precision_Recall()
val measure: EvalMeasure= new Precision_Mean()
val evalIrmodele=new EvalIRModele(categorizer,measure,queriesList)
evalIrmodele.eval()
println(evalIrmodele.mean)

/* ----- Test TME5 ---- */
import com.fulldeep.evaluation._

//indexation
import com.fulldeep.indexation._
import java.io.FileInputStream
val filename :String = "../../data/cacm/cacm.txt"
val parser = new ParserCISI_CACM()
val stemmer = new Stemmer()
val indexer = new Index.Index("cacm",parser,stemmer)
indexer.indexation(filename)
//weighter et query
import com.fulldeep.modeles._
val weighter = new Weighter.WeighterTF1(indexer)
val query=" What articles exist which deal with TSS (Time Sharing System), an operating system for IBM computers?"
val mapquery :Map[String,Int]= indexer.getMapWordOccurFromString(query)
weighter.getWeightsForQuery(mapquery)
indexer.getTfsForStem("techniqu")


//variables pour le randomWalk
val modelInit = new IRmodele.Vectoriel(indexer,weighter)
val walkMoon = new pageRank(0.8f,100)
val pred = indexer.linksPred.filter(x=>x._2!=None).map(e=>e._1->e._2.get).toMap
val succ = indexer.linksSucc.filter(x=>x._2!=None).map(e=>e._1->e._2.get).toMap
val nSeed = 30
val nIn = 10

val categorizer = new IRmodele.IRRandomWalk(indexer, weighter, modelInit, walkMoon,  succ, pred,  nSeed, nIn)

categorizer.getRanking(mapquery)

// test du query parser
// val fileQuery:String="../../data/cisi/cisi.qry"
// val fileRel:String="../../data/cisi/cisi.rel"
// val qp=new QueryParser(fileQuery,fileRel)

val fileQuery:String="../../data/cacm/cacm.qry"
val fileRel:String="../../data/cacm/cacm.rel"
val qp=new QueryParser(fileQuery,fileRel)

val queries:scala.collection.mutable.ListBuffer[Query]=scala.collection.mutable.ListBuffer()
var q = qp.nextQuery()
while (q!=None){
  queries+=q.get
  q = qp.nextQuery()
}


val queriesList=queries.toList
//val measure: EvalMeasure= new Precision_Recall()
val measure: EvalMeasure= new Precision_Mean()
val evalIrmodele=new EvalIRModele(categorizer,measure,queriesList)
evalIrmodele.eval()
println(evalIrmodele.mean)
