package org.tron.service.eventactuator.msgactuator;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.service.eventactuator.MsgActuator;
import org.tron.service.eventactuator.msgactuator.mainchain.DepositTRC10MsgActuator;
import org.tron.service.eventactuator.msgactuator.mainchain.DepositTRC20MsgActuator;
import org.tron.service.eventactuator.msgactuator.mainchain.DepositTRC721MsgActuator;
import org.tron.service.eventactuator.msgactuator.mainchain.DepositTRXMsgActuator;
import org.tron.service.eventactuator.msgactuator.mainchain.MappingTRC20MsgActuator;
import org.tron.service.eventactuator.msgactuator.mainchain.MappingTRC721MsgActuator;
import org.tron.service.eventactuator.msgactuator.sidechain.WithdrawTRC10MsgActuator;
import org.tron.service.eventactuator.msgactuator.sidechain.WithdrawTRC20MsgActuator;
import org.tron.service.eventactuator.msgactuator.sidechain.WithdrawTRC721MsgActuator;
import org.tron.service.eventactuator.msgactuator.sidechain.WithdrawTRXMsgActuator;

@Slf4j(topic = "task")
public class EventMsgActuatorFactory {

  public static MsgActuator CreateActuator(EventMsg eventMsg)
      throws InvalidProtocolBufferException {
    switch (eventMsg.getType()) {
      case DEPOSIT_TRX_EVENT:
        return new DepositTRXMsgActuator(eventMsg);
      case DEPOSIT_TRC10_EVENT:
        return new DepositTRC10MsgActuator(eventMsg);
      case DEPOSIT_TRC20_EVENT:
        return new DepositTRC20MsgActuator(eventMsg);
      case DEPOSIT_TRC721_EVENT:
        return new DepositTRC721MsgActuator(eventMsg);
      case WITHDRAW_TRX_EVENT:
        return new WithdrawTRXMsgActuator(eventMsg);
      case WITHDRAW_TRC10_EVENT:
        return new WithdrawTRC10MsgActuator(eventMsg);
      case WITHDRAW_TRC20_EVENT:
        return new WithdrawTRC20MsgActuator(eventMsg);
      case WITHDRAW_TRC721_EVENT:
        return new WithdrawTRC721MsgActuator(eventMsg);
      case MAPPING_TRC20:
        return new MappingTRC20MsgActuator(eventMsg);
      case MAPPING_TRC721:
        return new MappingTRC721MsgActuator(eventMsg);
      default:
        logger.warn("unknown event ");
        return null;
    }
  }
}
