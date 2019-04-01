pragma solidity ^0.4.24;


/**
 * @title TRC20 token receiver interface
 * @dev Interface for any contract that wants to support safeTransfers
 *  from TRC20 asset contracts.
 */
contract TRC10Receiver {
    /**
     * @dev Magic value to be returned upon successful reception of an NFT
     *  Equals to `bytes4(keccak256("onTRC20Received(uint256,uint256)"))`,
     *  which can be also obtained as `TRC20Receiver(0).onTRC20Received.selector`
     */
    bytes4 constant TRC10_RECEIVED = 0x7d775a16;

    function onTRC10Received(uint256 _tokenId, uint256 amount) public returns (bytes4);
}
