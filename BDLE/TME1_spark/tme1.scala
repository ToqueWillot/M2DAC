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

val q9avg1 = q8.map(a=>(a,1.0)).reduce((a,b)=>((a._1+a._2),(a._2+b._2)))
val q9avg = q9avg1._1/q9avg1._2

val q10 = q2.countByValue


//RDDs Clés-valeurs
val list= data.map(_.split(" "))
list.take(100).foreach(line => println(line(3)))
val listnb = list.map(elem=> (elem(0),elem(2).toInt))
val group = listnb.reduceByKey(_+_)
val listnb2 = list.map(elem=> (elem(0).slice(0,2),elem(2).toInt))
val group2 = listnb2.reduceByKey(_+_)
