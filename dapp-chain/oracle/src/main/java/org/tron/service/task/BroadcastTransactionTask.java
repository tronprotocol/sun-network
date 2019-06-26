package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.SystemSetting;
import org.tron.common.logger.LoggerOracle;
import org.tron.db.Manager;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;
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

  private final ScheduledExecutorService broadcastPool = Executors
      .newScheduledThreadPool(SystemSetting.BROADCAST_POOL_SIZE);

  void submitBroadcast(Actuator eventActuator, long delay) {
    broadcastPool
        .schedule(() -> instance.broadcastTransaction(eventActuator), delay, TimeUnit.SECONDS);
  }

  private void broadcastTransaction(Actuator eventActuator) {
    BroadcastRet broadcastRet = eventActuator.broadcastTransactionExtensionCapsule();
    if (broadcastRet == BroadcastRet.SUCCESS) {
      CheckTransactionTask.getInstance()
          .submitCheck(eventActuator, 60);
    } else if (broadcastRet == BroadcastRet.DONE) {
      Manager.getInstance().setProcessStatus(eventActuator.getNonceKey(), NonceStatus.SUCCESS);
    } else {
      Manager.getInstance().setProcessStatus(eventActuator.getNonceKey(), NonceStatus.FAIL);
    }
  }

}

