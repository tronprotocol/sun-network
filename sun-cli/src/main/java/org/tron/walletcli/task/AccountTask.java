package org.tron.walletcli.task;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.sunapi.ErrorCodeEnum;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.walletcli.config.ConfigInfo;

public class AccountTask extends SideChainTask {

  private static final Logger logger = LoggerFactory.getLogger("AccountTask");

  private List<String> accountList = new ArrayList<>();

  private AtomicInteger index = new AtomicInteger(0);

  private ScheduledExecutorService service = Executors
      .newSingleThreadScheduledExecutor();


  public void runTask(SunNetwork sdk) {
    service.scheduleAtFixedRate(() -> sendCoinAndDeposit(sdk), 0, ConfigInfo.interval, TimeUnit.MILLISECONDS);
  }

  public void sendCoinAndDeposit(SunNetwork sdk) {

    logger.info("send coin and deposit");

    sdk.setPrivateKey(ConfigInfo.privateKey);
    if (accountList.size() > 0) {
      if (index.get() >= accountList.size()) {
        index.set(0);
      }
      String accountInfo = accountList.get(index.getAndAdd(1));
      sdk.getSideChainService().sendCoin(accountInfo.split(",")[0], ConfigInfo.contractDepositValue);
      sdk.setPrivateKey(accountInfo.split(",")[1]);
      triggerContract(sdk, ConfigInfo.contractDepositValue, ConfigInfo.contractDeposit);
    }
  }

  public void initAccounts (SunNetwork sdk) {

    logger.info("init accounts !");
    sdk.setPrivateKey(ConfigInfo.privateKey);
    try {
      BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(ConfigInfo.privateKeyAddressFile, true)));
      for (int i = 0; i < ConfigInfo.accountNum; i ++ ) {

        sdk.setPrivateKey(ConfigInfo.privateKey);
        SunNetworkResponse<AddressPrKeyPairMessage> resp = sdk.getMainChainService().generateAddress();
        if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
          String info = resp.getData().getAddress() + "," + resp.getData().getPrivateKey();

          sdk.getSideChainService().sendCoin(resp.getData().getAddress(), ConfigInfo.contractDepositValue);

          sdk.setPrivateKey(resp.getData().getPrivateKey());
          sdk.getSideChainService().freezeBalance(ConfigInfo.accountFreezeBalance, 3, 0, null);
          sdk.getSideChainService().freezeBalance(ConfigInfo.accountFreezeBalance, 3, 1, null);
          accountList.add(info);
          out.write(info);
          out.newLine();
          out.flush();
          try {
            Thread.sleep(10);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
      out.close();
    } catch (Exception e) {

    }

  }

}
