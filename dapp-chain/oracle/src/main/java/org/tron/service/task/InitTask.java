package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.tron.db.EventStore;
import org.tron.db.TransactionExtensionStore;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.ActuatorRun;
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

  public void batchProcessTxInDb() {

    Set<byte[]> allTxs = TransactionExtensionStore.getInstance().allValues();
    for (byte[] txExtensionBytes : allTxs) {
      try {
        TransactionExtensionCapsule txExtensionCapsule = new TransactionExtensionCapsule(
            txExtensionBytes);
        logger.info("init check tx id:{}", txExtensionCapsule.getTransactionId());
        CheckTransaction.getInstance().submitCheck(txExtensionCapsule, 1);
      } catch (InvalidProtocolBufferException e) {
        logger.error(e.getMessage(), e);
      }
    }

    Set<byte[]> allEvents = EventStore.getInstance().allValues();
    Set<String> allTxKeyHexStrings = TransactionExtensionStore.getInstance().allKeyHexStrings();
    for (byte[] event : allEvents) {
      try {
        Actuator actuator = getActuatorByEventMsg(event);
        if (actuator == null || allTxKeyHexStrings
            .contains(Hex.toHexString(actuator.getNonceKey()))) {
          continue;
        }
        ActuatorRun.getInstance().start(actuator);
      } catch (InvalidProtocolBufferException e) {
        // FIXME
        logger.error(e.getMessage(), e);
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
        return null;
    }
  }

}
