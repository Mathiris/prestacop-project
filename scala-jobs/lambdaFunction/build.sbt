javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

lazy val root = (project in file(".")).
  settings(
    name := "lambdaFunction",
    version := "0.1",
    scalaVersion := "2.13.3",
    retrieveManaged := true,
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-lambda-java-events" % "3.1.0",
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
      "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.815",
      "com.typesafe.play" %% "play-json" % "2.8.1",
      "com.typesafe.play" %% "play-json-joda" % "2.8.1"
    )
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
