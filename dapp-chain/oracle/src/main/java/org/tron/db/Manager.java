package org.tron.db;

import java.nio.ByteBuffer;

public class Manager {

  private static Manager ourInstance = new Manager();

  public static Manager getInstance() {
    return ourInstance;
  }

  private Manager() {
  }

  public void FinishProcessNonce(byte[] nonceKeyBytes, int status) {
    NonceStore.getInstance()
        .putData(nonceKeyBytes, ByteBuffer.allocate(4).putInt(status).array());
    EventStore.getInstance().deleteData(nonceKeyBytes);
    TransactionExtensionStore.getInstance().deleteData(nonceKeyBytes);
  }
}
