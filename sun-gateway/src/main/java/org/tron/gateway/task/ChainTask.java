package org.tron.gateway.task;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tron.gateway.eventenum.EventType;
import org.tron.gateway.kafka.KfkConsumer;

@Slf4j(topic = "task")
public class ChainTask extends Thread {

  private String gatewayAddress;
  private String kfkServer;
  private int fixedThreads;

  private ExecutorService executor;

  public ChainTask(String gatewayAddress, String kfkServer, int fixedThreads) {
    super();
    this.gatewayAddress = gatewayAddress;
    this.kfkServer = kfkServer;
    executor = Executors.newFixedThreadPool(fixedThreads);
  }

  @Override
  public void run() {
    KfkConsumer kfkConsumer = new KfkConsumer(kfkServer,
        Arrays.asList("contractevent"));
    while (true) {
      ConsumerRecords<String, String> record = kfkConsumer.getRecord();
      for (ConsumerRecord<String, String> key : record) {
        JSONObject obj = (JSONObject) JSONValue.parse(key.value());
        if (!obj.get("contractAddress").toString().equals(gatewayAddress)) {
          continue;
        }
        EventType eventSignature = EventType.fromSignature(obj.get("eventSignature").toString());
        EventTask eventTask = EventTashFactory.CreateTask(eventSignature, obj);
        if (Objects.isNull(eventTask)) {
          continue;
        }
        executor.execute(eventTask);
      }
    }
  }
}
