pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "../common/token/TRC20/ITRC20Receiver.sol";
import "../common/token/TRC721/ITRC721Receiver.sol";
import "./DAppTRC20.sol";
import "./DAppTRC721.sol";
import "../common/ECVerify.sol";
import "../common/DataModel.sol";

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


    event DeployDAppTRC20AndMapping(address mainChainAddress, address sideChainAddress);
    event DeployDAppTRC721AndMapping(address mainChainAddress, address sideChainAddress);

    event DepositTRC10(address to, trcToken tokenId, uint256 value);
    event DepositTRC20(address to, address sideChainAddress, uint256 value);
    event DepositTRC721(address to, address sideChainAddress, uint256 uId);
    event DepositTRX(address to, uint256 value);

    event WithdrawTRC10(address from, trcToken tokenId, uint256 value, uint256 nonce);
    event WithdrawTRC20(address from, address mainChainAddress, uint256 value, uint256 nonce);
    event WithdrawTRC721(address from, address mainChainAddress, uint256 uId, uint256 nonce);
    event WithdrawTRX(address from, uint256 value, uint256 nonce);

    event MultiSignForWithdrawTRC10(address from, trcToken tokenId, uint256 value, uint256 nonce);
    event MultiSignForWithdrawTRC20(address from, address mainChainAddress, uint256 value, uint256 nonce);
    event MultiSignForWithdrawTRC721(address from, address mainChainAddress, uint256 uId, uint256 nonce);
    event MultiSignForWithdrawTRX(address from, uint256 value, uint256 nonce);

    mapping(address => address) public mainToSideContractMap;
    mapping(address => address) public sideToMainContractMap;
    mapping(uint256 => bool) public tokenIdMap;
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

    mapping(uint256 => SignMsg) public depositSigns;
    mapping(uint256 => SignMsg) public withdrawSigns;
    mapping(uint256 => SignMsg) public mappingSigns;

    WithdrawMsg[] userWithdrawList;

    struct SignMsg {
        mapping(address => bool) oracleSigned;
        bytes[] signs;
        uint256 signCnt;
        bool success;
    }

    struct WithdrawMsg {
        address user;
        address mainChainAddress;
        trcToken tokenId;
        uint256 valueOrUid;
        DataModel.TokenKind _type;
        DataModel.Status status;
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

    function getWithdrawSigns(uint256 nonce) view public returns (bytes[]) {
        return withdrawSigns[nonce].signs;
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
    function multiSignForDeployDAppTRC20AndMapping(address mainChainAddress, string name, string symbol, uint8 decimals, uint256 nonce) public onlyOracle {
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        bool needMapping = multiSignForMapping(nonce);
        if (needMapping) {
            deployDAppTRC20AndMapping(mainChainAddress, name, symbol, decimals);
        }
    }

    function deployDAppTRC20AndMapping(address mainChainAddress, string name, string symbol, uint8 decimals) internal returns (address r) {
        address sideChainAddress = new DAppTRC20(address(this), name, symbol, decimals);
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC20AndMapping(mainChainAddress, sideChainAddress);
        r = sideChainAddress;
    }

    // 2. deployDAppTRC721AndMapping
    function multiSignForDeployDAppTRC721AndMapping(address mainChainAddress, string name, string symbol, uint256 nonce) public onlyOracle {
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        bool needMapping = multiSignForMapping(nonce);
        if (needMapping) {
            deployDAppTRC721AndMapping(mainChainAddress, name, symbol);
        }
    }

    function deployDAppTRC721AndMapping(address mainChainAddress, string name, string symbol) internal returns (address r) {
        address sideChainAddress = new DAppTRC721(address(this), name, symbol);
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC721AndMapping(mainChainAddress, sideChainAddress);
        r = sideChainAddress;
    }

    function multiSignForMapping(uint256 nonce) internal returns (bool) {
        if (mappingSigns[nonce].oracleSigned[msg.sender]) {
            return false;
        }
        mappingSigns[nonce].oracleSigned[msg.sender] = true;
        // mappingToSideSigns[nonce].signs.push(oracleSign);
        mappingSigns[nonce].signCnt += 1;

        if (mappingSigns[nonce].signCnt > oracleCnt * 2 / 3 && !mappingSigns[nonce].success) {
            mappingSigns[nonce].success = true;
            return true;
        }
        return false;
    }

    // 3. depositTRC10
    function multiSignForDepositTRC10(address to, trcToken tokenId, uint256 value, bytes32 name, bytes32 symbol, uint8 decimals, uint256 nonce) public onlyOracle {
        require(tokenId > 1000000 && tokenId <= 2000000, "tokenId <= 1000000 or tokenId > 2000000");
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRC10(to, tokenId, value, name, symbol, decimals);
        }
    }

    function depositTRC10(address to, trcToken tokenId, uint256 value, bytes32 name, bytes32 symbol, uint8 decimals) internal {
        bool exist = tokenIdMap[tokenId];
        if (exist == false) {
            tokenIdMap[tokenId] = true;
        }
        mintTRC10Contract.call(value, tokenId, name, symbol, decimals);
        to.transferToken(value, tokenId);
        emit DepositTRC10(to, tokenId, value);
    }

    // 4. depositTRC20
    function multiSignForDepositTRC20(address to, address mainChainAddress, uint256 value, uint256 nonce) public onlyOracle {
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRC20(to, sideChainAddress, value);
        }
    }

    function depositTRC20(address to, address sideChainAddress, uint256 value) internal {
        IDApp(sideChainAddress).mint(to, value);
        emit DepositTRC20(to, sideChainAddress, value);
    }

    // 5. depositTRC721
    function multiSignForDepositTRC721(address to, address mainChainAddress, uint256 uId, uint256 nonce) public onlyOracle {
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRC721(to, sideChainAddress, uId);
        }
    }

    function depositTRC721(address to, address sideChainAddress, uint256 uId) internal {
        IDApp(sideChainAddress).mint(to, uId);
        emit DepositTRC721(to, sideChainAddress, uId);
    }

    // 6. depositTRX
    function multiSignForDepositTRX(address to, uint256 value, uint256 nonce) public onlyOracle {
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRX(to, value);
        }
    }

    function depositTRX(address to, uint256 value) internal {
        mintTRXContract.call(value);
        to.transfer(value);
        emit DepositTRX(to, value);
    }

    function multiSignForDeposit(uint256 nonce) internal returns (bool) {
        if (depositSigns[nonce].oracleSigned[msg.sender]) {
            return false;
        }
        depositSigns[nonce].oracleSigned[msg.sender] = true;
        // depositSigns[nonce].signs.push(oracleSign);
        depositSigns[nonce].signCnt += 1;

        if (depositSigns[nonce].signCnt > oracleCnt * 2 / 3 && !depositSigns[nonce].success) {
            depositSigns[nonce].success = true;
            return true;
        }
        return false;
    }

    // 7. withdrawTRC10
    function withdrawTRC10() payable public returns (uint256 r) {
        if (msg.value > 0) {
            bonus += msg.value;
        }
        require(tokenIdMap[msg.tokenid], "tokenIdMap[msg.tokenid] == false");
        require(msg.tokenvalue > withdrawMinTrc10, "tokenvalue must be > withdrawMinTrc10");

        userWithdrawList.push(WithdrawMsg(msg.sender, address(0), msg.tokenid, msg.tokenvalue, DataModel.TokenKind.TRC10, DataModel.Status.SUCCESS));
        // burn
        address(0).transferToken(msg.tokenvalue, msg.tokenid);
        emit WithdrawTRC10(msg.sender, msg.tokenid, msg.tokenvalue, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRC10(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        // bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.tokenId, withdrawMsg.valueOrUid, nonce));
        bool needEmit = multiSignForWithdraw(nonce, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawTRC10(withdrawMsg.user, withdrawMsg.tokenId, withdrawMsg.valueOrUid, nonce);
        }
    }

    // 8. withdrawTRC20
    function onTRC20Received(address from, uint256 value) public returns (uint256 r) {
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");
        require(value > withdrawMinTrc20, "value must be > withdrawMinTrc20");

        userWithdrawList.push(WithdrawMsg(from, mainChainAddress, 0, value, DataModel.TokenKind.TRC20, DataModel.Status.SUCCESS));

        // burn
        DAppTRC20(sideChainAddress).burn(value);
        emit WithdrawTRC20(from, mainChainAddress, value, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRC20(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        // bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce));
        bool needEmit = multiSignForWithdraw(nonce, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawTRC20(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
        }
    }

    // 9. withdrawTRC721
    function onTRC721Received(address from, uint256 uId) public returns (uint256 r) {
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");

        userWithdrawList.push(WithdrawMsg(from, mainChainAddress, 0, uId, DataModel.TokenKind.TRC721, DataModel.Status.SUCCESS));

        // burn
        DAppTRC721(sideChainAddress).burn(uId);
        emit WithdrawTRC721(from, mainChainAddress, uId, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRC721(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        // bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce));
        bool needEmit = multiSignForWithdraw(nonce, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawTRC721(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
        }
    }

    // 10. withdrawTRX
    function withdrawTRX() payable public returns (uint256 r) {
        require(msg.value > withdrawMinTrx, "value must be > withdrawMinTrx");

        userWithdrawList.push(WithdrawMsg(msg.sender, address(0), 0, msg.value, DataModel.TokenKind.TRX, DataModel.Status.SUCCESS));
        // burn
        address(0).transfer(msg.value);
        emit WithdrawTRX(msg.sender, msg.value, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRX(uint256 nonce, bytes oracleSign) public onlyOracle {
        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        // bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.valueOrUid, nonce));
        bool needEmit = multiSignForWithdraw(nonce, oracleSign);
        if (needEmit) {
            emit MultiSignForWithdrawTRX(withdrawMsg.user, withdrawMsg.valueOrUid, nonce);
        }
    }

    function multiSignForWithdraw(uint256 nonce, bytes oracleSign) internal returns (bool) {
        if (withdrawSigns[nonce].oracleSigned[msg.sender]) {
            return false;
        }
        withdrawSigns[nonce].oracleSigned[msg.sender] = true;
        withdrawSigns[nonce].signs.push(oracleSign);
        withdrawSigns[nonce].signCnt += 1;

        if (withdrawSigns[nonce].signCnt > oracleCnt * 2 / 3 && !withdrawSigns[nonce].success) {
            withdrawSigns[nonce].success = true;
            return true;
        }
        return false;
    }

    // 11. retryWithdraw
    function retryWithdraw(uint256 nonce) public {
        // FIXME: free retry attack
        require(nonce < userWithdrawList.length, "nonce >= userWithdrawList.length");
        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        if (withdrawMsg._type == DataModel.TokenKind.TRC10) {
            if (withdrawSigns[nonce].success) {
                emit MultiSignForWithdrawTRC10(withdrawMsg.user, withdrawMsg.tokenId, withdrawMsg.valueOrUid, nonce);
            } else {
                emit WithdrawTRC10(withdrawMsg.user, withdrawMsg.tokenId, withdrawMsg.valueOrUid, nonce);
            }
        } else if (withdrawMsg._type == DataModel.TokenKind.TRC20) {
            if (withdrawSigns[nonce].success) {
                emit MultiSignForWithdrawTRC20(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
            } else {
                emit WithdrawTRC20(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
            }
        } else if (withdrawMsg._type == DataModel.TokenKind.TRC721) {
            if (withdrawSigns[nonce].success) {
                emit MultiSignForWithdrawTRC721(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
            } else {
                emit WithdrawTRC721(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
            }
        } else {
            if (withdrawSigns[nonce].success) {
                emit WithdrawTRX(withdrawMsg.user, withdrawMsg.valueOrUid, nonce);
            } else {
                emit MultiSignForWithdrawTRX(withdrawMsg.user, withdrawMsg.valueOrUid, nonce);
            }
        }
    }

    function setMappingFee(uint256 fee) public onlyOwner {
        mappingFee = fee;
    }

}
