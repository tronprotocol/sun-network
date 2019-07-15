pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "../common/ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;


    uint256 public numOracles;
    uint256 public numCommonOracles;
    mapping(address => bool) public isOracle;
    mapping(address => SignMsg)  delegateSigns;
    mapping(uint256 => mapping(bytes32 => SignMsg)) withdrawMultiSignList;

    address logicAddress;
    bool pause;
    bool stop;

    struct SignMsg {
        mapping(address => bool) signedOracle;
        mapping(bytes => bool) signList;
        uint256 countSign;
        bool success;
    }

    event NewOracles(address oracle);
    event LogicAddressChanged(address oldAddress, address newAddress);

    modifier onlyOracle() {require(isOracle[msg.sender], "not oracle");
        _;}
    modifier onlyNotPause() {require(!pause, "is pause");
        _;}
    modifier onlyNotStop() {require(!stop, "is stop");
        _;}

    modifier goDelegateCall() {
        if (logicAddress != address(0)) {
            logicAddress.delegatecall(msg.data);
            return;
        }
        _;
    }

    modifier checkForTrc10(uint64 tokenId, uint64 tokenValue) {
        require(tokenId == uint64(msg.tokenid), "tokenId != msg.tokenid");
        require(tokenValue == uint64(msg.tokenvalue), "tokenValue != msg.tokenvalue");
        _;
    }

    function checkOracles(bytes32 dataHash, uint256 nonce, bytes[] sigList) internal returns (uint256) {
        SignMsg storage msl = withdrawMultiSignList[nonce][dataHash];
        if (msl.countSign > numCommonOracles) {
            return msl.countSign;
        }
        for (uint256 i = 0; i < sigList.length; i++) {
            if (msl.signList[sigList[i]]) {
                continue;
            }
            address _oracle = dataHash.recover(sigList[i]);
            if (isOracle[_oracle] && !msl.signedOracle[_oracle]) {
                msl.signedOracle[_oracle] = true;
                msl.signList[sigList[i]] = true;
                msl.countSign++;
                if (msl.countSign > numCommonOracles) {
                    break;
                }
            }
        }
        return msl.countSign;
    }

    function addOracle(address _oracle) public onlyOwner {
        require(!isOracle[_oracle], "oracle is oracle");
        isOracle[_oracle] = true;
        numOracles++;
        numCommonOracles = numOracles * 2 / 3;
    }

    function delOracle(address _oracle) public onlyOwner {
        require(isOracle[_oracle], "oracle is not oracle");
        isOracle[_oracle] = false;
        numOracles--;
        numCommonOracles = numOracles * 2 / 3;
    }

    function setDelegateAddress(address newAddress) public onlyOracle {
        bool needDelegate = multiSignForDelegate(newAddress);
        if (needDelegate) {
            emit LogicAddressChanged(logicAddress, newAddress);
            logicAddress = newAddress;
        }
    }

    function setPause(bool status) public onlyOwner {
        pause = status;
    }

    function setStop(bool status) public onlyOwner {
        stop = status;
    }

    function multiSignForDelegate(address newAddress) internal returns (bool) {
        if (delegateSigns[newAddress].signedOracle[msg.sender]) {
            return false;
        }
        delegateSigns[newAddress].signedOracle[msg.sender] = true;
        delegateSigns[newAddress].countSign += 1;

        if (delegateSigns[newAddress].countSign > numCommonOracles && !delegateSigns[newAddress].success) {
            delegateSigns[newAddress].success = true;
            return true;
        }
        return false;
    }
}
