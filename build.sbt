name := "scala-zio-pg-notifs"

version := "0.1"

scalaVersion := "2.12.10"

// Because our tests use a real database, running them in parallel can break things.
parallelExecution in Test := false

libraryDependencies += "dev.zio" %% "zio" % "1.0.0-RC16"

libraryDependencies += "org.postgresql" % "postgresql" % "42.2.8"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.3"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
