pragma solidity ^0.4.24;

import "../common/token/TRC20/TRC20.sol";

contract TRC20BasicToken is TRC20 {
    // Called by the DAppChain Gateway contract to mint tokens that have been deposited to the
    // Mainnet Gateway.
    //
    // NOTE: This function will only be called by the DAppChain Gateway contract if it doesn't have
    // enough tokens to complete a transfer, so it's possible to omit this function if you wish to
    // manually allocate tokens for the Gateway.
    function mintToGateway(uint256 amount) public;
    function
    event mint(
        address gateway,
        uint256 amount
    );
}