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
public class WithdrawTRC721Task extends EventTaskImpl {

  // "event WithdrawTRC721(address from, uint256 tokenId, address mainChainAddress, bytes memory txData);"

  private String from;
  private String tokenId;
  private String mainChainAddress;
  private String txData;

  public WithdrawTRC721Task(String from, String tokenId, String mainChainAddress, String txData) {
    this.from = from;
    this.tokenId = tokenId;
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
          .withdrawTRC721Transaction(this.from, this.mainChainAddress, this.tokenId, this.txData);
      this.transactionExtension = new TransactionExtension(TaskEnum.MAIN_CHAIN, tx);
    } catch (Exception e) {
      logger
          .error("WithdrawTRC721Task fail, from: {}, tokenId: {}, mainChainAddress: {}, txData: {}",
              this.from, this.tokenId, this.mainChainAddress, this.txData);
    }
    return this.transactionExtension;
  }

  @Override
  public void run() {
    logger.info("from: {}, tokenId: {}, mainChainAddress: {}, txData: {}", this.from, this.tokenId,
        this.mainChainAddress, this.txData);
    try {
      TransactionExtension txId = MainChainGatewayApi
          .withdrawTRC721(this.from, this.mainChainAddress, this.tokenId, this.txData);
      txId.setType(TaskEnum.MAIN_CHAIN);
      MainChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger
          .error("WithdrawTRC721Task fail, from: {}, tokenId: {}, mainChainAddress: {}, txData: {}",
              this.from, this.tokenId, this.mainChainAddress, this.txData);
    }
  }

}
