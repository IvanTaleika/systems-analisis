package by.bsuir.sa

import breeze.stats.distributions._
import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.filter.GrayscaleFilter
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import org.jfree.chart.plot.CombinedDomainXYPlot
import scalax.chart._
import scalax.chart.api._

object Runner extends App {

//  println("Normal distribution test")
//  val normalDistrPlot = testImageDistribution("image", Gaussian(12.5, 3))
//  println("\nF-distribution test")

  val fDistrPlot = testImageDistribution("fDistr", LogNormal(2, 1))
  val plot = new CombinedDomainXYPlot()
//  plot.add(normalDistrPlot)
  plot.add(fDistrPlot)
  XYChart(plot, "histogram", legend = false)(ChartTheme.Default).show("histogram")
//  val spark = SparkSession.builder().master("local[*]").getOrCreate()

//  val ksResult = Statistics.kolmogorovSmirnovTest(
//    spark.sparkContext.parallelize(expectedValues.map(_._2)),
//    myCdf)

//  val ksResult = Statistics.kolmogorovSmirnovTest(distribution., "norm")

//  println(ksResult)

  def testImageDistribution(
      imageName: String,
      expectedDistribution: ContinuousDistr[Double] with HasCdf)
    : CombinedDomainXYPlot = {
    val image = Image
      .fromStream(getClass.getResourceAsStream(s"$imageName.jpg"))
      .filter(GrayscaleFilter)
    image.output(s"${imageName}Grey.jpg")

    val expectedValues =
      (0 to 25).map(i => (i, expectedDistribution.probability(i, i + 1)))
    val histogram = createColorHistogram(image)

    val chi2Result =
      Statistics.chiSqTest(Vectors.dense(histogram.map(_._2).toArray),
                           Vectors.dense(expectedValues.map(_._2).toArray))

    println(chi2Result)
    createCombinedPlot(expectedValues, histogram)
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
