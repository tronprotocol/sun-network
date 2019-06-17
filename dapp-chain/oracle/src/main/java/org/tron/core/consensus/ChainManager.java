package org.tron.core.consensus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tron.common.config.Args;
import org.tron.core.capsule.utils.BlockUtil;
import org.tron.db.BlockStore;

//@Slf4j(topic = "Chain")
//@Component
public class ChainManager {
//  @Autowired
  private BlockStore blockStore;

  /**
   * init genesis block.
   */
  public ChainManager(){

  }
//  public void initGenesis() {
//    this.genesisBlock = BlockUtil.newGenesisBlockCapsule();
//    if (this.containBlock(this.genesisBlock.getBlockId())) {
//      Args.getInstance().setChainId(this.genesisBlock.getBlockId().toString());
//    } else {
//      if (this.hasBlocks()) {
//        logger.error(
//            "genesis block modify, please delete database directory({}) and restart",
//            Args.getInstance().getOutputDirectory());
//        System.exit(1);
//      } else {
//        logger.info("create genesis block");
//        Args.getInstance().setChainId(this.genesisBlock.getBlockId().toString());
//
//        blockStore.putData(this.genesisBlock.getBlockId().getBytes(), this.genesisBlock);
//        this.blockIndexStore.put(this.genesisBlock.getBlockId());
//
//        logger.info("save block: " + this.genesisBlock);
//        // init DynamicPropertiesStore
//        this.dynamicPropertiesStore.saveLatestBlockHeaderNumber(0);
//        this.dynamicPropertiesStore.saveLatestBlockHeaderHash(
//            this.genesisBlock.getBlockId().getByteString());
//        this.dynamicPropertiesStore.saveLatestBlockHeaderTimestamp(
//            this.genesisBlock.getTimeStamp());
//        this.initAccount();
//        this.initWitness();
//        this.witnessController.initWits();
//        this.khaosDb.start(genesisBlock);
//        this.updateRecentBlock(genesisBlock);
//      }
//    }
//  }
}
