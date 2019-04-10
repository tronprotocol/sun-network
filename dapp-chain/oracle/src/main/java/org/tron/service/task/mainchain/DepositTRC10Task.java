package org.tron.service.task.mainchain;

import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.service.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Task implements EventTask {

  private String from;
  private BigInteger amount;
  private BigInteger tokenId;

  public DepositTRC10Task(String from, String amount, String tokenId) {
    this.from = from;
    this.amount = new BigInteger(amount, 10);
    this.tokenId = new BigInteger(tokenId, 10);
  }

  @Override
  public void run() {
    try {
      logger.info("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      String sideContractAddress = SideChainGatewayApi
          .getMainToSideTRC10Map(this.tokenId);
      String trxId = SideChainGatewayApi
          .mintToken(sideContractAddress, this.from, this.amount);
      SideChainGatewayApi.getTxInfo(trxId);
    } catch (Exception e) {
      logger.error("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      e.printStackTrace();
    }
  }
}
