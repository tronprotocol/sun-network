package org.tron.service.task;

import org.tron.service.check.TransactionExtention;

public abstract class EventTaskImpl implements EventTask {

  protected TransactionExtention transactionExtention;

  public abstract TransactionExtention getTransactionExtention();

}
