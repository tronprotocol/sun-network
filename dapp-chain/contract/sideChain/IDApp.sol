pragma solidity ^0.4.24;

interface IDApp {

    function mint(address to, uint256 value) external;

    function withdrawal(uint256 value) external returns (uint256);
}