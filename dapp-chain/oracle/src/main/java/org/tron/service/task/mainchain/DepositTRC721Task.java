package org.tron.service.task.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtension;
import org.tron.service.task.EventTaskImpl;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "mainChainTask")
public class DepositTRC721Task extends EventTaskImpl {

  private String from;
  private String uid;
  private String contractAddress;

  public DepositTRC721Task(String from, String uid, String contractAddress) {
    this.from = from;
    this.uid = uid;
    this.contractAddress = contractAddress;
  }

  @Override
  public TransactionExtension getTransactionExtension() {
    if (Objects.nonNull(transactionExtension)) {
      return transactionExtension;
    }
    try {
      Transaction tx = SideChainGatewayApi
          .mintToken721Transaction(this.from, this.contractAddress, this.uid);
      this.transactionExtension = new TransactionExtension(TaskEnum.SIDE_CHAIN, tx);
    } catch (Exception e) {
      logger
          .error("from:{},uid:{},contractAddress{}", this.from, this.uid, this.contractAddress);
      e.printStackTrace();
    }
    return this.transactionExtension;
  }

  @Override
  public void run() {
    logger
        .info("from:{},uid:{},contractAddress{}", this.from, this.uid, this.contractAddress);
    try {
      TransactionExtension txId = SideChainGatewayApi
          .mintToken721(this.from, this.contractAddress, this.uid);
      txId.setType(TaskEnum.SIDE_CHAIN);
      SideChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger
          .error("from:{},uid:{},contractAddress{}", this.from, this.uid, this.contractAddress);
      e.printStackTrace();
    }
  }
}
