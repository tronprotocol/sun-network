package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.TransactionExtentionStore;
import org.tron.protos.Sidechain.TransactionExtension;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j(topic = "task")
public class InitTask {

  private ExecutorService executor;

  public InitTask(int fixedThreads) {
    this.executor = Executors.newFixedThreadPool(fixedThreads);
  }

  public void batchProcessTxInDb() {
    TransactionExtentionStore store = TransactionExtentionStore.getInstance();

    LinkedHashMap<byte[], byte[]> allTx = store.getAllData();
    for (Entry<byte[], byte[]> entry : allTx.entrySet()) {
      byte[] txExtensionBytes = entry.getValue();
      try {
        executor.execute(new TxExtensionTask(new TransactionExtensionCapsule(txExtensionBytes)));
      } catch (InvalidProtocolBufferException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }
}
