package org.tron.service.check;

import lombok.Getter;
import lombok.Setter;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.task.TaskEnum;

public class TransactionExtention {

  @Getter
  @Setter
  private TaskEnum type;
  @Getter
  @Setter
  private String transactionId;
  @Getter
  @Setter
  private Transaction transaction;

  public TransactionExtention(TaskEnum type, Transaction transaction) {
    this.type = type;
    this.transactionId = ByteArray
        .toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    this.transaction = transaction;
  }

  public TransactionExtention(Transaction transaction) {
    this.transactionId = ByteArray
        .toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    this.transaction = transaction;
  }
}
