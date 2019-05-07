package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.TransactionExtentionStore;
import org.tron.protos.Sidechain.TransactionExtension;

@Slf4j(topic = "task")
public class InitTask {

  public void batchProcessTxInDb() {
    TransactionExtentionStore store = TransactionExtentionStore.getInstance();

    // TODO: 遍历
    byte[] txIdBytes = new byte[0];
    byte[] txExtensionBytes = store.getData(txIdBytes);
    try {
      TransactionExtension txExtension = TransactionExtension.parseFrom(txExtensionBytes);
      // executor.execute(eventTask);
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
  }
}
