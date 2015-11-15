//TME1 Scala spark

val data = sc.textFile("data/wordcount.txt")

val q1 = data.map(line=>line.split(" ")(3).toDouble)

val q2 = q1.filter(elem=> (elem>1000 && elem<1300))

val q33 = q2.filter(elem=> (elem%3==0))
val q39 = q2.filter(elem=> (elem%9==0))

val q4 = q33.map(a=> a/10)

val q5 = q4.distinct

val q6 = q33.union(q39).distinct

val q7 = q33.subtract(q39)

val q8 = q33.subtract(q2.filter(e=>e%10==0)).union(q2.filter(e=>e%10==0).subtract(q33)).distinct

val q9sum = q8.sum
val q9min = q8.min
val q9max = q8.max

val q9avg1 = q8.map(a=>(a,1.0)).reduce((a,b)=>((a._1+b._1),(a._2+b._2)))
val q9avg = q9avg1._1/q9avg1._2

val q10 = q2.countByValue


//RDDs Clés-valeurs
val list= data.map(_.split(" "))
list.take(100).foreach(line => println(line(3)))
val listnb = list.map(elem=> (elem(0),elem(2).toInt))
val group = listnb.reduceByKey(_+_)
val listnb2 = list.map(elem=> (elem(0).split("\\.")(0),elem(2).toInt))
val group2 = listnb2.reduceByKey(_+_)

val list=List(1,2,3).toArray
val list=Array(1,2,3)

val tuple=(3,9,4,5)
//TME1 Exercice 2

val note = sc.textFile("data/ratings.dat")
val utils = sc.textFile("data/users.dat")
val films = sc.textFile("data/movies.dat")

val noteSplit= note.map(_.split("::"))
val utilsSplit=utils.map(_.split("::"))
val filmsSplit=films.map(_.split("::"))

val a = note.map(line=>(line.split("::")(0),1)).reduceByKey(_+_)

//on veut join by key notes et utils = notes.userId et
//utils.UserId,utils.Zipcode
val b = noteSplit.map(_(0)->1).join(utilsSplit.map(u=>(u(0),u(4)))).map(e=>e._2._2->e._2._1).reduceByKey(_+_)

val c = noteSplit.map(n=>n(1)->1).join(filmsSplit.map(f=>(f(0),f(2)))).map(e=>e._2._2->e._2._1).reduceByKey(_+_)


val d = note.map(line=>(line.split("::")(0),1)).reduceByKey(_+_).takeOrdered(10)(Ordering[Int].reverse.on(x=>x._2))

//moi je prefere ca
val d = note.map(line=>(line.split("::")(0),1)).reduceByKey(_+_).sortBy{-_._2}.take(10)


val e = noteSplit.map(n=>n(1)->1).reduceByKey(_+_).sortBy{_._2}.take(10)
val e = noteSplit.map(n=>n(1)->1).reduceByKey(_+_).takeOrdered(10)(Ordering[Int].on(x=>x._2))


val f = utilsSplit.map(_(0)).subtract(noteSplit.map(_(0)))


//join, reduceByKey, union, distinct, substract
