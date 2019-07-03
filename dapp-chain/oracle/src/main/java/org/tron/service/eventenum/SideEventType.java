package org.tron.service.eventenum;

public enum SideEventType implements EventType {

  WITHDRAW_TRC10("WithdrawTRC10(address,trcToken,uint256,uint256)"),
  WITHDRAW_TRC20("WithdrawTRC20(address,address,uint256,uint256)"),
  WITHDRAW_TRC721("WithdrawTRC721(address,address,uint256,uint256)"),
  WITHDRAW_TRX("WithdrawTRX(address,uint256,uint256)"),

  MULTISIGN_FOR_WITHDRAW_TRC10("MultiSignForWithdrawTRC10(address,trcToken,uint256,uint256)"),
  MULTISIGN_FOR_WITHDRAW_TRC20("MultiSignForWithdrawTRC20(address,address,uint256,uint256)"),
  MULTISIGN_FOR_WITHDRAW_TRC721("MultiSignForWithdrawTRC721(address,address,uint256,uint256)"),
  MULTISIGN_FOR_WITHDRAW_TRX("MultiSignForWithdrawTRX(address,uint256,uint256)"),

  UNKNOWN_EVENT("UnknownEvent");

  private String method;


  SideEventType(String method) {
    this.method = method;
  }

  public static SideEventType fromMethod(String method) {
    for (SideEventType eventType : SideEventType.values()) {
      if (eventType.getMethod().equals(method)) {
        return eventType;
      }
    }
    return UNKNOWN_EVENT;
  }

  public String getMethod() {
    return method;
  }
}
