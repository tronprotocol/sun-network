pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "./ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => address) public allowes;
    mapping(address => uint256) public nonces;
    mapping(address => bool) oracles;

    uint256 public numOracles;

    // address[]  _oracles;
    event NewOracles(address oracle);

    modifier onlyOracle() {require(checkOracle(msg.sender));
        _;}

    constructor(address _oracle) public {
        // _oracles.push(_oracle);
        // uint256 length = _oracles.length;
        // require(length > 0);

        // for (uint256 i = 0; i < length; i++) {
        //     require(_oracles[i] != address(0));
        //     oracles[_oracles[i]] = true;
        //     emit NewOracles(_oracles[i]);
        // }
        // numOracles = _oracles.length;

        numOracles = 1;
        oracles[_oracle] = true;
        emit NewOracles(_oracle);
    }

    modifier checkGainer(address _to,uint256 num, address contractAddress, bytes sig) {
        require(checkOracle(msg.sender));
        uint256[] memory nonum=new uint256[](2);
        nonum[0]=nonces[_to];
        nonum[1]=num;
        bytes32 hash = keccak256(abi.encodePacked(contractAddress,nonum));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");
        _;
        nonces[_to]++;
    }

    modifier checkTrc10Gainer(address _to,uint256 num, trcToken tokenId, bytes sig) {
        require(checkOracle(msg.sender));
        uint256[] memory nonum=new uint256[](3);
        nonum[0]=tokenId;
        nonum[1]=nonces[_to];
        nonum[2]=num;
        bytes32 hash = keccak256(abi.encodePacked(nonum));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");
        _;
        nonces[_to]++;
    }
    function checkOracle(address _address) public view returns (bool) {
        if (_address == owner) {
            return true;
        }
        return oracles[_address];
    }

    function migrationToken(address mainChainToken,address sideChainToken) public onlyOracle {
        allowes[mainChainToken] = sideChainToken;
    }
}
