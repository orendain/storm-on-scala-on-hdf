package com.orendainx.trucking.storm.topologies

import java.util.Properties

import com.orendainx.hortonworks.trucking.storm.bolts._
import com.orendainx.trucking.storm.bolts.{CSVStringToObjectBolt, DataWindowingBolt, ObjectToCSVStringBolt, TruckAndTrafficJoinBolt}
import com.orendainx.trucking.storm.schemes.BufferToStringScheme
import com.typesafe.config.{ConfigFactory, Config => TypeConfig}
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.storm.generated.StormTopology
import org.apache.storm.kafka.bolt.KafkaBolt
import org.apache.storm.kafka.bolt.mapper.FieldNameBasedTupleToKafkaMapper
import org.apache.storm.kafka.bolt.selector.DefaultTopicSelector
import org.apache.storm.kafka.{KafkaSpout, SpoutConfig, ZkHosts}
import org.apache.storm.spout.SchemeAsMultiScheme
import org.apache.storm.topology.TopologyBuilder
import org.apache.storm.topology.base.BaseWindowedBolt
import org.apache.storm.tuple.Fields
import org.apache.storm.{Config, StormSubmitter}

import scala.concurrent.duration._

/**
  * Companion object to [[KafkaToKafka]] class.
  * Provides an entry point for building the default topology.
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
object KafkaToKafka {

  def main(args: Array[String]): Unit = {
    // Load global configs
    val config = ConfigFactory.load()

    // Set up configuration for the Storm Topology
    val stormConfig = new Config()
    stormConfig.setDebug(config.getBoolean(Config.TOPOLOGY_DEBUG))
    stormConfig.setMessageTimeoutSecs(config.getInt(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS))
    stormConfig.setNumWorkers(config.getInt(Config.TOPOLOGY_WORKERS))

    // Build and submit the Storm config and topology
    val topology = new KafkaToKafka(config).buildTopology()
    StormSubmitter.submitTopologyWithProgressBar("KafkaToKafka", stormConfig, topology)
  }
}

/**
  * Create a topology with the following components.
  *
  * Spouts:
  *   - KafkaSpout (for injesting TruckData)
  *   - KafkaSpout (for injesting TrafficData)
  * Bolt:
  *   - CSVStringToObjectBolt (for creating JVM objects from strings)
  *   - TruckAndTrafficJoinBolt (for joining two datatypes into one)
  *   - DataWindowingBolt (for reducing lists of tuples into models for machine learning)
  *   - ObjectToCSVStringBolt (for serializing JVM objects into strings)
  *   - KafkaBolt (for pushing strings into Kafka topics)
  *
  * @author Edgar Orendain <edgar@orendainx.com>
  */
class KafkaToKafka(config: TypeConfig) {

  def buildTopology(): StormTopology = {
    // Builder to perform the construction of the topology.
    implicit val builder = new TopologyBuilder()

    // Configurations used for Kafka components
    val zkHosts = new ZkHosts(config.getString(Config.STORM_ZOOKEEPER_SERVERS))
    val zkRoot = config.getString(Config.STORM_ZOOKEEPER_ROOT)
    val groupId = config.getString("kafka.group-id")



    /*
     * Build a Kafka spout for ingesting enriched truck data
     */

    // Name of the Kafka topic to connect to
    val truckTopic = config.getString("kafka.truck-data.topic")

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val truckSpoutConfig = new SpoutConfig(zkHosts, truckTopic, zkRoot, groupId)
    truckSpoutConfig.scheme = new SchemeAsMultiScheme(new BufferToStringScheme("EnrichedTruckData"))

    // Force the spout to ignore where it left off during previous runs (for demo purposes)
    truckSpoutConfig.ignoreZkOffsets = true

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("enrichedTruckData", new KafkaSpout(truckSpoutConfig), 1)



    /*
     * Build a second Kafka spout for ingesting traffic data
     */

    // Name of the Kafka topic to connect to
    val trafficTopic = config.getString("kafka.traffic-data.topic")

    // Create a Spout configuration object and apply the scheme for the data that will come through this spout
    val trafficSpoutConfig = new SpoutConfig(zkHosts, trafficTopic, zkRoot, groupId)
    trafficSpoutConfig.scheme = new SchemeAsMultiScheme(new BufferToStringScheme("TrafficData"))

    // Force the spout to ignore where it left off during previous runs (for demo purposes)
    trafficSpoutConfig.ignoreZkOffsets = true

    // Create a spout with the specified configuration, and place it in the topology blueprint
    builder.setSpout("trafficData", new KafkaSpout(trafficSpoutConfig), 1)



    /*
     * Build a bolt for creating JVM objects from the ingested strings
     */

    // Our custom bolt, CSVStringToObjectBolt, is given the bolt id of "unpackagedData".  Storm is told to assign only
    // a single task for this bolt (i.e. create only 1 instance of this bolt in the cluster).
    // ShuffleGrouping shuffles data flowing in from the specified spouts evenly across all instances of the newly
    // created bolt (which is only 1 in this example)
    builder.setBolt("unpackagedData", new CSVStringToObjectBolt(), 1)
      .shuffleGrouping("enrichedTruckData")
      .shuffleGrouping("trafficData")



    /*
     * Build a windowed bolt for joining two types of Tuples into one
     */

    // The length of the window, in milliseconds.
    val windowDuration = config.getInt(Config.TOPOLOGY_BOLTS_WINDOW_LENGTH_DURATION_MS)

    // Create a tumbling windowed bolt using our custom TruckAndTrafficJoinBolt, which houses the logic for how to
    // merge the different Tuples.
    //
    // A tumbling window with a duration means the stream of incoming Tuples are partitioned based on the time
    // they were processed (think of a traffic light, allowing all vehicles to pass but only the ones that get there
    // by the time the light turns red).  All tuples that made it within the window are then processed all at once
    // in the TruckAndTrafficJoinBolt.
    val joinBolt = new TruckAndTrafficJoinBolt().withTumblingWindow(new BaseWindowedBolt.Duration(windowDuration, MILLISECONDS))

    // GlobalGrouping suggests that all data from the specified component (unpackagedData) goes to a single one of the
    // bolt's tasks.
    builder.setBolt("joinedData", joinBolt, 1).globalGrouping("unpackagedData")



    /*
     * Build a bolt to generate driver stats from the Tuples in the stream.
     */

    // The size of the window, in number of Tuples.
    val intervalCount = config.getInt(Config.TOPOLOGY_BOLTS_SLIDING_INTERVAL_COUNT)

    // Creates a sliding windowed bolt using our custom DataWindowindBolt, which is responsible for reducing a list
    // of recent Tuples(data) for a particular driver into a single datatype.  This data is used for machine learning.
    //
    // This sliding windowed bolt with a tuple count as a length means we always process the last 'N' tuples in the
    // specified bolt.  The window slides over by one, dropping the oldest, each time a new tuple is processed.
    val statsBolt = new DataWindowingBolt().withWindow(new BaseWindowedBolt.Count(intervalCount))

    // Build a bolt and then place in the topology blueprint connected to the "joinedData" stream.
    //
    // Create 5 tasks for this bolt, to ease the load for any single instance of this bolt.
    // FieldsGrouping partitions the stream of tuples by the fields specified.  Tuples with the same driverId will
    // always go to the same task.  Tuples with different driverIds may go to different tasks.
    builder.setBolt("windowedDriverStats", statsBolt, 1).fieldsGrouping("joinedData", new Fields("driverId"))



    /*
     * Build bolts to serialize data into a CSV string.
     */

    // This bolt ingests tuples from the "joinedData" bolt, which streams instances of EnrichedTruckAndTrafficData
    builder.setBolt("serializedJoinedData", new ObjectToCSVStringBolt()).shuffleGrouping("joinedData")

    // This bolt ingests tuples from the "joinedData" bolt, which streams instances of WindowedDriverStats
    builder.setBolt("serializedDriverStats", new ObjectToCSVStringBolt()).shuffleGrouping("windowedDriverStats")



    /*
     * Build KafkaBolts to stream tuples into a Kafka topic
     */

    // Define properties to pass along to the KafkaBolt
    val kafkaBoltProps = new Properties()
    kafkaBoltProps.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getString("kafka.bootstrap-servers"))
    kafkaBoltProps.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, config.getString("kafka.key-serializer"))
    kafkaBoltProps.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, config.getString("kafka.value-serializer"))

    // withTopicSelector() specifies the Kafka topic to drop entries into
    //
    // withTupleToKafkaMapper() is passed an instance of FieldNameBasedTupleToKafkaMapper, which tells the bolt
    // which fields of a Tuple the data to pass in is stored as.
    //
    // withProducerProperties() takes in properties to set itself up with
    val truckingKafkaBolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.joined-data.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "data"))
      .withProducerProperties(kafkaBoltProps)

    builder.setBolt("joinedDataToKafka", truckingKafkaBolt, 1).shuffleGrouping("serializedJoinedData")



    /*
     * Build KafkaBolts to stream tuples into a Kafka topic
     */

    // Build a KafkaBolt
    val statsKafkaBolt = new KafkaBolt()
      .withTopicSelector(new DefaultTopicSelector(config.getString("kafka.driver-stats.topic")))
      .withTupleToKafkaMapper(new FieldNameBasedTupleToKafkaMapper("key", "data"))
      .withProducerProperties(kafkaBoltProps)

    builder.setBolt("driverStatsToKafka", statsKafkaBolt, 1).shuffleGrouping("serializedDriverStats")



    // Now that the entire topology blueprint has been built, we create an actual topology from it
    builder.createTopology()
  }
}
