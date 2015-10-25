package com.fulldeep.modeles;

import com.fulldeep.indexation._
import scala.util.Random

object IRmodele {

  abstract class IRmodele(){

    def getScores(query: Map[String,Int],normalized:Boolean):Seq[(Int,Float)]

    def getRanking(query: Map[String,Int]):Seq[(Int,Float)]={
        val s =System.nanoTime
        val scores:Seq[(Int,Float)] = getScores(query,false)
        val rank = Random.shuffle(scores).sortBy{-_._2}
        println("--- temps du ranking ==== "+(System.nanoTime-s)/1e9+"s")
        rank
    }
  }




  class Vectoriel(val index:Index.Index,val weighter:Weighter.Weighter) extends IRmodele {

    def getScores(query: Map[String,Int],normalized : Boolean = false):Seq[(Int,Float)]={
      var timeDocWeight=0.0f
      var timetot:Float=0.0f
      val res = normalized match {
        case false => {
          val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
          index.docs.toSeq.map(doc=> {
            timeDocWeight=System.nanoTime.toFloat
            val mapDocWeights = weighter.getDocWeightsForDoc(doc._1).get.toMap
            timetot+=(System.nanoTime-timeDocWeight).toFloat
            (doc._1,queryWeight.map(word=>word._2 * mapDocWeights.getOrElse(word._1,0.0f)).sum)
          })
        }
        case true =>{
          val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
          val normQuery :Float= math.sqrt(queryWeight.map(word => word._2*word._2).sum).toFloat
          index.docs.toSeq.map(doc=> {
            val mapDocWeights = weighter.getDocWeightsForDoc(doc._1).get.toMap
            val normDoc :Float= math.sqrt(mapDocWeights.map(word => word._2*word._2).sum).toFloat
            (doc._1,queryWeight.map(word=>(word._2*mapDocWeights.getOrElse(word._1,0.0f))).sum/(normDoc*normQuery)  )
          })
        }
      }
      println("temps total getdocw "+(timetot)/1e9+"s")
      return res
    }

  }

  // class LanguageModel(val index:Index.Index,val weighter:Weighter.Weighter) extends IRmodele {
  //
  //
  //   def pmquery(query:Map[String,Int],doc:Map[String,Int]):Float={
  //     val tailledoc=doc.map(_._2).sum
  //     query.map(w=>{
  //       Option(doc(w._1)) match {
  //         case Some(n)=> n*math.log(n.toFloat/tailledoc).toFloat
  //         case None=> 0.0f
  //       }
  //     }).sum
  //   }
  //
  //
  //
  //   def getScores(query: Map[String,Int],normalized : Boolean = false):Seq[(Int,Float)]={
  //     normalized match {
  //       case false => {
  //         val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
  //
  //
  //       }
  //       case true =>{
  //
  //       }
  //     }
  //
  //   }
  //
  // }

}
