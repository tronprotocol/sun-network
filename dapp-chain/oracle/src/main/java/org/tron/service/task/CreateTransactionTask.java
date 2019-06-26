package org.tron.service.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.Manager;
import org.tron.db.TransactionExtensionStore;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.Actuator.CreateRet;

@Slf4j(topic = "actuatorRun")
public class CreateTransactionTask {

  private static CreateTransactionTask instance = new CreateTransactionTask();

  public static CreateTransactionTask getInstance() {
    return instance;
  }

  private CreateTransactionTask() {
  }

  private final ScheduledExecutorService createPool = Executors.newScheduledThreadPool(100);

  private final TransactionExtensionStore transactionExtensionStore = TransactionExtensionStore
      .getInstance();

  public void submitCreate(Actuator eventActuator) {
    createPool.submit(() -> instance.createTransaction(eventActuator));
  }

  private void createTransaction(Actuator eventActuator) {
    CreateRet createRet = eventActuator
        .createTransactionExtensionCapsule();
    if (createRet == CreateRet.SUCCESS) {
      TransactionExtensionCapsule txExtensionCapsule = eventActuator
          .getTransactionExtensionCapsule();
      this.transactionExtensionStore
          .putData(eventActuator.getNonceKey(), txExtensionCapsule.getData());
      // TODO: and store_success status
      BroadcastTransactionTask.getInstance()
          .submitBroadcast(eventActuator, txExtensionCapsule.getDelay());
    } else {
      byte[] nonceKeyBytes = eventActuator.getNonceKey();
      Manager.getInstance().setProcessFail(nonceKeyBytes);
    }
  }
}
