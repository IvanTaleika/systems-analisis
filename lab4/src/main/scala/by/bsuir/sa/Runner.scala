package by.bsuir.sa

import java.nio.charset.Charset

import org.apache.commons.io.IOUtils
import org.apache.spark.ml.clustering.KMeans
import org.apache.spark.ml.feature.CountVectorizerModel
import org.apache.spark.ml.linalg.{DenseVector, SparseVector}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object Runner extends App {
  val graphicsColName = "graphicsFeatures"
  val medicineColName = "medicineFeatures"
  val atheismColName = "atheismFeatures"

  val spark =
    SparkSession.builder().master("local[*]").appName("lab4").getOrCreate()

  import spark.implicits._

  spark.sparkContext.setLogLevel("WARN")
  val cleanerModel = ModelFactory.cleanerModel(spark)

  val graphicsDf = createDf("graphics", 9)
  val medicineDf = createDf("medicine", 9)
  val atheismDf = createDf("atheism", 9)

  val graphicsModel =
    ModelFactory.wordCountModel(graphicsDf.limit(4), graphicsColName)
  val medicineModel =
    ModelFactory.wordCountModel(medicineDf.limit(4), medicineColName)
  val atheismModel =
    ModelFactory.wordCountModel(atheismDf.limit(4), atheismColName)

  val graphicsTransformed = wordCountVectorized(graphicsDf)
  val medicineTransformed = wordCountVectorized(medicineDf)
  val atheismTransformed = wordCountVectorized(atheismDf)

  spark
    .createDataFrame(
      createVocabularyWithCount(graphicsTransformed.limit(4),
                                graphicsColName,
                                graphicsModel)
        .join(createVocabularyWithCount(medicineTransformed.limit(4),
                                        medicineColName,
                                        medicineModel))
        .join(createVocabularyWithCount(atheismTransformed.limit(4),
                                        atheismColName,
                                        atheismModel))
        .map(t => (t._1, t._2._1._1, t._2._1._2, t._2._2)))
    .toDF("index", "graphics", "medicine", "atheism")
    .sort($"index")
    .show(false)

  val kMeans = new KMeans()
    .setK(3)
    .setFeaturesCol("wordsCount")
    .setPredictionCol("clusterIndex")
  println(kMeans.explainParams())
  val kMeansDataset = graphicsTransformed
    .limit(4)
    .union(medicineTransformed.limit(4))
    .union(atheismTransformed.limit(4))

  val kMeansModel = kMeans.fit(kMeansDataset)
  println(kMeansModel.clusterCenters.mkString(" "))

  kMeansModel.transform(graphicsTransformed).show()
  kMeansModel.transform(medicineTransformed).show()
  kMeansModel.transform(atheismTransformed).show()

  def createDf(folder: String, n: Int): DataFrame = {
    cleanerModel.transform(
      spark
        .createDataFrame(
          (0 to n).map(i =>
            (i,
             IOUtils.toString(this.getClass.getResourceAsStream(s"/$folder/$i"),
                              Charset.defaultCharset()))))
        .toDF("index", "text"))
  }

  def wordCountVectorized(df: DataFrame): DataFrame = {

    val countWords = udf(
      (v1: SparseVector, v2: SparseVector, v3: SparseVector) =>
        new DenseVector(
          Array(v1.values.sum.toInt, v2.values.sum.toInt, v3.values.sum.toInt)))
    atheismModel
      .transform(medicineModel.transform(graphicsModel.transform(df)))
      .withColumn("wordsCount",
                  countWords($"graphicsFeatures",
                             $"medicineFeatures",
                             $"atheismFeatures"))
  }

  def createVocabularyWithCount(
      df: DataFrame,
      colName: String,
      model: CountVectorizerModel): RDD[(Int, (Double, String))] = {
    df.rdd
      .map(_.getAs[SparseVector](colName))
      .flatMap(v => v.indices.zip(v.values))
      .reduceByKey(_ + _)
      .join(spark.sparkContext.parallelize(
        model.vocabulary.zipWithIndex.map(t => (t._2, t._1)).toSeq))
  }

}
