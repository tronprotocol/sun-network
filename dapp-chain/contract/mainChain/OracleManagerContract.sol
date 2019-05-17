pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "../common/ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => address) public allows;
    mapping(address => uint256) public nonce;
    mapping(address => bool) oracles;

    uint256 public numOracles;
    mapping(bytes32=>SignMsg) withdrawList;
    struct SignMsg{
        mapping(address=>bool) signedOracle;
        uint256 countSign;
    }
    // address[]  _oracles;
    event NewOracles(address oracle);

    modifier onlyOracle() {require(isOracle(msg.sender));
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

    function checkGainer(address _to,uint256 num, address contractAddress, bytes sig) internal {
        uint256[] memory nonum=new uint256[](2);
        nonum[0]= nonce[_to];
        nonum[1]=num;
        bytes32 hash = keccak256(abi.encodePacked(contractAddress,nonum));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");
        
    }

    function checkTrc10Gainer(address _to,uint256 num, trcToken tokenId, bytes sig) internal {
        uint256[] memory nonum=new uint256[](3);
        nonum[0]=tokenId;
        nonum[1]= nonce[_to];
        nonum[2]=num;
        bytes32 hash = keccak256(abi.encodePacked(nonum));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");

    }

    function checkOracles(address _to,uint256 num, address contractAddress, bytes32 txid, bytes[] sigList) internal {
        SignMsg storage wm;

        uint256[] memory nonum=new uint256[](2);
        nonum[0]= nonce[_to];
        nonum[1]=num;
        bytes32 hash = keccak256(abi.encodePacked(_to, contractAddress, nonum, txid));
        for (uint256 i=0; i<sigList.length; i++){
            address _oracle = hash.recover(sigList[i]);
            if(isOracle(_oracle)&&!wm.signedOracle[_oracle]){
                wm.signedOracle[_oracle]=true;
                wm.conuntSign++;
            }
        }
        require(wm.conuntSign > numOracles * 2 / 3,"oracle num not enough 2/3");
        withdrawList[txid]=wm;
        
    }

    function checkTrc10Oracles(address _to,uint256 num, trcToken tokenId, bytes32 txid, bytes[] sigList) internal {
        SignMsg storage wm;

        uint256[] memory nonum=new uint256[](3);
        nonum[0]=tokenId;
        nonum[1]= nonce[_to];
        nonum[2]=num;
        bytes32 hash = keccak256(abi.encodePacked(to, nonum,txid));
        for (uint256 i=0; i<sigList.length; i++){
            address _oracle = hash.recover(sigList[i]);
            if(isOracle(_oracle)){
                wm.signedOracle[_oracle]=true;
                wm.conuntSign++;
            }
        }
        require(wm.conuntSign > numOracles*2/3,"oracle num not enough 2/3");
        withdrawList[txid]=wm;
        
    }

    function isOracle(address _address) public view returns (bool) {
        if (_address == owner) {
            return true;
        }
        return oracles[_address];
    }

    function migrationToken(address mainChainToken,address sideChainToken) public onlyOracle {
        allows[mainChainToken] = sideChainToken;
    }

    function addOracle(address _oracle) public onlyOwner {
        require(!isOracle(_oracle));
        oracles[_oracle] = true;
        numOracles++;
    }
    function delOracle(address _oracle) public onlyOwner {
        require(!isOracle(_oracle));
        oracles[_oracle] = false;
        numOracles--;
    }
}
