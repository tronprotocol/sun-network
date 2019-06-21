pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "../common/ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => address) public allows;
    mapping(address => bool) oracles;

    uint256 public numOracles;
    mapping(uint256 => SignMsg) withdrawMultiSignList;

    struct SignMsg {
        mapping(address => bool) signedOracle;
        uint256 countSign;
    }
    // address[]  _oracles;
    event NewOracles(address oracle);

    modifier onlyOracle() {require(isOracle(msg.sender), "not oracle");
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

    function checkOracles(bytes32 dataHash, uint256 nonce, bytes[] sigList) internal {
        SignMsg storage msl = withdrawMultiSignList[nonce];
        for (uint256 i = 0; i < sigList.length; i++) {
            address _oracle = dataHash.recover(sigList[i]);
            if (isOracle(_oracle) && !msl.signedOracle[_oracle]) {
                msl.signedOracle[_oracle] = true;
                msl.countSign++;
                if (msl.countSign > numOracles * 2 / 3) {
                    return;
                }
            }
        }
        revert("oracle num is not enough 2/3");
    }

    function isOracle(address _address) public view returns (bool) {
        if (_address == owner) {
            return true;
        }
        return oracles[_address];
    }

    function addOracle(address _oracle) public onlyOwner {
        require(!isOracle(_oracle), "oracle is oracle");
        oracles[_oracle] = true;
        numOracles++;
    }

    function delOracle(address _oracle) public onlyOwner {
        require(isOracle(_oracle), "oracle is not oracle");
        oracles[_oracle] = false;
        numOracles--;
    }
}
