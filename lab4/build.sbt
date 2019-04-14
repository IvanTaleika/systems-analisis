name := "lab4"

version := "0.1"

scalaVersion := "2.11.12"

val sparkVersion = "2.4.0"

resolvers += "jzy3d-repo" at "http://maven.jzy3d.org/releases/"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-mllib" % sparkVersion
libraryDependencies += "com.johnsnowlabs.nlp" %% "spark-nlp" % "2.0.1"
libraryDependencies += "commons-io" % "commons-io" % "2.6"

libraryDependencies += "org.jzy3d" % "jzy3d-api" % "1.0.2"
