package org.tron.service.eventactuator;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.AlertUtil;
import org.tron.db.EventStore;
import org.tron.db.NonceStore;
import org.tron.db.TransactionExtensionStore;
import org.tron.protos.Sidechain.NonceStatus;
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

  private final ExecutorService executor = Executors.newFixedThreadPool(100);

  private final TransactionExtensionStore transactionExtensionStore = TransactionExtensionStore
      .getInstance();

  public void start(Actuator eventActuator) {
    executor.submit(() -> {
      TransactionExtensionCapsule txExtensionCapsule = null;
      try {
        txExtensionCapsule = eventActuator.getTransactionExtensionCapsule();
      } catch (RpcConnectException e) {
        // TODO
        String msg = "create transaction extension capsule fail";
        AlertUtil.sendAlert(msg);
        logger.error(msg, e);
        return;
      }
      if (txExtensionCapsule == null) {//TODO move
        byte[] nonceKeyBytes = eventActuator.getNonceKey();
        //TODO ByteBuffer.allocate(4).putInt(NonceStatus.SUCCESS_VALUE).array()
        NonceStore.getInstance()
            .putData(nonceKeyBytes,
                ByteBuffer.allocate(4).putInt(NonceStatus.SUCCESS_VALUE).array());
        EventStore.getInstance().deleteData(nonceKeyBytes);
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
