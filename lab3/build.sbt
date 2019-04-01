name := "lab3"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.8"

val scrimageVersion = "2.1.8"
val sparkVersion = "2.4.0"

libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-core" % scrimageVersion
libraryDependencies += "com.sksamuel.scrimage" %% "scrimage-filters" % scrimageVersion
libraryDependencies += "com.github.wookietreiber" %% "scala-chart" % "0.5.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion