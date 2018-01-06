#!/usr/bin/env bash

projectDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )/.." && pwd)"

storm jar $projectDir/target/scala-2.12/trucking-iot-on-hdf-assembly-1.1.0.jar com.orendainx.trucking.storm.topologies.KafkaToKafka
