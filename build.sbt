ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.2.0"

enablePlugins(
  JavaAppPackaging,
  DockerPlugin
)

lazy val root = (project in file("."))
  .settings(
    name := "budgetBot",
    libraryDependencies ++= Dependencies.`budget-bot`
)

