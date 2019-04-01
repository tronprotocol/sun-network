pragma solidity ^0.4.0;


pragma solidity ^0.4.24;

import "openzeppelin-solidity/contracts/token/ERC721/ERC721Token.sol";
import "openzeppelin-solidity/contracts/token/ERC721/ERC721Receiver.sol";
import "./ERC721DAppToken.sol";

/**
 * @title Full ERC721 Token for Loom DAppChains
 * This implementation includes all the required and some optional functionality of the ERC721
 * standard, it also contains some required functionality for Loom DAppChain compatiblity.
 */
contract CryptoCardsDappChain is ERC721DAppToken, ERC721Token, ERC721Receiver {
    // Transfer Gateway contract address
    address public gateway;

    /**
      * @dev Constructor function
      */
    constructor(address _gateway) ERC721Token("CryptoCardsDappChain", "CRC") public {
        gateway = _gateway;
    }

    // Called by the gateway contract to mint tokens that have been deposited to the Mainnet gateway.
    function mintToGateway(uint256 _uid) public {
        require(msg.sender == gateway);
        _mint(gateway, _uid);
    }

    function onERC721Received(
        address _from,
        uint256 _tokenId,
        bytes _data
    ) public returns (bytes4) {
        return ERC721_RECEIVED;
    }
}


contract Gateway {

    mapping(address => address) mainToSideContractMap;
    mapping(address => address) sideToMainContractMap;

    constructor () public {

    }

    modifier onlyOracle {
        require(msg.sender == oracle);
        _;
    }

    function gateway(address oracle){

    }

    function calcContractAddress(bytes memory txId, address owner) public returns (address c){
        bytes memory addressBytes = addressToBytes(owner);
        bytes memory combinedBytes = concatBytes(txId, addressBytes);
        c = keccak256(combinedBytes);
    }

    function addressToBytes(address a) public pure returns (bytes memory b){
        assembly {
            let m := mload(0x40)
            a := and(a, 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF)
            mstore(add(m, 20), xor(0x140000000000000000000000000000000000000000, a))
            mstore(0x40, add(m, 52))
            b := m
        }
    }

    function concatBytes(bytes memory b1, bytes memory b2) pure public returns (bytes memory r) {
        r = new bytes(b1.length + b2.length);
        uint256 k = 0;
        for (uint256 i = 0; i < b1.length; i++)
            r[k++] = b1[i];
        for (i = 0; i < b2.length; i++)
            r[k++] = b2[i];
    }

    function createBaseContract(uint256 contractType, bytes txId) public onlyOracle {
        address sideAddress;
        if (contractType == 1) {
            sideAddress = new ERC721Token(address(this));
        } else if (contractType == 2) {
            sideAddress = new ERC20Token(address(this));
        } else {
            require("unsupported type");
        }

        address mainAddress = calcContractAddress(txId, msg.sender);
        mainToSideContractMap[mainAddress] = sideAddress;
        sideToMainContractMap[sideAddress] = mainAddress;
    }
}
