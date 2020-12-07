package org.tron.common.logsfilter.capsule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.BalanceTrackerTrigger;
import org.tron.core.capsule.BlockCapsule;

@Slf4j
public class TRC20SolidityTrackerCapsule extends TriggerCapsule {

  @Getter
  @Setter
  BalanceTrackerTrigger trc20TrackerTrigger;

  public TRC20SolidityTrackerCapsule(BlockCapsule block) {
    trc20TrackerTrigger = new BalanceTrackerTrigger();
    trc20TrackerTrigger.setBlockHash(block.getBlockId().toString());
    trc20TrackerTrigger.setParentHash(block.getParentHash().toString());
    trc20TrackerTrigger.setBlockNumber(block.getNum());
    trc20TrackerTrigger.setTimeStamp(block.getTimeStamp());
    trc20TrackerTrigger.solidityType();
    trc20TrackerTrigger.setSolidity(true);
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTRC20TrackerTrigger(trc20TrackerTrigger);
  }

}
