package org.tron.service.check;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxFailException;
import org.tron.common.exception.TxRollbackException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.utils.AlertUtil;
import org.tron.db.EventStore;
import org.tron.db.TransactionExtensionStore;

@Slf4j
public class CheckTransaction {

  private static CheckTransaction instance = new CheckTransaction();

  public static CheckTransaction getInstance() {
    return instance;
  }

  private CheckTransaction() {
  }

  private final ScheduledExecutorService syncExecutor = Executors
      .newScheduledThreadPool(100);

  public void submitCheck(TransactionExtensionCapsule txExtensionCapsule, int submitCnt) {
    // TODO: from solidity node
    syncExecutor
        .scheduleWithFixedDelay(() -> instance.checkTransaction(txExtensionCapsule, submitCnt),
            60000,
            60000, TimeUnit.MILLISECONDS);
  }

  private void checkTransaction(TransactionExtensionCapsule txExtensionCapsule, int checkCnt) {
    try {
      if (StringUtils.isEmpty(txExtensionCapsule.getTransactionId())) {
        return;
      }
      switch (txExtensionCapsule.getType()) {
        case MAIN_CHAIN:
          MainChainGatewayApi.checkTxInfo(txExtensionCapsule);
          break;
        case SIDE_CHAIN:
          SideChainGatewayApi.checkTxInfo(txExtensionCapsule);
          break;
      }
      EventStore.getInstance()
          .deleteData(txExtensionCapsule.getEventTransactionIdBytes());
      TransactionExtensionStore.getInstance()
          .deleteData(txExtensionCapsule.getEventTransactionIdBytes());
    } catch (TxRollbackException e) {
      // NOTE: http://106.39.105.178:8090/pages/viewpage.action?pageId=8992655 4.2
      logger.error(e.getMessage());
      if (checkCnt > 5) {
        AlertUtil.sendAlert("4.2, checkTransaction exceeds 5 times");
        logger.error("checkTransaction exceeds 5 times");
      } else {
        try {
          broadcastTransaction(txExtensionCapsule);
        } catch (RpcConnectException e1) {
          // NOTE: http://106.39.105.178:8090/pages/viewpage.action?pageId=8992655 1.2
          // NOTE: have retried for 5 times in broadcastTransaction
          AlertUtil.sendAlert("1.2");
          logger.error(e1.getMessage(), e1);
          return;
        } catch (TxValidateException e1) {
          // NOTE: http://106.39.105.178:8090/pages/viewpage.action?pageId=8992655 4.1
          AlertUtil.sendAlert("4.1");
          logger.error(e1.getMessage(), e1);
          return;
        }
        instance.submitCheck(txExtensionCapsule, checkCnt + 1);
      }
    } catch (TxFailException e) {
      // NOTE: http://106.39.105.178:8090/pages/viewpage.action?pageId=8992655 5.1 5.2 5.3
      AlertUtil.sendAlert("5.1 5.2 5.3");
      logger.error(e.getMessage(), e);
    }
  }

  public boolean broadcastTransaction(TransactionExtensionCapsule txExtensionCapsule)
      throws RpcConnectException, TxValidateException {
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
