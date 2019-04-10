package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
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
      String sideContractAddress = SideChainGatewayApi
          .getMainToSideContractMap(this.contractAddress);
      String trxId = SideChainGatewayApi
          .mintToken(sideContractAddress, this.from, this.amount);
      SideChainGatewayApi.getTxInfo(trxId);
    } catch (Exception e) {
      logger.error("from:{},amount:{},contractAddress{}", this.from, this.amount,
          this.contractAddress);
      e.printStackTrace();
    }
  }
}
