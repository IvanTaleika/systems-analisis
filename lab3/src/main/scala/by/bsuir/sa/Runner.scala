package by.bsuir.sa

import com.sksamuel.scrimage.Image
import com.sksamuel.scrimage.filter.GrayscaleFilter
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.stat.Statistics
import scalax.chart.api._
object Runner extends App {
  val imageUrl = "image.jpg"
//  Image
//    .fromStream(getClass.getResourceAsStream(imageUrl))
//    .filter(GrayscaleFilter).output("birdGrey.jpg")
  val histogram = Image
    .fromStream(getClass.getResourceAsStream(imageUrl))
    .filter(GrayscaleFilter)
    .pixels
    .map(_.red)
    .groupBy(_ / 10)
    .map(t => (t._1, t._2.length))

  val distribution = Vectors.dense(histogram.values.map(_.toDouble).toArray)
  val chi2Result = Statistics.chiSqTest(distribution)

//  val ksResult = Statistics.kolmogorovSmirnovTest(distribution., "norm")
  println(chi2Result)
//  println("--------------------------------------")
//  println(
//    Statistics.chiSqTest(
//      Vectors.dense(StudentsT(25).sample(26).map(abs(_)).toArray)))

  val chart = XYBarChart(histogram.toList)
  chart.plot.getDomainAxis.setLabel("bucket")
  chart.plot.getRangeAxis.setLabel("number of pixels")

  chart.show("histogram")
}
