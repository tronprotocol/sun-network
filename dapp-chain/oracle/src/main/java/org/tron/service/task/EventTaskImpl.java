package org.tron.service.task;

import org.tron.service.check.TransactionExtension;

public abstract class EventTaskImpl implements EventTask {

  protected TransactionExtension transactionExtension;

  public abstract TransactionExtension getTransactionExtension();

}
