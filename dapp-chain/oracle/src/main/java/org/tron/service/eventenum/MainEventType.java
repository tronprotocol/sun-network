package org.tron.service.eventenum;

public enum MainEventType implements EventType {
  TRXReceived("TRXReceived(address,uint256)"),
  TRC10Received("TRC10Received(address,uint256,uint256)"),
  TRC20Received("TRC20Received(address,uint256,address)"),
  TRC721Received("TRC721Received(address,uint256,address)"),
  TokenWithdrawn("TokenWithdrawn(address,uint8,address,uint256)"),
  Token10Withdrawn("Token10Withdrawn(address,uint8,uint256,uint256)"),
  UnknownEvent("UnknownEvent");
  private String signature;


  MainEventType(String signature) {
    this.signature = signature;
  }

  public static MainEventType fromSignature(String signature) {
    for (MainEventType eventType : MainEventType.values()) {
      if (eventType.getSignature().equals(signature)) {
        return eventType;
      }
    }
    return UnknownEvent;
  }

  public String getSignature() {
    return signature;
  }
}
