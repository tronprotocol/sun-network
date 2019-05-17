pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "../common/ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => address) public allowes;
    mapping(address => uint256) public nonces;
    mapping(address => bool) oracles;

    uint256 public numOracles;
    mapping(bytes32=>WithdrawMsg) witdrawLsit; 
    struct WithdrawMsg{
        address to;
        uint256 value;
        address contractAddress;
        mapping(address=>bool) signedOracle;
        uint256 conuntSign;
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

    modifier checkGainer(address _to,uint256 num, address contractAddress, bytes sig) {
        require(isOracle(msg.sender));
        uint256[] memory nonum=new uint256[](2);
        nonum[0]=nonces[_to];
        nonum[1]=num;
        bytes32 hash = keccak256(abi.encodePacked(contractAddress,nonum));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");
        nonces[_to]++;
        _;
    }

    modifier checkTrc10Gainer(address _to,uint256 num, trcToken tokenId, bytes sig) {
        require(isOracle(msg.sender));
        uint256[] memory nonum=new uint256[](3);
        nonum[0]=tokenId;
        nonum[1]=nonces[_to];
        nonum[2]=num;
        bytes32 hash = keccak256(abi.encodePacked(nonum));
        address sender = hash.recover(sig);
        require(sender == _to, "Message not signed by a gainer");
        nonces[_to]++;
        _;
    }

    modifier checkOracles(address _to,uint256 num, address contractAddress, bytes32 txid, bytes[] sigList) {
        require(isOracle(msg.sender));
        WithdrawMsg storage wm;

        uint256[] memory nonum=new uint256[](3);
        nonum[0]=nonces[_to];
        nonum[1]=num;
        bytes32 hash = keccak256(abi.encodePacked(contractAddress, nonum, txid));
        for (uint256 i=0; i<sigList.length; i++){
            address _oracle = hash.recover(sigList[i]);
            if(isOracle(_oracle)&&!wm.signedOracle[_oracle]){
                wm.signedOracle[_oracle]=true;
                wm.conuntSign++;
            }
        }
        require(wm.conuntSign > numOracles * 2 / 3,"oracle num not enough 2/3");
        nonces[_to]++;
        witdrawLsit[txid]=wm;
        _;
    }

    modifier checkTrc10Oracles(address _to,uint256 num, trcToken tokenId, bytes32 txid, bytes[] sigList) {
        require(isOracle(msg.sender));
        WithdrawMsg storage wm;

        uint256[] memory nonum=new uint256[](3);
        nonum[0]=tokenId;
        nonum[1]=nonces[_to];
        nonum[2]=num;
        bytes32 hash = keccak256(abi.encodePacked(nonum,txid));
        for (uint256 i=0; i<sigList.length; i++){
            address _oracle = hash.recover(sigList[i]);
            if(isOracle(_oracle)){
                wm.signedOracle[_oracle]=true;
                wm.conuntSign++;
            }
        }
        require(wm.conuntSign > numOracles*2/3,"oracle num not enough 2/3");
        nonces[_to]++;
        witdrawLsit[txid]=wm;
        _;
    }

    function isOracle(address _address) public view returns (bool) {
        if (_address == owner) {
            return true;
        }
        return oracles[_address];
    }

    function migrationToken(address mainChainToken,address sideChainToken) public onlyOracle {
        allowes[mainChainToken] = sideChainToken;
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
