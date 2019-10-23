package org.tron.service.task;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.common.MessageCode;
import org.tron.common.config.Args;
import org.tron.common.config.SystemSetting;
import org.tron.common.overlay.discover.node.NodeManager;
import org.tron.common.utils.ByteArray;
import org.tron.core.net.TronNetDelegate;
import org.tron.core.net.TronNetService;
import org.tron.core.net.message.EventNetMessage;
import org.tron.db.NonceStore;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

@Slf4j(topic = "eventTask")
public class EventTask {

  private KfkConsumer kfkConsumer;

  @Autowired
  private TronNetService tronNetService;
  @Autowired
  private TronNetDelegate tronNetDelegate;
  @Autowired
  private NodeManager nodeManager;
  private int minEffectiveConnection = Args.getInstance().getMinEffectiveConnection();

  public EventTask() {
    Args args = Args.getInstance();
    this.kfkConsumer = new KfkConsumer(args.getMainchainKafka(), args.getKafkaGroupId(),
        Arrays.asList("contractevent"), args.getKafkaConfig());
  }

  public void processEvent2() {
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
            // first time to receive this nonce
            // TODO: Implement actuator switch case here
            if (true) {
              signAndBroadcast(eventActuator);
            } else {
              processAndSubmit(eventActuator);
            }
          } else {
            // TODO: add retry function
            continue;
          }
          this.kfkConsumer.commit();
        }
      } catch (Exception e) {
        logger.error("in main loop: {}", e.getMessage(), e);
      }
    }
  }

  private void signAndBroadcast(Actuator eventActuator) {
    EventNetMessage netMessage = eventActuator.generateSignedEventMsg();
    broadcastEvent(netMessage);
  }

  public GrpcAPI.Return broadcastEvent(EventNetMessage signedEvent) {
    GrpcAPI.Return.Builder builder = GrpcAPI.Return.newBuilder();

    try {
      if (minEffectiveConnection != 0) {
        if (tronNetDelegate.getActivePeer().isEmpty()) {
          logger
              .warn("Broadcast event {} has failed, no connection.", signedEvent.getMessageId());
          return builder.setResult(false).setCode(response_code.NO_CONNECTION)
              .setMessage(ByteString.copyFromUtf8("no connection"))
              .build();
        }

        int count = (int) tronNetDelegate.getActivePeer().stream()
            .filter(p -> !p.isNeedSyncFromUs() && !p.isNeedSyncFromPeer())
            .count();

        if (count < minEffectiveConnection) {
          String info = "effective connection:" + count + " lt minEffectiveConnection:"
              + minEffectiveConnection;
          logger.warn("Broadcast event {} has failed, {}.", signedEvent.getMessageId(), info);
          return builder.setResult(false).setCode(response_code.NOT_ENOUGH_EFFECTIVE_CONNECTION)
              .setMessage(ByteString.copyFromUtf8(info)).build();
        }
      }

      tronNetService.broadcast(signedEvent);
      logger.info("Broadcast transaction {} successfully.", signedEvent.getMessageId());
      return builder.setResult(true).setCode(response_code.SUCCESS).build();
    } catch (Exception e) {
      logger.error("Broadcast transaction {} failed, {}.", signedEvent.getMessageId(),
          e.getMessage());
      return builder.setResult(false).setCode(response_code.OTHER_ERROR)
          .setMessage(ByteString.copyFromUtf8("other error : " + e.getMessage()))
          .build();
    }
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
      int retryTimesInStore = NonceMsg
          .parseFrom(NonceStore.getInstance().getData(actuator.getNonceKey())).getRetryTimes();
      int retryNew = (retryTimesInStore / SystemSetting.RETRY_TIMES_EPOCH_OFFSET
          + 1) * SystemSetting.RETRY_TIMES_EPOCH_OFFSET;
      actuator.setRetryTimes(retryNew);
    } catch (Exception e) {
      logger.error("setRetryTimesForUserRetry catch error! nouce = {}", actuator.getNonceKey(), e);
      throw e;
    }

  }
}
