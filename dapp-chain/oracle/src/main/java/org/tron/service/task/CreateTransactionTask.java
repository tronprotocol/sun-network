package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.MessageCode;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
import org.tron.db.Manager;
import org.tron.db.TransactionExtensionStore;
import org.tron.service.capsule.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.Actuator.CreateRet;

@Slf4j(topic = "createTxTask")
public class CreateTransactionTask {

  private static CreateTransactionTask instance = new CreateTransactionTask();

  public static CreateTransactionTask getInstance() {
    return instance;
  }

  private CreateTransactionTask() {
  }

  private final ScheduledExecutorService createPool = Executors
      .newScheduledThreadPool(SystemSetting.CREATE_POOL_SIZE);

  private final TransactionExtensionStore transactionExtensionStore = TransactionExtensionStore
      .getInstance();

  void submitCreate(Actuator eventActuator, long delay) {
    if (logger.isInfoEnabled()) {
      logger.info("create tx task submit check nonceKey is {}  ",
          ByteArray.toStr(eventActuator.getNonceKey()));
    }
    createPool.schedule(() -> instance.createTransaction(eventActuator), delay, TimeUnit.SECONDS);
  }

  private void createTransaction(Actuator eventActuator) {

    try {
      if (!Manager.getInstance().setProcessProcessing(eventActuator.getNonceKey(),
          eventActuator.getMessage().toByteArray(), eventActuator.getRetryTimes())) {
        logger.info("createTransaction setProcessProcessing fail, return, nouce = {}",
            ByteArray.toStr(eventActuator.getNonceKey()));
        return;
      }

      CreateRet createRet = eventActuator.createTransactionExtensionCapsule();
      String chain = eventActuator.getTaskEnum().name();
      if (createRet == CreateRet.SUCCESS) {
        TransactionExtensionCapsule txExtensionCapsule = eventActuator
            .getTransactionExtensionCapsule();
        this.transactionExtensionStore
            .putData(eventActuator.getNonceKey(), txExtensionCapsule.getData());

        BroadcastTransactionTask.getInstance()
            .submitBroadcast(eventActuator, txExtensionCapsule.getDelay());
        if (logger.isInfoEnabled()) {
          String msg = MessageCode.CREATE_TRANSACTION_SUCCESS
              .getMsg(chain, txExtensionCapsule.getTransactionId());
          logger.info(msg);
        }
      } else {
        String msg = MessageCode.CREATE_TRANSACTION_FAIL
            .getMsg(chain, ByteArray.toStr(eventActuator.getNonceKey()));
        RetryTransactionTask.getInstance().processAndSubmit(eventActuator, msg);

      }
    } catch (Exception e) {
      logger.error("createTransaction catch error! nouce = {}",
          ByteArray.toStr(eventActuator.getNonceKey()), e);
      Manager.getInstance()
          .setProcessFail(eventActuator.getNonceKey(), eventActuator.getRetryTimes());
    }
  }
}
