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
import org.tron.service.task.sidechain.DeployDAppTRC20AndMappingTask;
import org.tron.service.task.sidechain.DeployDAppTRC721AndMappingTask;
import org.tron.service.task.sidechain.WithdrawTRC10Task;
import org.tron.service.task.sidechain.WithdrawTRC20Task;
import org.tron.service.task.sidechain.WithdrawTRC721Task;
import org.tron.service.task.sidechain.WithdrawTRXTask;

@Slf4j(topic = "task")
public class EventTaskFactory {

  static EventTask CreateTask(TaskEnum taskType,
      JSONObject obj) {
    try {

      switch (taskType) {
        case MAIN_CHAIN:
          return CreateMainChainTask(obj);
        case SIDE_CHAIN:
          return createSideChainTask(obj);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return null;
  }

  private static EventTask CreateMainChainTask(JSONObject obj) {
    EventTask task;
    MainEventType eventSignature = MainEventType
        .fromSignature(obj.get("eventSignature").toString());
    JSONObject dataMap = (JSONObject) obj.get("dataMap");
    JSONObject topicMap = (JSONObject) obj.get("topicMap");

    switch (eventSignature) {
      case TOKEN_WITHDRAWN: {
        task = new TokenWithdrawnTask(topicMap.get("owner").toString(),
            dataMap.get("kind").toString(), dataMap.get("contractAddress").toString(),
            dataMap.get("value").toString());
        return task;
      }
      case TOKEN10_WITHDRAWN: {
        task = new TokenWithdrawnTask(topicMap.get("owner").toString(),
            dataMap.get("kind").toString(), dataMap.get("tokenId").toString(),
            dataMap.get("value").toString());
        return task;
      }
      case TRX_RECEIVED: {
        task = new DepositTRXTask(dataMap.get("from").toString(),
            dataMap.get("amount").toString());
        return task;
      }
      case TRC10_RECEIVED: {
        task = new DepositTRC10Task(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("tokenId").toString());
        return task;
      }
      case TRC20_RECEIVED: {
        task = new DepositTRC20Task(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("contractAddress").toString());
        return task;
      }
      case TRC721_RECEIVED: {
        task = new DepositTRC721Task(dataMap.get("from").toString(),
            dataMap.get("uid").toString(), dataMap.get("contractAddress").toString());
        return task;
      }
      default:
        logger.info("event:{},signature:{}.", obj.get("eventSignature").toString(),
            eventSignature.getSignature());

    }
    return null;
  }

  private static EventTask createSideChainTask(JSONObject obj) {
    EventTask task;
    SideEventType eventType = SideEventType
        .fromMethod(obj.get("eventSignature").toString());
    switch (eventType) {
      case DEPLOY_DAPPTRC20_AND_MAPPING: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DeployDAppTRC20AndMappingTask(dataMap.get("developer").toString(),
            dataMap.get("mainChainAddress").toString(), dataMap.get("sideChainAddress").toString());
        return task;
      }
      case DEPLOY_DAPPTRC721_AND_MAPPING: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new DeployDAppTRC721AndMappingTask(dataMap.get("developer").toString(),
            dataMap.get("mainChainAddress").toString(), dataMap.get("sideChainAddress").toString());
        return task;
      }
      case WITHDRAW_TRC10: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC10Task(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("trc10").toString(),
            dataMap.get("txData").toString());
        return task;
      }
      case WITHDRAW_TRC20: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC20Task(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("mainChainAddress").toString(),
            dataMap.get("txData").toString());
        return task;
      }
      case WITHDRAW_TRC721: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC721Task(dataMap.get("from").toString(),
            dataMap.get("tokenId").toString(), dataMap.get("mainChainAddress").toString(),
            dataMap.get("txData").toString());
        return task;
      }
      case WITHDRAW_TRX: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRXTask(dataMap.get("from").toString(), dataMap.get("value").toString(),
            dataMap.get("txData").toString());
        return task;
      }
      default:
        logger.info("event:{},signature:{}.",
            obj.get("eventSignature").toString(), eventType.getMethod());
    }
    return null;
  }
}
