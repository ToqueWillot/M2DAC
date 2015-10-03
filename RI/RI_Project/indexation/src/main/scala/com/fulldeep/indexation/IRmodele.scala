package com.fulldeep.indexation;

import com.fulldeep.indexation._
import scala.util.Random

object IRmodele {

  abstract class IRmodele(){

    def getScores(query: Map[String,Int],normalized:Boolean):Map[Int,Float]

    def getRanking(query: Map[String,Int]):Seq[(Int,Float)]={
        val scores:Map[Int,Float] = getScores(query,true)
        Random.shuffle(scores.toSeq).sortBy{-_._2}
    }
  }




  class Vectoriel(val index:Index.Index,val weighter:Weighter.Weighter) extends IRmodele {

    def getScores(query: Map[String,Int],normalized : Boolean = false):Map[Int,Float]={
      normalized match {
        case false => {
          val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
          index.docs.map(doc=> {
            val mapDocWeights = weighter.getDocWeightsForDoc(doc._1).get.toMap
            (doc._1,queryWeight.map(word=>word._2*mapDocWeights.getOrElse(word._1,0.0f)).sum)
          }).toMap
        }
        case true =>{
          val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
          val normQuery :Float= math.sqrt(queryWeight.map(word => word._2*word._2).sum).toFloat
          index.docs.map(doc=> {
            val mapDocWeights = weighter.getDocWeightsForDoc(doc._1).get.toMap
            val normDoc :Float= math.sqrt(mapDocWeights.map(word => word._2*word._2).sum).toFloat
            (doc._1,queryWeight.map(word=>(word._2*mapDocWeights.getOrElse(word._1,0.0f))).sum/(normDoc*normQuery)  )
          }).toMap
        }
      }

    }

  }

}
