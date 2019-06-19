package org.tron.service.eventactuator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.AlertUtil;
import org.tron.db.TransactionExtensionStore;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.task.TxExtensionTask;

public class ActuatorRun {

  private static ActuatorRun instance = new ActuatorRun();

  public static ActuatorRun getInstance() {
    return instance;
  }

  private ActuatorRun() {
  }

  private final ExecutorService executor = Executors.newFixedThreadPool(5);

  private final TransactionExtensionStore transactionExtensionStore = TransactionExtensionStore
      .getInstance();

  public void start(Actuator eventActuator) {
    executor.submit(() -> {
      TransactionExtensionCapsule txExtensionCapsule = null;
      try {
        txExtensionCapsule = eventActuator.createTransactionExtensionCapsule();
      } catch (RpcConnectException e) {
        AlertUtil.sendAlert("createTransactionExtensionCapsule fail, system exit");
        System.exit(1);
      }
      if (txExtensionCapsule == null) {
        return;
      }

      if (!this.transactionExtensionStore.exist(eventActuator.getNonceKey())) {
        this.transactionExtensionStore
            .putData(eventActuator.getNonceKey(), txExtensionCapsule.getData());
      }
      executor.execute(new TxExtensionTask(txExtensionCapsule));
    });
  }

}
