ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "fs2-kafka-example"
  )

val http4sVersion = "0.23.7"

// sbt setup for cats-effect : https://typelevel.org/cats-effect/docs/getting-started
// sbt setup for fs2-kafka : https://fd4s.github.io/fs2-kafka/

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.14",

  "com.github.fd4s" %% "fs2-kafka" % "3.0.0-M8",
  "com.github.fd4s" %% "fs2-kafka-vulcan" % "3.0.0-M8",

  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-jdk-http-client" % "0.7.0",
  "io.circe" %% "circe-derivation" % "0.13.0-M5"
)

resolvers += "confluent" at "https://packages.confluent.io/maven/"

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

