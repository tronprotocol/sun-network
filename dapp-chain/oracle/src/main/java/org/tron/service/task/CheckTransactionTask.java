package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.SystemSetting;
import org.tron.common.logger.LoggerOracle;
import org.tron.db.Manager;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.Actuator.CheckTxRet;

@Slf4j
public class CheckTransactionTask {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  private static CheckTransactionTask instance = new CheckTransactionTask();

  public static CheckTransactionTask getInstance() {
    return instance;
  }

  private CheckTransactionTask() {
  }

  private final ScheduledExecutorService checkPool = Executors
      .newScheduledThreadPool(SystemSetting.CHECK_POOL_SIZE);

  public void submitCheck(Actuator eventActuator) {
    checkPool.schedule(() -> instance.checkTransaction(eventActuator), 60,
        TimeUnit.SECONDS);
  }

  private void checkTransaction(Actuator eventActuator) {
    CheckTxRet checkTxRet = eventActuator.checkTxInfo();
    if (checkTxRet == CheckTxRet.SUCCESS) {
      Manager.getInstance().setProcessSuccess(eventActuator.getNonceKey());
    } else {
      Manager.getInstance().setProcessFail(eventActuator.getNonceKey());
    }
  }
}
