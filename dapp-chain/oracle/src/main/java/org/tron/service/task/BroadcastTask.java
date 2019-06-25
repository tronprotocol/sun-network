package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.check.CheckTransaction;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "task")
public class BroadcastTask implements Runnable {

  private Actuator eventActuator;

  public BroadcastTask(Actuator eventActuator) {
    this.eventActuator = eventActuator;
  }

  @Override
  public void run() {
    boolean success = eventActuator.broadcastTransactionExtensionCapsule();
    if (success) {
      CheckTransaction.getInstance().submitCheck(eventActuator.getTransactionExtensionCapsule());
    } else {
      // FIXME: write fail to db
    }
  }
}
