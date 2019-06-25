package org.tron.service.eventactuator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.EventStore;
import org.tron.db.TransactionExtensionStore;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.task.TxExtensionTask;

@Slf4j(topic = "actuatorRun")
public class ActuatorRun {

  private static ActuatorRun instance = new ActuatorRun();

  public static ActuatorRun getInstance() {
    return instance;
  }

  private ActuatorRun() {
  }

  private final ExecutorService broadcastExecutor = Executors.newFixedThreadPool(100);

  private final TransactionExtensionStore transactionExtensionStore = TransactionExtensionStore
      .getInstance();

  public void start(Actuator eventActuator) {
    broadcastExecutor.submit(() -> {
      TransactionExtensionCapsule txExtensionCapsule = eventActuator
          .createTransactionExtensionCapsule();
      if (txExtensionCapsule == null) {
        // TODO: fail
        byte[] nonceKeyBytes = eventActuator.getNonceKey();
//        NonceStore.getInstance()
//            .putData(nonceKeyBytes,
//                ByteBuffer.allocate(4).putInt(NonceStatus.SUCCESS_VALUE).array());
        EventStore.getInstance().deleteData(nonceKeyBytes);
        return;
      }

      this.transactionExtensionStore
          .putData(eventActuator.getNonceKey(), txExtensionCapsule.getData());
      // TODO: and store_ssuccess status

      // TODO: 延迟提交
      broadcastExecutor.execute(new TxExtensionTask(txExtensionCapsule));
    });
  }

}
