contract Ballot {
    constructor() payable public {}
    function setTokenOwner(address tokenAddress, address tokenOwner) public {
        address(0x10002).call(abi.encode(tokenAddress, tokenOwner));
    }
}