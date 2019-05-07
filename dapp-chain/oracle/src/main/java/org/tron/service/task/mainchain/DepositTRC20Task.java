package org.tron.service.task.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtension;
import org.tron.service.task.EventTaskImpl;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "mainChainTask")
public class DepositTRC20Task extends EventTaskImpl {

  private String from;
  private String amount;
  private String contractAddress;

  public DepositTRC20Task(String from, String amount, String contractAddress) {
    this.from = from;
    this.amount = amount;
    this.contractAddress = contractAddress;
  }

  @Override
  public TransactionExtension getTransactionExtension() {
    if (Objects.nonNull(transactionExtension)) {
      return this.transactionExtension;
    }
    try {
      if (WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress())
          .equalsIgnoreCase(this.contractAddress)) {
        Transaction tx = SideChainGatewayApi
            .mintToken10Transaction(this.from, "2000000", this.amount, "sun token", "ST", 6);
        this.transactionExtension = new TransactionExtension(TaskEnum.SIDE_CHAIN, tx);
      } else {
        Transaction tx = SideChainGatewayApi
            .mintToken20Transaction(this.from, this.contractAddress, this.amount);
        this.transactionExtension = new TransactionExtension(TaskEnum.SIDE_CHAIN, tx);
      }
    } catch (Exception e) {
      logger.error("from:{},amount:{},contractAddress:{}", this.from, this.amount,
          this.contractAddress);
      e.printStackTrace();
    }
    return this.transactionExtension;
  }

  @Override
  public void run() {
    logger
        .info("from:{},amount:{},contractAddress{}", this.from, this.amount, this.contractAddress);
    try {
      if (WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress())
          .equalsIgnoreCase(this.contractAddress)) {
        TransactionExtension txId = SideChainGatewayApi
            .mintToken10(this.from, "2000000", this.amount, "sun token", "ST", 6);
        txId.setType(TaskEnum.SIDE_CHAIN);
        SideChainGatewayApi.checkTxInfo(txId);
        CheckTransaction.getInstance().submitCheck(txId);
      } else {
        TransactionExtension txId = SideChainGatewayApi
            .mintToken20(this.from, this.contractAddress, this.amount);
        txId.setType(TaskEnum.SIDE_CHAIN);
        SideChainGatewayApi.checkTxInfo(txId);
        CheckTransaction.getInstance().submitCheck(txId);
      }
    } catch (Exception e) {
      logger.error("from:{},amount:{},contractAddress{}", this.from, this.amount,
          this.contractAddress);
      e.printStackTrace();
    }
  }
}
