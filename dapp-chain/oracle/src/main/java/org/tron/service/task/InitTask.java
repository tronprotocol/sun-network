package org.tron.service.task;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.tron.db.TransactionExtensionStore;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j(topic = "task")
public class InitTask {

  private ExecutorService executor;

  public InitTask(int fixedThreads) {
    this.executor = Executors.newFixedThreadPool(fixedThreads);
  }

  public void batchProcessTxInDb() {
    TransactionExtensionStore store = TransactionExtensionStore.getInstance();

    Set<byte[]> allTx = store.allValues();
    for (byte[] txExtensionBytes : allTx) {
      try {
        CheckTransaction.getInstance()
            .submitCheck(new TransactionExtensionCapsule(txExtensionBytes), 1);
      } catch (InvalidProtocolBufferException e) {
        logger.error(e.getMessage(), e);
      }
    }
  }
}
