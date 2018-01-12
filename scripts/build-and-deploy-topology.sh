#!/usr/bin/env bash

projectDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"
cd $projectDir

# Build topology
sbt assembly

# Deploy built topology
storm jar $projectDir/target/scala-2.12/trucking-iot-demo-storm-on-scala-assembly-1.1.0.jar com.orendainx.trucking.storm.topologies.KafkaToKafka
