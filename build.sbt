import Dependencies._

name := """TechBlogPicks"""
organization := "com.techblogpicks"

version := "1.0-SNAPSHOT"

lazy val codegen = taskKey[Unit]("generate slick table code")

lazy val settings = Seq(
  scalacOptions ++= Seq(
    "-Ymacro-annotations",
    "-feature",
    "-language:implicitConversions",
    "-language:higherKinds",
  ),
  libraryDependencies ++= rootDeps,
  wartremoverWarnings ++= Warts.unsafe,
  wartremoverExcluded ++= routes.in(Compile).value,
  wartremoverExcluded += baseDirectory.value / "app" / "infra" / "dto"
)

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(codegen := CodeGenerator.gen())
  .settings(settings)

scalaVersion := "2.13.3"

javaOptions in Runtime += "-Dconfig.file=./conf/application.dev.conf"
envFileName in ThisBuild := ".env"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.techblogpicks.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.techblogpicks.binders._"
