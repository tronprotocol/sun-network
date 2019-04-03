package org.tron.gateway.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.gateway.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRXTask implements EventTask {

  private String from;
  private String amount;

  public DepositTRXTask(String from, String amount) {
    this.from = from;
    this.amount = amount;
  }

  @Override
  public void run() {
    logger.info("from:{},amount:{}", this.from, this.amount);
  }
}
