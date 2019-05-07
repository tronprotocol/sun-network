package org.tron.service.task.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtension;
import org.tron.service.task.EventTaskImpl;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC20Task extends EventTaskImpl {

  // "event WithdrawTRC20(address from, uint256 value, address mainChainAddress, bytes memory txData);"

  private String from;
  private String value;
  private String mainChainAddress;
  private String txData;

  public WithdrawTRC20Task(String from, String value, String mainChainAddress, String txData) {
    this.from = from;
    this.value = value;
    this.mainChainAddress = mainChainAddress;
    this.txData = txData;
  }

  @Override
  public TransactionExtension getTransactionExtension() {
    if (Objects.nonNull(transactionExtension)) {
      return this.transactionExtension;
    }
    try {
      Transaction tx = MainChainGatewayApi
          .withdrawTRC20Transaction(this.from, this.mainChainAddress, this.value, this.txData);
      this.transactionExtension = new TransactionExtension(TaskEnum.MAIN_CHAIN, tx);
    } catch (Exception e) {
      logger.error("WithdrawTRC20Task fail, from: {}, value: {}, mainChainAddress: {}, txData: {}",
          this.from, this.value, this.mainChainAddress, this.txData);
    }
    return this.transactionExtension;
  }

  @Override
  public void run() {
    logger.info("from: {}, value: {}, mainChainAddress: {}, txData: {}", this.from, this.value,
        this.mainChainAddress, this.txData);
    try {
      TransactionExtension txId = MainChainGatewayApi
          .withdrawTRC20(this.from, this.mainChainAddress, this.value, this.txData);
      txId.setType(TaskEnum.MAIN_CHAIN);
      MainChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.error("WithdrawTRC20Task fail, from: {}, value: {}, mainChainAddress: {}, txData: {}",
          this.from, this.value, this.mainChainAddress, this.txData);
    }
  }

}
