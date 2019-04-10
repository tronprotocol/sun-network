pragma solidity ^0.4.24;

/**
 * @title TRC721 token receiver interface
 * @dev Interface for any contract that wants to support safeTransfers
 *  from TRC721 asset contracts.
 */
contract ITRC721Receiver {
    /**
     * @dev Magic value to be returned upon successful reception of an NFT
     *  Equals to `bytes4(keccak256("onTRC721Received(address,uint256,bytes)"))`,
     *  which can be also obtained as `ITRC721Receiver(0).onTRC721Received.selector`
     */
    bytes4 constant _TRC721_RECEIVED = 0xcb912b1e;

    /**
     * @notice Handle the receipt of an NFT
     * @dev The TRC721 smart contract calls this function on the recipient
     *  after a `safetransfer`. This function MAY throw to revert and reject the
     *  transfer. This function MUST use 50,000 gas or less. Return of other
     *  than the magic value MUST result in the transaction being reverted.
     *  Note: the contract address is always the message sender.
     * @param from The sending address
     * @param tokenId The NFT identifier which is being transfered
     * @param txData Additional data with no specified format
     * @return `bytes4(keccak256("onTRC721Received(address,uint256,bytes)"))`
     */
    function onTRC721Received(address from, uint256 tokenId, bytes memory txData) public returns (bytes4);
}