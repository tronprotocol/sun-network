pragma solidity ^0.4.24;
pragma experimental ABIEncoderV2;

import "../common/token/TRC721/TRC721.sol";
import "../common/token/TRC20/TRC20.sol";
import "../common/math/SafeMath.sol";
import "../common/DataModel.sol";
import "../common/token/TRC721/ITRC721Receiver.sol";
import "../common/token/TRC20/ITRC20Receiver.sol";
import "./OracleManagerContract.sol";


contract MainChainGateway is ITRC20Receiver, ITRC721Receiver, OracleManagerContract, DataModel {

    using SafeMath for uint256;

    struct Balance {
        uint256 tron;
        mapping(uint256 => uint256) trc10;
        mapping(address => uint256) trc20;
        mapping(address => mapping(uint256 => bool)) trc721;
    }

    Balance balances;
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
    event TRXWithdraw(address  owner,  uint256 value, uint256 nonce);
    event TRC10Withdraw(address  owner,  trcToken tokenId, uint256 value, uint256 nonce);
    event TRC20Withdraw(address  owner,  address contractAddress, uint256 value, uint256 nonce);
    event TRC721Withdraw(address  owner,  address contractAddress, uint256 uid, uint256 nonce);

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
        TokenKind _type;
        Status status;
    }

    struct MappingMsg {
        address mainChainAddress;
        TokenKind _type;
        Status status;
    }


    constructor (address _oracle)
    public OracleManagerContract(_oracle) {
    }

    // Deposit functions

    function _depositTRC721(uint256 uid) private {
        balances.trc721[msg.sender][uid] = true;
    }

    function _depositTRC20(uint256 value) private {
        balances.trc20[msg.sender] = balances.trc20[msg.sender].add(value);
    }

    // Withdrawal functions
    function withdrawTRC20(address _to, address contractAddress, uint256 value, uint256 nonce, bytes[] oracleSigns)
    public onlyOracle()
    {
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, contractAddress, value, nonce));
        checkOracles(dataHash, nonce, oracleSigns);
        balances.trc20[contractAddress] = balances.trc20[contractAddress].sub(value);
        TRC20(contractAddress).transfer(_to, value);
        withdrawDone[nonce] = true;
        emit TRC20Withdraw(_to, contractAddress, value, nonce);
    }

    function withdrawTRC721(address _to, address contractAddress, uint256 uid, uint256 nonce, bytes[] oracleSigns)
    public onlyOracle()
    {
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, contractAddress, uid, nonce));
        checkOracles(dataHash, nonce, oracleSigns);
        require(balances.trc721[contractAddress][uid], "Does not own token");
        // FIXME: can not use transfer
        TRC721(contractAddress).transfer(_to, uid);
        delete balances.trc721[contractAddress][uid];
        withdrawDone[nonce] = true;
        emit TRC721Withdraw(_to, contractAddress, uid, nonce);
    }

    function withdrawTRX(address _to, uint256 value, uint256 nonce, bytes[] oracleSigns)
    public onlyOracle()
    {
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, value, nonce));
        checkOracles(dataHash, nonce, oracleSigns);
        balances.tron = balances.tron.sub(value);
        _to.transfer(value);
        // ensure it's not reentrant
        withdrawDone[nonce] = true;
        emit TRXWithdraw(_to, value, nonce);
    }

    function withdrawTRC10(address _to, trcToken tokenId, uint256 value, uint256 nonce, bytes[] oracleSigns)
    public onlyOracle()
    {
        require(withdrawDone[nonce] == false, "withdrawDone[nonce] != false");
        bytes32 dataHash = keccak256(abi.encodePacked(_to, tokenId, value, nonce));
        checkOracles(dataHash, nonce, oracleSigns);
        balances.trc10[tokenId] = balances.trc10[tokenId].sub(value);
        _to.transferToken(value, tokenId);
        withdrawDone[nonce] = true;
        emit TRC10Withdraw(msg.sender, tokenId, value, nonce);
    }

    // Approve and Deposit function for 2-step deposits
    // Requires first to have called `approve` on the specified TRC20 contract
    function depositTRC20( address contractAddress, uint256 value) public {
        require(mainToSideContractMap[contractAddress] == 1, "Not an allowe token");
        require(value > 0, "value must > 0");
        TRC20(contractAddress).transferFrom(msg.sender, address(this), value);
        userDepositList.push(DepositMsg( msg.sender, contractAddress, 0, value, TokenKind.TRC20, Status.locking));
        balances.trc20[contractAddress] = balances.trc20[contractAddress].add(value);
        emit TRC20Received(msg.sender,contractAddress, value,  userDepositList.length - 1);
    }

    function depositTRC721( address contractAddress, uint256 uid) public {
        require(mainToSideContractMap[contractAddress] == 1, "Not an allowe token");
        TRC721(contractAddress).transferFrom(msg.sender, address(this), uid);
        userDepositList.push(DepositMsg( msg.sender, contractAddress, 0, uid ,TokenKind.TRC721, Status.locking));
        balances.trc721[contractAddress][uid] = true;
        emit TRC721Received(msg.sender,contractAddress, uid,  userDepositList.length - 1);
    }

    function depositTRX() payable public {
        require(msg.value > 0, "tokenvalue must > 0");
        userDepositList.push(DepositMsg( msg.sender, address(0), 0, msg.value, TokenKind.TRX, Status.locking));
        balances.tron = balances.tron.add(msg.value);
        emit TRXReceived(msg.sender, msg.value, userDepositList.length - 1);
    }

    function depositTRC10() payable public {
        require(msg.tokenvalue > 0, "tokenvalue must > 0");
        userDepositList.push(DepositMsg( msg.sender,  address(0), msg.tokenid, msg.tokenvalue, TokenKind.TRC10, Status.locking));
        balances.trc10[msg.tokenid] = balances.trc10[msg.tokenid].add(msg.tokenvalue);
        emit TRC10Received(msg.sender, msg.tokenvalue, msg.tokenid, userDepositList.length - 1);
    }

    // Receiver functions for 1-step deposits to the gateway


    function onTRC20Received(address _from, uint256 value, bytes)
    public
    returns (bytes4)
    {
        require(mainToSideContractMap[msg.sender] == 1, "Not an allowe token");
        userDepositList.push(DepositMsg( _from, msg.sender, 0, value, TokenKind.TRC20, Status.locking));
        _depositTRC20(value);
        emit TRC20Received(_from, msg.sender, value, userDepositList.length - 1);
        return _TRC20_RECEIVED;
    }

    function onTRC721Received(address _from, uint256 _uid, bytes)
    public
    returns (bytes4)
    {
        require(mainToSideContractMap[msg.sender] == 1, "Not an allowe token");
        userDepositList.push(DepositMsg( _from,  msg.sender, 0, _uid, TokenKind.TRC721, Status.locking));
        _depositTRC721(_uid);
        emit TRC721Received(_from, msg.sender, _uid, userDepositList.length - 1);
        return _TRC721_RECEIVED;
    }

    function() external payable {
        if (msg.tokenid > 1000000) {
            depositTRC10();
        } else {
            depositTRX();
        }
    }

    function mappingTRC20(bytes txId) public payable {
        require(msg.value >= mappingFee, "trc20MappingFee not enough");
        address trc20Address = calcContractAddress(txId, msg.sender);
        require(trc20Address != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        require(mainToSideContractMap[trc20Address] != 1, "trc20Address mapped");
        uint256 size;
        assembly {size := extcodesize(trc20Address)}
        require(size > 0);
        userMappingList.push(MappingMsg( trc20Address,TokenKind.TRC20, Status.locking));
        mainToSideContractMap[trc20Address] = 1;
        emit TRC20Mapping(trc20Address, userMappingList.length - 1);
    }

    // 2. deployDAppTRC721AndMapping
    function mappingTRC721(bytes txId) public payable {
        require(msg.value >= mappingFee, "trc721MappingFee not enough");
        address trc721Address = calcContractAddress(txId, msg.sender);
        require(trc721Address != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        require(mainToSideContractMap[trc721Address] != 1, "trc721Address mapped");
        uint256 size;
        assembly {size := extcodesize(trc721Address)}
        require(size > 0);
        userMappingList.push(MappingMsg( trc721Address,TokenKind.TRC721, Status.locking));
        mainToSideContractMap[trc721Address] = 1;
        emit TRC721Mapping(trc721Address, userMappingList.length - 1);
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

    // Returns all the TRX
    function getTRX() external view returns (uint256) {
        return balances.tron;
    }

    // Returns all the TRC10
    function getTRC10(trcToken tokenId) external view returns (uint256) {
        return balances.trc10[tokenId];
    }

    // Returns all the TRC20
    function getTRC20(address contractAddress) external view returns (uint256) {
        return balances.trc20[contractAddress];
    }

    // Returns TRC721 token by uid
    function getNFT(uint256 uid, address contractAddress) external view returns (bool) {
        return balances.trc721[contractAddress][uid];
    }

    function setMappingFee(uint256 fee) public onlyOwner {
        mappingFee = fee;
    }

    function setSunTokenAddress(address _sunTokenAddress) public onlyOwner {
        require(_sunTokenAddress != address(0), "_sunTokenAddress == address(0)");
        sunTokenAddress = _sunTokenAddress;
    }
}
