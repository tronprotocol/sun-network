package org.tron.service.kafka;

import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.tron.common.config.KafkaConfig;


public class KfkConsumer {

  private KafkaConsumer<String, String> kafkaConsumer = null;

  public KfkConsumer(String server, String groupId, List<String> topicList, KafkaConfig config) {
    Properties properties = new Properties();
    properties.put("bootstrap.servers", server);
    properties.put("group.id", groupId);
    properties.put("enable.auto.commit", "false");
    // properties.put("auto.commit.interval.ms", "1000");
    properties.put("auto.offset.reset", "earliest");
    properties.put("session.timeout.ms", "30000");
    properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties
        .put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    if (Objects.nonNull(config)) {
      properties
          .put("security.protocol", "SASL_PLAINTEXT");
      properties
          .put("sasl.mechanism", "SCRAM-SHA-512");
      properties
          .put("sasl.jaas.config", String.format(
              "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
              config.getUsername(), config.getPassword()));
    }
    this.kafkaConsumer = new KafkaConsumer<>(properties);
    this.kafkaConsumer.subscribe(topicList);
  }

  public void commit() {
    this.kafkaConsumer.commitSync();
  }

  public ConsumerRecords<String, String> getRecord() {
    return this.kafkaConsumer.poll(100);
  }
}
