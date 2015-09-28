package com.fulldeep.modeles;

import com.fulldeep.indexation._

object Weighter {

  abstract class Weighter(val index:Index.Index){
    def getDocWeightsForDoc(idDoc:String):Seq[(String,Float)]
    def getDocWeightsForStem(stem:String):Seq[(Int,Float)]
    def getWeightsForQuery(query:Map[String,Int]):Seq[(String,Float)]
  }
  // class Weighter1(val index:Index) extends Weighter {
  //
  // }

}
