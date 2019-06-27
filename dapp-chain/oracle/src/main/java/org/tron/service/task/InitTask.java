package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.EventStore;
import org.tron.db.Manager;
import org.tron.db.TransactionExtensionStore;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC10Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC20Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC721Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRXActuator;
import org.tron.service.eventactuator.mainchain.MappingTRC20Actuator;
import org.tron.service.eventactuator.mainchain.MappingTRC721Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC10Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC20Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC721Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRXActuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC10Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC20Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC721Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRXActuator;

@Slf4j(topic = "task")
public class InitTask {

  public void batchProcessEventAndTx() {

    // process txs
    Set<ByteBuffer> allTxKeys = TransactionExtensionStore.getInstance().allKeys();
    for (ByteBuffer TxKey : allTxKeys) {
      byte[] event = EventStore.getInstance().getData(TxKey.array());
      if (event == null) {
        // impossible
        continue;
      }
      try {
        Actuator actuator = getActuatorByEventMsg(event);
        if (actuator == null) {
          continue;
        }
        Manager.getInstance().setProcessProcessing(actuator.getNonceKey());
        CheckTransactionTask.getInstance().submitCheck(actuator, 60);
      } catch (InvalidProtocolBufferException e) {
        logger.error("parse pb error", e);
      }
    }

    // process events
    Set<byte[]> allEvents = EventStore.getInstance().allValues();
    for (byte[] event : allEvents) {
      try {
        Actuator actuator = getActuatorByEventMsg(event);
        if (actuator == null || allTxKeys
            .contains(ByteBuffer.wrap(actuator.getNonceKey()).asReadOnlyBuffer())) {
          continue;
        }
        Manager.getInstance().setProcessProcessing(actuator.getNonceKey());
        CreateTransactionTask.getInstance().submitCreate(actuator);
      } catch (InvalidProtocolBufferException e) {
        logger.error("parse pb error", e);
      }
    }
  }

  public static Actuator getActuatorByEventMsg(byte[] data) throws InvalidProtocolBufferException {
    EventMsg eventMsg = EventMsg.parseFrom(data);
    switch (eventMsg.getType()) {
      case DEPOSIT_TRX_EVENT:
        return new DepositTRXActuator(eventMsg);
      case DEPOSIT_TRC10_EVENT:
        return new DepositTRC10Actuator(eventMsg);
      case DEPOSIT_TRC20_EVENT:
        return new DepositTRC20Actuator(eventMsg);
      case DEPOSIT_TRC721_EVENT:
        return new DepositTRC721Actuator(eventMsg);
      case WITHDRAW_TRX_EVENT:
        return new WithdrawTRXActuator(eventMsg);
      case WITHDRAW_TRC10_EVENT:
        return new WithdrawTRC10Actuator(eventMsg);
      case WITHDRAW_TRC20_EVENT:
        return new WithdrawTRC20Actuator(eventMsg);
      case WITHDRAW_TRC721_EVENT:
        return new WithdrawTRC721Actuator(eventMsg);
      case MULTISIGN_FOR_WITHDRAW_TRX_EVENT:
        return new MultiSignForWithdrawTRXActuator(eventMsg);
      case MULTISIGN_FOR_WITHDRAW_TRC10_EVENT:
        return new MultiSignForWithdrawTRC10Actuator(eventMsg);
      case MULTISIGN_FOR_WITHDRAW_TRC20_EVENT:
        return new MultiSignForWithdrawTRC20Actuator(eventMsg);
      case MULTISIGN_FOR_WITHDRAW_TRC721_EVENT:
        return new MultiSignForWithdrawTRC721Actuator(eventMsg);
      case MAPPING_TRC20:
        return new MappingTRC20Actuator(eventMsg);
      case MAPPING_TRC721:
        return new MappingTRC721Actuator(eventMsg);
      default:
        logger.warn("unknown event ");
        return null;
    }
  }
}
