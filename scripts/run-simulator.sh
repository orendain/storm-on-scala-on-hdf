#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

# Run trucking simulator, logging data to two different Kafka topics (trucking_data_truck and trucking_data_traffic)
java -jar $scriptDir/trucking-simulator-assembly-0.5.1.jar com.orendainx.trucking.simulator.simulators.EnrichToKafkaSimulator
