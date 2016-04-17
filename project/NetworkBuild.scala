import sbt.Keys._
import sbt._

object NetworkBuild extends Build {

    lazy val sVersion = "2.11.8"

    lazy val dependencies = Seq(
        "org.scala-lang" % "scala-reflect" % "2.11.8",
        "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4",
        "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
        "com.typesafe.play" %% "play" % "2.5.1",
        "org.slf4j" % "slf4j-simple" % "1.7.16",
        "org.scalatest" % "scalatest_2.11" % "2.2.6" % "test"
    )

    lazy val root = Project(id = "network-analyzer",
        base = file(".")
    ) settings(
        version := "0.1",
        scalaVersion := sVersion,
        libraryDependencies ++= dependencies
        )
}