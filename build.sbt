name := "trucking-iot-on-hdf"

version := "1.1.0"
scalaVersion := "2.12.4"

description := """Trucking IoT on HDF"""
organization := "com.orendainx.trucking"
homepage := Some(url("https://github.com/orendain/trucking-iot"))
organizationHomepage := Some(url("https://github.com/orendain/trucking-iot"))
licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))

resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "com.github.pathikrit" %% "better-files" % "3.4.0",

  "org.apache.storm" % "storm-core" % "1.1.0" % "provided",
  //"org.apache.storm" % "storm-kafka" % "1.1.0",
  "org.apache.storm" % "storm-kafka-client" % "1.1.0",
  ("org.apache.kafka" %% "kafka" % "0.10.2.1")
    .exclude("org.apache.zookeeper", "zookeeper")
    .exclude("org.slf4j", "slf4j-log4j12"),

  "com.orendainx.trucking" %% "trucking-commons" % "0.5.1"
)

scalacOptions ++= Seq("-feature", "-Yresolve-term-conflict:package")
