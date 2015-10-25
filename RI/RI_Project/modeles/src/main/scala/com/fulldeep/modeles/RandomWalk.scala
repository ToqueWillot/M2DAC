package com.fulldeep.modeles;

import com.fulldeep.indexation._
import scala.util.Random

//
// abstract class RandomWalk(val index:Index.Index){
//   val succ:Map[(Int,Seq[Int])]=index.links
//
//   def getScores(query: Map[String,Int],normalized:Boolean):Map[Int,Float]
//
//   def getRanking(query: Map[String,Int]):Seq[(Int,Float)]={
//       val scores:Seq[(Int,Float)] = getScores(query,false)
//       Random.shuffle(scores).sortBy{-_._2}
//   }
// }
//
//
//
//
// class Vectoriel(val index:Index.Index,val weighter:Weighter.Weighter) extends IRmodele {
//
//   def getScores(query: Map[String,Int],normalized : Boolean = false):Seq[(Int,Float)]={
//     normalized match {
//       case false => {
//         val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
//         index.docs.map(doc=> {
//           val mapDocWeights = weighter.getDocWeightsForDoc(doc._1).get.toMap
//           (doc._1,queryWeight.map(word=>word._2 * mapDocWeights.getOrElse(word._1,0.0f)).sum)
//         })
//       }
//       case true =>{
//         val queryWeight: Seq[(String,Float)]= weighter.getWeightsForQuery(query)
//         val normQuery :Float= math.sqrt(queryWeight.map(word => word._2*word._2).sum).toFloat
//         index.docs.map(doc=> {
//           val mapDocWeights = weighter.getDocWeightsForDoc(doc._1).get.toMap
//           val normDoc :Float= math.sqrt(mapDocWeights.map(word => word._2*word._2).sum).toFloat
//           (doc._1,queryWeight.map(word=>(word._2*mapDocWeights.getOrElse(word._1,0.0f))).sum/(normDoc*normQuery)  )
//         }).toMap
//       }
//     }
//
//   }
//
// }
//
// class LanguageModel(val index:Index.Index,val weighter:Weighter.Weighter) extends IRmodele {
//
//
//   def pmquery(query:Map[String,Int],doc:Map[String,Int]):Float={
//     val tailledoc=doc.map(_._2).sum
//     query.map(w=>{
//       doc(w) match {
//         case n=> n*math.log(n.toFloat/tailledoc)
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
