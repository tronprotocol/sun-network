pragma solidity ^0.4.23;

import "./TRC10Basic.sol";


/**
 * @title ERC20 interface
 * @dev see https://github.com/ethereum/EIPs/issues/20
 */
contract TRC10 is TRC10Basic {

    event Approval(
        address indexed owner,
        address indexed spender,
        uint256 value
    );
}
