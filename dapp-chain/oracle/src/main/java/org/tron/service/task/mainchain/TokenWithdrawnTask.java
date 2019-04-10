package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class TokenWithdrawnTask implements EventTask {

  private String owner;
  private String kind;
  private String contractAddress;
  private String value;

  public TokenWithdrawnTask(String owner, String kind, String contractAddress, String value) {
    this.owner = owner;
    this.kind = kind;
    this.contractAddress = contractAddress;
    this.value = value;
  }

  @Override
  public void run() {
    //TODO
    logger.info("owner:{},kind:{},contractAddress:{},value{}", this.owner, this.kind,
        this.contractAddress, this.value);
  }
}
