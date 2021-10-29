

name := "scala_grpc"

version := "0.1"

scalaVersion := "2.13.6"
 assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
//  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case x => MergeStrategy.first
}
Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

val logbackVersion = "1.3.0-alpha10"
val sfl4sVersion = "2.0.0-alpha5"
val typesafeConfigVersion = "1.4.1"
val scalacticVersion = "3.2.9"

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-core" % logbackVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.slf4j" % "slf4j-api" % sfl4sVersion,
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest-featurespec" % scalacticVersion % Test,
  "com.typesafe" % "config" % typesafeConfigVersion,
  "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
  // https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.90",
  "commons-io" % "commons-io" % "20030203.000550",
 "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
"com.amazonaws" % "aws-lambda-java-events" % "3.10.0",
  "org.scalaj" %% "scalaj-http" % "2.4.2"


)