package org.tron.service.task.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtention;
import org.tron.service.task.EventTaskImpl;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Task extends EventTaskImpl {

  private String from;
  private String amount;
  private String tokenId;

  public DepositTRC10Task(String from, String amount, String tokenId) {
    this.from = from;
    this.amount = amount;
    this.tokenId = tokenId;
  }

  @Override
  public TransactionExtention getTransactionExtention() {
    if (Objects.nonNull(this.transactionExtention)) {
      return this.transactionExtention;
    }
    try {
      logger.info("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      AssetIssueContract assetIssue = MainChainGatewayApi.getAssetIssueById(this.tokenId);
      Transaction tx = SideChainGatewayApi
          .mintToken10Transaction(this.from, this.tokenId, this.amount,
              assetIssue.getName().toStringUtf8(),
              assetIssue.getName().toStringUtf8(), assetIssue.getPrecision());
      this.transactionExtention = new TransactionExtention(TaskEnum.SIDE_CHAIN, tx);
    } catch (Exception e) {
      logger.error("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      e.printStackTrace();
    }
    return this.transactionExtention;
  }

  @Override
  public void run() {
    try {
      logger.info("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      AssetIssueContract assetIssue = MainChainGatewayApi.getAssetIssueById(this.tokenId);
      TransactionExtention txId = SideChainGatewayApi
          .mintToken10(this.from, this.tokenId, this.amount, assetIssue.getName().toStringUtf8(),
              assetIssue.getName().toStringUtf8(), assetIssue.getPrecision());
      Thread.sleep(3000L);
      txId.setType(TaskEnum.SIDE_CHAIN);
      SideChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.error("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
      e.printStackTrace();
    }
  }
}
