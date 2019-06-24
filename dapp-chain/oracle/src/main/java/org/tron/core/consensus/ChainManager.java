package org.tron.core.consensus;

import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.common.config.Args;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.utils.BlockUtil;
import org.tron.db.BlockStore;
import org.tron.db.DBManager;


/**
 *
 *  Chain logics for Oracle
 *
 */

@Slf4j(topic = "Chain")
//@Component
public class ChainManager {


  private BlockCapsule genesisBlock;

  @Autowired
  private DBManager manager;

  public ChainManager(){

  }

  @PostConstruct
  public void init(){
    this.initGenesis();
  }

  /**
   * init genesis block.
   */
  public void initGenesis() {
    this.genesisBlock = BlockUtil.newGenesisBlockCapsule();
    if (manager.containBlock(this.genesisBlock.getBlockId())) {
      Args.getInstance().setChainId(this.genesisBlock.getBlockId().toString());
    } else {
      if (manager.hasBlocks()) {
        logger.error(
            "genesis block modify, please delete database directory({}) and restart",
            Args.getInstance().getDatabaseDirectory());
        System.exit(1);
      } else {
        logger.info("create genesis block");
        Args.getInstance().setChainId(this.genesisBlock.getBlockId().toString());

        manager.persistBlockData(this.genesisBlock.getBlockId().getBytes(), this.genesisBlock);
        // TODO: update block number info and save to block index
        //this.blockIndexStore.put(this.genesisBlock.getBlockId());

        logger.info("save block: " + this.genesisBlock);

      }
    }
 }
}
