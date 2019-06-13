package org.tron.service.eventenum;

public enum SideEventType implements EventType {

  DEPLOY_DAPPTRC20_AND_MAPPING("DeployDAppTRC20AndMapping(address,address,address)"),
  DEPLOY_DAPPTRC721_AND_MAPPING("DeployDAppTRC721AndMapping(address,address,address)"),
  WITHDRAW_TRC10("WithdrawTRC10(uint256,address,uint256,uint256,bytes)"),
  WITHDRAW_TRC20("WithdrawTRC20(uint256,address,uint256,address,bytes)"),
  WITHDRAW_TRC721("WithdrawTRC721(uint256,address,uint256,address,bytes)"),
  WITHDRAW_TRX("WithdrawTRX(uint256,address,uint256,bytes)"),

  MULTISIGN_FOR_DEPLOY_AND_MAPPING("MultiSignForDeployAndMapping(address,address,bytes32,bytes32)"),
  MULTISIGN_FOR_WITHDRAW_TRX("MultiSignForWithdrawTRX(address,uint256,bytes,bytes32,bytes32)"),
  MULTISIGN_FOR_WITHDRAW_TRC10("MultiSignForWithdrawTRC10(address,uint256,uint256,bytes,bytes32,bytes32)"),
  MULTISIGN_FOR_WITHDRAW_TOKEN("MultiSignForWithdrawToken(address,address,uint256,uint256,bytes,bytes32,bytes32)"),
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
