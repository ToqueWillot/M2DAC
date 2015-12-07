package com.fulldeep.modeles;

import com.fulldeep.indexation._
import scala.util.Random

object IRmodele {

  abstract class IRmodele(var index:Index.Index,var weighter:Weighter.Weighter){

    def getScores(query: Map[String,Int],normalized:Boolean):Seq[(Int,Float)]

    def getRanking(query: Map[String,Int]):Seq[(Int,Float)]={
        val s =System.nanoTime
        val scores:Seq[(Int,Float)] = getScores(query,false)
        val rank = Random.shuffle(scores).sortBy{-_._2}
        println("--- temps du ranking ==== "+(System.nanoTime-s)/1e9+"s")
        rank
    }
  }




  class Vectoriel(index:Index.Index,weighter:Weighter.Weighter) extends IRmodele(index,weighter) {

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

  class LanguageModel(index:Index.Index,weighter:Weighter.Weighter,val lambda:Float) extends IRmodele(index,weighter) {

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
                mapp.put(doc._1,mapp(doc._1)+ queryWeight.toMap.getOrElse(w._1,0.0f) *math.log(lambda *pdt+(1-lambda)*weightWinCorpus).toFloat)
              })
            }
          }
        })
        mapp.toSeq
    }
  }

  class OKAPI(index:Index.Index,weighter:Weighter.Weighter,val k:Float, val b :Float) extends IRmodele(index,weighter) {
    //nombre de documents dans le corpus
    val nbDoc:Int=index.docsTf.size
    //longueur moyenne dun document
    val lMoyDoc:Int=index.docsTf.map(id=> id._2.map(t=>t._2).sum).sum/nbDoc

    def getScores(query: Map[String,Int],normalized : Boolean = false):Seq[(Int,Float)]={
        val mapScores = scala.collection.mutable.HashMap.empty[Int,Float]
        var idf:Float=0;
        var idft:Float=0;
        var tf:Float=0;

        query.foreach(stem=>{
          val docsOfStem:Option[Seq[(Int,Float)]] = weighter.getDocWeightsForStem(stem._1)
          docsOfStem match {
            case None =>
            case Some(s)=> s.foreach(doc=>{
              idf=doc._2
              tf=weighter.getDocWeightsForDoc(doc._1).get.toMap.get(stem._1).get
              idft=math.max(0.0f, math.log((nbDoc - idf+0.5f)/(idf +0.5f)).toFloat)
              if (!mapScores.contains(doc._1)){
                mapScores+=((doc._1,0.0f))
              }
              mapScores(doc._1)+= idft * ((k+1)*tf)/(k*((1-b)+ b*index.docsTf(doc._1).map(_._2).sum /lMoyDoc)+tf)
            })
          }

       })
       mapScores.toSeq
  }
 }

 class IRRandomWalk(index:Index.Index,weighter:Weighter.Weighter, modelInit:IRmodele, walkMoon: RandomWalk,
			pred: Map[Int,Seq[Int]], succ: Map[Int,Seq[Int]],  nSeed:Int, nIn:Int) extends IRmodele(index,weighter) {

      def getScores(query: Map[String,Int],normalized : Boolean = false):Seq[(Int,Float)]={

        val seeds=scala.collection.mutable.HashSet.empty[Int]
        val rankingInit:Seq[(Int,Float)]=modelInit.getRanking(query)
        val rankingIterator = rankingInit.map(_._1).toIterator
        var i = 0

        while (i<nSeed && rankingIterator.hasNext){
          val doc = rankingIterator.next
          seeds+=doc
          if(succ.contains(doc))
            succ(doc).foreach(elem=> seeds+=elem)

          if(pred.contains(doc)){
            val predecesseurs=scala.collection.mutable.HashSet.empty[Int]
            pred(doc).foreach(d=> predecesseurs += d )
            var k = 0
            while ( !(predecesseurs.isEmpty) && k<nIn ){
         				val ind: Int = math.floor(math.random * predecesseurs.size).toInt
                seeds+=predecesseurs.toList(ind)
                predecesseurs.remove(ind)
                k+=1
         		}
        	}
         	i+=1
        }

        val sousSucc = scala.collection.mutable.HashMap.empty[Int,scala.collection.mutable.HashSet[Int]]
        val sousPred = scala.collection.mutable.HashMap.empty[Int,scala.collection.mutable.HashSet[Int]]
        seeds.foreach(doc=>{
          sousSucc+=((doc,scala.collection.mutable.HashSet.empty[Int]))
          if(!sousPred.contains(doc)){
            sousPred+=((doc,scala.collection.mutable.HashSet.empty[Int]))
          }
          if(succ.contains(doc)){
            succ(doc).foreach(s=>{
              if(seeds.contains(s)){
                sousSucc(doc)+=s
                if(! sousPred.contains(s)){
                  sousPred+=((s,scala.collection.mutable.HashSet.empty[Int]))
                }
              sousPred(s)+=doc
              }
            })
          }
        })

        walkMoon.walk(sousPred.map(e=>e._1->e._2.toSeq).toMap, sousSucc.map(e=>e._1->e._2.toSeq).toMap).toSeq
      }

 }


}
