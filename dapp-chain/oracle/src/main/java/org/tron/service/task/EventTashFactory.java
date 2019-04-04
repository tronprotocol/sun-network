package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.tron.service.eventenum.EventType;
import org.tron.service.task.mainchain.DepositTRXTask;
import org.tron.service.task.mainchain.TokenWithdrawnTask;

@Slf4j(topic = "task")
public class EventTashFactory {

  public static EventTask CreateTask(EventType eventSignature, JSONObject obj) {
    EventTask task;
    switch (eventSignature) {
      case TokenWithdrawn: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        JSONObject topicMap = (JSONObject) obj.get("topicMap");
        task = new TokenWithdrawnTask(topicMap.get("owner").toString(),
            dataMap.get("kind").toString(), dataMap.get("contractAddress").toString(),
            dataMap.get("value").toString());
        return task;
      }
      case TRC20Received: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DepositTRXTask(dataMap.get("from").toString(),
            dataMap.get("amount").toString());
        return task;
      }
      default:
        logger.info(String
            .format("event:%s,signature:%s.",
                obj.get("eventSignature").toString(), eventSignature.getSignature()));
    }
    return null;
  }
}
