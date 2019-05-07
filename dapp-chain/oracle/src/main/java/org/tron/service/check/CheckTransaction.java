package org.tron.service.check;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.exception.TxRollbackException;
import org.tron.common.exception.TxFailException;
import org.tron.db.TransactionExtentionStore;

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

  public void submitCheck(TransactionExtensionCapsule txExtensionCapsule) {
    // TODO: from solidity node
    syncExecutor
      .scheduleWithFixedDelay(() -> instance.checkTransactionId(txExtensionCapsule), 60000, 60000,
        TimeUnit.MILLISECONDS);
  }

  private void checkTransactionId(TransactionExtensionCapsule txExtensionCapsule) {
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
      TransactionExtentionStore.getInstance().deleteData(txExtensionCapsule.getTransactionIdBytes());
    } catch (TxRollbackException e) {
      // TODO: 4.2 oracle执行的交易被回退
      // TODO: 等待60s后从solidity节点获取交易状态，被回退后重试5次，仍然没有进固化块则告警、排查问题(第一次执行完就会移动kafka的offset)
      logger.error(e.getMessage());
      try {
        broadcastTransaction(txExtensionCapsule);
      } catch (RpcConnectException e1) {
        e1.printStackTrace();
      } catch (TxValidateException e1) {
        e1.printStackTrace();
      }
      instance.submitCheck(txExtensionCapsule);
    } catch (TxFailException e) {
      // TODO: 5. 接收到事件，发送的交易，执行失败
      logger.error(e.getMessage());
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
