package org.tron.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.common.exception.BadItemException;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.capsule.BlockCapsule;


/**
 *   DB operation for oracle
 */
@Slf4j
@Component
public class DBManager {
  @Autowired
  private BlockStore blockStore;

  @Autowired
  private   EventStore eventStore;

  @Autowired
  private   TransactionExtensionStore transactionExtensionStore;

  private DBManager(){
  }

  /**
   * judge id.
   *
   * @param blockHash blockHash
   */
  public boolean containBlock(final Sha256Hash blockHash) {
      return (this.blockStore.getData(blockHash.getBytes()) != null);
  }

  /**
   * judge has blocks.
   */
  public boolean hasBlocks() {
    return blockStore.hasNext();
  }


  public void persistBlockData(byte[] key,  BlockCapsule blockCapsule){
    blockStore.putData(key, blockCapsule.getData());
  }

}
