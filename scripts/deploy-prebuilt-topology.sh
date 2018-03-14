#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

# Deploy pre-built topology
storm jar $scriptDir/trucking-iot-demo-storm-on-scala-assembly-1.1.0.jar com.orendainx.trucking.storm.topologies.KafkaToKafka
