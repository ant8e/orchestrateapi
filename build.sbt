name := "orchestrateapi"

version := "1.0"

scalaVersion := "2.10.3"

resolvers += "spray" at "http://repo.spray.io/"


libraryDependencies ++= Seq(
  "io.spray" %% "spray-json" % "1.2.5",
  "io.spray" % "spray-client" % "1.2.0",
  "io.spray" % "spray-can" % "1.2.0",
  "io.spray" % "spray-http" % "1.2.0",
  "io.spray" % "spray-httpx" % "1.2.0",
  "io.spray" % "spray-util" % "1.2.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3"  ,
  "org.scalatest" % "scalatest_2.10" % "2.0" % "test"
)

