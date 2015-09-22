//Fichier de test et de nimp


import com.fulldeep.indexation._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.io.RandomAccessFile
import scala.io

val filename :String = "../../data/cacm/cacm.txt"
val parser = new ParserCISI_CACM()
parser.init(filename)



val words: String=  "il fait super beau aujourd'hui"
val stemmer = new Stemmer()
val hashWordNumber:Map[String,Int] = stemmer.porterStemmerHash(words).asScala.mapValues(_.intValue).toMap - " * "



val filename :String = "src/resources/test_index"
val file :String =
val raf : RandomAccessFile = new RandomAccessFile(file, "rw");
