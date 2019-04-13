package by.bsuir.sa

import java.nio.charset.Charset

import org.apache.commons.io.IOUtils
import org.apache.spark.ml.feature.CountVectorizerModel
import org.apache.spark.ml.linalg.SparseVector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

object Runner extends App {
  val spark =
    SparkSession.builder().master("local[*]").appName("lab4").getOrCreate()
  spark.sparkContext.setLogLevel("WARN")
  val cleanerModel = ModelFactory.cleanerModel(spark)
import spark.implicits._
  val spaceTestDf = createDf("space", 3)
  val medicineTestDf = createDf("medicine", 3)
  val cryptographyTestDf = createDf("cryptography", 3)

  val spaceModel = ModelFactory.wordCountModel(spaceTestDf)
  val medicineModel = ModelFactory.wordCountModel(medicineTestDf)
  val cryptographyModel = ModelFactory.wordCountModel(cryptographyTestDf)

  //  val spaceDf = createDf("space", 9)
  //  val medicineDf = createDf("medicine", 9)
  //  val cryptographyDf = createDf("cryptography", 9)
  spark
    .createDataFrame(
      createVocabularyWithCount(spaceTestDf, spaceModel)
        .join(createVocabularyWithCount(medicineTestDf, medicineModel))
        .join(createVocabularyWithCount(cryptographyTestDf, cryptographyModel))
        .map(t => (t._1, t._2._1._1, t._2._1._2, t._2._2)))
    .toDF("index", "space", "medicine", "cryptography").sort($"index")
    .show(false)

  //  println(spaceModel.vocabulary.mkString(" "))
  //  println(medicineModel.vocabulary.mkString(" "))
  //  println(cryptographyModel.vocabulary.mkString(" "))

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

  def countWords(df: DataFrame, model: CountVectorizerModel): Int = {
    transformToSparseVectorRdd(df, model).flatMap(_.values).sum().toInt
  }

  def createVocabularyWithCount(
      df: DataFrame,
      model: CountVectorizerModel): RDD[(Int, (Double, String))] = {
    transformToSparseVectorRdd(df, model)
      .flatMap(v => v.indices.zip(v.values))
      .reduceByKey(_ + _)
      .join(spark.sparkContext.parallelize(
        model.vocabulary.zipWithIndex.map(t => (t._2, t._1)).toSeq))
  }

  def transformToSparseVectorRdd(
      df: DataFrame,
      model: CountVectorizerModel): RDD[SparseVector] = {
    model
      .transform(df)
      .rdd
      .map(_.getAs[SparseVector]("features"))
  }
}
