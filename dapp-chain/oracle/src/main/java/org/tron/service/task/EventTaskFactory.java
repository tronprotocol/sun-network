package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.tron.service.eventenum.MainEventType;
import org.tron.service.eventenum.SideEventType;
import org.tron.service.task.mainchain.DepositTRC10Task;
import org.tron.service.task.mainchain.DepositTRC20Task;
import org.tron.service.task.mainchain.DepositTRC721Task;
import org.tron.service.task.mainchain.DepositTRXTask;
import org.tron.service.task.mainchain.TokenWithdrawnTask;

@Slf4j(topic = "task")
public class EventTaskFactory {

  static EventTask CreateTask(TaskEnum taskType,
      JSONObject obj) {
    switch (taskType) {
      case MAIN_CHAIN:
        return CreateMainChainTask(obj);
      case SIDE_CHAIN:
        return CreateSideChainTask(obj);
    }
    return null;
  }

  private static EventTask CreateMainChainTask(JSONObject obj) {
    EventTask task;
    SideEventType eventSignature = SideEventType
        .fromSignature(obj.get("eventSignature").toString());
    JSONObject dataMap = (JSONObject) obj.get("dataMap");
    JSONObject topicMap = (JSONObject) obj.get("topicMap");
    switch (eventSignature) {
      case TokenWithdrawn: {
        task = new TokenWithdrawnTask(topicMap.get("owner").toString(),
            dataMap.get("kind").toString(), dataMap.get("contractAddress").toString(),
            dataMap.get("value").toString());
        return task;
      }
      case Token10Withdrawn: {
        task = new TokenWithdrawnTask(topicMap.get("owner").toString(),
            dataMap.get("kind").toString(), dataMap.get("tokenId").toString(),
            dataMap.get("value").toString());
        return task;
      }
      case TRXReceived: {
        task = new DepositTRXTask(dataMap.get("from").toString(),
            dataMap.get("amount").toString());
        return task;
      }
      case TRC10Received: {
        task = new DepositTRC10Task(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("tokenId").toString());
        return task;
      }
      case TRC20Received: {
        task = new DepositTRC20Task(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("contractAddress").toString());
        return task;
      }
      case TRC721Received: {
        task = new DepositTRC721Task(dataMap.get("from").toString(),
            dataMap.get("uid").toString(), dataMap.get("contractAddress").toString());
        return task;
      }
      default:
        logger.info(String
            .format("event:{},signature:{}.",
                obj.get("eventSignature").toString(), eventSignature.getSignature()));
    }
    return null;
  }

  static EventTask CreateSideChainTask(JSONObject obj) {
    EventTask task;
    MainEventType eventSignature = MainEventType
        .fromSignature(obj.get("eventSignature").toString());
    switch (eventSignature) {
      case TRXReceived: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DepositTRXTask(dataMap.get("from").toString(),
            dataMap.get("amount").toString());
        return task;
      }
      default:
        logger.info(String
            .format("event:{},signature:{}.",
                obj.get("eventSignature").toString(), eventSignature.getSignature()));
    }
    return null;
  }
}
