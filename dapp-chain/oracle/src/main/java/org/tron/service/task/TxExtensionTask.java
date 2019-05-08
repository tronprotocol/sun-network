package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxValidateException;
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
      // NOTE: http://106.39.105.178:8090/pages/viewpage.action?pageId=8992655 1.2
      // NOTE: have retried for 5 times in broadcastTransaction
      CheckTransaction.getInstance().sendAlert("1.2");
      logger.error(e.getMessage(), e);
    } catch (TxValidateException e) {
      // NOTE: http://106.39.105.178:8090/pages/viewpage.action?pageId=8992655 4.1
      CheckTransaction.getInstance().sendAlert("4.1");
      logger.error(e.getMessage(), e);
    }
  }
}
