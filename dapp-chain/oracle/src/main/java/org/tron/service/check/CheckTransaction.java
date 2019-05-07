package org.tron.service.check;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.ContractException;
import org.tron.common.exception.TxNotFoundException;
import org.tron.service.task.TaskEnum;

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

  public void submitCheck(TransactionId trxId) {
    syncExecutor
        .scheduleWithFixedDelay(() -> instance.checkTransactionId(trxId), 90000, 90000,
            TimeUnit.MILLISECONDS);
  }

  private void checkTransactionId(TransactionId trxId) {
    try {
      if (StringUtils.isEmpty(trxId.getTransactionId())) {
        return;
      }
      switch (trxId.getType()) {
        case MAIN_CHAIN:
          trxId.setType(TaskEnum.MAIN_CHAIN);
          MainChainGatewayApi.checkTxInfo(trxId);
          break;
        case SIDE_CHAIN:
          trxId.setType(TaskEnum.SIDE_CHAIN);
          SideChainGatewayApi.checkTxInfo(trxId);
          break;
      }
    } catch (TxNotFoundException e) {
      logger.error(e.getMessage());
      broadcastTransaction(trxId);
      instance.submitCheck(trxId);
    } catch (ContractException e) {
      logger.error(e.getMessage());
    }
  }

  private boolean broadcastTransaction(TransactionId trxId) {
    switch (trxId.getType()) {
      case MAIN_CHAIN:
        return MainChainGatewayApi.broadcast(trxId.getTransaction());
      case SIDE_CHAIN:
        return SideChainGatewayApi.broadcast(trxId.getTransaction());
    }
    return false;
  }
}
