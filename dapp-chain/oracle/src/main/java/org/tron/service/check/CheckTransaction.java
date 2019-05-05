package org.tron.service.check;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;

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
      if (trxId.getTransactionId().equals("")) {
        return;
      }
      switch (trxId.getType()) {
        case MAIN_CHAIN:
          MainChainGatewayApi.checkTxInfo(trxId);
          break;
        case SIDE_CHAIN:
          SideChainGatewayApi.checkTxInfo(trxId);
          break;
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      instance.submitCheck(trxId);
    }
  }
}
