package com.fulldeep.indexation;

import com.fulldeep.indexation._
import scala.util.Random



abstract class EvalMeasure(){

  def eval(irList:IRList):Map[Int,Float]

}



class Precision_Recall() extends EvalMeasure {

  def eval(irList:IRList):Map[Int,Float]={
    return Map()
  }

}

class Precision_Mean() extends EvalMeasure {

  def eval(irList:IRList):Map[Int,Float]={
    return Map()
  }

}
