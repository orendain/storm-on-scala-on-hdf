#!/bin/bash

# Make sure Kafka is running
$(find / -type f -wholename '/usr/hd*/kafka' -print -quit 2> /dev/null) start

kafkaTopicsSh=$(find / -type f -wholename '/usr/hd*/kafka-topics.sh' -print -quit 2> /dev/null)
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_truck
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_traffic
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_joined
$kafkaTopicsSh --create --zookeeper sandbox-hdf.hortonworks.com:2181 --replication-factor 1 --partition 10 --topic trucking_data_driverstats
