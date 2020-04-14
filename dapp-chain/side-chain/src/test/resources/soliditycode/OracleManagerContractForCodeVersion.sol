import "./OwnableForCodeVersion.sol";
import "./ECVerify.sol";


contract OracleManagerContract is Ownable {
    using ECVerify for bytes32;

    uint256 public numOracles;
    mapping(address => uint256) public oracleIndex;
    mapping(uint256 => address) public indexOracle;
    mapping(address => SignMsg)  delegateSigns;
    mapping(uint256 => mapping(bytes32 => SignMsg)) withdrawMultiSignList;

    bool public multivalidatesignSwitch;
    bool public pause;
    bool public stop;

    struct SignMsg {
        uint256 signedOracleFlag;
        uint64 countSign;
        bool success;
    }

    event NewOracles(address oracle);

    modifier onlyOracle() {require(oracleIndex[msg.sender] > 0, "not oracle");
        _;}
    modifier isHuman() {require(msg.sender == tx.origin, "not allow contract");
        _;}
    modifier onlyNotPause() {require(!pause, "is pause");
        _;}
    modifier onlyNotStop() {require(!stop, "is stop");
        _;}

    modifier checkForTrc10(uint64 tokenId, uint64 tokenValue) {
        require(tokenId == uint64(msg.tokenid), "tokenId != msg.tokenid");
        require(tokenValue == uint64(msg.tokenvalue), "tokenValue != msg.tokenvalue");
        _;
    }

    function checkOracles(bytes32 dataHash, uint256 nonce, bytes[] memory sigList, address[] memory signOracles) internal returns (bool) {
        require(sigList.length == signOracles.length, "error sigList.length or signOracles.length");
        if (multivalidatesignSwitch) {
            uint256 signFlag = 0;
            for (uint256 i = 0; i < sigList.length; i++) {
                uint256 _oracleIndex = oracleIndex[signOracles[i]];
                if (_oracleIndex == 0) {
                    signOracles[i] = address(0);
                    continue;
                }
                uint256 thisFlag = (1 << (_oracleIndex - 1));
                if ((thisFlag & signFlag) == 0) {
                    // not signed
                    signFlag = thisFlag | signFlag;
                } else {
                    signOracles[i] = address(0);
                }
            }
            return checkOraclesWithMultiValidate(dataHash, nonce, sigList, signOracles);
        }

        SignMsg storage signMsg = withdrawMultiSignList[nonce][dataHash];
        uint256 _signedOracleFlag = signMsg.signedOracleFlag;
        uint64 _countSign = signMsg.countSign;

        for (uint256 i = 0; i < sigList.length; i++) {
            address _oracle = dataHash.recover(sigList[i]);
            uint256 _oracleIndex = oracleIndex[_oracle];
            if (_oracleIndex == 0) {// not oracle
                continue;
            }
            uint256 thisFlag = (1 << (_oracleIndex - 1));
            if ((thisFlag & _signedOracleFlag) == 0) {
                // not signed
                _signedOracleFlag = thisFlag | _signedOracleFlag;
                _countSign++;
            }
        }
        signMsg.signedOracleFlag = _signedOracleFlag;
        signMsg.countSign = _countSign;

        if (!signMsg.success && signMsg.countSign > numOracles * 2 / 3) {
            signMsg.success = true;
            return true;
        }
        return false;
    }

    function checkOraclesWithMultiValidate(bytes32 dataHash, uint256 nonce, bytes[] memory sigList, address[] memory oracleList) internal returns (bool) {
        SignMsg storage signMsg = withdrawMultiSignList[nonce][dataHash];

        bytes32 ret = batchvalidatesign(dataHash, sigList, oracleList);
        signMsg.countSign = countSuccess(ret);

        if (!signMsg.success && signMsg.countSign > numOracles * 2 / 3) {
            signMsg.success = true;
            return true;
        }
        return false;
    }

    function countSuccess(bytes32 ret) internal returns (uint64 count) {
        uint256 _num = uint256(ret);
        for (; _num > 0; ++count) {_num &= (_num - 1);}
        return count;
    }

    function addOracle(address _oracle) public onlyOwner {
        require(_oracle != address(0), "this address cannot be zero");
        require(oracleIndex[_oracle] == 0, "this address is already oracle");
        require(numOracles < 256, "cannot more than 256 oracles");

        uint256 i;
        for (i = 1; i <= 256; i++) {
            if (indexOracle[i] == address(0)) {
                break;
            }
        }
        oracleIndex[_oracle] = i;
        indexOracle[i] = _oracle;

        numOracles++;
    }

    function delOracle(address _oracle) public onlyOwner {
        require(oracleIndex[_oracle] > 0, "this address is not oracle");

        indexOracle[oracleIndex[_oracle]] = address(0);
        oracleIndex[_oracle] = 0;

        numOracles--;
    }

    function setLogicAddress(address _logicAddress) public onlyOracle {
        if (multiSignForDelegate(_logicAddress)) {
            changeLogicAddress(_logicAddress);
        }
    }

    function setPause(bool status) public onlyOwner {
        pause = status;
    }

    function setStop(bool status) public onlyOwner {
        stop = status;
    }

    function setMultivalidatesignSwitch(bool status) public onlyOwner {
        multivalidatesignSwitch = status;
    }

    function multiSignForDelegate(address newAddress) internal returns (bool) {
        SignMsg storage signMsg = delegateSigns[newAddress];
        uint256 _signedOracleFlag = signMsg.signedOracleFlag;
        uint256 _thisFlag = (1 << (oracleIndex[msg.sender] - 1));

        if (_thisFlag & _signedOracleFlag == 0) {
            signMsg.signedOracleFlag = _thisFlag | _signedOracleFlag;
            signMsg.countSign++;

            if (!signMsg.success && signMsg.countSign > numOracles * 2 / 3) {
                signMsg.success = true;
                return true;
            }
        }
        return false;
    }

    function withdrawDone(bytes32 dataHash, uint256 nonce) view public returns (bool r) {
        r = withdrawMultiSignList[nonce][dataHash].success;
    }

    function isOracle(address _oracle) view public returns (bool) {
        return oracleIndex[_oracle] > 0;
    }
}