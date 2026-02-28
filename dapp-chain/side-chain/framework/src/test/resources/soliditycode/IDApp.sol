interface IDApp {

    function mint(address to, uint256 value) external;

    function withdrawal(uint256 value) payable external returns (uint256);
}