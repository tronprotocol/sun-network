package org.tron.common.logsfilter.capsule;

import lombok.Getter;
import lombok.Setter;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.BlockErasedTrigger;
import org.tron.core.capsule.BlockCapsule;

public class BlockErasedTriggerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  BlockErasedTrigger blockErasedTrigger;

  public BlockErasedTriggerCapsule(BlockCapsule block) {
    blockErasedTrigger = new BlockErasedTrigger();
    blockErasedTrigger.setBlockHash(block.getBlockId().toString());
    blockErasedTrigger.setBlockNumber(block.getNum());
    blockErasedTrigger.setTimeStamp(block.getTimeStamp());
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postBlockErasedTrigger(blockErasedTrigger);
  }

}
