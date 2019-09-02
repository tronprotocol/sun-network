package org.tron.walletcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.walletcli.config.ConfigInfo;
import org.tron.walletcli.task.AccountTask;
import org.tron.walletcli.task.WithdrawTask;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger("Main");

  public static void main(String[] args) {

    SunNetwork sdk = new SunNetwork();
    SunNetworkResponse<Integer> ret = sdk.init("config.conf", new MultiSignTransactionImpl());
    if (ret.getData() != 0) {
      logger.info("Failed to init sdk");
    }

    logger.info("sdk inited !");
    sdk.setPrivateKey(ConfigInfo.privateKey);
    sdk.getSideChainService().freezeBalance(ConfigInfo.basicFreezeBalance, 3, 0, null);
    sdk.getSideChainService().freezeBalance(ConfigInfo.basicFreezeBalance, 3, 1, null);

    new WithdrawTask().runTask(sdk);

    AccountTask accountTask = new AccountTask();
    accountTask.initAccounts(sdk);
    accountTask.runTask(sdk);

  }

}
