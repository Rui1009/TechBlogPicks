import slick.codegen.SourceCodeGenerator

name := """TechBlogPicks"""
organization := "com.techblogpicks"

version := "1.0-SNAPSHOT"

lazy val codegen = taskKey[Unit]("generate slick table code")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    codegen := {
      SourceCodeGenerator.main(
        Array(
          "slick.jdbc.PostgresProfile",
          "org.postgresql.Driver",
          "jdbc:postgresql://localhost:5432/tech_blog_picks_server",
          "app/infra",
          "dto",
          sys.env.getOrElse("DB_USER", ""),
          sys.env.getOrElse("DB_PASSWORD", ""),
          "true",
          "slick.codegen.SourceCodeGenerator",
          "true"
        )
      )
    }
  )

scalacOptions ++= Seq(
  "-Ymacro-annotations"
)

scalaVersion := "2.13.3"

libraryDependencies += guice
libraryDependencies ++= Seq(
  "org.postgresql"       % "postgresql"             % "42.2.14",
  "com.typesafe.play"    %% "play-slick"            % "5.0.0",
  "com.typesafe.play"    %% "play-slick-evolutions" % "5.0.0",
  "com.typesafe.slick"   %% "slick-codegen"         % "3.3.2",
  evolutions
)


libraryDependencies ++= Seq(
  "eu.timepit" %% "refined" % "0.9.21",
  "eu.timepit" %% "refined-cats" % "0.9.21",
)
libraryDependencies += "io.estatico" %% "newtype" % "0.4.4"

val circeVersion = "0.12.3"
libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "org.scalatestplus.play"   %% "scalatestplus-play"  % "5.1.0",
  "org.scalatestplus"        %% "scalacheck-1-14"     % "3.2.2.0",
  "org.scalacheck"           %% "scalacheck"          % "1.15.2",
  "org.mockito"              %% "mockito-scala"       % "1.16.29",
).map(_ % Test)

libraryDependencies += "org.typelevel" %% "cats-core" % "2.4.2"

javaOptions in Runtime += "-Dconfig.file=./conf/application.dev.conf"
envFileName in ThisBuild := ".env"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.techblogpicks.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.techblogpicks.binders._"
