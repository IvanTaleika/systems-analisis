package by.bsuir.sa.lab2

import breeze.stats._
import breeze.stats.distributions.ChiSquared
import breeze.stats.hypothesis._

import scala.io.Source
import scala.math._
import scala.util.Try

object Runner extends App {
  val tCriteria = 1.6478
  val a = 0.05
  val (roughnesses, pressSpeeds) = Source
    .fromResource("18-bands.csv")
    .getLines
    .map(s => {
      val columns = s.split(',')
      (parseDouble(columns(25)), parseDouble(columns(28)))
    })
    .collect { case (a: Some[Double], b: Some[Double]) => (a.get, b.get) }
    .toList
    .unzip
  val roughnessesParam = meanAndVariance(roughnesses)
  val pressSpeedParam = meanAndVariance(pressSpeeds)

  println("task1:")
  println(s"Roughness mean = ${roughnessesParam.mean}. Confidence interval = \u00B1 " +
    s"${calcMeanDeviation(roughnessesParam.mean, tCriteria, roughnesses.length)}")
  println(s"PressSpeed mean = ${pressSpeedParam.mean}. Confidence interval = \u00B1 " +
    s"${calcMeanDeviation(pressSpeedParam.mean, tCriteria, pressSpeeds.length)}")

  println(s"Roughness variance = ${roughnessesParam.variance}. Confidence interval = " +
    s"${calcVarianceDeviation(roughnessesParam.variance, a, roughnesses.length)}")
  println(s"PressSpeed variance = ${pressSpeedParam.variance}. Confidence interval = " +
    s"${calcVarianceDeviation(pressSpeedParam.variance, a, pressSpeeds.length)}")

  println("\ntask2:")
  println(
    "Assuming unknown variance. Welch's t-test: " +
      reportProbability(tTest(roughnesses, pressSpeeds), a))
  val z = zTest(pressSpeedParam, roughnessesParam)
  val probability =
    if (z < tCriteria) s"likely (z = $z < $tCriteria)"
    else s"unlikely (z = $z > $tCriteria)"
  println(s"Assuming known variance. Z-test: $probability")
  def parseDouble(s: String): Option[Double] = Try { s.toDouble }.toOption

  def reportProbability(d: Double, a: Double): String =
    if (d < a) s"unlikely (p = $d < $a)" else s"likely (p = $d > $a)"

  def calcMeanDeviation(mean: Double, t: Double, size: Int): Double =
    sqrt(mean * size / (size - 1)) * t / sqrt(size)

  def calcVarianceDeviation(v: Double, a: Double, c: Int): (Double, Double) = {
    val s = correctVariance(v, c)
    (c * s / chi2(c - 1, 1 - a / 2), c * s / chi2(c - 1, a / 2))
  }

  def chi2(k: Int, a: Double): Double = ChiSquared(k).inverseCdf(a)

  def correctVariance(v: Double, c: Int): Double = c * v / (c - 1)

  def zTest(m1: MeanAndVariance, m2: MeanAndVariance): Double = {
    abs(
      (m1.mean - m2.mean) / sqrt(
        m1.variance / m1.count + m2.variance / m2.count))
  }
}
