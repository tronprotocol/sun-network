contract ContractCallWithdraw {
    address payable public gateway;

    function setGatewayAddress(address payable _gateway) public {
        gateway = _gateway;
    }

    constructor() payable public{}
    function callWithdrawTRC10(uint256 tokenId, uint256 tokenValue) public returns (bool success, bytes memory data){
        (success, data) = gateway.call.value(1000)(abi.encodeWithSignature("withdrawTRC10(uint256,uint256)", tokenId, tokenValue));

    }

    function callWithdrawTRX() public returns (bool success, bytes memory data){
        (success, data) = gateway.call.value(1000)(abi.encodeWithSignature("withdrawTRX()"));

    }

    function callRetryWithdraw(uint256 nonce) public returns (bool success, bytes memory data){
        (success, data) = gateway.call.value(1000)(abi.encodeWithSignature("retryWithdraw(uint256)", nonce));

    }


}