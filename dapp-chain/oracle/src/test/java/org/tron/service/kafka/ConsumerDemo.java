package org.tron.service.kafka;

import java.util.Arrays;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.Ignore;
import org.junit.Test;

public abstract class  ConsumerDemo {

  @Test
  public void main(String[] args) {
    Properties properties = new Properties();
    properties.put("bootstrap.servers", "172.16.22.167:9092");
    properties.put("group.id", "Oracle_TRxgBU7HFTQvU6zPheLHphqpwhDKNxB6Rk");
    properties.put("enable.auto.commit", "true");
    properties.put("auto.commit.interval.ms", "1000");
    properties.put("auto.offset.reset", "earliest");
    properties.put("session.timeout.ms", "30000");
    properties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties
        .put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
    properties
        .put("security.protocol", "SASL_PLAINTEXT");
    properties
        .put("sasl.mechanism", "SCRAM-SHA-512");
    properties
        .put("sasl.jaas.config",
            "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"oracle\" password=\"oracle_secret\";");

    KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(properties);
    kafkaConsumer.subscribe(Arrays.asList("block"));
    while (true) {
      ConsumerRecords<String, String> records = kafkaConsumer.poll(100);
      for (ConsumerRecord<String, String> record : records) {
        System.out.printf("offset = %d, value = %s", record.offset(), record.value());
        System.out.println();
      }
    }

  }
}