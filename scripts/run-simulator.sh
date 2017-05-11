#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
java -jar $scriptDir/trucking-simulator-assembly-0.4.0-SNAPSHOT.jar com.orendainx.hortonworks.trucking.simulator.simulators.EnrichToKafkaSimulator
