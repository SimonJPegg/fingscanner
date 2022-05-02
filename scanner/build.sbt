//==================================================================================================
// PROJECT
//==================================================================================================
name := "fingscanner"
version := "0.1"
organization := "org.antipathy"
startYear := Some(2022)
licenses += ("Apache-2.0", url(
  "https://www.apache.org/licenses/LICENSE-2.0.html"
))
homepage := Some(url("https://github.com/SimonJPegg"))
developers := List(
  Developer("SimonJPegg",
    "Ciaran Kearney",
    "ciaran@antipathy.org",
    url("http://www.antipathy.org")))
//==================================================================================================
// DEPENDENCIES


libraryDependencies ++=
  Seq(
    "com.github.scopt" %% "scopt" % "4.0.1"  % "compile",
    "com.influxdb" % "influxdb-client-java" % "6.0.0" % "compile",
    "dnsjava" % "dnsjava" % "3.5.0" % "compile",
    "ch.qos.logback" % "logback-classic" % "1.2.9" % "compile",
    "com.typesafe.akka" %% "akka-actor" % "2.6.19" % "compile",
    "joda-time" % "joda-time" % "2.10.14" % "compile"
  )


//==================================================================================================
// SETTINGS
//==================================================================================================
publishMavenStyle := true
scalaVersion := "2.13.1"
scalacOptions := Seq("-unchecked",
  "-feature",
  "-deprecation",
  "-encoding",
  "utf8")

assembly / mainClass := Some("org.antipathy.fing.scanner.Main")
assembly / assemblyJarName := s"${name.value}-${version.value}-jar-with-dependencies.jar"
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
