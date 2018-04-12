#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

# Deploy pre-built topology
storm jar $scriptDir/storm-on-scala-on-hdf-assembly-1.1.1.jar com.orendainx.trucking.storm.topologies.KafkaToKafka
