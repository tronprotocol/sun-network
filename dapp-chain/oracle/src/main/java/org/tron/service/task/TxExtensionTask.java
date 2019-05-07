package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtention;

@Slf4j(topic = "sideChainTask")
public class TxExtensionTask implements EventTask {

  // "event DeployDAppTRC20AndMapping(address developer, address mainChainAddress, address sideChainAddress);"

  private String developer;
  private String mainChainAddress;
  private String sideChainAddress;

  public TxExtensionTask(String developer, String mainChainAddress,
      String sideChainAddress) {
    this.developer = developer;
    this.mainChainAddress = mainChainAddress;
    this.sideChainAddress = sideChainAddress;
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
          "DeployDAppTRC20AndMappingTask fail, developer: {}, mainChainAddress: {}, sideChainAddress: {}",
          this.developer, this.mainChainAddress, this.sideChainAddress);
    }
  }
}
