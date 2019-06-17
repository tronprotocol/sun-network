package org.tron.service.task;

import java.util.Arrays;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.tron.common.config.Args;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.WalletUtil;
import org.tron.db.EventStore;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.ActuatorRun;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.kafka.KfkConsumer;

@Slf4j
public class EventTask {

  private KfkConsumer kfkConsumer;

  @Autowired
  private EventStore store;
  private String mainGateway;
  private String sideGateway;

  public EventTask(String mainGateway, String sideGateway) {
    this.mainGateway = mainGateway;
    this.sideGateway = sideGateway;
    this.kfkConsumer = new KfkConsumer(Args.getInstance().getMainchainKafka(),
        "Oracle_" + getOracleAddress(),
        Arrays.asList("contractevent"));
  }

  private String getOracleAddress() {
    return WalletUtil.encode58Check(
        ECKey.fromPrivate(Args.getInstance().getOraclePrivateKey()).getAddress());
  }

  public void processEvent() {
    new Thread(() -> {
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
          store.putData(eventActuator.getKey(), eventActuator.getMessage().toByteArray());
          this.kfkConsumer.commit();

          ActuatorRun.getInstance().start(eventActuator);
        }
      }
    }).start();

  }
}
