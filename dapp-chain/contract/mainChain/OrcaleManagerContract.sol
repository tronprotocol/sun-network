pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "./ECVerify.sol";


contract OrcaleManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => address) public allowes;
    mapping(address => uint256) public nonces;
    mapping(address => bool) orcales;

    uint256 public numOrcales;
    uint256 public nonce; 

    event NewOrcales(address orcale);

    modifier onlyOrcale() {require(checkOrcale(msg.sender));
        _;}

    constructor(address[] _orcales) public {
        uint256 length = _orcales.length;
        require(length > 0);

        for (uint256 i = 0; i < length; i++) {
            require(_orcales[i] != address(0));
            orcales[_orcales[i]] = true;
            emit NewOrcales(_orcales[i]);
        }
        numOrcales = _orcales.length;
    }

    modifier checkGainer(address _to,uint256 num, address contractAddress, bytes sig) {

        bytes32 hash = keccak256(abi.encodePacked(contractAddress, nonces[_to], num));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");
        _;
        nonces[msg.sender]++;
    }

    function checkOrcale(address _address) public view returns (bool) {
        if (_address == owner) {
            return true;
        }
        return orcales[_address];
    }

    function migrationToken(address mainChainToken,address sideChainToken) public onlyOrcale {
        allowes[mainChainToken] = sideChainToken;
    }
}
