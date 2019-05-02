package by.bsuir.sa.lab5

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import scala.io.Source
import scala.math._
import scala.util.Random

object Runner extends App {

  val data = Source
    .fromResource("kaggle_datasets_train.csv")
    .getLines()
    .map(_.split(""",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"""))
  // kernels, discussions, views, downloads, size, featured, super_featured, upvotes
  val labels = data
    .map(a => {
      val temp = a.slice(4, a.length)
      temp.take(4).map(_.toDouble)
//      ++ temp.takeRight(4).map(_.toDouble)
    })
    .toList

  val N = labels.head.length

  val columnCoeffs =
    (0 until N).map { i =>
      val maxMin = (labels.maxBy(_(i))(implicitly[Ordering[Double]])(i),
                    labels.minBy(_(i))(implicitly[Ordering[Double]])(i))
      (calcA(maxMin), calcB(maxMin))
    }

  val normalizedLabels = labels.map(r =>
    r.indices.map(i => r(i) * columnCoeffs(i)._1 + columnCoeffs(i)._2))

  // 4 neurons, for example
  var neurons =
    (0 to 3).map(_ => (0 until N).map(_ => Random.nextDouble() * 0.2 + 0.1))

  val l = 0.3
  val step = -0.05

  BigDecimal(l).until(0).by(step).foreach { speed =>
    normalizedLabels.foreach { xs =>
      val i =
        neurons
          .map(vectorsDistance(xs, _))
          .zipWithIndex
          .maxBy(_._1)
          ._2
      neurons = neurons.updated(i, weightUpdate(neurons(i), xs, speed.toDouble))
    }
  }

 val denormalizedNeurons = neurons.map(ws =>
    ws.indices.map(i =>( ws(i) - columnCoeffs(i)._2) / columnCoeffs(i)._1))
  Files.write(Paths.get("weights.csv"),
    denormalizedNeurons
                .map(_.mkString(","))
                .mkString("\n")
                .getBytes(StandardCharsets.UTF_8))
//
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
                   l: Double): collection.immutable.IndexedSeq[Double] =
    ws.indices.map(i => ws(i) + l * (xs(i) - ws(i)))
}
