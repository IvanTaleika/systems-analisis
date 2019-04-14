package by.bsuir.sa

import java.nio.charset.Charset

import by.bsuir.sa.model.ModelFactory
import by.bsuir.sa.ui.OutputUtils
import org.apache.commons.io.IOUtils
import org.apache.spark.ml.linalg.{DenseVector, SparseVector}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, SparkSession}

object Runner extends App {
  val graphicsColName = "graphicsFeatures"
  val medicineColName = "medicineFeatures"
  val atheismColName = "atheismFeatures"

  implicit val spark: SparkSession =
    SparkSession.builder().master("local[*]").appName("lab4").getOrCreate()
  spark.sparkContext.setLogLevel("ERROR")

  import spark.implicits._

  val cleanerModel = ModelFactory.cleanerModel

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

  OutputUtils.showVocabulary(
    graphicsTransformed.limit(4),
    graphicsColName,
    graphicsModel,
    medicineTransformed.limit(4),
    medicineColName,
    medicineModel,
    atheismTransformed.limit(4),
    atheismColName,
    atheismModel
  )
  val educationDf = graphicsTransformed
    .limit(4)
    .union(medicineTransformed.limit(4))
    .union(atheismTransformed.limit(4))

  val kMeansModel = ModelFactory.kMeansModel(educationDf)

  println("Graphics")
  kMeansModel.transform(graphicsTransformed).show()
  println("Medicine")
  kMeansModel.transform(medicineTransformed).show()
  println("Atheism")
  kMeansModel.transform(atheismTransformed).show()

  println(s"Clusters centres ${kMeansModel.clusterCenters.mkString(" ")}")

  OutputUtils.showClusters3dChart(
    kMeansModel.clusterCenters,
    educationDf,
    graphicsTransformed.sort('index.desc).limit(6),
    medicineTransformed.sort('index.desc).limit(6),
    atheismTransformed.sort('index.desc).limit(6)
  )

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

}
