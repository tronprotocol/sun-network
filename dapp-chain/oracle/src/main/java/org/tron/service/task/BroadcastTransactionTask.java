package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.tron.db.Manager;
import org.tron.service.check.CheckTransaction;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "task")
public class BroadcastTransactionTask implements Runnable {

  private Actuator eventActuator;

  public BroadcastTransactionTask(Actuator eventActuator) {
    this.eventActuator = eventActuator;
  }

  @Override
  public void run() {
    boolean success = eventActuator.broadcastTransactionExtensionCapsule();
    if (success) {
      CheckTransaction.getInstance().submitCheck(eventActuator.getTransactionExtensionCapsule());
    } else {
      // fail
      // FIXME: write fail to db
      Manager.getInstance().FinishProcessNonce(eventActuator.getNonceKey(), 1);
    }
  }
}
