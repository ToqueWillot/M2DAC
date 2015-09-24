//Fichier de test et de nimp


import com.fulldeep.indexation._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.io.RandomAccessFile
import scala.io
import java.io.File




val filename :String = "../../data/cacm/cacm.txt"
val parser = new ParserCISI_CACM()
parser.init(filename)


val words: String=  "il fait super beau aujourd'hui"
val stemmer = new Stemmer()
val hashWordNumber:Map[String,Int] = stemmer.porterStemmerHash(words).asScala.mapValues(_.intValue).toMap - " * "



val filename :String = "src/resources/test_index"
val file : File = new File(filename)
val raf : RandomAccessFile = new RandomAccessFile(file, "rw")

val name = "blabla"
val filename :String = name+"_index"


def createStringFromMap(myMap: Map[String,Int]):String={
  myMap.map(tuple=> tuple._1+","+tuple._2.toString).toArray.mkString(";")

}
def createMapFromString(myString: String ): Map[String,Int]={
  myString.split(";").toList.map(elem=> (elem.split(",")(0),elem.split(",")(1).toInt)).toMap
}
def getMapWordOccurFromString(text : String,textRepresenter:Stemmer=stemmer): Map[String,Int]={
  textRepresenter.porterStemmerHash(text).asScala.mapValues(_.intValue).toMap - " * "
}

val filename :String = "../cacmTaille2Test.txt"
val parser = new ParserCISI_CACM()
parser.init(filename)

raf.seek(0)
var doc = parser.nextDocument()
while(doc!=null){
  val curF:Int=raf.getFilePointer().toInt
  val line:String = doc.getId()+";"+createStringFromMap(getMapWordOccurFromString(doc.getText()))
  val sizeLine:Int = line.size

  raf.writeChars(line+"\n")
  docs.put(doc.getId().toInt ,(curF, sizeLine-1))
  doc = parser.nextDocument()
}



docs.map(e => {val a = e._2._1
               val b = e._2._2
               println(e._1)
               raf.seek(a)
               for( i <- a to a+b){
                 print(raf.readChar())
               }
               println("  ")
             })






 raf.seek(144)
 for( i <- 72 to 135){
   print(""+ raf.readChar())
 }


 val textRepresenter=stemmer
 def getMapWordOccurFromString(text : String,textRepresenter:Stemmer=textRepresenter): Map[String,Int]={
   textRepresenter.porterStemmerHash(text).asScala.mapValues(_.intValue).toMap - " * "
 }
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


 val file_inverted : File = new File("tesst_inverted.csv")
 val inverted: RandomAccessFile = new RandomAccessFile(file_inverted, "rw")
 inverted.seek(0)
 mapWordDoc.map(e=>{
   val curF:Int=inverted.getFilePointer().toInt
   val line:String = e._1+";"+createStringFromMapIntInt(e._2.toMap)
   val sizeLine:Int = line.size
   inverted.writeChars(line+"\n")
   //stems.put(doc.getId().toInt ,(curF, sizeLine-1))
 })

 mapWordDoc.map(e=>{
   val line:String = e._1+";"+createStringFromMapIntInt(e._2.toMap)
   println(line)
 })


 def readRAF(begin:Int,size:Int, file: RandomAccessFile):String={
   file.seek(begin)
   List.range(1,size).map(_=>file.readChar()).toArray.mkString("")
 }
val filename :String = "../cacmTaille2Test.txt"

indexer.createSeqFromString(indexer.readRAF(0,81,indexer.index).split(":")(1))
 //-------------------------------------------
  import com.fulldeep.indexation._
  import scala.collection.JavaConversions._
  import scala.collection.JavaConverters._
  import java.io.RandomAccessFile
  import scala.io
  import java.io.File

  //val filename :String = "../../data/cacm/cacm.txt"
  val filename :String = "../cacmTaille2Test.txt"

  val parser = new ParserCISI_CACM()
  val stemmer = new Stemmer()

  val indexer = new Index.Index("cacm2",parser,stemmer)
  indexer.docs
  indexer.indexation_index(filename)
  indexer.indexation_inverted(filename)
