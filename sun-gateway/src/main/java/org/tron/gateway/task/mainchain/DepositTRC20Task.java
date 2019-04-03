package org.tron.gateway.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.gateway.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRC20Task implements EventTask {

  private String from;
  private String amount;
  private String contractAddress;

  public DepositTRC20Task(String from, String amount, String contractAddress) {
    this.from = from;
    this.amount = amount;
    this.contractAddress = contractAddress;
  }

  @Override
  public void run() {
    logger
        .info("from:{},amount:{},contractAddress{}", this.from, this.amount, this.contractAddress);
  }
}
