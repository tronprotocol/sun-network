package org.tron.service.task;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tron.common.config.Args;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.AlertUtil;
import org.tron.db.TransactionExtentionStore;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

public class EventTask {

  private ExecutorService executor;

  private KfkConsumer kfkConsumer;

  private TransactionExtentionStore store;
  private String mainGateway;
  private String sideGateway;

  public EventTask(String mainGateway, String sideGateway, int fixedThreads) {
    this.mainGateway = mainGateway;
    this.sideGateway = sideGateway;
    this.executor = Executors.newFixedThreadPool(fixedThreads);
    this.kfkConsumer = new KfkConsumer(Args.getInstance().getMainchainKafka(), "Oracle",
        Arrays.asList("contractevent"));
    this.store = TransactionExtentionStore.getInstance();
  }

  public void processEvent() {
    for (; ; ) {
      ConsumerRecords<String, String> record = this.kfkConsumer.getRecord();
      for (ConsumerRecord<String, String> key : record) {
        JSONObject obj = (JSONObject) JSONValue.parse(key.value());
        if (Objects.isNull(obj.get("contractAddress"))) {
          this.kfkConsumer.commit();
          continue;
        }
        Actuator eventActuator;
        if (obj.get("contractAddress").equals(this.mainGateway)) {
          eventActuator = EventActuatorFactory.CreateActuator(TaskEnum.MAIN_CHAIN, obj);
        } else if (obj.get("contractAddress").equals(this.sideGateway)) {
          eventActuator = EventActuatorFactory.CreateActuator(TaskEnum.SIDE_CHAIN, obj);
        } else {
          //Unrelated contract address
          this.kfkConsumer.commit();
          continue;
        }
        if (Objects.isNull(eventActuator)) {
          //Unrelated contract event
          this.kfkConsumer.commit();
          continue;
        }
        TransactionExtensionCapsule txExtensionCapsule = null;
        try {
          txExtensionCapsule = eventActuator
              .createTransactionExtensionCapsule();
        } catch (RpcConnectException e) {
          AlertUtil.sendAlert("createTransactionExtensionCapsule fail, system exit");
          System.exit(1);
        }
        byte[] txIdBytes = txExtensionCapsule.getTransactionIdBytes();
        if (!this.store.exist(txIdBytes)) {
          this.store.putData(txIdBytes, txExtensionCapsule.getData());
        }
        this.kfkConsumer.commit();
        executor.execute(new TxExtensionTask(txExtensionCapsule));
      }
    }
  }
}
