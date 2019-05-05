package org.tron.service.task.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionId;
import org.tron.service.task.EventTask;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC10Task implements EventTask {

  // "event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes memory txData);"

  private String from;
  private String value;
  private String trc10;
  private String txData;

  public WithdrawTRC10Task(String from, String value, String trc10, String txData) {
    this.from = from;
    this.value = value;
    this.trc10 = trc10;
    this.txData = txData;
  }

  @Override
  public void run() {
    logger.info("from: {}, value: {}, trc10: {}, txData: {}", this.from, this.value, this.trc10,
        this.txData);
    try {
      TransactionId txId = MainChainGatewayApi
          .withdrawTRC10(this.from, this.trc10, this.value, this.txData);
      MainChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.error("WithdrawTRC10Task fail, from: {}, value: {}, trc10: {}, txData: {}", this.from,
          this.value, this.trc10, this.txData);
    }
  }
}
