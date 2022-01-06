import sbt._

object Dependencies {

  val scalatest = "org.scalatest" %% "scalatest" % "3.2.10" % "test"

  val cats = "org.typelevel" %% "cats-core" % "2.6.1"
  val catsEffect = "org.typelevel" %% "cats-effect" % "2.5.4"
  val mtl = "org.typelevel" %% "cats-mtl" % "1.2.1"

  val log = Seq(
    "io.chrisdavenport" %% "log4cats-core" % "1.0.1",
    "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
    "org.slf4j" % "slf4j-api" % "1.7.30",
    "org.slf4j" % "slf4j-simple" % "1.7.30"
  )

  private val fs2Version = "2.5.10"

  val fs2 = Seq (
    "co.fs2" %% "fs2-core" % fs2Version,
    "co.fs2" %% "fs2-io" % fs2Version,
    "co.fs2" %% "fs2-reactive-streams" % fs2Version
  )

  private val pureConfigVersion = "0.12.2"

  val config = Seq(
    "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-core" % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-generic" % pureConfigVersion,
    "com.github.pureconfig" %% "pureconfig-macros" % pureConfigVersion
  )

  private val circeVersion = "0.14.1"
  val json = Seq(
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion
  )

  val http4sVersion = "0.22.8"

  val http4s = Seq(
    "org.http4s" %% "http4s-core" % http4sVersion,
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-circe" % http4sVersion,
    "org.http4s" %% "http4s-blaze-server" % http4sVersion,
    "org.http4s" %% "http4s-client" % http4sVersion,
    "org.http4s" %% "http4s-blaze-client" % http4sVersion,
    "org.http4s" %% "http4s-blaze-core" % http4sVersion,
    "org.http4s" %% "http4s-server" % http4sVersion

  )

  val all = Seq(scalatest, cats, catsEffect, mtl) ++ log ++ json ++ http4s ++ config ++ fs2
}
