package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.ErrorCode;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.AlertUtil;
import org.tron.db.Manager;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j
public class CheckTransactionTask {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  private static CheckTransactionTask instance = new CheckTransactionTask();

  public static CheckTransactionTask getInstance() {
    return instance;
  }

  private CheckTransactionTask() {
  }

  private final ScheduledExecutorService checkPool = Executors.newScheduledThreadPool(100);

  public void submitCheck(TransactionExtensionCapsule txExtensionCapsule) {
    checkPool.schedule(() -> instance.checkTransaction(txExtensionCapsule), 60,
        TimeUnit.SECONDS);
  }

  private void checkTransaction(TransactionExtensionCapsule txExtensionCapsule) {

    String transactionId = txExtensionCapsule.getTransactionId();
    if (StringUtils.isEmpty(transactionId)) {
      return;
    }
    try {
      switch (txExtensionCapsule.getType()) {
        case MAIN_CHAIN:
          MainChainGatewayApi.checkTxInfo(transactionId);
          break;
        case SIDE_CHAIN:
          SideChainGatewayApi.checkTxInfo(transactionId);
          break;
      }
      // success
      byte[] nonceKeyBytes = txExtensionCapsule.getNonceKeyBytes();
      Manager.getInstance().setProcessSuccess(nonceKeyBytes);
      String msg = ErrorCode.getCheckTransactionSuccess(transactionId);
      loggerOracle.info(msg);

    } catch (Exception e) {
      // fail
      // FIXME: do not catch in this task level, just return ret
      byte[] nonceKeyBytes = txExtensionCapsule.getNonceKeyBytes();
      Manager.getInstance().setProcessFail(nonceKeyBytes);
      String msg = ErrorCode.getCheckTransactionFail(transactionId);
      AlertUtil.sendAlert(msg);
    }
  }

}
