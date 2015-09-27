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
