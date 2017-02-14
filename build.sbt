name := "paintShop"

version := "1.0"

scalaVersion := "2.10.5"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.10",
  "com.typesafe.akka" %% "akka-actor" % "2.3.14",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.14",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.scalactic" %% "scalactic" % "3.0.0"
)

resolvers ++= Seq(
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/"
)
