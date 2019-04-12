package org.tron.service.eventenum;

public enum SideEventType implements EventType {

  // event DeployDAppTRC20AndMapping(address developer, address mainChainAddress, address sideChainAddress);
  // event DeployDAppTRC721AndMapping(address developer, address mainChainAddress, address sideChainAddress);
  // event DepositTRC10(address to, uint256 trc10, uint256 value, address sideChainAddress);
  // event DepositTRC20(address sideChainAddress, address to, uint256 value);
  // event DepositTRC721(address sideChainAddress, address to, uint256 tokenId);
  // event DepositTRX(address to, uint256 value);
  // event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes memory txData);
  // event WithdrawTRC20(address from, uint256 value, address mainChainAddress, bytes memory txData);
  // event WithdrawTRC721(address from, uint256 tokenId, address mainChainAddress, bytes memory txData);
  // event WithdrawTRX(address from, uint256 value, bytes memory txData);

  DEPLOY_DAPPTRC20_AND_MAPPING("DeployDAppTRC20AndMapping(address,address,address)"),
  DEPLOY_DAPPTRC721_AND_MAPPING("DeployDAppTRC721AndMapping(address,address,address)"),
  WITHDRAW_TRC10("WithdrawTRC10(address,uint256,uint256,bytes)"),
  WITHDRAW_TRC20("WithdrawTRC20(address,uint256,address,bytes)"),
  WITHDRAW_TRC721("WithdrawTRC721(address,uint256,address,bytes)"),
  WITHDRAW_TRX("WithdrawTRX(address,uint256,bytes)"),
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
