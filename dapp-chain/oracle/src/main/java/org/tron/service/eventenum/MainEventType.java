package org.tron.service.eventenum;

public enum MainEventType implements EventType {
  TRX_RECEIVED("TRXReceived(address,uint256)"),
  TRC10_RECEIVED("TRC10Received(address,uint256,uint256)"),
  TRC20_RECEIVED("TRC20Received(address,uint256,address)"),
  TRC721_RECEIVED("TRC721Received(address,uint256,address)"),
  TOKEN_WITHDRAWN("TokenWithdrawn(address,uint8,address,uint256)"),
  TOKEN10_WITHDRAWN("Token10Withdrawn(address,uint8,uint256,uint256)"),
  UNKNOWN_EVENT("UnknownEvent");
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
    return UNKNOWN_EVENT;
  }

  public String getSignature() {
    return signature;
  }
}
