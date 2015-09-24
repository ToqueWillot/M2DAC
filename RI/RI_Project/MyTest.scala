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
  indexer.indexation_index(filename)
  indexer.indexation_inverted(filename)



  //----------------------------------------------------
