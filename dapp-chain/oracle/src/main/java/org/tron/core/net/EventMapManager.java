package org.tron.core.net;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.common.utils.ProtoBufUtils;
import org.tron.protos.Sidechain.EventNetMsg;

@Slf4j(topic = "net")
@Component
public class EventMapManager {

  public ConcurrentHashMap<String,ArrayList<byte[]>> eventMap = new ConcurrentHashMap<>();

  public EventMapManager(){ }

  public void addEventNetMessage(EventNetMsg eventNetMsg){
    String type = eventNetMsg.getRaw().getEventMsg().getType().getDescriptorForType().getName();
    String nonce = ProtoBufUtils.unpackNonce(eventNetMsg.getRaw().getEventMsg());
    String key = type + nonce;

    ArrayList<byte[]> list = eventMap.getOrDefault(key, new ArrayList<byte[]>());
    if(!list.contains(eventNetMsg.toByteArray())) {
      list.add(eventNetMsg.toByteArray());
    }
  }

}
