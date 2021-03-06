addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.7")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.11.0")

addSbtPlugin("com.github.tototoshi" % "sbt-slick-codegen" % "1.4.0")
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.14"

addSbtPlugin("au.com.onegeek"       %% "sbt-dotenv"       % "2.1.146")
