#!/usr/bin/env bash

#
# Make sure that Ambari is up and running before this script is run.
#

hostname="sandbox-hdf.hortonworks.com"
ambariClusterName="Sandbox"
ambariUser="admin"
ambariPass="admin"

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
cd $scriptDir

#echo "Setting delete.topic.enable to true via Ambari"
#/var/lib/ambari-server/resources/scripts/configs.py $ambariUser $ambariPass 8080 http set $hostname $ambariClusterName kafka-broker delete.topic.enable true


#echo "Restarting Kafka via Ambari"
#curl -u $ambariUser:$ambariPass -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Stop Kafka"}, "ServiceInfo": {"state": "INSTALLED"}}' http://$hostname:8080/api/v1/clusters/$ambariClusterName/services/KAFKA
#sleep 10
#curl -u $ambariUser:$ambariPass -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context": "Start Kafka"}, "ServiceInfo": {"state": "STARTED"}}' http://$hostname:8080/api/v1/clusters/$ambariClusterName/services/KAFKA


echo "Checking for SBT, installing if missing"
#curl https://bintray.com/sbt/rpm/rpm | tac | sudo tee /etc/yum.repos.d/bintray-sbt-rpm.repo
#yum -y install sbt-0.13.13.1-1


#echo "Creating Kafka topics"
#scripts/create-kafka-topics.sh


echo "Extracting included web application"
tar zxvf $scriptDir/trucking-web-application-backend-0.4.0-SNAPSHOT.tgz


#echo "Building Storm topology"
#scripts/build-topology.sh
