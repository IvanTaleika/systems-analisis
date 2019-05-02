package by.bsuir.sa.lab5

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import scala.io.Source
import scala.math._
import scala.util.Random

object Runner extends App {

  val Array(mode, path) = args
  mode match {
    case "train" => train(extractData(path))
    case "test"  => test(extractWeights(), extractData(path))
  }

  def test(neurons: Seq[Seq[Double]], data: Seq[Seq[Double]]): Unit = {
    println("TODO")
  }

  def extractWeights(): Seq[Seq[Double]] = {
    Source
      .fromFile("weights/weights.csv")
      .getLines()
      .map(_.split(",").map(_.toDouble).toSeq)
      .toSeq
  }

  def train(data: Seq[Seq[Double]]): Unit = {

    val normalizationCoeffs = calcNormalizationCoeffs(data)
    val normalizedData = data.map(r =>
      r.indices.map(i =>
        r(i) * normalizationCoeffs(i)._1 + normalizationCoeffs(i)._2))
    val neurons = trainNeurons(normalizedData)

    val denormalizedNeurons = neurons.map(ws =>
      ws.indices.map(i =>
        (ws(i) - normalizationCoeffs(i)._2) / normalizationCoeffs(i)._1))

    Files.write(Paths.get("weights/weights.csv"),
                denormalizedNeurons
                  .map(_.mkString(","))
                  .mkString("\n")
                  .getBytes(StandardCharsets.UTF_8))

  }

  def trainNeurons(data: Seq[Seq[Double]]): Seq[Seq[Double]] = {
    val Speed = 0.3
    val Step = -0.05
    // 4 neurons, for example
    val M = 4

    var neurons =
      (0 until M).map(_ =>
        data.head.indices.map(_ => Random.nextDouble() * 0.2 + 0.1))

    BigDecimal(Speed).until(0).by(Step).foreach { speed =>
      data.foreach { xs =>
        val i =
          neurons
            .map(vectorsDistance(xs, _))
            .zipWithIndex
            .maxBy(_._1)
            ._2
        neurons =
          neurons.updated(i, weightUpdate(neurons(i), xs, speed.toDouble))
      }
    }
    neurons
  }

  def extractData(paths: String): Seq[Seq[Double]] = {
    val data = Source
      .fromResource(path)
      .getLines()
      .map(_.split(""",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"""))

    // kernels, discussions, views, downloads, size, featured, super_featured, upvotes
    data
      .map(a => {
        val temp = a.slice(4, a.length)
        (temp
          .take(4)
          .map(_.toDouble) ++ temp.takeRight(4).map(_.toDouble)).toList
      })
      .toList
  }

  def calcNormalizationCoeffs(data: Seq[Seq[Double]]): Seq[(Double, Double)] = {
    data.head.indices.map { i =>
      val maxMin = (data.maxBy(_(i))(implicitly[Ordering[Double]])(i),
                    data.minBy(_(i))(implicitly[Ordering[Double]])(i))
      (calcA(maxMin), calcB(maxMin))
    }
  }

  def calcA(maxMin: (Double, Double)): Double = {
    1d / (maxMin._1 - maxMin._2)
  }

  def calcB(maxMin: (Double, Double)): Double = {
    val divisor = maxMin._2 * (maxMin._1 - maxMin._2)
    if (divisor != 0) -1d / divisor else 0d
  }

  def vectorsDistance(xs1: Seq[Double], xs2: Seq[Double]): Double =
    sqrt(xs1.indices.map(i => pow(xs1(i) - xs2(i), 2)).sum)

  def weightUpdate(ws: Seq[Double],
                   xs: Seq[Double],
                   L: Double): collection.immutable.IndexedSeq[Double] =
    ws.indices.map(i => ws(i) + L * (xs(i) - ws(i)))

}
