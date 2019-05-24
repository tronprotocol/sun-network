package org.tron.service.eventactuator;

import lombok.Getter;
import org.tron.common.exception.RpcConnectException;
import org.tron.service.check.TransactionExtensionCapsule;

public abstract class Actuator {

  protected TransactionExtensionCapsule transactionExtensionCapsule;
  @Getter
  protected String txId;

  public abstract TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException;

}
