package com.fulldeep.indexation;



// import java.io._;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
import com.fulldeep.indexation._

object index {
//REMPLIIIR DE PLEIN DE BONNES CHOSES
  class index(val name:String, val parser:Parser, val textRepresenter:Stemmer) extends Serializable {

    def indexation(filename:String)={

    }
    def getTfsForDoc(index:RandomAccessFile):Map[Int,List[(String,Int)]] = {

    }
    def getTfsForStem(index:RandomAccessFile):Map[String,List[(Int,Int)]] = {

    }
    def getStrDoc(id:Int):String={

    }
  }


}
