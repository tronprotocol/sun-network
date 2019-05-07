package org.tron.service.task.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtention;
import org.tron.service.task.EventTaskImpl;
import org.tron.service.task.TaskEnum;

@Slf4j(topic = "sideChainTask")
public class DeployDAppTRC721AndMappingTask extends EventTaskImpl {

  // "event DeployDAppTRC721AndMapping(address developer, address mainChainAddress, address sideChainAddress);"

  private String developer;
  private String mainChainAddress;
  private String sideChainAddress;

  public DeployDAppTRC721AndMappingTask(String developer, String mainChainAddress,
      String sideChainAddress) {
    this.developer = developer;
    this.mainChainAddress = mainChainAddress;
    this.sideChainAddress = sideChainAddress;
  }

  @Override
  public TransactionExtention getTransactionExtention() {
    if (Objects.nonNull(transactionExtention)) {
      return transactionExtention;
    }
    try {
      Transaction tx = MainChainGatewayApi
          .addTokenMappingTransaction(this.mainChainAddress, this.sideChainAddress);
      this.transactionExtention = new TransactionExtention(TaskEnum.MAIN_CHAIN, tx);
    } catch (Exception e) {
      logger.error(
          "DeployDAppTRC721AndMappingTask fail, developer: {}, mainChainAddress: {}, sideChainAddress: {}",
          this.developer, this.mainChainAddress, this.sideChainAddress);
    }
    return this.transactionExtention;
  }

  @Override
  public void run() {
    logger.info("developer: {}, mainChainAddress: {}, sideChainAddress: {}", this.developer,
        this.mainChainAddress, this.sideChainAddress);
    try {
      TransactionExtention txId = MainChainGatewayApi
          .addTokenMapping(this.mainChainAddress, this.sideChainAddress);
      txId.setType(TaskEnum.MAIN_CHAIN);
      MainChainGatewayApi.checkTxInfo(txId);
      CheckTransaction.getInstance().submitCheck(txId);
    } catch (Exception e) {
      logger.error(
          "DeployDAppTRC721AndMappingTask fail, developer: {}, mainChainAddress: {}, sideChainAddress: {}",
          this.developer, this.mainChainAddress, this.sideChainAddress);
    }
  }
}
