package com.fulldeep.evaluation;

import com.fulldeep.indexation._
import com.fulldeep.modeles._
import scala.util.Random
import breeze.linalg._
import breeze.numerics._


abstract class EvalMeasure(){

  def eval(irList:IRList):Map[Int,Float]

  }



class Precision_Recall() extends EvalMeasure {

  def eval(irList:IRList):Map[Int,Float]={
    return Map()
  }

  def getPrecisionRecall(irList:IRList,nbLevels:Int=10):Seq[(Float,Float)]={
    val good=irList.query.relation
    val pred=irList.listScores.toSeq.sortBy{-_._2}

    val k = (breeze.linalg.DenseVector.range(0,nbLevels+1).map(_.toFloat)/nbLevels.toFloat).toArray.toSeq

    var nbPertinentDoc=0
    var rang=1
    var goodSize=good.size
    val pr = pred.map(p=>{
      if(good.contains(p._1)){
        nbPertinentDoc+=1}
      rang+=1
      (nbPertinentDoc/rang, nbPertinentDoc/goodSize)
    })

    k.map(ki=>{
      val list = pr.filter(a=> (ki<=a._2))
      list.map(_._1) match{
        case Nil => (0.0f,ki)
        case l => (l.max.toFloat,ki)
      }
    })
  }

}

class Precision_Mean() extends EvalMeasure {

  def eval(irList:IRList):Map[Int,Float]={
    Map()
  }

  def getPrecisionRecall(irList:IRList,nbLevels:Int=10):Seq[Float]={
    val good=irList.query.relation
    val pred=irList.listScores.toSeq.sortBy{-_._2}

    var nbPertinentDoc=0
    var totalPrecision=0.0f
    var rang=0
    pred.map(tuple=>{
      rang+=1
      if(good.contains(tuple._1)){
        nbPertinentDoc+=1
        totalPrecision+=nbPertinentDoc.toFloat/rang.toFloat
        Some(totalPrecision/nbPertinentDoc.toFloat)
      }
      else{None}
      }
    ).flatten
    }
}
