package org.tron.walletcli.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.protos.Protocol.Account;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.walletcli.config.ConfigInfo;

public abstract class SideChainTask {

  private static final Logger logger = LoggerFactory.getLogger("SideChainTask");

  protected void triggerContract(SunNetwork sdk, long callValue, String methodStr) {

    logger.info("trigger contract, methodStr = {}", methodStr);

    TriggerContractRequest request = new TriggerContractRequest();

    request.setContractAddrStr(ConfigInfo.contractAddress);
    request.setMethodStr(methodStr);
    request.setArgsStr("#");
    request.setHex(false);
    request.setFeeLimit(10000000);
    request.setCallValue(callValue);
    request.setTokenCallValue(0);
    request.setTokenId("#");

    SunNetworkResponse<TransactionResponse> sunNetworkResponse = sdk.getSideChainService()
        .triggerContract(request);

    logger.info("sun network response txid = {}", sunNetworkResponse.getData().getTrxId());
  }

  protected long getBalance(SunNetwork sdk, String accountAddress) {

    SunNetworkResponse<Account> account = sdk.getSideChainService().getAccount(accountAddress);
    return account.getData().getBalance();
  }

  public abstract void runTask(SunNetwork sdk);

}
