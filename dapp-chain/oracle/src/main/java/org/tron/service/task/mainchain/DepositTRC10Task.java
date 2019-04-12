package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.service.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Task implements EventTask {

  private String from;
  private String amount;
  private String tokenId;

  public DepositTRC10Task(String from, String amount, String tokenId) {
    this.from = from;
    this.amount = amount;
    this.tokenId = tokenId;
  }

  @Override
  public void run() {
    try {
      logger.info("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      AssetIssueContract assetIssue = MainChainGatewayApi.getAssetIssueById(this.tokenId);
      String trxId = SideChainGatewayApi
          .mintToken10(this.from, this.tokenId, this.amount, assetIssue.getName().toString(),
              assetIssue.getAbbr().toString(), assetIssue.getPrecision());
      SideChainGatewayApi.checkTxInfo(trxId);
    } catch (Exception e) {
      logger.error("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      e.printStackTrace();
    }
  }
}
