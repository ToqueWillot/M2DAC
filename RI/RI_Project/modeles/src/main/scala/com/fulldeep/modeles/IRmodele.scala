package com.fulldeep.modeles;

import com.fulldeep.indexation._
import scala.util.Random

object IRmodele {

  abstract class IRmodele(val index:Index.Index,val weighter:Weighter.Weighter){

    def getScores(query: Map[String,Int],normalized:Boolean):Seq[(Int,Float)]

    def getRanking(query: Map[String,Int]):Seq[(Int,Float)]={
        val s =System.nanoTime
        val scores:Seq[(Int,Float)] = getScores(query,false)
        val rank = Random.shuffle(scores).sortBy{-_._2}
        println("--- temps du ranking ==== "+(System.nanoTime-s)/1e9+"s")
        rank
    }
  }




  class Vectoriel(val index:Index.Index,val weighter:Weighter.Weighter) extends IRmodele(index,weighter) {

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
      //println("temps total getdocw "+(timetot)/1e9+"s")
      return res
    }

  }

  class LanguageModel(val index:Index.Index,val weighter:Weighter.Weighter,val lambda:Float) extends IRmodele(index,weighter) {

    def getScores(query: Map[String,Int],normalized : Boolean = false):Seq[(Int,Float)]={
        val mapp:scala.collection.mutable.Map[Int,Float]=scala.collection.mutable.Map()
        val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)

        queryWeight.foreach(w=> {
          val weightWinCorpus=weighter.getDocWeightsForStem(w._1) match{
            case None=>0.0f
            case Some(l)=>l.map(_._2).sum
          }

          weighter.getDocWeightsForStem(w._1) match{
            case None=>
            case Some(s)=>{
                index.docs.toSeq.foreach(doc=>{

                var pdt= s.toMap.getOrElse(doc._1,0.0f)/weighter.getDocWeightsForDoc(doc._1).get.map(_._2).sum
                if(!(mapp.contains(doc._1)) ){
                  mapp.put(doc._1,0.0f)

                }
                mapp.put(doc._1,mapp(doc._1)+ queryWeight.toMap.getOrElse(w._1,0.0f) *math.log(lambda *pdt+(1-lambda)*weightWinCorpus))
              })
            }
          }
        })
        mapp.toSeq
    }
  }


}
