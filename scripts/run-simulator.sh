#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"

# Run trucking simulator, logging data to two different Kafka topics (trucking_data_truck and trucking_data_traffic)
# By default, this is set up to run the main function from the class com.orendainx.trucking.simulator.simulators.EnrichToKafkaSimulator
java -jar $scriptDir/trucking-simulator-assembly-0.5.2.jar
