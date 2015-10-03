package com.fulldeep.indexation;

import com.fulldeep.indexation._
import scala.util.Random



class EvalIRModele(){

  def mean(irList:Seq[IRList],measureList:Seq[EvalMeasure]):Seq[Float]={
    measureList.map(m=>irList.map(ir=>m.eval(ir).map(_._2).sum/m.eval(ir).toList.size).sum/irList.size )
  }
  def standard_deviation(irList:Seq[IRList],measureList:Seq[EvalMeasure]):Seq[Float]={
   val mean = measureList.map(m=> (m,(irList.map(ir=>m.eval(ir).map(_._2).sum/m.eval(ir).map(_._2).size).sum/irList.size)) ).toMap
   measureList.map(m=>irList.map(ir=>math.abs(m.eval(ir).map(_._2).sum/m.eval(ir).toList.size - mean(m))).sum/irList.size )
 }
}
