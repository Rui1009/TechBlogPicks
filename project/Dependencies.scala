import play.sbt.PlayImport._
import sbt._

object Dependencies {
  object Versions {
    val refinedVersion = "0.9.21"
    val circeVersion = "0.12.3"
  }

  val playDeps = Seq(guice, ws)

  val dbDeps = Seq(
    "org.postgresql" % "postgresql" % "42.2.14",
    "com.typesafe.play" %% "play-slick" % "5.0.0",
    "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
    "com.typesafe.slick" %% "slick-codegen" % "3.3.2",
    evolutions
  )

  val testDeps = Seq(
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
    "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0",
    "org.scalacheck" %% "scalacheck" % "1.15.2",
    "de.leanovate.play-mockws" %% "play-mockws" % "2.7.1",
    "org.mockito" %% "mockito-scala" % "1.16.29"
  ).map(_ % Test)

  val refinedDeps = Seq(
    "eu.timepit" %% "refined",
    "eu.timepit" %% "refined-cats"
  ).map(_ % Versions.refinedVersion)

  val circeDeps = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % Versions.circeVersion)

  val newtype = "io.estatico" %% "newtype" % "0.4.4"
  val cats = "org.typelevel" %% "cats-core" % "2.4.2"

  val rootDeps = Seq(newtype, cats) ++ playDeps ++ dbDeps ++ testDeps ++ refinedDeps ++ circeDeps
}
