package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tron.db.TransactionExtentionStore;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.protos.Sidechain.TransactionExtension;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

@Slf4j(topic = "task")
public class ChainTask extends Thread {

  private String gatewayAddress;
  private TaskEnum taskType;
  private ExecutorService executor;
  private final KfkConsumer kfkConsumer;

  public ChainTask(TaskEnum taskType, String gatewayAddress,
      String kfkServer, int fixedThreads) {
    super();
    this.gatewayAddress = gatewayAddress;
    this.taskType = taskType;
    this.executor = Executors.newFixedThreadPool(fixedThreads);
    this.kfkConsumer = new KfkConsumer(kfkServer, taskType.toString(),
        Arrays.asList("contractevent"));
    logger.info("task name is {},task type is {}", getName(), this.taskType);
  }

  @Override
  public void run() {
    for (; ; ) {
      ConsumerRecords<String, String> record = kfkConsumer.getRecord();
      for (ConsumerRecord<String, String> key : record) {
        JSONObject obj = (JSONObject) JSONValue.parse(key.value());
        if (Objects.isNull(obj.get("contractAddress")) || !obj.get("contractAddress").toString()
            .equals(gatewayAddress)) {
          kfkConsumer.commit();
          continue;
        }

        TransactionExtentionStore store = TransactionExtentionStore.getInstance();

        Actuator eventTask = EventActuatorFactory.CreateActuator(this.taskType, obj);
        if (Objects.isNull(eventTask)) {
          kfkConsumer.commit();
          // TODO: 不需要的event都应该continue
          continue;
        }

        // TransactionExtensionCapsule tx = eventTask.getTx();
        try {

          TransactionExtension txExtension = TransactionExtension.parseFrom(new byte[0]);

          byte[] txIdBytes = txExtension.getTxid().toByteArray();
          if (!store.exist(txIdBytes)) {
            store.putData(txIdBytes, txExtension.toByteArray());
          }

          kfkConsumer.commit();
        } catch (InvalidProtocolBufferException e) {
          e.printStackTrace();
        }
      }

    }
  }
}
