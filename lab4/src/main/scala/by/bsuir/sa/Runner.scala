package by.bsuir.sa

import org.apache.spark.ml.feature.CountVectorizer
import org.apache.spark.sql.{DataFrame, SparkSession}

object Runner extends App {
  val spark =
    SparkSession.builder().master("local[*]").appName("lab4").getOrCreate()
  spark.sparkContext.setLogLevel("WARN")
  val cleanerModel = CleanerModelFactory.model(spark)
  val vectorizer = new CountVectorizer()
    .setInputCol("result")
    .setOutputCol("features")
    .setVocabSize(10)
    .setMinDF(1)

  val spaceModel = vectorizer.fit(cleanerModel.transform(createDf("space", 3)))
  val christianModel = vectorizer.fit(cleanerModel.transform(createDf("christian", 3)))
  val cryptographyModel = vectorizer.fit(cleanerModel.transform(createDf("cryptography", 3)))
  println(spaceModel.vocabulary.mkString(" "))
  spaceModel.transform(cleanerModel.transform(createDf("space", 9))).select("features").show(false)


  def createDf(folder: String, n: Int): DataFrame = {
    spark
      .createDataFrame(
        (0 to n).map(
          i =>
            (i,
              new String(
                this.getClass.getResourceAsStream(s"/$folder/$i").readAllBytes()))))
      .toDF("index", "text")
  }
}
