pragma solidity ^0.4.24;

interface IDApp {

    function mint(address to, uint256 value) public;

    function withdrawal(uint256 value, bytes memory txData) public;
}