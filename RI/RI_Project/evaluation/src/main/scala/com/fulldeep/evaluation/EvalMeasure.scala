package com.fulldeep.evaluation;

import com.fulldeep.indexation._
import com.fulldeep.modeles._
import scala.util.Random
import breeze.linalg._
import breeze.numerics._


abstract class EvalMeasure(){

  def eval(irList:IRList):Seq[Float]

}



class Precision_Recall(val nbLevels:Int=10) extends EvalMeasure {

    def eval(irList:IRList):Seq[Float]={
      val good=irList.query.relation
      val pred=irList.listScores.toSeq.sortBy{-_._2}

      val k = (breeze.linalg.DenseVector.range(0,nbLevels+1).map(_.toFloat)/nbLevels.toFloat).toArray.toSeq

      var nbPertinentDoc=0
      var rang=1
      val goodSize=good.size
      val pr = pred.map(p=>{
        if(good.contains(p._1)){
          nbPertinentDoc+=1}
        rang+=1
        if(goodSize!=0){
          (nbPertinentDoc.toFloat/rang, nbPertinentDoc.toFloat/goodSize)
        }
        else{(nbPertinentDoc.toFloat/rang, 0.0f)}
      })

      k.map(ki=>{
        val list = pr.filter(a=> (ki <= a._2))
        list.map(_._1) match{
          case Nil => 0.0f
          case l => l.max.toFloat
        }
      })
    }
  }


class Precision_Mean() extends EvalMeasure {

  def eval(irList:IRList):Seq[Float]={
    val good=irList.query.relation
    val pred=irList.listScores.toSeq.sortBy{-_._2}

    var nbPertinentDoc=0
    val totalPrecision=0.0f
    var rang=0
    Seq(
      pred.map(tuple=>{
      rang+=1
      if(good.contains(tuple._1)){
        nbPertinentDoc+=1
        Some(nbPertinentDoc.toFloat/rang.toFloat)
      }
      else{None}
      }
    ).flatten match{
      case Nil=>0.0f
      case l=>l.sum.toFloat/good.size
    })

  }
  }
