package org.tron.service.capsule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TransactionExtension;

public class TransactionExtensionCapsule {

  private TransactionExtension.Builder instance;

  public TransactionExtensionCapsule(String nonceKey, Transaction transaction, long delay) {
    byte[] txId = Sha256Hash.hash(transaction.getRawData().toByteArray());
    instance = TransactionExtension.newBuilder()
        .setNonceKey(ByteString.copyFrom(ByteArray.fromString(nonceKey)))
        .setTransactionId(ByteString.copyFrom(txId)).setTransaction(transaction).setDelay(delay);
  }

  public TransactionExtensionCapsule(byte[] data) throws InvalidProtocolBufferException {
    this.instance = TransactionExtension.parseFrom(data).toBuilder();
  }

  public String getTransactionId() {
    return ByteArray.toHexString(instance.getTransactionId().toByteArray());
  }

  public byte[] getNonceKeyBytes() {
    return instance.getNonceKey().toByteArray();
  }

  public byte[] getData() {
    return this.instance.build().toByteArray();
  }

  public Transaction getTransaction() {
    return instance.getTransaction();
  }

  public long getDelay() {
    return instance.getDelay();
  }
}
