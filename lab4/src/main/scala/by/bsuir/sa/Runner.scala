package by.bsuir.sa

import com.johnsnowlabs.nlp.annotators.{Normalizer, Stemmer, Tokenizer}
import com.johnsnowlabs.nlp.{DocumentAssembler, Finisher}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.feature.{CountVectorizer, StopWordsRemover}
import org.apache.spark.sql.SparkSession

// TODO: remove empty words
// TODO: remove stop words before stamming
object Runner extends App {
  val spark =
    SparkSession.builder().master("local[*]").appName("lab4").getOrCreate()
  spark.sparkContext.setLogLevel("WARN")
  val spaceDf =
    spark
      .createDataFrame(
        (0 to 3).map(
          i =>
            (i,
              new String(
                this.getClass.getResourceAsStream(s"/space/$i").readAllBytes()))))
      .toDF("index", "text")
  import spark.implicits._

  val documentAssembler =
    new DocumentAssembler().setInputCol("text").setOutputCol("document")
  val tokenizer =
    new Tokenizer()
      .setInputCols("document")
      .setOutputCol("tokens")

  val normalizer =
    new Normalizer().setInputCols("tokens").setOutputCol("normalized")

  val stemmer = new Stemmer()
    .setInputCols("normalized")
    .setOutputCol("stemms")

  val finisher = new Finisher()
    .setInputCols("stemms")
    .setOutputCols("result")

  val remover = new StopWordsRemover()
    .setInputCol("result")
    .setOutputCol("filtered")
  val pipeline = new Pipeline()
    .setStages(
      Array(documentAssembler,
            tokenizer,
            normalizer,
            stemmer,
            finisher,
            remover))
    .fit(
      spark
        .createDataset(Seq(new String(
          this.getClass.getResourceAsStream("/space/0").readAllBytes())))
        .toDF("text"))
  //  new Pipeline().setStages(Array(tokenizer, stemmer)).fit()


  val temp = pipeline.transform(spaceDf)
  temp.show(true)
  val vectorizer = new CountVectorizer()
    .setInputCol("filtered")
    .setOutputCol("features")
    .setVocabSize(10)
    .setMinDF(1)
  val model = vectorizer.fit(temp)
  println(model.vocabulary.mkString(" "))
  model.transform(temp).select("filtered", "features").show(false)
}
