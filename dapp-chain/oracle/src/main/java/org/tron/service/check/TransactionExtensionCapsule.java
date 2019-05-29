package org.tron.service.check;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.protos.Sidechain.TransactionExtension;

public class TransactionExtensionCapsule {

  private TransactionExtension.Builder instance;

  public TransactionExtensionCapsule(TaskEnum type, Transaction transaction) {
    byte[] trxId = Sha256Hash.hash(transaction.getRawData().toByteArray());
    instance = TransactionExtension.newBuilder().setTaskEnum(type)
        .setTransactionId(ByteString.copyFrom(trxId)).setTransaction(transaction);
  }

  public TransactionExtensionCapsule(Transaction transaction) {
    byte[] trxId = Sha256Hash.hash(transaction.getRawData().toByteArray());
    instance = TransactionExtension.newBuilder().setTransactionId(ByteString.copyFrom(trxId))
        .setTransaction(transaction);
  }

  public TransactionExtensionCapsule(byte[] data) throws InvalidProtocolBufferException {
    this.instance = TransactionExtension.parseFrom(data).toBuilder();
  }

  public TaskEnum getType() {
    return instance.getTaskEnum();
  }

  public void setType(TaskEnum chain) {
    instance.setTaskEnum(chain);
  }

  public String getTransactionId() {
    return ByteArray.toHexString(instance.getTransactionId().toByteArray());
  }

  public byte[] getTransactionIdBytes() {
    return instance.getTransactionId().toByteArray();
  }

  public byte[] getData() {
    return this.instance.build().toByteArray();
  }

  public Transaction getTransaction() {
    return instance.getTransaction();
  }
}
