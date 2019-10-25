package org.tron.service.eventmsgactuator;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.tron.common.config.Args;
import org.tron.service.eventenum.MainEventType;
import org.tron.service.eventenum.SideEventType;
import org.tron.service.eventmsgactuator.mainchain.DepositTRC10Actuator;
import org.tron.service.eventmsgactuator.mainchain.DepositTRC20Actuator;
import org.tron.service.eventmsgactuator.mainchain.DepositTRC721Actuator;
import org.tron.service.eventmsgactuator.mainchain.DepositTRXActuator;
import org.tron.service.eventmsgactuator.mainchain.MappingTRC20Actuator;
import org.tron.service.eventmsgactuator.mainchain.MappingTRC721Actuator;
import org.tron.service.eventmsgactuator.sidechain.WithdrawTRC10Actuator;
import org.tron.service.eventmsgactuator.sidechain.WithdrawTRC20Actuator;
import org.tron.service.eventmsgactuator.sidechain.WithdrawTRC721Actuator;
import org.tron.service.eventmsgactuator.sidechain.WithdrawTRXActuator;

@Slf4j(topic = "task")
public class EventMsgActuatorFactory {

  public static MsgActuator CreateActuator(String eventStr) {
    try {

      JSONObject obj = (JSONObject) JSONValue.parse(eventStr);
      Args args = Args.getInstance();
      if (Objects.isNull(obj) || Objects.isNull(obj.get("contractAddress"))) {
        return null;
      }
      if (obj.get("contractAddress").equals(args.getMainchainGatewayStr())) {
        return createMainChainActuator(obj);
      } else if (obj.get("contractAddress").equals(args.getSidechainGatewayStr())) {
        return createSideChainActuator(obj);
      }
      logger.debug("unknown contract address:{}", obj.get("contractAddress"));
    } catch (Exception e) {
      logger.info("{} create actuator err", eventStr);
      logger.error("{}", e);
      return null;
    }
    return null;
  }

  private static MsgActuator createMainChainActuator(JSONObject obj) {
    MsgActuator task;
    MainEventType eventSignature = MainEventType
        .fromSignature(obj.get("eventSignature").toString());
    JSONObject dataMap = (JSONObject) obj.get("dataMap");

    switch (eventSignature) {
      case TRX_RECEIVED: {
        task = new DepositTRXActuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("nonce").toString());
        return task;
      }
      case TRC10_RECEIVED: {
        task = new DepositTRC10Actuator(dataMap.get("from").toString(),
            dataMap.get("tokenId").toString(), dataMap.get("tokenValue").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case TRC20_RECEIVED: {
        task = new DepositTRC20Actuator(dataMap.get("from").toString(),
            dataMap.get("contractAddress").toString(), dataMap.get("value").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case TRC721_RECEIVED: {
        task = new DepositTRC721Actuator(dataMap.get("from").toString(),
            dataMap.get("contractAddress").toString(), dataMap.get("uid").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case TRC20_MAPPING: {
        task = new MappingTRC20Actuator(dataMap.get("contractAddress").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case TRC721_MAPPING: {
        task = new MappingTRC721Actuator(dataMap.get("contractAddress").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      default: {
        if (logger.isInfoEnabled()) {
          logger.info("main chain event:{},signature:{}.", obj.get("eventSignature").toString(),
              eventSignature.getSignature());
        }
      }
    }
    return null;
  }

  private static MsgActuator createSideChainActuator(JSONObject obj) {
    MsgActuator task;
    SideEventType eventType = SideEventType
        .fromMethod(obj.get("eventSignature").toString());
    switch (eventType) {
      case WITHDRAW_TRC10: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC10Actuator(dataMap.get("from").toString(),
            dataMap.get("tokenId").toString(), dataMap.get("value").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case WITHDRAW_TRC20: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC20Actuator(dataMap.get("from").toString(),
            dataMap.get("mainChainAddress").toString(), dataMap.get("value").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case WITHDRAW_TRC721: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRC721Actuator(dataMap.get("from").toString(),
            dataMap.get("mainChainAddress").toString(), dataMap.get("uId").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case WITHDRAW_TRX: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new WithdrawTRXActuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("nonce").toString());
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TRC10: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTRC10Actuator(dataMap.get("from").toString(),
            dataMap.get("tokenId").toString(), dataMap.get("value").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TRC20: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTRC20Actuator(dataMap.get("from").toString(),
            dataMap.get("mainChainAddress").toString(), dataMap.get("value").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TRC721: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTRC721Actuator(dataMap.get("from").toString(),
            dataMap.get("mainChainAddress").toString(), dataMap.get("uId").toString(),
            dataMap.get("nonce").toString());
        return task;
      }
      case MULTISIGN_FOR_WITHDRAW_TRX: {
        JSONObject dataMap = (JSONObject) obj.get("dataMap");
        task = new MultiSignForWithdrawTRXActuator(dataMap.get("from").toString(),
            dataMap.get("value").toString(), dataMap.get("nonce").toString());
        return task;
      }
      default: {
        if (logger.isInfoEnabled()) {
          logger.info("side chain event:{},signature:{}.", obj.get("eventSignature").toString(),
              eventType.getMethod());
        }
      }
    }
    return null;
  }
}
