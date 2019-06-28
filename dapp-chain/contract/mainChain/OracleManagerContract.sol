pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "../common/ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => bool) public isOracle;

    uint256 public numOracles;
    uint256 public numCommonOracles;
    mapping(uint256=>mapping(bytes32 => SignMsg)) withdrawMultiSignList;

    struct SignMsg {
        mapping(address => bool) signedOracle;
        mapping(bytes => bool) signList;
        uint256 countSign;
    }

    event NewOracles(address oracle);

    modifier onlyOracle() {require(isOracle[msg.sender], "not oracle");
        _;}

    function checkOracles(bytes32 dataHash, uint256 nonce, bytes[] sigList) internal returns(uint256) {
        SignMsg storage msl = withdrawMultiSignList[nonce][dataHash];
        if (msl.countSign > numCommonOracles) {
            return msl.countSign;
        }
        for (uint256 i = 0; i < sigList.length; i++) {
            if(msl.signList[sigList[i]]){
                continue;
            }
            address _oracle = dataHash.recover(sigList[i]);
            if (isOracle[_oracle] && !msl.signedOracle[_oracle]) {
                msl.signedOracle[_oracle] = true;
                msl.signList[sigList[i]] =true;
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
        numCommonOracles=numOracles * 2 / 3;
    }

    function delOracle(address _oracle) public onlyOwner {
        require(isOracle[_oracle], "oracle is not oracle");
        isOracle[_oracle] = false;
        numOracles--;
        numCommonOracles=numOracles * 2 / 3;
    }
}
