package org.tron.service.eventactuator;

import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;

public abstract class Actuator {

  protected TransactionExtensionCapsule transactionExtensionCapsule;

  public abstract EventMsg getMessage();

  public abstract EventType getType();

  public abstract TransactionExtensionCapsule getTransactionExtensionCapsule();

  public boolean broadcastTransactionExtensionCapsule() {
    CheckTransaction.getInstance().broadcastTransaction(this.transactionExtensionCapsule);
    return true;
  }

  public abstract byte[] getNonceKey();

  public abstract byte[] getNonce();
}