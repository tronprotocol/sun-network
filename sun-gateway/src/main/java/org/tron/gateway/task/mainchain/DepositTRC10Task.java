package org.tron.gateway.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.gateway.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Task implements EventTask {

  private String from;
  private String amount;
  private String tokenId;

  public DepositTRC10Task(String from, String amount, String tokenId) {
    this.from = from;
    this.amount = amount;
    this.tokenId = tokenId;
  }

  @Override
  public void run() {
    logger.info("from:{},amount:{},tokenId:{}", this.from, this.amount, this.tokenId);
  }
}
