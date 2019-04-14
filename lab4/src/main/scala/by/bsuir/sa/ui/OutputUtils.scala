package by.bsuir.sa.ui

import by.bsuir.sa.chart.{ScatterPlot, ScatterPoint}
import org.apache.spark.ml.feature.CountVectorizerModel
import org.apache.spark.ml.linalg.{DenseVector, SparseVector}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.jzy3d.analysis.AnalysisLauncher
import org.jzy3d.colors.Color

import scala.collection.JavaConverters._
object OutputUtils {

  def showVocabulary(
      df1: DataFrame,
      col1: String,
      model1: CountVectorizerModel,
      df2: DataFrame,
      col2: String,
      mode2: CountVectorizerModel,
      df3: DataFrame,
      col3: String,
      model3: CountVectorizerModel)(implicit spark: SparkSession): Unit = {
    import spark.implicits._
    spark
      .createDataFrame(
        createVocabularyWithCount(df1, col1, model1)
          .join(createVocabularyWithCount(df2, col2, mode2))
          .join(createVocabularyWithCount(df3, col3, model3))
          .map(t => (t._1, t._2._1._1, t._2._1._2, t._2._2)))
      .toDF("index", "graphics", "medicine", "atheism")
      .sort($"index")
      .show(false)
  }

  private def createVocabularyWithCount(df: DataFrame,
                                        colName: String,
                                        model: CountVectorizerModel)(
      implicit spark: SparkSession): RDD[(Int, (Double, String))] = {
    df.rdd
      .map(_.getAs[SparseVector](colName))
      .flatMap(v => v.indices.zip(v.values))
      .reduceByKey(_ + _)
      .join(spark.sparkContext.parallelize(
        model.vocabulary.zipWithIndex.map(t => (t._2, t._1)).toSeq))
  }

  def showClusters3dChart(clusters: Array[org.apache.spark.ml.linalg.Vector],
                          educationDf: DataFrame,
                          df1: DataFrame,
                          df2: DataFrame,
                          df3: DataFrame): Unit = {

    val clusterPoints = clusters
      .foldLeft(List[ScatterPoint]())((l, p) =>
        l :+ new ScatterPoint(p(0), p(1), p(2), Color.BLACK))

    AnalysisLauncher.open(
      new ScatterPlot(
        (clusterPoints :::
          wordsVectorDfToPointList(educationDf, Color.MAGENTA) :::
          wordsVectorDfToPointList(df1, Color.RED) :::
          wordsVectorDfToPointList(df2, Color.BLUE) :::
          wordsVectorDfToPointList(df3, Color.GREEN)).asJava))

  }

  private def wordsVectorDfToPointList(df: DataFrame,
                                       c: Color): List[ScatterPoint] = {
    df.select("wordsCount")
      .rdd
      .collect()
      .map(row => {
        val v = row.getAs[DenseVector](0)
        new ScatterPoint(v.values(0), v.values(1), v.values(2), c)
      })
      .toList
  }
}
