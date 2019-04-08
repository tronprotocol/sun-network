package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.tron.service.eventenum.EventType;
import org.tron.service.task.mainchain.DepositTRC10Task;
import org.tron.service.task.mainchain.DepositTRC20Task;
import org.tron.service.task.mainchain.DepositTRC721Task;
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
      case Token10Withdrawn: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        JSONObject topicMap = (JSONObject) obj.get("topicMap");
        task = new TokenWithdrawnTask(topicMap.get("owner").toString(),
            dataMap.get("kind").toString(), dataMap.get("tokenId").toString(),
            dataMap.get("value").toString());
        return task;
      }
      case TRXReceived: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DepositTRXTask(dataMap.get("from").toString(),
            dataMap.get("amount").toString());
        return task;
      }
      case TRC10Received: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DepositTRC10Task(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("tokenId").toString());
        return task;
      }
      case TRC20Received: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DepositTRC20Task(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("contractAddress").toString());
        return task;
      }
      case TRC721Received: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DepositTRC721Task(dataMap.get("from").toString(),
            dataMap.get("uid").toString(), dataMap.get("contractAddress").toString());
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
