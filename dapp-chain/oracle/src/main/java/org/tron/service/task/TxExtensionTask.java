package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxExpiredException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.utils.AlertUtil;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j(topic = "task")
public class TxExtensionTask implements Runnable {

  private TransactionExtensionCapsule txExtensionCapsule;

  public TxExtensionTask(TransactionExtensionCapsule txExtensionCapsule) {
    this.txExtensionCapsule = txExtensionCapsule;
  }

  @Override
  public void run() {
    try {
      broadcastTransaction(this.txExtensionCapsule);
      CheckTransaction.getInstance().submitCheck(this.txExtensionCapsule);
    } catch (RpcConnectException e) {
      AlertUtil.sendAlert(
          String.format("tx: %s, rpc connect fail", txExtensionCapsule.getTransactionId()));
      logger.error(e.getMessage(), e);
    } catch (TxValidateException e) {
      AlertUtil.sendAlert(String.format("tx: %s, validation fail, will not exist on chain",
          txExtensionCapsule.getTransactionId()));
      logger.error(e.getMessage(), e);
    } catch (TxExpiredException e) {
      AlertUtil.sendAlert(String.format("tx: %s, expired", txExtensionCapsule.getTransactionId()));
      logger.error(e.getMessage(), e);
    }
  }

  private boolean broadcastTransaction(TransactionExtensionCapsule txExtensionCapsule)
      throws RpcConnectException, TxValidateException, TxExpiredException {
    switch (txExtensionCapsule.getType()) {
      case MAIN_CHAIN:
        return MainChainGatewayApi.broadcast(txExtensionCapsule.getTransaction());
      case SIDE_CHAIN:
        return SideChainGatewayApi.broadcast(txExtensionCapsule.getTransaction());
      default:
        return false;
    }
  }
}
