package by.bsuir.sa.model

import com.johnsnowlabs.nlp.annotators.{Normalizer, Stemmer, Tokenizer}
import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import org.apache.spark.ml.clustering.{KMeans, KMeansModel}
import org.apache.spark.ml.feature.{CountVectorizer, CountVectorizerModel, StopWordsRemover}
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.{DataFrame, SparkSession}

object ModelFactory {
  private val documentAssembler =
    new DocumentAssembler().setInputCol("text").setOutputCol("document")
  private val tokenizer =
    new Tokenizer()
      .setInputCols("document")
      .setOutputCol("tokened")
  private val normalizer =
    new Normalizer().setInputCols("tokened").setOutputCol("normalized")
  private val stemmer = new Stemmer()
    .setInputCols("normalized")
    .setOutputCol("stemmed")
  private val finisher = new Finisher()
    .setInputCols("stemmed")
    .setOutputCols("finished")
  private val cleaner = new StopWordsRemover()
    .setInputCol("finished")
    .setOutputCol("result")
  cleaner.setStopWords(
    cleaner.getStopWords
      ++ Array("", "ar", "us", "thi", "wa", "+", "d", "also", "thei", "nt"))
  private val pipeline: Pipeline = new Pipeline()
    .setStages(
      Array(documentAssembler,
            tokenizer,
            normalizer,
            stemmer,
            finisher,
            cleaner))

  def cleanerModel(implicit spark: SparkSession): PipelineModel = {
    import spark.implicits._
    pipeline.fit(spark.createDataset(Seq.empty[String]).toDF("text"))
  }

  def wordCountModel(educationDf: DataFrame,
                     outputColName: String): CountVectorizerModel = {
    val vectorizer = new CountVectorizer()
      .setInputCol("result")
      .setOutputCol(outputColName)
      .setVocabSize(10)
      .setMinDF(1)
    vectorizer.fit(educationDf)
  }

  def kMeansModel(educationDf: DataFrame): KMeansModel ={
    val kMeans = new KMeans()
      .setK(3)
      .setFeaturesCol("wordsCount")
    kMeans.fit(educationDf)
  }

}
