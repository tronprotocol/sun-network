pragma solidity ^0.4.24;

import "../common/token/TRC20/StandardToken.sol";
import "../common/token/TRC20/TRC20Receiver.sol";
import "./TRC20BasicToken.sol";

/**
 * @title Full ERC20 Token for Loom DAppChains
 */
contract StandardTRC20 is TRC20BasicToken, StandardToken, TRC20Receiver {
    // Transfer Gateway contract address
    address public gateway;

    string public name;
    string public symbol;
    uint8 public decimals;
    uint256 public totalSupply;
    // address public owner;

    /**
      * @dev Constructor function
      */

    constructor (address _gateway, string tokenName, string tokenSymbol, uint8 decimalUnits) public {
        gateway = _gateway;
        name = tokenName;
        symbol = tokenSymbol;
        decimals = decimalUnits;
        totalSupply = 0;
    }

    // Called by the gateway contract to mint tokens that have been deposited to the Mainnet gateway.
    function mintToGateway(uint256 _amount) public {
        require(msg.sender == gateway);
        totalSupply = totalSupply.add(_amount);
        balances[gateway] = balances[gateway].add(_amount);
        emit mint(gateway, _amount);
    }

    function withdrawal(uint256 _amount) public {
        transferFrom(msg.sender, gateway, _amount);
        gateway.call(bytes4(keccak256("withdrawal(address,address,uint256)")), address(this), msg.sender, _amount);
    }

    function onERC20Received(
        address _from,
        uint256 amount
    ) public returns (bytes4) {
        return ERC20_RECEIVED;
    }
}