package org.tron.db;

import org.tron.common.config.SystemSetting;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;

public class Manager {

  private static Manager ourInstance = new Manager();

  public static Manager getInstance() {
    return ourInstance;
  }

  private Manager() {
  }

  public void setProcessProcessing(byte[] nonceKeyBytes, byte[] msgBytes) {
    EventStore.getInstance().putData(nonceKeyBytes, msgBytes);
    NonceMsg nonceMsg = NonceMsg.newBuilder().setStatus(NonceStatus.PROCESSING)
        .setNextProcessTimestamp(System.currentTimeMillis() / 1000 +
            SystemSetting.RETRY_PROCESSING_INTERVAL).build();
    NonceStore.getInstance().putData(nonceKeyBytes, nonceMsg.toByteArray());
  }

  public void setProcessSuccess(byte[] nonceKeyBytes) {
    NonceMsg nonceMsg = NonceMsg.newBuilder().setStatus(NonceStatus.SUCCESS)
        .setNextProcessTimestamp(0).build();
    NonceStore.getInstance()
        .putData(nonceKeyBytes, nonceMsg.toByteArray());
    deleteEventAndTxStore(nonceKeyBytes);
  }

  public void setProcessFail(byte[] nonceKeyBytes) {
    NonceMsg nonceMsg = NonceMsg.newBuilder().setStatus(NonceStatus.FAIL)
        .setNextProcessTimestamp(0).build();
    NonceStore.getInstance()
        .putData(nonceKeyBytes, nonceMsg.toByteArray());
    deleteEventAndTxStore(nonceKeyBytes);
  }

  private void deleteEventAndTxStore(byte[] nonceKeyBytes) {
    EventStore.getInstance().deleteData(nonceKeyBytes);
    TransactionExtensionStore.getInstance().deleteData(nonceKeyBytes);
  }


}
