pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "../common/token/TRC20/ITRC20Receiver.sol";
import "../common/token/TRC721/ITRC721Receiver.sol";
import "./DAppTRC20.sol";
import "./DAppTRC721.sol";
import "../common/ECVerify.sol";

contract SideChainGateway is ITRC20Receiver, ITRC721Receiver {
    using ECVerify for bytes32;

    // 1. deployDAppTRC20AndMapping
    // 2. deployDAppTRC721AndMapping
    // 3. depositTRC10
    // 4. depositTRC20
    // 5. depositTRC721
    // 6. depositTRX
    // 7. withdrawTRC10
    // 8. withdrawTRC20
    // 9. withdrawTRC721
    // 10. withdrawTRX


    event DeployDAppTRC20AndMapping(address developer, address mainChainAddress, address sideChainAddress);
    event DeployDAppTRC721AndMapping(address developer, address mainChainAddress, address sideChainAddress);
    event DepositTRC10(address to, uint256 trc10, uint256 value);
    event DepositTRC20(address sideChainAddress, address to, uint256 value);
    event DepositTRC721(address sideChainAddress, address to, uint256 tokenId);
    event DepositTRX(address to, uint256 value);

    event WithdrawTRC10(uint256 nonce, address from, uint256 value, uint256 trc10, bytes userSign);
    event WithdrawTRC20(uint256 nonce, address from, uint256 value, address mainChainAddress, bytes userSign);
    event WithdrawTRC721(uint256 nonce, address from, uint256 tokenId, address mainChainAddress, bytes userSign);
    event WithdrawTRX(uint256 nonce, address from, uint256 value, bytes userSign);

    event MultiSignForWithdrawTRC10(address from, uint256 trc10, uint256 value, bytes userSign, bytes32 dataHash, uint256 nonce);
    event MultiSignForWithdrawToken(address from, address mainChainAddress, uint256 valueOrTokenId, uint256 _type, bytes userSign, bytes32 dataHash, uint256 nonce);
    event MultiSignForWithdrawTRX(address from, uint256 value, bytes userSign, bytes32 dataHash, uint256 nonce);
    event MultiSignForDeployAndMapping(address mainChainAddress, address sideChainAddress, bytes32 dataHash, bytes32 txId);



    // TODO: type enum
    mapping(address => address) public mainToSideContractMap;
    mapping(address => address) public sideToMainContractMap;
    mapping(uint256 => bool) public trc10Map;
    mapping(address => bool) public oracles;
    uint256 oracleCnt;
    address public owner;
    address public sunTokenAddress;
    address mintTRXContract = 0x10000;
    address mintTRC10Contract = 0x10001;
    uint256 mappingFee;
    uint256 withdrawMinTrx = 0;
    uint256 withdrawMinTrc10 = 0;
    uint256 withdrawMinTrc20 = 0;
    uint256 bonus;

    mapping(bytes32 => mapping(bytes32 => SignMsg)) public depositSigns;
    mapping(uint256 => mapping(bytes32 => SignMsg)) public withdrawSigns;
    SignMsg[] public withdrawStatusSigns;
    mapping(bytes32 => mapping(bytes32 => SignMsg)) public mappingToSideSigns;
    mapping(bytes32 => SignMsg) depositList;
    WithdrawMsg[] userWithdrawList;

    struct SignMsg {
        mapping(address => bool) oracleSigned;
        bytes[] signs;
        uint256 signCnt;
        bool success;
    }

    struct WithdrawMsg {
        // _type:
        // 1: trx
        // 2: trc20
        // 3: trc721
        // 4: trc10
        uint8 _type;
        address user;
        uint256 valueOrTokenId;
        uint256 trc10;
        address mainChainAddress;
        bytes userSign;
        // status:
        // 0: locking
        // 1: success
        // 2: fail
        // 3: refunded
        uint8 status;
    }

    constructor (address _oracle) public {
        owner = msg.sender;
        oracles[_oracle] = true;
        oracleCnt = 1;
    }

    modifier onlyOracle {
        require(oracles[msg.sender]);
        _;
    }

    modifier onlyOwner {
        require(msg.sender == owner);
        _;
    }

    function getWithdrawSigns(uint256 nonce, bytes32 dataHash) view returns (bytes[]) {
        return withdrawSigns[nonce][dataHash].signs;
    }

    function modifyOracle(address _oracle, bool isOracle) public onlyOwner {
        if (oracles[_oracle] && !isOracle) {
            oracleCnt -= 1;
        }
        if (!oracles[_oracle] && isOracle) {
            oracleCnt += 1;
        }
        oracles[_oracle] = isOracle;
    }

    function setSunTokenAddress(address _sunTokenAddress) public onlyOwner {
        require(_sunTokenAddress != address(0), "_sunTokenAddress == address(0)");
        sunTokenAddress = _sunTokenAddress;
    }

    // 1. deployDAppTRC20AndMapping
    function deployDAppTRC20AndMapping(address mainChainAddress, string name, string symbol, uint8 decimals) internal returns (address r) {

        require(mainToSideContractMap[mainChainAddress] == address(0), "the main chain address has mapped");
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        address sideChainAddress = new DAppTRC20(address(this), name, symbol, decimals);
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC20AndMapping(msg.sender, mainChainAddress, sideChainAddress);
        r = sideChainAddress;
    }

    function multiSignForDeployDAppTRC20AndMapping(address mainChainAddress, string name, string symbol, uint8 decimals, bytes32 txId, bytes oracleSign) public onlyOracle {

        bytes32 dataHash = keccak256(abi.encodePacked(mainChainAddress, name, symbol, uint256(decimals), txId));

        require(dataHash.recover(oracleSign) == msg.sender, "sign error");

        bool needMapping = multiSignForMappingToSide(txId, dataHash, oracleSign);
        if (needMapping) {
            deployDAppTRC20AndMapping(mainChainAddress, name, symbol, decimals);
        }
    }

    // 2. deployDAppTRC721AndMapping
    function deployDAppTRC721AndMapping(address mainChainAddress, string name, string symbol) internal returns (address r) {
        // can be called by everyone (contract developer)
        // require(sunTokenAddress != address(0), "sunTokenAddress == address(0)");

        require(mainToSideContractMap[mainChainAddress] == address(0), "the main chain address has mapped");
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        address sideChainAddress = new DAppTRC721(address(this), name, symbol);
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC721AndMapping(msg.sender, mainChainAddress, sideChainAddress);
        r = sideChainAddress;
    }

    function multiSignForDeployDAppTRC721AndMapping(address mainChainAddress, string name, string symbol, bytes32 txId, bytes oracleSign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(mainChainAddress, name, symbol, txId));

        require(dataHash.recover(oracleSign) == msg.sender, "sign error");

        bool needMapping = multiSignForMappingToSide(txId, dataHash, oracleSign);
        if (needMapping) {
            deployDAppTRC721AndMapping(mainChainAddress, name, symbol);
        }
    }
    // deposit deposit deposit
    // 3. depositTRC10
    function depositTRC10(address to, uint256 trc10, uint256 value, bytes32 name, bytes32 symbol, uint8 decimals) internal {
        require(trc10 > 1000000 && trc10 <= 2000000, "trc10 <= 1000000 or trc10 > 2000000");
        bool exist = trc10Map[trc10];
        if (exist == false) {
            trc10Map[trc10] = true;
        }
        mintTRC10Contract.call(value, trc10, name, symbol, decimals);
        to.transferToken(value, trc10);
        emit DepositTRC10(to, trc10, value);
    }

    // 4. depositTRC20
    function depositTRC20(address to, address mainChainAddress, uint256 value) internal {
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        IDApp(sideChainAddress).mint(to, value);
        emit DepositTRC20(sideChainAddress, to, value);
    }

    // 5. depositTRC721
    function depositTRC721(address to, address mainChainAddress, uint256 tokenId) internal {
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        IDApp(sideChainAddress).mint(to, tokenId);
        emit DepositTRC721(sideChainAddress, to, tokenId);
    }

    // 6. depositTRX
    function depositTRX(address to, uint256 value) internal {
        mintTRXContract.call(value);
        to.transfer(value);
        emit DepositTRX(to, value);
    }

    // _type:
    //      1: trx
    //      2: trc20
    //      3: trc721
    //      4: trc10

    // 7. withdrawTRC10
    function withdrawTRC10(bytes userSign) payable public {
        // TODO: verify userSign
        if (msg.value > 0) {
            bonus += msg.value;
        }
        require(trc10Map[msg.tokenid], "trc10Map[msg.tokenid] == false");
        require(msg.tokenvalue > withdrawMinTrc10, "tokenvalue must be > withdrawMinTrc10");

        userWithdrawList.push(WithdrawMsg(4, msg.sender, msg.tokenvalue, msg.tokenid, address(0), userSign, 0));
        // burn
        address(0).transferToken(msg.tokenvalue, msg.tokenid);
        emit WithdrawTRC10(userWithdrawList.length - 1, msg.sender, msg.tokenvalue, msg.tokenid, userSign);
    }

    // 8. withdrawTRC20
    function onTRC20Received(address from, uint256 value, bytes userSign) public returns (bytes4) {
        // TODO: verify userSign
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");
        require(value > withdrawMinTrc20, "value must be > withdrawMinTrc20");

        userWithdrawList.push(WithdrawMsg(2, from, value, 0, mainChainAddress, userSign, 0));

        DAppTRC20(sideChainAddress).burn(value);
        emit WithdrawTRC20(userWithdrawList.length - 1, from, value, mainChainAddress, userSign);

        return _TRC20_RECEIVED;
    }

    // 9. withdrawTRC721
    function onTRC721Received(address from, uint256 tokenId, bytes userSign) public returns (bytes4) {
        // TODO: verify userSign
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "the trc721 must have been deposited");

        userWithdrawList.push(WithdrawMsg(3, from, tokenId, 0, mainChainAddress, userSign, 0));

        // burn
        DAppTRC721(sideChainAddress).burn(tokenId);
        emit WithdrawTRC721(userWithdrawList.length - 1, from, tokenId, mainChainAddress, userSign);

        return _TRC721_RECEIVED;
    }

    // 10. withdrawTRX
    function withdrawTRX(bytes userSign) payable public {
        // TODO: verify userSign

        require(msg.value > withdrawMinTrx, "value must be > withdrawMinTrx");
        // burn
        userWithdrawList.push(WithdrawMsg(1, msg.sender, msg.value, 0, address(0), userSign, 0));
        address(0).transfer(msg.value);
        emit WithdrawTRX(userWithdrawList.length - 1, msg.sender, msg.value, userSign);
    }

    function retryWithdraw(uint256 nonce) public {
        // TODO: free attack ?
        require(nonce < userWithdrawList.length, "nonce >= userWithdrawList.length");
        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        require(withdrawMsg.status == 0 || withdrawMsg.status == 2, "withdrawMsg.status != 0 && withdrawMsg.status != 2");
        if (withdrawMsg.status == 2) {
            withdrawMsg.status = 0;
        }
        if (withdrawMsg._type == 1) {
            emit WithdrawTRX(nonce, withdrawMsg.user, withdrawMsg.valueOrTokenId, withdrawMsg.userSign);
        } else if (withdrawMsg._type == 2) {
            emit WithdrawTRC20(nonce, withdrawMsg.user, withdrawMsg.valueOrTokenId, withdrawMsg.mainChainAddress, withdrawMsg.userSign);
        } else if (withdrawMsg._type == 3) {
            emit WithdrawTRC721(nonce, withdrawMsg.user, withdrawMsg.valueOrTokenId, withdrawMsg.mainChainAddress, withdrawMsg.userSign);
        } else {
            // 4
            emit WithdrawTRC10(nonce, withdrawMsg.user, withdrawMsg.valueOrTokenId, withdrawMsg.trc10, withdrawMsg.userSign);
        }
    }

    function calcContractAddress(bytes txId, address _owner) public pure returns (address r) {
        bytes memory addressBytes = addressToBytes(_owner);
        bytes memory combinedBytes = concatBytes(txId, addressBytes);
        r = address(keccak256(combinedBytes));
    }

    function addressToBytes(address a) public pure returns (bytes memory b) {
        assembly {
            let m := mload(0x40)
            a := and(a, 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF)
            mstore(add(m, 20), xor(0x140000000000000000000000000000000000000000, a))
            mstore(0x40, add(m, 52))
            b := m
        }
    }

    function concatBytes(bytes memory b1, bytes memory b2) pure public returns (bytes memory r) {
        r = abi.encodePacked(b1, 0x41, b2);
    }

    function multiSignForDeposit(bytes32 txId, bytes32 dataHash, bytes oracleSign) internal returns (bool) {
        if (depositSigns[txId][dataHash].oracleSigned[msg.sender]) {
            return false;
        }
        depositSigns[txId][dataHash].oracleSigned[msg.sender] = true;
        // depositSigns[txId][dataHash].signs.push(oracleSign);
        depositSigns[txId][dataHash].signCnt += 1;

        if (depositSigns[txId][dataHash].signCnt > oracleCnt * 2 / 3 && !depositSigns[txId][dataHash].success) {
            depositSigns[txId][dataHash].success = true;
            return true;
        }
        return false;
    }
    function multiSignForMappingToSide(bytes32 txId, bytes32 dataHash, bytes oracleSign) internal returns (bool) {
        if (mappingToSideSigns[txId][dataHash].oracleSigned[msg.sender]) {
            return false;
        }
        mappingToSideSigns[txId][dataHash].oracleSigned[msg.sender] = true;
        // depositSigns[txId][dataHash].signs.push(oracleSign);
        mappingToSideSigns[txId][dataHash].signCnt += 1;

        if (mappingToSideSigns[txId][dataHash].signCnt > oracleCnt * 2 / 3 && !mappingToSideSigns[txId][dataHash].success) {
            mappingToSideSigns[txId][dataHash].success = true;
            return true;
        }
        return false;
    }

    function multiSignForDepositTRC10(address to, uint256 trc10, uint256 value, bytes32 name, bytes32 symbol, uint8 decimals, bytes32 txId, bytes oracleSign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(to, trc10, value, uint256(decimals), name, symbol, txId));
        require(dataHash.recover(oracleSign) == msg.sender, "sign error");

        bool needDeposit = multiSignForDeposit(txId, dataHash, oracleSign);
        if (needDeposit) {
            depositTRC10(to, trc10, value, name, symbol, decimals);
        }
    }

    // _type:
    //      1: trx
    //      2: trc20
    //      3: trc721
    function multiSignForDepositToken(address to, address mainChainAddress, uint256 valueOrTokenId, uint256 _type, bytes32 txId, bytes oracleSign) public onlyOracle {

        bytes32 dataHash = keccak256(abi.encodePacked(to, mainChainAddress, valueOrTokenId, _type, txId));

        require(dataHash.recover(oracleSign) == msg.sender, "sign error");

        bool needDeposit = multiSignForDeposit(txId, dataHash, oracleSign);
        if (needDeposit) {
            if (_type == 1) {
                depositTRX(to, valueOrTokenId);
            } else if (_type == 2) {
                depositTRC20(to, mainChainAddress, valueOrTokenId);
            } else if (_type == 3) {
                depositTRC721(to, mainChainAddress, valueOrTokenId);
            } else {
                revert("unknown type");
            }
        }
    }

    function multiSignForWithdraw(uint256 nonce, bytes32 dataHash, bytes oracleSign) internal returns (bool) {

        if (withdrawSigns[nonce][dataHash].oracleSigned[msg.sender]) {
            return false;
        }

        withdrawSigns[nonce][dataHash].oracleSigned[msg.sender] = true;
        withdrawSigns[nonce][dataHash].signs.push(oracleSign);
        withdrawSigns[nonce][dataHash].signCnt += 1;

        if (withdrawSigns[nonce][dataHash].signCnt > oracleCnt * 2 / 3 && !withdrawSigns[nonce][dataHash].success) {
            withdrawSigns[nonce][dataHash].success = true;
            return true;
        }
        return false;
    }

    function multiSignForWithdrawTRX(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user,
            withdrawMsg.valueOrTokenId, withdrawMsg.userSign,nonce));
        bool needEmit = multiSignForWithdraw(nonce, dataHash, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawTRX(withdrawMsg.user,
                withdrawMsg.valueOrTokenId, withdrawMsg.userSign, dataHash, nonce);
        }
    }

    function multiSignForWithdrawTRC10(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.trc10,
            withdrawMsg.valueOrTokenId, withdrawMsg.userSign,nonce));
        bool needEmit = multiSignForWithdraw(nonce, dataHash, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawTRC10(withdrawMsg.user, withdrawMsg.trc10,
            withdrawMsg.valueOrTokenId, withdrawMsg.userSign, dataHash, nonce);
        }
    }

    // _type:
    //      1: trx
    //      2: trc20
    //      3: trc721
    function multiSignForWithdrawToken(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.mainChainAddress,
            withdrawMsg.valueOrTokenId, withdrawMsg._type, withdrawMsg.userSign,nonce));
        bool needEmit = multiSignForWithdraw(nonce, dataHash, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawToken(withdrawMsg.user, withdrawMsg.mainChainAddress,
            withdrawMsg.valueOrTokenId, withdrawMsg._type, withdrawMsg.userSign, dataHash, nonce);
        }
    }

    function setMappingFee(uint256 fee) public onlyOwner {
        mappingFee = fee;
    }

}
