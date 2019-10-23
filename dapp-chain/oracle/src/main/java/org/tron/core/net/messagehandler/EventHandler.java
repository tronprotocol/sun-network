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

@Slf4j(topic = "net")
@Component
public class EventHandler implements TronMsgHandler {

  private final ExecutorService eventProcessor = Executors.newFixedThreadPool(SystemSetting.CREATE_POOL_SIZE);

  @Autowired
  EventMapManager eventMapManager;

  @Override
  public void processMessage(PeerConnection peer, TronMessage msg) throws P2pException {
    EventNetMessage message = (EventNetMessage) msg;
    EventNetMsg eventNetMsg = message.getEventNetMsg();

    eventProcessor.submit(() -> checkAndStoreSignedMessage(eventNetMsg));

  }

  public void checkAndStoreSignedMessage(EventNetMsg eventNetMsg){
    try {
      if(!validateSignature()){
        return;
      }
      eventMapManager.addEventNetMessage(eventNetMsg);
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
