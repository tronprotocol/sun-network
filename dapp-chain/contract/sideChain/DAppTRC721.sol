pragma solidity ^0.4.24;

import "../common/token/TRC721/TRC721.sol";
import "../common/token/TRC721/ITRC721Receiver.sol";
import "./IDApp.sol";

/**
 * @title Full TRC721 Token for Sun Network DAppChains
 */

contract DAppTRC721 is TRC721, ITRC721Receiver, IDApp {
    // Transfer Gateway contract address
    address public gateway;

    string public name;
    string public symbol;
    // address public owner;

    /**
      * @dev Constructor function
      */

    constructor (address _gateway, string _name, string _symbol) public {
        gateway = _gateway;
        name = _name;
        symbol = _symbol;
    }

    modifier onlyGateway {
        require(msg.sender == gateway);
        _;
    }

    /**
         * @dev Internal function to mint a new token.
         * Reverts if the given token ID already exists.
         * @param to The address that will own the minted token
         * @param tokenId uint256 ID of the token to be minted
         */
    function mint(address to, uint256 tokenId) public onlyGateway {
        require(to != address(0));
        require(!_exists(tokenId));

        _tokenOwner[tokenId] = to;
        _ownedTokensCount[to] = _ownedTokensCount[to].add(1);

        emit Transfer(address(0), to, tokenId);
    }

    /**
     * @dev Safely transfers the ownership of a given token ID to another address
     * If the target address is a contract, it must implement `onERC721Received`,
     * which is called upon a safe transfer, and return the magic value
     * `bytes4(keccak256("onERC721Received(address,address,uint256,bytes)"))`; otherwise,
     * the transfer is reverted.
     * Requires the msg.sender to be the owner, approved, or operator
     * @param from current owner of the token
     * @param to address to receive the ownership of the given token ID
     * @param tokenId uint256 ID of the token to be transferred
     * @param _data bytes data to send along with a safe transfer check
     */
    function withdrawal(uint256 tokenId, bytes memory txData) public {
        transfer(gateway, tokenId);
        bytes4 retval = ITRC721Receiver(gateway).onTRC721Received(msg.sender, tokenId, txData);
        require(retval == _TRC721_RECEIVED);
    }
}