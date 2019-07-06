package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tron.common.MessageCode;
import org.tron.common.config.Args;
import org.tron.common.utils.ByteArray;
import org.tron.db.Manager;
import org.tron.db.NonceStore;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

@Slf4j(topic = "eventTask")
public class EventTask {

  private KfkConsumer kfkConsumer;

  public EventTask() {
    Args args = Args.getInstance();
    this.kfkConsumer = new KfkConsumer(args.getMainchainKafka(), args.getKafkaGroupId(),
        Arrays.asList("contractevent"), args.getKafukaConfig());
  }

  public void processEvent() {
    while (true) {
      ConsumerRecords<String, String> record = this.kfkConsumer.getRecord();
      for (ConsumerRecord<String, String> key : record) {
        JSONObject obj = (JSONObject) JSONValue.parse(key.value());

        Actuator eventActuator = EventActuatorFactory.CreateActuator(obj);
        if (Objects.isNull(eventActuator)) {
          //Unrelated contract or event
          this.kfkConsumer.commit();
          continue;
        }

        byte[] nonceMsgBytes = NonceStore.getInstance().getData(eventActuator.getNonceKey());
        if (nonceMsgBytes == null) {
          // receive this nonce firstly
          processAndSubmit(eventActuator);
        } else {
          try {
            NonceMsg nonceMsg = NonceMsg.parseFrom(nonceMsgBytes);
            if (nonceMsg.getStatus() == NonceStatus.SUCCESS) {
              if (logger.isInfoEnabled()) {
                String msg = MessageCode.NONCE_HAS_BE_SUCCEED
                    .getMsg(ByteArray.toStr(eventActuator.getNonce()));
                logger.info(msg);
              }
            } else if (nonceMsg.getStatus() == NonceStatus.FAIL) {
              processAndSubmit(eventActuator);
            } else {
              // processing or broadcasted
              if (System.currentTimeMillis() / 1000 >= nonceMsg.getNextProcessTimestamp()) {
                processAndSubmit(eventActuator);
              } else {
                if (logger.isInfoEnabled()) {
                  String msg = MessageCode.NONCE_IS_PROCESSING
                      .getMsg(ByteArray.toStr(eventActuator.getNonce()));
                  logger.info(msg);
                }
              }
            }
          } catch (InvalidProtocolBufferException e) {
            logger.error("retry fail: {}", e.getMessage(), e);
          }
        }
        this.kfkConsumer.commit();
      }
    }
  }

  private void processAndSubmit(Actuator eventActuator) {
    Manager.getInstance().setProcessProcessing(eventActuator.getNonceKey(),
        eventActuator.getMessage().toByteArray());
    CreateTransactionTask.getInstance().submitCreate(eventActuator);
  }
}
