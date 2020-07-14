name := "Spark Dataframe 1"

version := "1.0"

scalaVersion := "2.12.8"
val sparkVersion = "2.4.5"

libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion
libraryDependencies += "jp.co.bizreach" %% "aws-kinesis-spark" % "0.0.12"
libraryDependencies += "jp.co.bizreach" %% "aws-kinesis-scala" % "0.0.12"

