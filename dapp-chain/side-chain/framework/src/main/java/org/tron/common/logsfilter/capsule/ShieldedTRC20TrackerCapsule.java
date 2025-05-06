package org.tron.common.logsfilter.capsule;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.trigger.ShieldedTRC20TrackerTrigger;
import org.tron.common.logsfilter.trigger.ShieldedTRC20TrackerTrigger.TransactionPojo;
import org.tron.core.capsule.BlockCapsule;

@Slf4j
public class ShieldedTRC20TrackerCapsule extends TriggerCapsule {


  @Getter
  @Setter
  ShieldedTRC20TrackerTrigger shieldedTRC20TrackerTrigger;

  public ShieldedTRC20TrackerCapsule(BlockCapsule block,
      List<TransactionPojo> transactionList) {

    shieldedTRC20TrackerTrigger = new ShieldedTRC20TrackerTrigger();
    shieldedTRC20TrackerTrigger.setBlockHash(block.getBlockId().toString());
    shieldedTRC20TrackerTrigger.setParentHash(block.getParentHash().toString());
    shieldedTRC20TrackerTrigger.setBlockNumber(block.getNum());
    shieldedTRC20TrackerTrigger.setTimeStamp(block.getTimeStamp());
    if (transactionList != null && transactionList.size() > 0) {
      shieldedTRC20TrackerTrigger.setTransactionList(transactionList);
    }
    //logger.info("---------------------shieldedTRC20TrackerTrigger------------------------{}", JSONObject.toJSONString(shieldedTRC20TrackerTrigger));

  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postShieldedTRC20TrackerTrigger(shieldedTRC20TrackerTrigger);
  }

}
