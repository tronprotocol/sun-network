package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.exception.ErrorCode;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.AlertUtil;
import org.tron.db.Manager;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.Actuator.BroadcastRet;

@Slf4j
public class BroadcastTransactionTask {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  private static BroadcastTransactionTask instance = new BroadcastTransactionTask();

  public static BroadcastTransactionTask getInstance() {
    return instance;
  }

  private BroadcastTransactionTask() {
  }

  private final ScheduledExecutorService broadcastPool = Executors.newScheduledThreadPool(100);

  void submitBroadcast(Actuator eventActuator, long delay) {
    broadcastPool
        .schedule(() -> instance.broadcastTransaction(eventActuator), delay, TimeUnit.SECONDS);
  }

  private void broadcastTransaction(Actuator eventActuator) {
    BroadcastRet broadcastRet = eventActuator.broadcastTransactionExtensionCapsule();
    String transactionId = eventActuator.getTransactionExtensionCapsule().getTransactionId();
    if (broadcastRet == BroadcastRet.SUCCESS) {
      CheckTransactionTask.getInstance()
          .submitCheck(eventActuator);
    } else if (broadcastRet == BroadcastRet.DONE) {
      Manager.getInstance().setProcessSuccess(eventActuator.getNonceKey());
      String msg = ErrorCode.BROADCAST_TRANSACTION_SUCCESS.getMsg(transactionId);
      loggerOracle.info(msg);
    } else {
      Manager.getInstance().setProcessFail(eventActuator.getNonceKey());
      String msg = ErrorCode.BROADCAST_TRANSACTION_FAIL.getMsg(transactionId);
      AlertUtil.sendAlert(msg);
    }
  }

}

