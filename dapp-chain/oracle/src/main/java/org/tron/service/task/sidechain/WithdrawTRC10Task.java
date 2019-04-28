package org.tron.service.task.sidechain;

import java.math.BigInteger;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.utils.WalletUtil;
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
      String txId;
      if (this.trc10.equalsIgnoreCase("2000000")) {
        txId = MainChainGatewayApi
          .withdrawTRC20(this.from,
            WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress()), this.value,
            this.txData);
      } else {
        txId = MainChainGatewayApi
          .withdrawTRC10(this.from, this.trc10, this.value, this.txData);
      }
      MainChainGatewayApi.checkTxInfo(txId);

    } catch (Exception e) {
      logger.error("WithdrawTRC10Task fail, from: {}, value: {}, trc10: {}, txData: {}", this.from,
        this.value, this.trc10, this.txData);
    }
  }
}
