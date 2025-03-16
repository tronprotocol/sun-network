package org.tron.common.logsfilter;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class EventPluginConfig {

  public static final String BLOCK_TRIGGER_NAME = "block";
  public static final String TRANSACTION_TRIGGER_NAME = "transaction";
  public static final String CONTRACTEVENT_TRIGGER_NAME = "contractevent";
  public static final String CONTRACTLOG_TRIGGER_NAME = "contractlog";
  public static final String SOLIDITY_TRIGGER_NAME = "solidity";
  public static final String SOLIDITY_EVENT_NAME = "solidityevent";
  public static final String SOLIDITY_LOG_NAME= "soliditylog";

  public static final String BALANCE_TRACKER = "balanceTracker";
  public static final String TRC20TRACKER_SOLIDITY_TRIGGER_NAME = "trc20SolidityTracker";
  public static final String BLOCK_ERASE_TRIGGER_NAME = "blockErased";
  public static final String SHIELDED_TRC20_SOLIDITY_TRACKER = "shieldedTRC20SolidityTracker";
  public static final String SHIELDED_TRC20_TRACKER = "shieldedTRC20Tracker";

  @Getter
  @Setter
  private String pluginPath;

  @Getter
  @Setter
  private String serverAddress;

  @Getter
  @Setter
  private String dbConfig;

  @Getter
  @Setter
  private boolean useNativeQueue;

  @Getter
  @Setter
  private int bindPort;

  @Getter
  @Setter
  private int sendQueueLength;


  @Getter
  @Setter
  private List<TriggerConfig> triggerConfigList;

  public EventPluginConfig() {
    pluginPath = "";
    serverAddress = "";
    dbConfig = "";
    useNativeQueue = false;
    bindPort = 0;
    sendQueueLength = 0;
    triggerConfigList = new ArrayList<>();
  }
}
