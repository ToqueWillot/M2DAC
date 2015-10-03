package com.fulldeep.indexation;


import com.fulldeep.indexation._
// import spark.SparkContext
// import SparkContext._


object QueryParser{

	  class QueryParser(val fileQuery:String, val fileRel:String) extends Serializable {
			val parser = new ParserCISI_CACM()
			parser.init(fileQuery)

			val mapQueryRel: Map[Int,Seq[Int]]= getMapQueryRel(fileRel)

			def getMapQueryRel(fileRel:String):Map[Int,Seq[Int]]={
				// val data = scala.io.Source.fromFile(fileRel, "UTF-8").getLines.toList.map(line=>{
				// 	val split = line.split(" ")
				// 	(split(0).toInt,split(1))
				// })
				// val rdd = sc.parallelize(data)
				// rdd.groupByKey().collect()

				scala.io.Source.fromFile(fileRel, "UTF-8").getLines.toList.map(line=>{
					val split = line.split(" ")
					(split(0).toInt,split(1))
				}).groupBy(_._1).map(e=>(e._1,e._2.map(z=>z._2.toInt).toSeq)).toMap

			}


			def nextQuery():Query = {
				val doc:Document = parser.nextDocument()
				docToQuery ( doc , mapQueryRel)
			}

			def docToQuery(doc:Document,mapQueryRel:Map[Int,Seq[Int]]):Query={
			 val id:Int = doc.getId().toInt
			 val text:String= doc.getText()
			 val rel:Seq[Int]= mapQueryRel.getOrElse(id, Seq())
			 new Query(id,text,rel)

			}
		}
}
