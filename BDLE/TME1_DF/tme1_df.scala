//TME1 Scala DF
//~/Spark/spark-1.5.2-bin-hadoop2.4/bin/spark-shell



val utils = sc.textFile("data/users.dat",10)
val note = sc.textFile("data/ratings.dat",20)
val films = sc.textFile("data/movies.dat")

val n= note.map(_.split("::")).map(x=> (x(0),(x(1),x(2).toInt,x(3))))
val u=utils.map(_.split("::")).map(x=> (x(0),(x(1),x(2).toInt,x(3),x(4))))
val f=films.map(_.split("::")).map(x=>(x(0),(x(1),x(2))))


//les DF
// NOTE: n=note, u=utilisateur, f=films

val ndf=n.map(x=>(x._1,x._2._1,x._2._2,x._2._3)).toDF("userId","movieId","rating","timeStamp")
val udf=u.map(x=>(x._1,x._2._1,x._2._2,x._2._3,x._2._4)).toDF("userId","gender","age","occupation","zipCode")
val fdf=f.map(x=>(x._1,x._2._1,x._2._2)).toDF("movieID","title","genres")

val j = u.join(n)

j.toDebugString



import org.apache.spark.HashPartitioner

val users2=u.partitionBy(new HashPartitioner(10))
val notes2=n.partitionBy(new HashPartitioner(10))
users2.setName("users2").persist
notes2.setName("notes2").persist

val j2 = users2.join(ratings2)
j2.count

val x = notes2.lookup("1")

val x1 = n.lookup("1")

val uu= sc.broadcast(u.collect)


n.map(r=>{

})
