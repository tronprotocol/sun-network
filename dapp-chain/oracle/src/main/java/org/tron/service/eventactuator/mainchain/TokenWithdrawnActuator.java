package org.tron.service.eventactuator.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class TokenWithdrawnActuator extends Actuator {

  private String owner;
  private String kind;
  private String contractAddress;
  private String value;

  public TokenWithdrawnActuator(String owner, String kind, String contractAddress, String value) {
    this.owner = owner;
    this.kind = kind;
    this.contractAddress = contractAddress;
    this.value = value;
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule() {
    return null;
  }

}
