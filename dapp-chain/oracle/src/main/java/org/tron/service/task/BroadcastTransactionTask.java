package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.MessageCode;
import org.tron.common.config.SystemSetting;
import org.tron.db.Manager;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.Actuator.BroadcastRet;

@Slf4j(topic = "broadcastTxTask")
public class BroadcastTransactionTask {

  private static BroadcastTransactionTask instance = new BroadcastTransactionTask();

  public static BroadcastTransactionTask getInstance() {
    return instance;
  }

  private BroadcastTransactionTask() {
  }

  private final ScheduledExecutorService broadcastPool = Executors
      .newScheduledThreadPool(SystemSetting.BROADCAST_POOL_SIZE);

  void submitBroadcast(Actuator eventActuator, long delay) {
    if (logger.isInfoEnabled()) {
      logger.info("broadcast tx submit Broadcast txId is {} , delay is {} ",
          eventActuator.getTransactionExtensionCapsule().getTransactionId(), delay);
    }
    broadcastPool
        .schedule(() -> instance.broadcastTransaction(eventActuator), delay, TimeUnit.SECONDS);
  }

  private void broadcastTransaction(Actuator eventActuator) {
    BroadcastRet broadcastRet = eventActuator.broadcastTransactionExtensionCapsule();
    Manager.getInstance().setProcessBroadcasted(eventActuator.getNonceKey());
    String transactionId = eventActuator.getTransactionExtensionCapsule().getTransactionId();
    if (broadcastRet == BroadcastRet.SUCCESS) {
      CheckTransactionTask.getInstance()
          .submitCheck(eventActuator, 60);
    } else {
      String chain = eventActuator.getTaskEnum().name();
      if (broadcastRet == BroadcastRet.DONE) {
        Manager.getInstance().setProcessStatus(eventActuator.getNonceKey(), NonceStatus.SUCCESS);
        if (logger.isInfoEnabled()) {
          String msg = MessageCode.BROADCAST_TRANSACTION_SUCCESS
              .getMsg(chain, transactionId);
          logger.info(msg);
        }
      } else {
        String msg = MessageCode.BROADCAST_TRANSACTION_FAIL
            .getMsg(chain, transactionId);
        RetryTransactionTask.getInstance().processAndSubmit(eventActuator, msg);
      }
    }
  }

}

