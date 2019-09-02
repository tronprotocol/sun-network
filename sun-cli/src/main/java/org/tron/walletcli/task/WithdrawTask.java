package org.tron.walletcli.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.sunapi.SunNetwork;
import org.tron.walletcli.config.ConfigInfo;

public class WithdrawTask extends SideChainTask {

  private static final Logger logger = LoggerFactory.getLogger("WithdrawTask");

  private ScheduledExecutorService service = Executors
      .newSingleThreadScheduledExecutor();

  @Override
  public void runTask(SunNetwork sdk) {
    service.scheduleAtFixedRate(() -> {
      logger.info("withdraw task run!");
      sdk.setPrivateKey(ConfigInfo.privateKey);
      triggerContract(sdk, 0, ConfigInfo.contractWithdraw);
    }, 0, 1, TimeUnit.DAYS);
  }

}
