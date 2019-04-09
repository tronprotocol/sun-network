package org.tron.service.task.sidechain;

import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.service.task.EventTask;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC20Task implements EventTask {

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
  public void run() {
    logger.info("from: {}, value: {}, mainChainAddress: {}, txData: {}", this.from, this.value,
      this.mainChainAddress, this.txData);
    try {
      String txId = MainChainGatewayApi
        .withdrawTRC20(this.from, this.mainChainAddress, this.value, this.txData);
      MainChainGatewayApi.checkTxInfo(txId);
    } catch (Exception e) {
      logger.error("WithdrawTRC20Task fail, from: {}, value: {}, mainChainAddress: {}, txData: {}",
        this.from, this.value, this.mainChainAddress, this.txData);
    }
  }
}
