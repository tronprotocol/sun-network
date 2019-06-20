package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.nio.ByteBuffer;
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
import org.tron.protos.Sidechain.NonceStatus;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.ActuatorRun;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

@Slf4j(topic = "eventTask")
public class EventTask {

  private KfkConsumer kfkConsumer;

  private EventStore eventStore;
  private NonceStore nonceStore;

  public EventTask() {

    this.kfkConsumer = new KfkConsumer(Args.getInstance().getMainchainKafka(),
        "Oracle_" + getOracleAddress(),
        Arrays.asList("contractevent"));
    this.eventStore = EventStore.getInstance();
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

        Actuator eventActuator = EventActuatorFactory.CreateActuator(obj);
        if (Objects.isNull(eventActuator)) {
          //Unrelated contract or event
          this.kfkConsumer.commit();
          continue;
        }

        byte[] nonceStatusBytes = nonceStore.getData(eventActuator.getNonceKey());
        if (nonceStatusBytes == null) {
          // receive this nonce firstly
          eventStore.putData(eventActuator.getNonceKey(), eventActuator.getMessage().toByteArray());
          nonceStore.putData(eventActuator.getNonceKey(),
              ByteBuffer.allocate(1).putInt(NonceStatus.PROCESSING_VALUE).array());
          ActuatorRun.getInstance().start(eventActuator);
        } else {
          NonceStatus nonceStatus = NonceStatus
              .forNumber(ByteBuffer.wrap(nonceStatusBytes).getInt());
          if (nonceStatus.equals(NonceStatus.SUCCESS)) {
            logger.info("the retried nonce has be executed successfully");
          } else {
            byte[] txExtensionBytes = TransactionExtensionStore.getInstance()
                .getData(eventActuator.getNonceKey());
            try {
              CheckTransaction.getInstance()
                  .submitCheck(new TransactionExtensionCapsule(txExtensionBytes), 1);
            } catch (InvalidProtocolBufferException e) {
              logger.error("retry fail: {}", e.getMessage(), e);
            }
          }
        }
        this.kfkConsumer.commit();
      }
    }
  }
}
