package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionId;
import org.tron.service.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRXTask implements EventTask {

  private String from;
  private String amount;

  public DepositTRXTask(String from, String amount) {
    this.from = from;
    this.amount = amount;
  }

  @Override
  public void run() {
    logger.info("from:{},amount:{}", this.from, this.amount);
    try {
      TransactionId txId = SideChainGatewayApi.mintTrx(this.from, this.amount);
      logger.info("deposit trx is {}", txId.getTransactionId());
      SideChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.error("from:{},amount:{}", this.from, this.amount);
      e.printStackTrace();
    }
  }
}
