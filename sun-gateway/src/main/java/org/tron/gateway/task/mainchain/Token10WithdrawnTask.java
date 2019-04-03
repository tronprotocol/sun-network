package org.tron.gateway.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.gateway.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class Token10WithdrawnTask implements EventTask {

  private String owner;
  private String kind;
  private String tokenId;
  private String value;

  public Token10WithdrawnTask(String owner, String kind, String tokenId, String value) {
    this.owner = owner;
    this.kind = kind;
    this.tokenId = tokenId;
    this.value = value;
  }

  @Override
  public void run() {
    logger.info("owner:{},kind:{},tokenId:{},value{}", this.owner, this.kind, this.tokenId,
        this.value);
  }
}
