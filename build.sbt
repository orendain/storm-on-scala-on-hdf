/*
 * Definition of common settings for all subprojects
 */
lazy val commonSettings = Seq(
  version := "0.3.2",
  scalaVersion := "2.11.8",
  description := """Trucking IoT application.""",
  organization := "com.orendainx.hortonworks",
  homepage := Some(url("https://github.com/orendain/trucking-iot")),
  organizationHomepage := Some(url("https://github.com/orendain/trucking-iot")),
  licenses := Seq(("Apache License 2.0", url("https://www.apache.org/licenses/LICENSE-2.0")))
)



/*
 * Root project definition
 */
lazy val truckingIot = (project in file("."))
  .settings(commonSettings)



/*
 * Subproject definition for trucking-commons
 */
lazy val commonsJVM = (project in file("trucking-commons"))
  .settings(
    commonSettings,
    name := "trucking-commons",
    isSnapshot := true // TODO: I forget exactly why this was necessary
  )



/*
 * Subproject definition for trucking-storm-topology
 */
lazy val stormTopology = (project in file("trucking-storm-topology"))
  .dependsOn(commonsJVM)
  .settings(
    commonSettings,
    name := "trucking-storm-topology",
    resolvers += "Hortonworks Nexus" at "http://nexus-private.hortonworks.com/nexus/content/groups/public",
    libraryDependencies ++= Seq(
      "org.apache.storm" % "storm-core" % "1.0.2" % "provided",
      "org.apache.storm" % "storm-kafka" % "1.0.2",
      "org.apache.nifi" % "nifi-storm-spout" % "1.1.2",
      ("org.apache.kafka" %% "kafka" % "0.10.2.0")
        .exclude("org.apache.zookeeper", "zookeeper")
        .exclude("org.slf4j", "slf4j-log4j12"),
      "com.typesafe" % "config" % "1.3.1",
      "com.github.pathikrit" %% "better-files" % "2.16.0",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",

      ("com.hortonworks.registries" % "schema-registry-serdes" % "0.0.1.3.0.0.0-55")
        .exclude("commons-beanutils", "commons-beanutils") // vs itself - commons-beanutils/commons-beanutils-core
        .exclude("commons-collections", "commons-collections") // vs itself - commons-beanutils/commons-beanutils-core
    ),

    scalacOptions ++= Seq("-feature", "-Yresolve-term-conflict:package")
  )
