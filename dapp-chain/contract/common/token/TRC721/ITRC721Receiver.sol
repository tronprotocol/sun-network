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

    function onTRC721Received(address from, uint256 tokenId) payable public returns (uint256);

    function getWithdrawFee() view public returns (uint256);
}