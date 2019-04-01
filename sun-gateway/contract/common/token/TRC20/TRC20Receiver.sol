pragma solidity ^0.4.24;


/**
 * @title TRC20 token receiver interface
 * @dev Interface for any contract that wants to support safeTransfers
 *  from TRC20 asset contracts.
 */
contract TRC20Receiver {
    /**
     * @dev Magic value to be returned upon successful reception of an NFT
     *  Equals to `bytes4(keccak256("onTRC20Received(address,uint256,bytes)"))`,
     *  which can be also obtained as `TRC20Receiver(0).onTRC20Received.selector`
     */
    bytes4 constant TRC20_RECEIVED = 0xbc04f0af;

    function onTRC20Received(address _from, uint256 amount) public returns (bytes4);
}
