package org.tron.service.eventactuator.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class Token10WithdrawnActuator extends Actuator {

  private String owner;
  private String kind;
  private String tokenId;
  private String value;

  public Token10WithdrawnActuator(String owner, String kind, String tokenId, String value) {
    this.owner = owner;
    this.kind = kind;
    this.tokenId = tokenId;
    this.value = value;
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule() {
    return null;
  }

}
