contract contractCallMain{
    address payable public gateway;
    function setGatewayAddress(address payable _gateway)public{
        gateway=_gateway;
    }
    constructor() payable public{}
    function callDepositTRX() public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("depositTRX()"));

    }
    function callDepositTRC10(uint64 tokenId, uint64 tokenValue) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("depositTRC10(uint64,uint64)",tokenId,tokenValue));

    }

    function callDepositTRC721(uint256 tokenValue) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("depositTRC721(address,uint256)",address(this),tokenValue));

    }

    function callDepositTRC20(uint64 tokenValue) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("depositTRC20(address,uint64)",address(this),tokenValue));

    }

     function callMappingTRC20(bytes memory txId) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("mappingTRC20(bytes)",txId));

    }


     function callMappingTRC721(bytes memory txId) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("mappingTRC721(bytes)",txId));

    }

     function callRetryDeposit(uint256 nonce) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("retryDeposit(uint256)",nonce));

    }

      function callRetryMapping(uint256 nonce) public returns(bool success, bytes memory data){
       ( success, data)= gateway.call.value(1000)(abi.encodeWithSignature("retryMapping(uint256)",nonce));

    }
}