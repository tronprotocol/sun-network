package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.check.TransactionExtension;
import org.tron.service.task.EventTaskImpl;

@Slf4j(topic = "mainChainTask")
public class Token10WithdrawnTask extends EventTaskImpl {

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
  public TransactionExtension getTransactionExtension() {
    return null;
  }

  @Override
  public void run() {
    //TODO
    logger.info("owner:{},kind:{},tokenId:{},value{}", this.owner, this.kind, this.tokenId,
        this.value);
  }

}
