import Dependencies._
import sbtwelcome._
import scala.{Console => SConsole}

ThisBuild / organization := "com.techblogpicks"
ThisBuild / scalaVersion := "2.13.3"

ThisBuild / version := "1.0-SNAPSHOT"

logo :=
  """
    | _______        _     ____  _             _____ _      _
    ||__   __|      | |   |  _ \| |           |  __ (_)    | |
    |   | | ___  ___| |__ | |_) | | ___   __ _| |__) |  ___| | _____
    |   | |/ _ \/ __| '_ \|  _ <| |/ _ \ / _` |  ___/ |/ __| |/ / __|
    |   | |  __/ (__| | | | |_) | | (_) | (_| | |   | | (__|   <\__ \
    |   |_|\___|\___|_| |_|____/|_|\___/ \__, |_|   |_|\___|_|\_\___/
    |                                     __/ |
    |                                    |___/
    |""".stripMargin

logoColor := SConsole.CYAN

import play.sbt.routes.RoutesKeys
RoutesKeys.routesImport := Seq.empty

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0"
usefulTasks := Seq(
  UsefulTask("fmt", "scalafmtAll; scalafixAll;", "Format code"),
  UsefulTask(
    "fmtCheck",
    "scalafmtCheckAll; scalafixAll --check;",
    "Check code format"
  ),
  UsefulTask("c", "compile", "Check code format"),
  UsefulTask("r", "reload", "Check code format"),
  UsefulTask("dbtest", "testOnly -- -n DBTest", "Run tests requiring DB"),
  UsefulTask("nondbtest", "testOnly -- -l DBTest", "Run tests not requiring DB")
)

inThisBuild(
  Seq(
    addCompilerPlugin(scalafixSemanticdb),
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val codegen = taskKey[Unit]("generate slick table code")

lazy val settings = Seq(
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-Wunused:imports",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds"
  ),
  libraryDependencies ++= rootDeps,
  wartremoverErrors ++= Warts.unsafe
    .filterNot(Seq(Wart.Throw, Wart.Any).contains),
  wartremoverExcluded ++= routes.in(Compile).value,
  wartremoverExcluded += baseDirectory.value / "app" / "infra" / "dto",
  wartremoverExcluded += baseDirectory.value / "app" / "Module",
  wartremoverExcluded += baseDirectory.value / "test"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(codegen := CodeGenerator.gen())
  .settings(name := "TechBlogPicks", settings)

javaOptions in Runtime += "-Dconfig.file=./conf/application.dev.conf"
javaOptions in Test ++= Seq(
  "-Dconfig.file=./conf/application.test.conf",
  "-Dlogger.file=./conf/logback.test.xml"
)
envFileName in ThisBuild := ".env"
