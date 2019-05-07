package org.tron.service.task.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtension;
import org.tron.service.task.EventTaskImpl;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC10Task extends EventTaskImpl {

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
  public TransactionExtension getTransactionExtension() {
    if (Objects.nonNull(transactionExtension)) {
      return this.transactionExtension;
    }
    try {
      if (this.trc10.equalsIgnoreCase("2000000")) {
        Transaction tx = MainChainGatewayApi
            .withdrawTRC20Transaction(this.from,
                WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress()), this.value,
                this.txData);
        this.transactionExtension = new TransactionExtension(TaskEnum.MAIN_CHAIN, tx);
      } else {
        Transaction tx = MainChainGatewayApi
            .withdrawTRC10Transaction(this.from, this.trc10, this.value, this.txData);
        this.transactionExtension = new TransactionExtension(TaskEnum.MAIN_CHAIN, tx);
      }
    } catch (Exception e) {
      logger.error("WithdrawTRC10Task fail, from: {}, value: {}, trc10: {}, txData: {}", this.from,
          this.value, this.trc10, this.txData);
    }
    return this.transactionExtension;
  }

  @Override
  public void run() {
    logger.info("from: {}, value: {}, trc10: {}, txData: {}", this.from, this.value, this.trc10,
        this.txData);
    try {
      TransactionExtension txId;
      if (this.trc10.equalsIgnoreCase("2000000")) {
        txId = MainChainGatewayApi
            .withdrawTRC20(this.from,
                WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress()), this.value,
                this.txData);
      } else {
        txId = MainChainGatewayApi
            .withdrawTRC10(this.from, this.trc10, this.value, this.txData);
      }
      txId.setType(TaskEnum.MAIN_CHAIN);
      MainChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.error("WithdrawTRC10Task fail, from: {}, value: {}, trc10: {}, txData: {}", this.from,
          this.value, this.trc10, this.txData);
    }
  }

}
