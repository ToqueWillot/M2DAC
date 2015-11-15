package com.fulldeep.modeles;

import com.fulldeep.indexation._
import scala.util.Random


abstract class RandomWalk(){
  def walk(succ:Map[Int,Seq[Int]],pred:Map[Int,Seq[Int]]):Map[Int,Float]

}
class pageRank(val d:Float, val it:Int=100) extends RandomWalk {


  def walk(succ:Map[Int,Seq[Int]],pred:Map[Int,Seq[Int]]):Map[Int,Float]={
    val nbNode:Int=pred.size
    var newMu = scala.collection.mutable.HashMap.empty[Int,Float]
    var mu = scala.collection.mutable.HashMap.empty[Int,Float]

    pred.foreach(id=>{
      mu+=((id._1,1.0f/nbNode))
      newMu+=((id._1,1.0f))
    })

    for( a <- 1 to it){
      val tmp = (1.0f - d)/nbNode
      pred.foreach(i=>{
        var sum = 0.0f
        pred(i._1).foreach(j=>{
          sum += mu(i._1) / succ(j).size
        })
        sum*=d
        newMu+=((i._1, tmp + sum))
      })
        val tmpMu = mu;
  			mu = newMu;
  			newMu = tmpMu;
    }
    mu.toMap
  }


}

class HITS(val d:Float, val it:Int=100) extends RandomWalk {

  val auth = scala.collection.mutable.HashMap.empty[Int,Float]
  val hubs = scala.collection.mutable.HashMap.empty[Int,Float]

  def walk(succ:Map[Int,Seq[Int]],pred:Map[Int,Seq[Int]]):Map[Int,Float]={

    val nbNode = pred.size
		val newAuth = scala.collection.mutable.HashMap.empty[Int,Float]
  	val newHubs = scala.collection.mutable.HashMap.empty[Int,Float]
		var normAuth:Float=0.0f
		var normHubs:Float=0.0f

		for( a <- 1 to nbNode){
			val tmp = math.sqrt(1.0f/nbNode).toFloat
      pred.foreach(key=>{
        auth+=((key._1,tmp))
        hubs+=((key._1,tmp))
        newAuth+=((key._1,1.0f))
        newHubs+=((key._1,1.0f))
      })
		}

	   for( a <- 1 to it){
			normHubs = 0.0f
			normAuth = 0.0f
      //loop pred
      pred.foreach(ni=>{
        var sum:Float=0.0f
        //loop pred ni
        pred(ni._1).foreach(nj=>{
          sum+=hubs.getOrElse(nj,0.0f)
        })
        newHubs+=((ni._1,sum))
        normHubs+= sum*sum
        sum=0
        //loop succ ni
        succ(ni._1).foreach(nj=>{
          sum+=auth.getOrElse(nj,0.0f)
        })
        newAuth+=((ni._1,sum))
        normAuth+=sum*sum
      })
      normAuth=math.sqrt(normAuth).toFloat
      normHubs=math.sqrt(normHubs).toFloat
      pred.foreach(ni=>{
        auth+=((ni._1, newAuth(ni._1)/normAuth))
        hubs+=((ni._1,newHubs(ni._1)/normHubs))
      })
    }
    auth.toMap
  }

}
