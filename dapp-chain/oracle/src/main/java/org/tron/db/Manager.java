package org.tron.db;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.SystemSetting;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;

@Slf4j(topic = "manager")
public class Manager {

  private static Manager ourInstance = new Manager();

  public static Manager getInstance() {
    return ourInstance;
  }

  private Manager() {
  }

  public void setProcessProcessing(byte[] nonceKeyBytes, byte[] msgBytes, int retryTimes) {
    // insert or set order:
    // 1. set nonce
    // 2. insert event
    // 3. insert tx (in other thread)
    setProcessProcessing(nonceKeyBytes, retryTimes);
    EventStore.getInstance().putData(nonceKeyBytes, msgBytes);
  }

  public void setProcessProcessing(byte[] nonceKeyBytes, int retryTimes) {
    NonceMsg nonceMsg = NonceMsg.newBuilder()
        .setRetryTimes(retryTimes)
        .setStatus(NonceStatus.PROCESSING)
        .setNextProcessTimestamp(System.currentTimeMillis() / 1000 +
            SystemSetting.RETRY_PROCESSING_INTERVAL).build();
    NonceStore.getInstance().putData(nonceKeyBytes, nonceMsg.toByteArray());
  }

  public void setProcessBroadcasted(byte[] nonceKeyBytes) {
    byte[] nonceMsgBytes = NonceStore.getInstance().getData(nonceKeyBytes);
    try {
      NonceMsg nonceMsg = NonceMsg.parseFrom(nonceMsgBytes).toBuilder()
          .setStatus(NonceStatus.BROADCASTED).build();
      NonceStore.getInstance().putData(nonceKeyBytes, nonceMsg.toByteArray());
    } catch (InvalidProtocolBufferException e) {
      logger.info("when set broadcasted, pb parse error");
    }
  }

  public void setProcessStatus(byte[] nonceKeyBytes, NonceStatus nonceStatus) {
    // delete or set order:
    // 1. delete tx store
    // 2. delete event store
    // 3. set nonce store
    TransactionExtensionStore.getInstance().deleteData(nonceKeyBytes);
    EventStore.getInstance().deleteData(nonceKeyBytes);
    NonceMsg nonceMsg = NonceMsg.newBuilder().setStatus(nonceStatus)
        .setNextProcessTimestamp(0).build();
    NonceStore.getInstance()
        .putData(nonceKeyBytes, nonceMsg.toByteArray());
  }

  public void setProcessRetry(byte[] nonceKeyBytes, int retryTimes) {
    // set order:
    // 1. delete tx store
    // 2. set nonce store
    TransactionExtensionStore.getInstance().deleteData(nonceKeyBytes);
    setProcessProcessing(nonceKeyBytes, retryTimes);
  }
}
