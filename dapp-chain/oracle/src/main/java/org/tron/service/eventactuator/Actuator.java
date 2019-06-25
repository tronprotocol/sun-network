package org.tron.service.eventactuator;

import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.task.CheckTransactionTask;

public abstract class Actuator {

  protected TransactionExtensionCapsule transactionExtensionCapsule;

  public abstract EventMsg getMessage();

  public abstract EventType getType();

  public abstract CreateRet createTransactionExtensionCapsule();

  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    return this.transactionExtensionCapsule;
  }

  public BroadcastRet broadcastTransactionExtensionCapsule() {
    CheckTransactionTask.getInstance().broadcastTransaction(this.transactionExtensionCapsule);
    return BroadcastRet.SUCCESS;
  }

  public abstract byte[] getNonceKey();

  public abstract byte[] getNonce();

  public enum BroadcastRet {
    SUCCESS,
    FAIL,
    DONE
  }

  public enum CreateRet {
    SUCCESS,
    FAIL
  }
}