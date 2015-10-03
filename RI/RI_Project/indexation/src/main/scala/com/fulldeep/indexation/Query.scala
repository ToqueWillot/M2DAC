package com.fulldeep.indexation;


import com.fulldeep.indexation._


class Query( val id:Int, val text:String , val relation:Seq[Int]) extends Serializable {
	def getId():Int={
		id
	}
}
