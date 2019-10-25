package org.tron.core.net;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.common.utils.ProtoBufUtils;
import org.tron.core.net.message.EventNetMessage;
import org.tron.protos.Sidechain.EventNetMsg;
import org.tron.service.task.CreateTransactionTask;

@Slf4j(topic = "net")
@Component
public class EventMapManager {

  /**
   *  eventMap
   *  ArrayList<EventNetMessage> list is a sorted list order by timestamp
   */
  public ConcurrentHashMap<String,ArrayList<EventNetMessage>> eventMap = new ConcurrentHashMap<>();

  public EventMapManager(){ }

  public String addEventNetMessage(EventNetMessage targetMessage){
    EventNetMsg targetProtoMsg = targetMessage.getEventNetMsg();

    // generate key
    String type = targetProtoMsg.getRaw().getEventMsg().getType().getDescriptorForType().getName();
    String nonce = ProtoBufUtils.unpackNonce(targetProtoMsg.getRaw().getEventMsg());
    String key = type + nonce;

    ArrayList<EventNetMessage> list = eventMap.getOrDefault(key, new ArrayList<>());

    // add element sorted by time stamp
    long targetTimeStamp = targetProtoMsg.getRaw().getTimestamp();
    int insertIndex = -1;

    for(int i = 0; i< list.size(); i++) {
      EventNetMessage msg = list.get(i);
      // avoid duplicate msg
      if (msg.getEventNetMsg().getRaw().getTimestamp() < targetTimeStamp) {
        //do nothing.
      }
      else if (msg.equals(targetMessage)) {
        break;
      }
      else {
        insertIndex = i;
        break;
      }
    }

    if (insertIndex != -1) {
      list.add(insertIndex, targetMessage);
    }
    return key;
  }
}
