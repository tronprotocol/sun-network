package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.check.TransactionExtention;
import org.tron.service.task.EventTaskImpl;

@Slf4j(topic = "mainChainTask")
public class TokenWithdrawnTask extends EventTaskImpl {

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
  public TransactionExtention getTransactionExtention() {
    return null;
  }

  @Override
  public void run() {
    //TODO
    logger.info("owner:{},kind:{},contractAddress:{},value{}", this.owner, this.kind,
        this.contractAddress, this.value);
  }
}
