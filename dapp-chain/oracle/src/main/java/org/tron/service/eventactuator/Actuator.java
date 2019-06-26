package org.tron.service.eventactuator;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.logger.LoggerOracle;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j(topic = "actuator")
public abstract class Actuator {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  protected TransactionExtensionCapsule transactionExtensionCapsule;

  public abstract EventMsg getMessage();

  public abstract EventType getType();

  public abstract CreateRet createTransactionExtensionCapsule();

  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    return this.transactionExtensionCapsule;
  }

  public BroadcastRet broadcastTransactionExtensionCapsule() {
    try {
      switch (transactionExtensionCapsule.getType()) {
        case MAIN_CHAIN:
          MainChainGatewayApi.broadcast(transactionExtensionCapsule.getTransaction());
        case SIDE_CHAIN:
          SideChainGatewayApi.broadcast(transactionExtensionCapsule.getTransaction());
      }
      return BroadcastRet.SUCCESS;
    } catch (Exception e) {
      //ERROR code
      loggerOracle
          .error("broadcast err txId is {}", transactionExtensionCapsule.getTransactionId(), e);
      return BroadcastRet.FAIL;
    }
  }

  public abstract byte[] getNonceKey();

  public abstract byte[] getNonce();

  public CheckTxRet checkTxInfo() {
    String transactionId = transactionExtensionCapsule.getTransactionId();
    try {
      switch (transactionExtensionCapsule.getType()) {
        case MAIN_CHAIN:
          MainChainGatewayApi.checkTxInfo(transactionId);
          break;
        case SIDE_CHAIN:
          SideChainGatewayApi.checkTxInfo(transactionId);
          break;
      }
      // success
      return CheckTxRet.SUCCESS;
    } catch (Exception e) {
      // fail
      loggerOracle.error("check err txId is {}", transactionId, e);
      return CheckTxRet.FAIL;
    }
  }

  public enum BroadcastRet {
    SUCCESS,
    FAIL,
    DONE
  }

  public enum CreateRet {
    SUCCESS,
    FAIL
  }

  public enum CheckTxRet {
    SUCCESS,
    FAIL
  }

}