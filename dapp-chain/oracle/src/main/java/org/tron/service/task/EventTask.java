package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.tron.common.MessageCode;
import org.tron.common.config.Args;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
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
        Arrays.asList("solidityevent"), args.getKafkaConfig());
  }

  public void processEvent() {
    while (true) {
      try {
        ConsumerRecords<String, String> record = this.kfkConsumer.getRecord();
        for (ConsumerRecord<String, String> key : record) {

          Actuator eventActuator = EventActuatorFactory.CreateActuator(key.value());
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
              String chain = eventActuator.getTaskEnum().name();
              if (nonceMsg.getStatus() == NonceStatus.SUCCESS) {
                if (logger.isInfoEnabled()) {
                  String msg = MessageCode.NONCE_HAS_BE_SUCCEED
                      .getMsg(chain, ByteArray.toStr(eventActuator.getNonceKey()));
                  logger.info(msg);
                }
              } else if (nonceMsg.getStatus() == NonceStatus.FAIL) {
                setRetryTimesForUserRetry(eventActuator);
                processAndSubmit(eventActuator);
              } else {
                // processing or broadcasted
                if (System.currentTimeMillis() / 1000 >= nonceMsg.getNextProcessTimestamp()) {
                  setRetryTimesForUserRetry(eventActuator);
                  processAndSubmit(eventActuator);
                } else {
                  if (logger.isInfoEnabled()) {
                    String msg = MessageCode.NONCE_IS_PROCESSING
                        .getMsg(chain, ByteArray.toStr(eventActuator.getNonceKey()));
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
      } catch (Exception e) {
        logger.error("in main loop: {}", e.getMessage(), e);
      }
    }
  }

  private void processAndSubmit(Actuator eventActuator) {
    CreateTransactionTask.getInstance().submitCreate(eventActuator, 0);
  }

  private void setRetryTimesForUserRetry(Actuator actuator) throws InvalidProtocolBufferException {
    try {
      int retryTimesInStore = NonceMsg.parseFrom(NonceStore.getInstance().getData(actuator.getNonceKey())).getRetryTimes();
      int retryNew = (retryTimesInStore / SystemSetting.RETRY_TIMES_EPOCH_OFFSET
          + 1) * SystemSetting.RETRY_TIMES_EPOCH_OFFSET;
      actuator.setRetryTimes(retryNew);
    } catch (Exception e) {
      logger.error("setRetryTimesForUserRetry catch error! nouce = {}", actuator.getNonceKey(), e);
      throw e;
    }

  }
}
