package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.client.WalletClient;
import org.tron.common.config.Args;
import org.tron.common.utils.WalletUtil;
import org.tron.service.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRC20Task implements EventTask {

  private String from;
  private String amount;
  private String contractAddress;

  public DepositTRC20Task(String from, String amount, String contractAddress) {
    this.from = from;
    this.amount = amount;
    this.contractAddress = contractAddress;
  }

  @Override
  public void run() {
    logger
      .info("from:{},amount:{},contractAddress{}", this.from, this.amount, this.contractAddress);
    try {
      if (WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress())
        .equalsIgnoreCase(this.contractAddress)) {
        String trxId = SideChainGatewayApi
          .mintToken10(this.from, "2000000", this.amount, "sun token", "ST", 6);
        SideChainGatewayApi.checkTxInfo(trxId);
      } else {
        String trxId = SideChainGatewayApi
          .mintToken20(this.from, this.contractAddress, this.amount);
        SideChainGatewayApi.checkTxInfo(trxId);
      }
    } catch (Exception e) {
      logger.error("from:{},amount:{},contractAddress{}", this.from, this.amount,
        this.contractAddress);
      e.printStackTrace();
    }
  }
}
