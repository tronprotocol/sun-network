package org.tron.service.eventactuator;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.EventStore;
import org.tron.db.TransactionExtensionStore;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.task.BroadcastTask;

@Slf4j(topic = "actuatorRun")
public class ActuatorRun {

  private static ActuatorRun instance = new ActuatorRun();

  public static ActuatorRun getInstance() {
    return instance;
  }

  private ActuatorRun() {
  }

  private final ScheduledExecutorService broadcastPool = Executors.newScheduledThreadPool(100);

  private final TransactionExtensionStore transactionExtensionStore = TransactionExtensionStore
      .getInstance();

  public void start(Actuator eventActuator) {
    broadcastPool.submit(() -> {
      TransactionExtensionCapsule txExtensionCapsule = eventActuator
          .getTransactionExtensionCapsule();
      if (txExtensionCapsule == null) {
        // TODO: fail
        byte[] nonceKeyBytes = eventActuator.getNonceKey();
//        NonceStore.getInstance()
//            .putData(nonceKeyBytes,
//                ByteBuffer.allocate(4).putInt(NonceStatus.SUCCESS_VALUE).array());
        EventStore.getInstance().deleteData(nonceKeyBytes);
      } else {
        this.transactionExtensionStore
            .putData(eventActuator.getNonceKey(), txExtensionCapsule.getData());
        // TODO: and store_ssuccess status

        broadcastPool.schedule(new BroadcastTask(eventActuator), txExtensionCapsule.getDelay(),
            TimeUnit.SECONDS);
      }
    });
  }
}
