pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "../common/token/TRC721/TRC721.sol";
import "../common/token/TRC20/TRC20.sol";
import "../common/math/SafeMath.sol";
import "../common/DataModel.sol";
import "../common/token/TRC721/ITRC721Receiver.sol";
import "../common/token/TRC20/ITRC20Receiver.sol";
import "./OracleManagerContract.sol";

contract MainChainGateway is OracleManagerContract {

    using SafeMath for uint256;

    mapping(uint256 => bool) public withdrawDone;

    event TRXReceived(address from, uint256 value, uint256 nonce);
    event TRC10Received(address from, trcToken tokenId, uint256 value, uint256 nonce);
    event TRC20Received(address from, address contractAddress, uint256 value, uint256 nonce);
    event TRC721Received(address from, address contractAddress, uint256 uid, uint256 nonce);
    event TRC20Mapping(address contractAddress, uint256 nonce);
    event TRC721Mapping(address contractAddress, uint256 nonce);
    /**
     * Event to log the withdrawal of a token from the Gateway.
     * @param owner Address of the entity that made the withdrawal.
     * @param value For TRC721 this is the uid of the token, for TRX/TRC20/TRC10 this is the amount.
     */
    event TRXWithdraw(address owner, uint256 value, uint256 nonce);
    event TRC10Withdraw(address owner, trcToken tokenId, uint256 value, uint256 nonce);
    event TRC20Withdraw(address owner, address contractAddress, uint256 value, uint256 nonce);
    event TRC721Withdraw(address owner, address contractAddress, uint256 uid, uint256 nonce);

    uint256 public mappingFee;
    address public sunTokenAddress;
    mapping(address => uint256) public mainToSideContractMap;
    DepositMsg[] userDepositList;
    MappingMsg[] userMappingList;

    struct DepositMsg {
        address user;
        address mainChainAddress;
        trcToken tokenId;
        uint256 valueOrUid;
        DataModel.TokenKind _type;
        DataModel.Status status;
    }

    struct MappingMsg {
        address mainChainAddress;
        DataModel.TokenKind _type;
        DataModel.Status status;
    }

    // Withdrawal functions
    function withdrawTRC10(address _to, trcToken tokenId, uint256 value, uint256 nonce, bytes[] oracleSigns)
    public onlyNotStop onlyOracle goDelegateCall
    {
        require(oracleSigns.length <= numOracles, "withdraw TRC10 signs num > oracles num");
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, tokenId, value, nonce));
        uint256 numCommonSign = checkOracles(dataHash, nonce, oracleSigns);
        if (numCommonSign <= numCommonOracles) {
            return;
        }
        _to.transferToken(value, tokenId);
        withdrawDone[nonce] = true;
        emit TRC10Withdraw(msg.sender, tokenId, value, nonce);
    }

    function withdrawTRC20(address _to, address contractAddress, uint256 value, uint256 nonce, bytes[] oracleSigns)
    public onlyNotStop onlyOracle goDelegateCall
    {
        require(oracleSigns.length <= numOracles, "withdraw TRC20 signs num > oracles num");
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, contractAddress, value, nonce));
        uint256 numCommonSign = checkOracles(dataHash, nonce, oracleSigns);
        if (numCommonSign <= numCommonOracles) {
            return;
        }
        TRC20(contractAddress).transfer(_to, value);
        withdrawDone[nonce] = true;
        emit TRC20Withdraw(_to, contractAddress, value, nonce);
    }

    function withdrawTRC721(address _to, address contractAddress, uint256 uid, uint256 nonce, bytes[] oracleSigns)
    public onlyNotStop onlyOracle goDelegateCall
    {
        require(oracleSigns.length <= numOracles, "withdraw TRC721 signs num > oracles num");
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, contractAddress, uid, nonce));
        uint256 numCommonSign = checkOracles(dataHash, nonce, oracleSigns);
        if (numCommonSign <= numCommonOracles) {
            return;
        }
        TRC721(contractAddress).transferFrom(address(this), _to, uid);
        withdrawDone[nonce] = true;
        emit TRC721Withdraw(_to, contractAddress, uid, nonce);
    }

    function withdrawTRX(address _to, uint256 value, uint256 nonce, bytes[] oracleSigns)
    public onlyNotStop onlyOracle goDelegateCall
    {
        require(oracleSigns.length <= numOracles, "withdraw TRX signs num > oracles num");
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, value, nonce));
        uint256 numCommonSign = checkOracles(dataHash, nonce, oracleSigns);
        if (numCommonSign <= numCommonOracles) {
            return;
        }
        _to.transfer(value);
        // ensure it's not reentrant
        withdrawDone[nonce] = true;
        emit TRXWithdraw(_to, value, nonce);
    }

    // Approve and Deposit function for 2-step deposits
    // Requires first to have called `approve` on the specified TRC20 contract
    function depositTRC20(address contractAddress, uint256 value)
    public onlyNotStop onlyNotPause goDelegateCall returns (uint256){
        require(mainToSideContractMap[contractAddress] == 1, "Not an allowe token");
        require(value > 0, "value must > 0");
        TRC20(contractAddress).transferFrom(msg.sender, address(this), value);
        userDepositList.push(DepositMsg(msg.sender, contractAddress, 0, value, DataModel.TokenKind.TRC20, DataModel.Status.SUCCESS));
        emit TRC20Received(msg.sender, contractAddress, value, userDepositList.length - 1);
        return userDepositList.length - 1;
    }

    function depositTRC721(address contractAddress, uint256 uid)
    public onlyNotStop onlyNotPause goDelegateCall returns (uint256) {
        require(mainToSideContractMap[contractAddress] == 1, "Not an allowe token");
        TRC721(contractAddress).transferFrom(msg.sender, address(this), uid);
        userDepositList.push(DepositMsg(msg.sender, contractAddress, 0, uid, DataModel.TokenKind.TRC721, DataModel.Status.SUCCESS));
        emit TRC721Received(msg.sender, contractAddress, uid, userDepositList.length - 1);
        return userDepositList.length - 1;
    }

    function depositTRX() payable public onlyNotStop onlyNotPause goDelegateCall returns (uint256) {
        require(msg.value > 0, "value must > 0");
        userDepositList.push(DepositMsg(msg.sender, address(0), 0, msg.value, DataModel.TokenKind.TRX, DataModel.Status.SUCCESS));
        emit TRXReceived(msg.sender, msg.value, userDepositList.length - 1);
        return userDepositList.length - 1;
    }

    function depositTRC10() payable public onlyNotStop onlyNotPause goDelegateCall returns (uint256) {
        require(msg.tokenvalue > 0, "tokenvalue must > 0");
        userDepositList.push(DepositMsg(msg.sender, address(0), msg.tokenid, msg.tokenvalue, DataModel.TokenKind.TRC10, DataModel.Status.SUCCESS));
        emit TRC10Received(msg.sender, msg.tokenid, msg.tokenvalue, userDepositList.length - 1);
        return userDepositList.length - 1;
    }

    function() external onlyNotStop onlyNotPause goDelegateCall payable {
        if (msg.tokenid > 1000000) {
            depositTRC10();
        } else {
            depositTRX();
        }
    }

    function mappingTRC20(bytes txId) public onlyNotStop onlyNotPause goDelegateCall payable {
        require(msg.value >= mappingFee, "trc20MappingFee not enough");
        address trc20Address = calcContractAddress(txId, msg.sender);
        require(trc20Address != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        require(mainToSideContractMap[trc20Address] != 1, "trc20Address mapped");
        uint256 size;
        assembly {size := extcodesize(trc20Address)}
        require(size > 0);
        userMappingList.push(MappingMsg(trc20Address, DataModel.TokenKind.TRC20, DataModel.Status.SUCCESS));
        mainToSideContractMap[trc20Address] = 1;
        emit TRC20Mapping(trc20Address, userMappingList.length - 1);
    }

    // 2. deployDAppTRC721AndMapping
    function mappingTRC721(bytes txId) public onlyNotStop onlyNotPause goDelegateCall payable {
        require(msg.value >= mappingFee, "trc721MappingFee not enough");
        address trc721Address = calcContractAddress(txId, msg.sender);
        require(trc721Address != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        require(mainToSideContractMap[trc721Address] != 1, "trc721Address mapped");
        uint256 size;
        assembly {size := extcodesize(trc721Address)}
        require(size > 0);
        userMappingList.push(MappingMsg(trc721Address, DataModel.TokenKind.TRC721, DataModel.Status.SUCCESS));
        mainToSideContractMap[trc721Address] = 1;
        emit TRC721Mapping(trc721Address, userMappingList.length - 1);
    }

    function retryDeposit(uint256 nonce) public onlyNotStop onlyNotPause goDelegateCall {
        // TODO: free attack ?
        require(nonce < userDepositList.length, "nonce >= userDepositList.length");
        DepositMsg storage depositMsg = userDepositList[nonce];
        require(depositMsg.status == DataModel.Status.SUCCESS, "depositMsg.status != SUCCESS ");

        if (depositMsg._type == DataModel.TokenKind.TRX) {
            emit TRXReceived(depositMsg.user, depositMsg.valueOrUid, nonce);
        } else if (depositMsg._type == DataModel.TokenKind.TRC20) {
            emit TRC20Received(depositMsg.user, depositMsg.mainChainAddress, depositMsg.valueOrUid, nonce);
        } else if (depositMsg._type == DataModel.TokenKind.TRC721) {
            emit TRC721Received(depositMsg.user, depositMsg.mainChainAddress, depositMsg.valueOrUid, nonce);
        } else {
            emit TRC10Received(depositMsg.user, depositMsg.tokenId, depositMsg.valueOrUid, nonce);
        }
    }

    function retryMapping(uint256 nonce) public onlyNotStop onlyNotPause goDelegateCall {
        // TODO: free attack ?
        require(nonce < userMappingList.length, "nonce >= userMappingList.length");
        MappingMsg storage mappingMsg = userMappingList[nonce];
        require(mappingMsg.status == DataModel.Status.SUCCESS, "mappingMsg.status != SUCCESS ");

        if (mappingMsg._type == DataModel.TokenKind.TRC20) {
            emit TRC20Mapping(mappingMsg.mainChainAddress, nonce);
        } else {
            emit TRC721Mapping(mappingMsg.mainChainAddress, nonce);
        }
    }

    function calcContractAddress(bytes txId, address _owner) internal pure returns (address r) {
        bytes memory addressBytes = addressToBytes(_owner);
        bytes memory combinedBytes = concatBytes(txId, addressBytes);
        r = address(keccak256(combinedBytes));
    }

    function addressToBytes(address a) internal pure returns (bytes memory b) {
        assembly {
            let m := mload(0x40)
            a := and(a, 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF)
            mstore(add(m, 20), xor(0x140000000000000000000000000000000000000000, a))
            mstore(0x40, add(m, 52))
            b := m
        }
    }

    function concatBytes(bytes memory b1, bytes memory b2) internal pure returns (bytes memory r) {
        r = abi.encodePacked(b1, 0x41, b2);
    }

    // Returns all the TRX
    function getTRX() external view returns (uint256) {
        return address(this).balance;
    }

    // Returns all the TRC10
    function getTRC10(trcToken tokenId) external view returns (uint256) {
        return address(this).tokenBalance(tokenId);
    }

    // Returns all the TRC20
    function getTRC20(address contractAddress) external view returns (uint256) {
        return TRC20(contractAddress).balanceOf(contractAddress);
    }

    // Returns TRC721 token by uid
    function getNFT(uint256 uid, address contractAddress) external view returns (bool) {
        return TRC721(contractAddress).ownerOf(uid) == address(this);
    }

    function setMappingFee(uint256 fee) public onlyOwner {
        mappingFee = fee;
    }

    function setSunTokenAddress(address _sunTokenAddress) public onlyOwner {
        require(_sunTokenAddress != address(0), "_sunTokenAddress == address(0)");
        sunTokenAddress = _sunTokenAddress;
    }
}
