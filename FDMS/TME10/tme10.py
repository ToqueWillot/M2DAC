from pyspark import SparkContext,SparkConf
conf = SparkConf().setAppName("wordcount")
sc = SparkContext(conf=conf)


text_file = sc.textFile("../text.txt")
words = text_file.flatMap(lambda line: line.split (" "))
pairs=words.map(lambda word: (word, 1))
counts=pairs.reduceByKey(lambda a, b: a + b)
counts.saveAsTextFile("../res.txt")
