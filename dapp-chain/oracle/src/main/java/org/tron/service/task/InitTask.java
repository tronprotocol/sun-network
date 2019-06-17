package org.tron.service.task;

import static org.tron.protos.Protocol.Transaction.Contract.ContractType.AccountUpdateContract;
import static org.tron.protos.Protocol.Transaction.Contract.ContractType.TransferAssetContract;
import static org.tron.protos.Protocol.Transaction.Contract.ContractType.TransferContract;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.db.EventStore;
import org.tron.db.TransactionExtensionStore;
import org.tron.protos.Protocol.Transaction.Contract;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.ActuatorRun;
import org.tron.service.eventactuator.mainchain.DepositTRC10Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC20Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRC721Actuator;
import org.tron.service.eventactuator.mainchain.DepositTRXActuator;
import org.tron.service.eventactuator.sidechain.DeployDAppTRC20AndMappingActuator;
import org.tron.service.eventactuator.sidechain.DeployDAppTRC721AndMappingActuator;
import org.tron.service.eventactuator.sidechain.MultiSignForMappingActuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC10Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRXActuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTokenActuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC10Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC20Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRC721Actuator;
import org.tron.service.eventactuator.sidechain.WithdrawTRXActuator;

@Slf4j(topic = "task")
public class InitTask {

  private ExecutorService executor;

  @Autowired
  private CheckTransaction checkTransaction;

  @Autowired
  private TransactionExtensionStore transactionExtensionStore;

  @Autowired
  private EventStore eventStore;

  public InitTask(int fixedThreads) {
    this.executor = Executors.newFixedThreadPool(fixedThreads);
  }

  public void batchProcessTxInDb() {

    Set<byte[]> allTxs = transactionExtensionStore.allValues();
    for (byte[] txExtensionBytes : allTxs) {
      try {
        checkTransaction
            .submitCheck(new TransactionExtensionCapsule(txExtensionBytes), 1);
      } catch (InvalidProtocolBufferException e) {
        // FIXME
        logger.error(e.getMessage(), e);
      }
    }

    Set<byte[]> allEvents = eventStore.allValues();
    Set<byte[]> allTxKeys = transactionExtensionStore.allKeys();
    for (byte[] event : allEvents) {
      try {
        Actuator actuator = getActuatorByEventMsg(event);
        if (actuator == null || allTxKeys.contains(actuator.getKey())) {
          continue;
        }
        ActuatorRun.getInstance().start(actuator);
      } catch (InvalidProtocolBufferException e) {
        // FIXME
        logger.error(e.getMessage(), e);
      }
    }
  }

  private static Actuator getActuatorByEventMsg(byte[] data) throws InvalidProtocolBufferException {
    EventMsg eventMsg =  EventMsg.parseFrom(data);
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
      case MULTISIGN_FOR_WITHDRAW_TOKEN_EVENT:
        return new MultiSignForWithdrawTokenActuator(eventMsg);
      case MULTISIGN_FOR_MAPPING_EVENT:
        return new MultiSignForMappingActuator(eventMsg);
      case DEPLOY_DAPPTRC20_AND_MAPPING_EVENT:
        return new DeployDAppTRC20AndMappingActuator(eventMsg);
      case DEPLOY_DAPPTRC721_AND_MAPPING_EVENT:
        return new DeployDAppTRC721AndMappingActuator(eventMsg);
      default:
        return null;
    }
  }

}
