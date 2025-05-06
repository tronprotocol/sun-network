package org.tron.common.logsfilter.capsule;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.ShieldedTRC20TrackerTrigger;
import org.tron.core.capsule.BlockCapsule;

@Slf4j
public class ShieldedTRC20SolidityTrackerCapsule extends TriggerCapsule {


  @Getter
  @Setter
  ShieldedTRC20TrackerTrigger shieldedTRC20TrackerTrigger;

  public ShieldedTRC20SolidityTrackerCapsule(BlockCapsule block) {

    shieldedTRC20TrackerTrigger = new ShieldedTRC20TrackerTrigger();
    shieldedTRC20TrackerTrigger.setBlockHash(block.getBlockId().toString());
    shieldedTRC20TrackerTrigger.setParentHash(block.getParentHash().toString());
    shieldedTRC20TrackerTrigger.setBlockNumber(block.getNum());
    shieldedTRC20TrackerTrigger.setTimeStamp(block.getTimeStamp());
    shieldedTRC20TrackerTrigger.solidityType();

    //logger.info("---------------------shieldedTRC20TrackerTrigger------------------------{}",JSONObject.toJSONString(shieldedTRC20TrackerTrigger));

  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postShieldedTRC20TrackerTrigger(shieldedTRC20TrackerTrigger);
  }

}
