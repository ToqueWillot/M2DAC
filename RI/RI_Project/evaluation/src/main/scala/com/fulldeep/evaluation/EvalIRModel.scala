package com.fulldeep.evaluation;

import com.fulldeep.indexation._
import com.fulldeep.modeles._
import scala.util.Random



class EvalIRModele(val model:IRmodele.IRmodele, val measure:EvalMeasure,val queries: List[Query]){

  var mean :Seq[Float]=Seq()
  var std :Seq[Float]=Seq()
  var evalValue:Float=0.0f

  def eval():Unit={
    var nuquery=1
    val seqseqfloat = queries.map(query=>{
      println("--------------------query numero ",nuquery)
      nuquery+=1
      val queryStems:Map[String,Int]=model.indexx.getMapWordOccurFromString(query.text)
      val rank:Seq[(Int,Float)]=model.getRanking(queryStems)
      val irList=new IRList(query,rank)
      measure.eval(irList)
    })

    //mean
    mean=seqseqfloat.transpose.map(e=> e.sum.toFloat/seqseqfloat.size)

    //std
    var idx = -1
    std = seqseqfloat.transpose.map(e=>{
      idx += 1
      math.sqrt(e.map(v=>(v-mean(idx))*(v-mean(idx))).sum).toFloat
    })

    evalValue = mean.sum.toFloat/mean.size

  }


}
