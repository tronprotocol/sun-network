package org.tron.common.logsfilter.trigger;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class BlockErasedTrigger extends Trigger {

  private long blockNumber;

  private String blockHash;

  public BlockErasedTrigger() {
    super();
    setTriggerName(Trigger.BLOCK_ERASE_TRIGGER_NAME);
  }
}
