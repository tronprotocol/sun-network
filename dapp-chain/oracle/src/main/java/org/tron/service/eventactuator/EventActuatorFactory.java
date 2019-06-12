package org.tron.service.eventactuator;

import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.eventactuator.mainchain.DepositTRC10Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC20Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC721Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRXActuator;
import org.tron.service.eventactuator.mainchain.MappingTRC20Actuator;
import org.tron.service.eventactuator.mainchain.MappingTRC721Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC10Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRXActuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTokenActuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC10Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC20Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC721Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRXActuator;
import org.tron.service.eventenum.MainEventType;
import org.tron.service.eventenum.SideEventType;

@Slf4j(topic = "task")
public class EventActuatorFactory {

  public static Actuator CreateActuator(TaskEnum taskType,
      JSONObject obj) {
    try {

      switch (taskType) {
        case MAIN_CHAIN:
          return createMainChainActuator(obj);
        case SIDE_CHAIN:
          return createSideChainActuator(obj);
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return null;
  }

  private static Actuator createMainChainActuator(JSONObject obj) {
    Actuator task;
    MainEventType eventSignature = MainEventType
        .fromSignature(obj.get("eventSignature").toString());
    JSONObject dataMap = (JSONObject) obj.get("dataMap");
    JSONObject topicMap = (JSONObject) obj.get("topicMap");

    switch (eventSignature) {
      case TRX_RECEIVED: {
        String txId = obj.get("transactionId").toString();
        task = new DepositTRXActuator(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), txId);
        return task;
      }
      case TRC10_RECEIVED: {
        String txId = obj.get("transactionId").toString();
        task = new DepositTRC10Actuator(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("tokenId").toString(), txId);
        return task;
      }
      case TRC20_RECEIVED: {
        String txId = obj.get("transactionId").toString();
        task = new DepositTRC20Actuator(dataMap.get("from").toString(),
            dataMap.get("amount").toString(), dataMap.get("contractAddress").toString(), txId);
        return task;
      }
      case TRC721_RECEIVED: {
        String txId = obj.get("transactionId").toString();
        task = new DepositTRC721Actuator(dataMap.get("from").toString(),
            dataMap.get("uid").toString(), dataMap.get("contractAddress").toString(), txId);
        return task;
      }
      case TRC20_MAPPING: {
        String txId = obj.get("transactionId").toString();
        task = new MappingTRC20Actuator(dataMap.get("contractAddress").toString(), txId);
        return task;
      }
      case TRC721_MAPPING: {
        String txId = obj.get("transactionId").toString();
        task = new MappingTRC721Actuator(dataMap.get("contractAddress").toString(), txId);
        return task;
      }
      default: {
        logger.warn("event:{},signature:{}.", obj.get("eventSignature").toString(),
            eventSignature.getSignature());
      }
    }
    return null;
  }

  private static Actuator createSideChainActuator(JSONObject obj) {
    Actuator task;
    SideEventType eventType = SideEventType
        .fromMethod(obj.get("eventSignature").toString());
    switch (eventType) {
      case WITHDRAW_TRC10: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC10Actuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("trc10").toString(),
            dataMap.get("userSign").toString(), txId);
        return task;
      }
      case WITHDRAW_TRC20: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC20Actuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("mainChainAddress").toString(),
            dataMap.get("userSign").toString(), txId);
        return task;
      }
      case WITHDRAW_TRC721: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC721Actuator(dataMap.get("from").toString(),
            dataMap.get("tokenId").toString(), dataMap.get("mainChainAddress").toString(),
            dataMap.get("userSign").toString(), txId);
        return task;
      }
      case WITHDRAW_TRX: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRXActuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(),
            dataMap.get("userSign").toString(), txId);
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TRX: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTRXActuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("userSign").toString(),
            dataMap.get("dataHash").toString(), dataMap.get("txId").toString(), txId);
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TRC10: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTRC10Actuator(dataMap.get("from").toString(),
            dataMap.get("trc10").toString(), dataMap.get("value").toString(),
            dataMap.get("userSign").toString(), dataMap.get("dataHash").toString(),
            dataMap.get("txId").toString(), txId);
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TOKEN: {
        String txId = obj.get("transactionId").toString();
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTokenActuator(dataMap.get("from").toString(),
            dataMap.get("mainChainAddress").toString(),
            dataMap.get("valueOrTokenId").toString(), dataMap.get("_type").toString(),
            dataMap.get("userSign").toString(), dataMap.get("dataHash").toString(),
            dataMap.get("txId").toString(), txId);
        return task;
      }
      default: {
        logger.info("event:{},signature:{}.",
            obj.get("eventSignature").toString(), eventType.getMethod());
      }
    }
    return null;
  }
}
