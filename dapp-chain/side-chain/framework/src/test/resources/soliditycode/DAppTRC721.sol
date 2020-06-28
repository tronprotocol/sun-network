import "./TRC721.sol";
import "./ITRC721Receiver.sol";
import "./IDApp.sol";

/**
 * @title Full TRC721 Token for Sun Network DAppChains
 */

contract DAppTRC721 is TRC721, IDApp {
    // Transfer Gateway contract address
    address public gateway;

    string public name;
    string public symbol;
    // address public owner;

    /**
      * @dev Constructor function
      */

    constructor (address _gateway, string memory _name, string memory _symbol) public {
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
    function mint(address to, uint256 tokenId) external onlyGateway {
        require(to != address(0));
        require(!_exists(tokenId));

        _tokenOwner[tokenId] = to;
        _ownedTokensCount[to] = _ownedTokensCount[to].add(1);

        emit Transfer(address(0), to, tokenId);
    }

    function withdrawal(uint256 tokenId) payable external returns (uint256 r) {
        uint256 withdrawFee = ITRC721Receiver(gateway).getWithdrawFee();
        require(msg.value >= withdrawFee, "value must be >= withdrawFee");
        if (msg.value > withdrawFee) {
            msg.sender.transfer(msg.value - withdrawFee);
        }
        transfer(gateway, tokenId);
        r = ITRC721Receiver(gateway).onTRC721Received.value(withdrawFee)(msg.sender, tokenId);
    }
}