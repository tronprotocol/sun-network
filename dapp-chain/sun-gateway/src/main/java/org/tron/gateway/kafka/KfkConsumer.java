package org.tron.gateway.kafka;

import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


public class KfkConsumer {

  private KafkaConsumer<String, String> kafkaConsumer = null;

  public KfkConsumer(String server, List<String> topicList) {
    Properties properties = new Properties();
    properties.put("bootstrap.servers", server);
    properties.put("group.id", "group-1");
    properties.put("enable.auto.commit", "true");
    properties.put("auto.commit.interval.ms", "1000");
    properties.put("auto.offset.reset", "earliest");
    properties.put("session.timeout.ms", "30000");
    properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties
        .put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

    this.kafkaConsumer = new KafkaConsumer<>(properties);
    this.kafkaConsumer.subscribe(topicList);
  }

  public ConsumerRecords<String, String> getRecord() {
    return this.kafkaConsumer.poll(100);
  }
}
