package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tron.common.config.Args;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.WalletUtil;
import org.tron.db.EventStore;
import org.tron.db.NonceStore;
import org.tron.db.TransactionExtensionStore;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.ActuatorRun;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

@Slf4j(topic = "eventTask")
public class EventTask {

  private KfkConsumer kfkConsumer;

  private EventStore store;
  private NonceStore nonceStore;
  private String mainGateway;
  private String sideGateway;

  public EventTask(String mainGateway, String sideGateway) {
    this.mainGateway = mainGateway;
    this.sideGateway = sideGateway;
    this.kfkConsumer = new KfkConsumer(Args.getInstance().getMainchainKafka(),
        "Oracle_" + getOracleAddress(),
        Arrays.asList("contractevent"));
    this.store = EventStore.getInstance();
    this.nonceStore = NonceStore.getInstance();
  }

  private String getOracleAddress() {
    return WalletUtil.encode58Check(
        ECKey.fromPrivate(Args.getInstance().getOraclePrivateKey()).getAddress());
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

        if (eventActuator.getNonce() != null && nonceStore.exist(eventActuator.getNonce())) {
          // TODO: handle expire
          byte[] txKeyBytes = nonceStore.getData(eventActuator.getNonce());
          byte[] txExtensionBytes = TransactionExtensionStore.getInstance().getData(txKeyBytes);
          if (txExtensionBytes != null) {
            try {
              CheckTransaction.getInstance()
                  .submitCheck(new TransactionExtensionCapsule(txExtensionBytes), 1);
            } catch (InvalidProtocolBufferException e) {
              // FIXME
              logger.error("retry fail: {}", e.getMessage(), e);
            }
          } else {
            logger.info("the retried nonce has succeeded");
          }
        } else {
          store.putData(eventActuator.getKey(), eventActuator.getMessage().toByteArray());
          if (eventActuator.getNonce() != null) {
            nonceStore.putData(eventActuator.getNonce(), eventActuator.getKey());
          }
          ActuatorRun.getInstance().start(eventActuator);
        }
        this.kfkConsumer.commit();
      }
    }
  }
}
