//Fichier de test et de nimp


import com.fulldeep.indexation._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

val filename :String = "/Users/floriantoque/Documents/M2DAC_TME/data/cacm/cacm.txt"
val parser = new ParserCISI_CACM()
parser.init()



val words: String=  "il fait super beau aujourd'hui"
val stemmer = new Stemmer()
val hashWordNumber:Map[String,Int] = stemmer.porterStemmerHash(words).asScala.mapValues(_.intValue).toMap - " * "
