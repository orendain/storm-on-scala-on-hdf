name := "trucking-iot-on-hdf",

version := "0.3.2",
scalaVersion := "2.11.8",

description := """Trucking IoT on HDF""",
organization := "com.orendainx",
homepage := Some(url("https://github.com/orendain/trucking-iot")),
organizationHomepage := Some(url("https://github.com/orendain/trucking-iot")),
licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0"))),

//resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
resolvers += Resolver.sonatypeRepo("snapshots"),
libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.1",
  "com.github.pathikrit" %% "better-files" % "2.16.0",

  "org.apache.storm" % "storm-core" % "1.0.2" % "provided",
  "org.apache.storm" % "storm-kafka" % "1.0.2",
  ("org.apache.kafka" %% "kafka" % "0.10.2.0")
    .exclude("org.apache.zookeeper", "zookeeper")
    .exclude("org.slf4j", "slf4j-log4j12"),

  "com.orendainx" %% "trucking-commons" % "0.3.2-SNAPSHOT"
),

scalacOptions ++= Seq("-feature", "-Yresolve-term-conflict:package")
