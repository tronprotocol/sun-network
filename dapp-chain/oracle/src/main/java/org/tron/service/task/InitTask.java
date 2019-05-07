package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.TransactionExtentionStore;
import org.tron.protos.Sidechain.TransactionExtension;

@Slf4j(topic = "task")
public class InitTask {

  public void batchProcessTxInDb() {
    TransactionExtentionStore store = TransactionExtentionStore.getInstance();

    LinkedHashMap<byte[], byte[]> allTx = store.getAllData();
    for (Entry<byte[], byte[]> entry : allTx.entrySet()) {
      byte[] txExtensionBytes = entry.getValue();
      try {
        TransactionExtension txExtension = TransactionExtension.parseFrom(txExtensionBytes);
        // executor.execute(eventTask);
      } catch (InvalidProtocolBufferException e) {
        e.printStackTrace();
      }

    }
  }
}
