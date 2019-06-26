package org.tron.service.eventactuator;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.ErrorCode;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.AlertUtil;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
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
      if (transactionExtensionCapsule.getType() == TaskEnum.MAIN_CHAIN) {
        MainChainGatewayApi.broadcast(transactionExtensionCapsule.getTransaction());
      } else {
        SideChainGatewayApi.broadcast(transactionExtensionCapsule.getTransaction());
      }
      return BroadcastRet.SUCCESS;
    } catch (Exception e) {
      //ERROR code
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
      String msg = ErrorCode.getCheckTransactionSuccess(transactionId);
      loggerOracle.info(msg);
      return CheckTxRet.SUCCESS;
    } catch (Exception e) {
      // fail
      String msg = ErrorCode.getCheckTransactionFail(transactionId, e.getMessage());
      AlertUtil.sendAlert(msg, e);
      loggerOracle.error(msg, e);
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