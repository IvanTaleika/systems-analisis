package by.bsuir.sa

import breeze.linalg.DenseVector
import breeze.stats.MeanAndVariance
import breeze.stats.distributions._
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.filter.GrayscaleFilter
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.apache.spark.sql.SparkSession
import org.jfree.chart.plot.CombinedDomainXYPlot
import scalax.chart._
import scalax.chart.api._

object Runner extends App {
  println("Normal distribution test")
  val normalDistrPlot = testImageDistribution("normDistr", Gaussian(12.5, 3))

  println("\n-------------------------\n")

  println("F-distribution test")
  val fDistrPlot = testImageDistribution("fDistr", LogNormal(2, 1))

  val plot = new CombinedDomainXYPlot()
  plot.add(normalDistrPlot)
  plot.add(fDistrPlot)
  XYChart(plot, "histogram", legend = false)(ChartTheme.Default)
    .show("histogram")

  def testImageDistribution(
      imageName: String,
      expectedDistribution: ContinuousDistr[Double] with HasCdf)
    : CombinedDomainXYPlot = {
    val image = Image
      .fromStream(getClass.getResourceAsStream(s"$imageName.jpg"))
      .filter(GrayscaleFilter)

    val expectedValues =
      (0 to 25).map(i => (i, expectedDistribution.probability(i, i + 1)))
    val histogram = createColorHistogram(image)

    val chi2Result =
      Statistics.chiSqTest(Vectors.dense(histogram.map(_._2).toArray),
                           Vectors.dense(expectedValues.map(_._2).toArray))

    reportSampleParameters(image.pixels.map(_.red.asInstanceOf[Double]))
    println("")
    println(chi2Result)

    createCombinedPlot(expectedValues, histogram)
  }

  def reportSampleParameters(values: Seq[Double]): Unit = {
    import breeze.stats._
    val mv = meanAndVariance(values)
    println(s"Mean: ${mv.mean}")
    println(s"Variance: ${mv.variance}")
    println(s"Mode: ${values.groupBy(x => x).maxBy(_._2.length)._1}")
    val sortedValues = values.sorted
    println(s"Median: ${sortedValues(values.length / 2 + 1)}")
  }

  def createColorHistogram(image: Image): Seq[(Int, Double)] = {
    image.pixels
      .map(_.red)
      .groupBy(_ / 10)
      .map(t => (t._1, t._2.length.toDouble / image.pixels.length))
      .toList
      .sortBy(_._1)
  }

  def createCombinedPlot(s1: Seq[(Int, Double)],
                         s2: Seq[(Int, Double)]): CombinedDomainXYPlot = {
    val plot = new CombinedDomainXYPlot()
    plot.add(XYBarChart(s1).plot)
    plot.add(XYBarChart(s2).plot)
    plot
  }
}
