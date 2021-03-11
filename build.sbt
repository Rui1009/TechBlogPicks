import Dependencies._
import sbtwelcome._
import scala.{ Console => SConsole }

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

usefulTasks := Seq(
  UsefulTask("fmt", "scalafmtAll", "Format code")
)

lazy val codegen = taskKey[Unit]("generate slick table code")

lazy val settings = Seq(
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds",
  ),
  libraryDependencies ++= rootDeps,
  wartremoverErrors ++= Warts.unsafe,
  wartremoverExcluded ++= routes.in(Compile).value,
  wartremoverExcluded += baseDirectory.value / "app" / "infra" / "dto",
  wartremoverExcluded += baseDirectory.value / "test"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(codegen := CodeGenerator.gen())
  .settings(name := "TechBlogPicks", settings)

javaOptions in Runtime += "-Dconfig.file=./conf/application.dev.conf"
envFileName in ThisBuild := ".env"