package org.tron.service.task.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionId;
import org.tron.service.task.EventTask;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRXTask implements EventTask {

  // "event WithdrawTRX(address from, uint256 value, bytes memory txData);"

  private String from;
  private String value;
  private String txData;

  public WithdrawTRXTask(String from, String value, String txData) {
    this.from = from;
    this.value = value;
    this.txData = txData;
  }

  @Override
  public void run() {
    logger.info("from: {}, value: {}, txData: {}", this.from, this.value, this.txData);
    try {
      TransactionId txId = MainChainGatewayApi.withdrawTRX(this.from, this.value, this.txData);
      txId.setType(TaskEnum.MAIN_CHAIN);
      MainChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.info("WithdrawTRXTask fail, from: {}, value: {}, txData: {}", this.from, this.value,
          this.txData);
    }
  }
}
