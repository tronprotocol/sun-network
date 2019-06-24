package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
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
      CheckTransaction.getInstance().broadcastTransaction(this.txExtensionCapsule);
      CheckTransaction.getInstance().submitCheck(this.txExtensionCapsule, 1);
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
}
