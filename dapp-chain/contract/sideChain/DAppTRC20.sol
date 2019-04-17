pragma solidity ^0.4.24;

import "../common/token/TRC20/TRC20.sol";
import "../common/token/TRC20/ITRC20Receiver.sol";
import "./IDApp.sol";

/**
 * @title Full TRC20 Token for Sun Network DAppChains
 */

contract DAppTRC20 is TRC20, IDApp {
    // Transfer Gateway contract address
    address public gateway;

    string public name;
    string public symbol;
    uint8 public decimals;
    // address public owner;

    /**
      * @dev Constructor function
      */

    constructor (address _gateway, string _name, string _symbol, uint8 _decimals) public {
        gateway = _gateway;
        name = _name;
        symbol = _symbol;
        decimals = _decimals;
        _totalSupply = 0;
    }

    modifier onlyGateway {
        require(msg.sender == gateway);
        _;
    }

    function mint(address to, uint256 value) public onlyGateway {
        require(to != address(0));

        _totalSupply = _totalSupply.add(value);
        _balances[to] = _balances[to].add(value);
        emit Transfer(address(0), to, value);
    }

    function withdrawal(uint256 value, bytes txData) public {
        transfer(gateway, value);
        bytes4 retval = ITRC20Receiver(gateway).onTRC20Received(msg.sender, value, txData);
        require(retval == _TRC20_RECEIVED);
    }
}