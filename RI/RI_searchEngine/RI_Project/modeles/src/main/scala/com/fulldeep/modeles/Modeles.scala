package com.fulldeep.modeles;

import com.fulldeep.indexation._

object Weighter {

  abstract class Weighter(){
    def getDocWeightsForDoc(idDoc:Int):Option[Seq[(String,Float)]]
    def getDocWeightsForStem(stem:String):Option[Seq[(Int,Float)]]
    def getWeightsForQuery(query:Map[String,Int]):Seq[(String,Float)]
  }

  class WeighterTF1(val index:Index.Index) extends Weighter {
    def getDocWeightsForDoc(idDoc:Int):Option[Seq[(String,Float)]]={
      val s =System.nanoTime
      val r = index.getTfsForDoc(idDoc) match {
        case None=>None
        case Some(seq)=>{
          //val nbWords:Int = seq.map(a=>a._2).sum
          //Option(seq.map(a=>(a._1,a._2/nbWords.toFloat)))
          Option(seq.map(a=>(a._1,a._2.toFloat)))
        }
      }
      //println("temps pour un getdocweightfordoc==== "+(System.nanoTime-s)/1e9+"s")
      return r
    }

    def getDocWeightsForStem(stem:String):Option[Seq[(Int,Float)]]={
      index.getTfsForStem(stem) match {
        case None=>None
        case Some(seq)=>{
          Option(seq.map(doc=>(doc._1,getDocWeightsForDoc(doc._1).get.filter(e=>e._1==stem)(0)._2)))
        }
      }
    }
    def getWeightsForQuery(query:Map[String,Int]):Seq[(String,Float)]={
      query.map(e=>(e._1,1.0f)).toSeq
    }
  }



  class WeighterTF(val index:Index.Index) extends Weighter {
    def getDocWeightsForDoc(idDoc:Int):Option[Seq[(String,Float)]]={
      index.getTfsForDoc(idDoc) match {
        case None=>None
        case Some(seq)=>{
          val nbWords:Int = seq.map(a=>a._2).sum
          Option(seq.map(a=>(a._1,a._2/nbWords.toFloat)))
        }
      }
    }

    def getDocWeightsForStem(stem:String):Option[Seq[(Int,Float)]]={
      index.getTfsForStem(stem) match {
        case None=>None
        case Some(seq)=>{
          Option(seq.map(doc=>(doc._1,getDocWeightsForDoc(doc._1).get.filter(e=>e._1==stem)(0)._2)))
        }
      }
    }
    def getWeightsForQuery(query:Map[String,Int]):Seq[(String,Float)]={
      query.toSeq.map(q=>{
        index.getTfsForStem(q._1) match {
          case None=> (q._1,0.0f)
          case Some(seq)=> (q._1,math.log(seq.size/q._2.toDouble).toFloat)
        }
      })
    }
  }
}
