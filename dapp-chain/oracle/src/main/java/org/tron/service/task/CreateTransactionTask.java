package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.MessageCode;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.AlertUtil;
import org.tron.common.utils.ByteArray;
import org.tron.db.Manager;
import org.tron.db.TransactionExtensionStore;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;
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

  void submitCreate(Actuator eventActuator) {
    if (logger.isInfoEnabled()) {
      logger.info("create tx task submit check nonceKey is {}  ",
          ByteArray.toStr(eventActuator.getNonceKey()));
    }
    createPool.submit(() -> instance.createTransaction(eventActuator));
  }

  private void createTransaction(Actuator eventActuator) {
    CreateRet createRet = eventActuator.createTransactionExtensionCapsule();
    if (createRet == CreateRet.SUCCESS) {
      TransactionExtensionCapsule txExtensionCapsule = eventActuator
          .getTransactionExtensionCapsule();
      this.transactionExtensionStore
          .putData(eventActuator.getNonceKey(), txExtensionCapsule.getData());
      BroadcastTransactionTask.getInstance()
          .submitBroadcast(eventActuator, txExtensionCapsule.getDelay());
      if (logger.isInfoEnabled()) {
        String msg = MessageCode.CREATE_TRANSACTION_SUCCESS
            .getMsg(eventActuator.getType().name(), txExtensionCapsule.getTransactionId());
        logger.info(msg);
      }
    } else {
      Manager.getInstance().setProcessStatus(eventActuator.getNonceKey(), NonceStatus.FAIL);
      String msg = MessageCode.CREATE_TRANSACTION_FAIL
          .getMsg(eventActuator.getType().name(), ByteArray.toStr(eventActuator.getNonceKey()));
      AlertUtil.sendAlert(msg);
    }
  }
}
