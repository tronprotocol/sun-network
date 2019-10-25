package org.tron.core.net.messagehandler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.common.config.SystemSetting;
import org.tron.common.exception.P2pException;
import org.tron.common.utils.ProtoBufUtils;
import org.tron.core.net.EventMapManager;
import org.tron.core.net.message.EventNetMessage;
import org.tron.core.net.message.TronMessage;
import org.tron.core.net.peer.PeerConnection;
import org.tron.protos.Sidechain.EventNetMsg;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.EventActuatorFactory;
import org.tron.service.task.CreateTransactionTask;

@Slf4j(topic = "net")
@Component
public class EventHandler implements TronMsgHandler {
  private int oracleNumbers = 4;

  private final ExecutorService eventProcessor = Executors.newFixedThreadPool(SystemSetting.CREATE_POOL_SIZE);

  @Autowired
  EventMapManager eventMapManager;

  @Override
  public void processMessage(PeerConnection peer, TronMessage msg) throws P2pException {
    EventNetMessage message = (EventNetMessage) msg;
    eventProcessor.submit(() -> processMessageByThread(message));
  }

  public void processMessageByThread(EventNetMessage eventNetMessage){
    try {
      if(!validateSignature()){
        return;
      }
      String key = eventMapManager.addEventNetMessage(eventNetMessage);
      if (eventMapManager.eventMap.get(key).size() == oracleNumbers * 2 / 3) {
        Actuator eventActuator = EventActuatorFactory.CreateActuatorByEventMap(eventMapManager.eventMap.get(key));
        CreateTransactionTask.getInstance().submitCreate(eventActuator, 0);
      }
    }
    catch (Exception e) {
      //TODO exception handling
      return;
    }


  }

  public boolean validateSignature(){
    //TODO
    return true;
  }
}
