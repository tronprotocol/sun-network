package org.tron.service.kafka;

import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;


public class ProducerDemo {

  public static void main(String[] args) {
    Properties properties = new Properties();
    properties.put("bootstrap.servers", "172.16.22.252:9092");
    properties.put("acks", "all");
    properties.put("retries", 0);
    properties.put("batch.size", 16384);
    properties.put("linger.ms", 1);
    properties.put("buffer.memory", 33554432);
    properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    Producer<String, String> producer = null;
    try {
      producer = new KafkaProducer<String, String>(properties);
      for (int i = 0; i < 1000; i++) {
        String msg = "Message " + i;
        producer.send(new ProducerRecord<String, String>("HelloWorld", msg));
        System.out.println("Sent:" + msg);
      }
    } catch (Exception e) {
      e.printStackTrace();

    } finally {
      producer.close();
    }

  }
}