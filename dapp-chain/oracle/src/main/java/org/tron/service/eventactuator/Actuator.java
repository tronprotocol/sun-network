package org.tron.service.eventactuator;

import org.tron.common.exception.RpcConnectException;
import org.tron.service.check.TransactionExtensionCapsule;

public abstract class Actuator {

  protected TransactionExtensionCapsule transactionExtensionCapsule;

  public abstract TransactionExtensionCapsule createTransactionExtensionCapsule()
    throws RpcConnectException;

}
