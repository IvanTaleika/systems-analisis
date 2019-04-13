package by.bsuir.sa

import com.johnsnowlabs.nlp.annotators.{Normalizer, Stemmer, Tokenizer}
import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import org.apache.spark.ml.feature.StopWordsRemover
import org.apache.spark.ml.{Pipeline, PipelineModel}
import org.apache.spark.sql.SparkSession

object CleanerModelFactory {
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
  cleaner.setStopWords(cleaner.getStopWords ++ Array("", "ar", "us", "thi", "wa"))
  private val pipeline: Pipeline = new Pipeline()
    .setStages(
      Array(documentAssembler,
        tokenizer,
        normalizer,
        stemmer,
        finisher,
        cleaner))

  def model(spark: SparkSession): PipelineModel = {
    import spark.implicits._
    pipeline.fit(spark.createDataset(Seq.empty[String]).toDF("text"))
  }

}
