package org.tron.walletcli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.response.TransactionResponse;
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
      System.exit(-1);
    }

    logger.info("sdk inited !");
    sdk.setPrivateKey(ConfigInfo.privateKey);
    SunNetworkResponse<TransactionResponse> bandwidthResponse = sdk.getSideChainService()
        .freezeBalance(ConfigInfo.basicFreezeBalance, 3, 0, null);
    SunNetworkResponse<TransactionResponse> energyResponse = sdk.getSideChainService()
        .freezeBalance(ConfigInfo.basicFreezeBalance, 3, 1, null);

    logger.info("bandwidth txid = {}, energy txid = {}", bandwidthResponse.getData().getTrxId(), energyResponse.getData().getTrxId());

    new WithdrawTask().runTask(sdk);

    AccountTask accountTask = new AccountTask();
    accountTask.initAccounts(sdk);
    accountTask.runTask(sdk);

  }

}
