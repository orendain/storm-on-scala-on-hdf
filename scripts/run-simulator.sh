#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

java -jar $scriptDir/trucking-simulator-assembly-0.5.0.jar com.orendainx.trucking.simulator.simulators.EnrichToKafkaSimulator
