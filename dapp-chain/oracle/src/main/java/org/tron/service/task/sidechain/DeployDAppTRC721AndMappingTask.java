package org.tron.service.task.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.service.task.EventTask;

@Slf4j(topic = "sideChainTask")
public class DeployDAppTRC721AndMappingTask implements EventTask {

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
  public void run() {
    logger.info("developer: {}, mainChainAddress: {}, sideChainAddress: {}", this.developer,
      this.mainChainAddress, this.sideChainAddress);
    try {
      String txId = MainChainGatewayApi
        .addTokenMapping(this.mainChainAddress, this.sideChainAddress);
      MainChainGatewayApi.checkTxInfo(txId);
    } catch (Exception e) {
      logger.error(
        "DeployDAppTRC721AndMappingTask fail, developer: {}, mainChainAddress: {}, sideChainAddress: {}",
        this.developer, this.mainChainAddress, this.sideChainAddress);
    }
  }
}
