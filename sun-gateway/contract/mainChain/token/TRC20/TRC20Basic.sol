pragma solidity ^0.4.23;


/**
 * @title TRC20Basic
 * @dev Simpler version of TRC20 interface
 * @dev see
 */
contract TRC20Basic {
    function totalSupply() public view returns (uint256);

    function balanceOf(address who) public view returns (uint256);

    function transfer(address to, uint256 value) public returns (bool);

    event Transfer(address indexed from, address indexed to, uint256 value);
}
