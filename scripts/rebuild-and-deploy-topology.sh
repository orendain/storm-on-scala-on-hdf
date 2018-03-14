#!/usr/bin/env bash

projectDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
cd $projectDir

echo "Checking for SBT, installing if missing"
curl https://bintray.com/sbt/rpm/rpm | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
sudo yum -y install sbt

# Rebuild topology
sbt assembly

# Deploy built topology
storm jar $projectDir/target/scala-2.12/trucking-iot-demo-storm-on-scala-assembly-1.1.0.jar com.orendainx.trucking.storm.topologies.KafkaToKafka
