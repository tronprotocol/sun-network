package org.tron.service.eventactuator;

import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.service.check.TransactionExtensionCapsule;

public abstract class Actuator {

  protected TransactionExtensionCapsule transactionExtensionCapsule;
  protected EventType type;

  public abstract EventMsg getMessage();

  public abstract TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException;


  public abstract byte[] getKey();
}
