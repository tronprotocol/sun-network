package org.tron.service.eventenum;

public enum MainEventType implements EventType {
  
  TRX_RECEIVED("TRXReceived(address,uint64,uint256)"),
  TRC10_RECEIVED("TRC10Received(address,uint64,uint64,uint256)"),
  TRC20_RECEIVED("TRC20Received(address,address,uint64,uint256)"),
  TRC20_RECEIVED_V2("TRC20Received(address,address,uint256,uint256)"),
  TRC721_RECEIVED("TRC721Received(address,address,uint256,uint256)"),

  TRX_WITHDRAW("TRXWithdraw(address,uint256,uint256)"),
  TRC10_WITHDRAW("TRC10Withdraw(address,trcToken,uint256,uint256)"),
  TRC20_WITHDRAW("TRC20Withdraw(address,address,uint256,uint256)"),
  TRC721_WITHDRAW("TRC721Withdraw(address,address,uint256,uint256)"),

  TRC20_MAPPING("TRC20Mapping(address,uint256)"),
  TRC721_MAPPING("TRC721Mapping(address,uint256)"),
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
