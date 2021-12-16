import Dependencies.all

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(organization := "com.ziverge.challenge", scalaVersion := "2.13.5")),
    name := "ziverge.challenge",
    version := "0.0.1",
    libraryDependencies ++= all
  )

assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = true, includeDependency = true)