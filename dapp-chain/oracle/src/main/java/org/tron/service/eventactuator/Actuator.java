package org.tron.service.eventactuator;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.EventMsg.TaskEnum;
import org.tron.service.capsule.TransactionExtensionCapsule;

@Slf4j(topic = "actuator")
public abstract class Actuator {

  private int retryTimes;

  @Setter
  protected TransactionExtensionCapsule transactionExtensionCapsule;

  public abstract EventMsg getMessage();

  public abstract EventType getType();

  public abstract TaskEnum getTaskEnum();

  public abstract Actuator getNextActuator();

  public abstract CreateRet createTransactionExtensionCapsule();

  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    return this.transactionExtensionCapsule;
  }

  public BroadcastRet broadcastTransactionExtensionCapsule() {
    try {
      if (getTaskEnum() == TaskEnum.MAIN_CHAIN) {
        MainChainGatewayApi.broadcast(transactionExtensionCapsule.getTransaction());
      } else {
        SideChainGatewayApi.broadcast(transactionExtensionCapsule.getTransaction());
      }
      return BroadcastRet.SUCCESS;
    } catch (Exception e) {
      //ERROR code
      logger
          .error("broadcast err txId is {}", transactionExtensionCapsule.getTransactionId(), e);
      return BroadcastRet.FAIL;
    }
  }

  public abstract byte[] getNonceKey();

  public abstract byte[] getNonce();

  public CheckTxRet checkTxInfo() {
    String transactionId = transactionExtensionCapsule.getTransactionId();
    try {
      if (getTaskEnum() == TaskEnum.MAIN_CHAIN) {
        MainChainGatewayApi.checkTxInfo(transactionId);
      } else {
        SideChainGatewayApi.checkTxInfo(transactionId);
      }
      // success
      return CheckTxRet.SUCCESS;
    } catch (Exception e) {
      // fail
      logger.error("check err txId is {}", transactionId, e);
      return CheckTxRet.FAIL;
    }
  }

  public int getRetryTimes() {
    return retryTimes;
  }

  public void setRetryTimes(int retryTimes) {
    this.retryTimes = retryTimes;
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