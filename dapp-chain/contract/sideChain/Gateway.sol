pragma solidity ^0.4.24;

import "../common/token/TRC20/ITRC20Receiver.sol";
import "../common/token/TRC721/ITRC721Receiver.sol";
import "./DAppTRC20.sol";
import "./DAppTRC721.sol";

contract Gateway is ITRC20Receiver, ITRC721Receiver {

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
    event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes32 txData);
    event WithdrawTRC20(address from, uint256 value, address mainChainAddress, bytes32 txData);
    event WithdrawTRC721(address from, uint256 tokenId, address mainChainAddress, bytes32 txData);
    event WithdrawTRX(address from, uint256 value, bytes32 txData);

    event MultiSignForDepositTRC10(bytes32 dataHash, bytes32 txId);
    event MultiSignForDepositToken(bytes32 dataHash, bytes32 txId);
    event MultiSignForDepositTRX(bytes32 dataHash, bytes32 txId);
    event MultiSignForWithdrawTRC10(bytes32 dataHash, bytes32 txId);
    event MultiSignForWithdrawToken(bytes32 dataHash, bytes32 txId);
    event MultiSignForWithdrawTRX(bytes32 dataHash, bytes32 txId);

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


    mapping(bytes32 => mapping(bytes32 => depositTrxMsg)) public signDepositTrx;
    mapping(bytes32 => mapping(bytes32 => depositTrc10Msg)) public signDepositTrc10;
    mapping(bytes32 => mapping(bytes32 => depositTokenMsg)) public signDepositToken;

    mapping(bytes32 => mapping(bytes32 => withdrawTrxMsg)) public signWithdrawTrx;
    mapping(bytes32 => mapping(bytes32 => withdrawTrc10Msg)) public signWithdrawTrc10;
    mapping(bytes32 => mapping(bytes32 => withdrawTokenMsg)) public signWithdrawToken;

    struct depositTrxMsg {
        address to;
        uint256 value;
        mapping(address => bool) oracleSigned;
        bytes32[] signs;
        uint256 signCnt;
        bool emitted;
    }

    struct depositTrc10Msg {
        address to;
        uint256 trc10;
        uint256 value;
        bytes32 name;
        bytes32 symbol;
        uint8 decimals;
        mapping(address => bool) oracleSigned;
        bytes32[] signs;
        uint256 signCnt;
        bool emitted;
    }

    struct depositTokenMsg {
        address to;
        address mainChainAddress;
        uint256 valueOrTokenId;
        uint256 _type;
        mapping(address => bool) oracleSigned;
        bytes32[] signs;
        uint256 signCnt;
        bool emitted;
    }

    struct withdrawTrxMsg {
        address from;
        uint256 value;
        bytes32 txData;
        mapping(address => bool) oracleSigned;
        bytes32[] signs;
        uint256 signCnt;
        bool emitted;
    }

    struct withdrawTrc10Msg {
        address from;
        uint256 trc10;
        uint256 value;
        bytes32 txData;
        mapping(address => bool) oracleSigned;
        bytes32[] signs;
        uint256 signCnt;
        bool emitted;
    }

    struct withdrawTokenMsg {
        address from;
        uint256 valueOrTokenId;
        uint256 _type;
        bytes32 txData;
        mapping(address => bool) oracleSigned;
        bytes32[] signs;
        uint256 signCnt;
        bool emitted;
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
    function deployDAppTRC20AndMapping(bytes txId, string name, string symbol, uint8 decimals) public returns (address r) {
        // can be called by everyone (contract developer)
        // require(sunTokenAddress != address(0), "sunTokenAddress == address(0)");
        address mainChainAddress = calcContractAddress(txId, msg.sender);
        require(mainToSideContractMap[mainChainAddress] == address(0), "the main chain address has mapped");
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        address sideChainAddress = new DAppTRC20(address(this), name, symbol, decimals);
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC20AndMapping(msg.sender, mainChainAddress, sideChainAddress);
        r = sideChainAddress;
    }

    // 2. deployDAppTRC721AndMapping
    function deployDAppTRC721AndMapping(bytes txId, string name, string symbol) public returns (address r) {
        // can be called by everyone (contract developer)
        // require(sunTokenAddress != address(0), "sunTokenAddress == address(0)");
        address mainChainAddress = calcContractAddress(txId, msg.sender);
        require(mainToSideContractMap[mainChainAddress] == address(0), "the main chain address has mapped");
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        address sideChainAddress = new DAppTRC721(address(this), name, symbol);
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC721AndMapping(msg.sender, mainChainAddress, sideChainAddress);
        r = sideChainAddress;
    }

    // 3. depositTRC10
    function depositTRC10(address to, uint256 trc10, uint256 value, bytes32 name, bytes32 symbol, uint8 decimals) public onlyOracle {
        // can only be called by oracle
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
    function depositTRC20(address to, address mainChainAddress, uint256 value) public onlyOracle {
        // can only be called by oracle
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        IDApp(sideChainAddress).mint(to, value);
        emit DepositTRC20(sideChainAddress, to, value);
    }

    // 5. depositTRC721
    function depositTRC721(address to, address mainChainAddress, uint256 tokenId) public onlyOracle {
        // can only be called by oracle
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        IDApp(sideChainAddress).mint(to, tokenId);
        emit DepositTRC721(sideChainAddress, to, tokenId);
    }

    // 6. depositTRX
    function depositTRX(address to, uint256 value) public onlyOracle {
        // can only be called by oracle
        // FIXME: must require
        // require(mintTRXContract.call(value), "mint fail");
        mintTRXContract.call(value);
        to.transfer(value);
        emit DepositTRX(to, value);
    }

    // 7. withdrawTRC10
    function withdrawTRC10(bytes32 txData) payable public {
        // TODO: verify txData
        require(trc10Map[msg.tokenid], "trc10Map[msg.tokenid] == false");
        // burn
        address(0).transferToken(msg.tokenvalue, msg.tokenid);
        emit WithdrawTRC10(msg.sender, msg.tokenvalue, msg.tokenid, txData);
    }

    // 8. withdrawTRC20
    function onTRC20Received(address from, uint256 value, bytes32 txData) public returns (bytes4) {
        // TODO: verify txData
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");
        DAppTRC20(sideChainAddress).burn(value);
        emit WithdrawTRC20(from, value, mainChainAddress, txData);

        return _TRC20_RECEIVED;
    }

    // 9. withdrawTRC721
    function onTRC721Received(address from, uint256 tokenId, bytes32 txData) public returns (bytes4) {
        // TODO: verify txData
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "the trc721 must have been deposited");
        // burn
        DAppTRC721(sideChainAddress).burn(tokenId);
        emit WithdrawTRC721(from, tokenId, mainChainAddress, txData);
        return _TRC721_RECEIVED;
    }

    // 10. withdrawTRX
    function withdrawTRX(bytes32 txData) payable public {
        // TODO: verify txData
        // burn
        address(0).transfer(msg.value);
        emit WithdrawTRX(msg.sender, msg.value, txData);
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

    function multiSignForDepositTRX(address to, uint256 value, bytes32 txId, bytes32 sign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(to, value));
        if (signDepositTrx[txId][dataHash].oracleSigned[msg.sender]) {
            return;
        }
        if (signDepositTrx[txId][dataHash].signCnt == 0) {
            signDepositTrx[txId][dataHash].to = to;
            signDepositTrx[txId][dataHash].value = value;
        }

        signDepositTrx[txId][dataHash].oracleSigned[msg.sender] = true;
        signDepositTrx[txId][dataHash].signs.push(sign);
        signDepositTrx[txId][dataHash].signCnt += 1;

        if (signDepositTrx[txId][dataHash].signCnt > 2 * oracleCnt / 3 && !signDepositTrx[txId][dataHash].emitted) {
            emit MultiSignForDepositTRX(dataHash, txId);
            signDepositTrx[txId][dataHash].emitted = true;
        }
    }

    function multiSignForDepositTRC10(address to, uint256 trc10, uint256 value, bytes32 name, bytes32 symbol, uint8 decimals, bytes32 txId, bytes32 sign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(to, trc10, value, name, symbol, decimals));
        if (signDepositTrc10[txId][dataHash].oracleSigned[msg.sender]) {
            return;
        }
        if (signDepositTrc10[txId][dataHash].signCnt == 0) {
            signDepositTrc10[txId][dataHash].to = to;
            signDepositTrc10[txId][dataHash].trc10 = trc10;
            signDepositTrc10[txId][dataHash].value = value;
            signDepositTrc10[txId][dataHash].name = name;
            signDepositTrc10[txId][dataHash].symbol = symbol;
            signDepositTrc10[txId][dataHash].decimals = decimals;
        }

        signDepositTrc10[txId][dataHash].oracleSigned[msg.sender] = true;
        signDepositTrc10[txId][dataHash].signs.push(sign);
        signDepositTrc10[txId][dataHash].signCnt += 1;

        if (signDepositTrc10[txId][dataHash].signCnt > 2 * oracleCnt / 3 && !signDepositTrc10[txId][dataHash].emitted) {
            emit MultiSignForDepositTRC10(dataHash, txId);
            signDepositTrc10[txId][dataHash].emitted = true;
        }
    }

    function multiSignForDepositToken(address to, address mainChainAddress, uint256 valueOrTokenId, uint256 _type, bytes32 txId, bytes32 sign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(to, mainChainAddress, valueOrTokenId, _type));
        if (signDepositToken[txId][dataHash].oracleSigned[msg.sender]) {
            return;
        }
        if (signDepositToken[txId][dataHash].signCnt == 0) {
            signDepositToken[txId][dataHash].to = to;
            signDepositToken[txId][dataHash].mainChainAddress = mainChainAddress;
            signDepositToken[txId][dataHash].valueOrTokenId = valueOrTokenId;
            signDepositToken[txId][dataHash]._type = _type;
        }

        signDepositToken[txId][dataHash].oracleSigned[msg.sender] = true;
        signDepositToken[txId][dataHash].signs.push(sign);
        signDepositToken[txId][dataHash].signCnt += 1;

        if (signDepositToken[txId][dataHash].signCnt > 2 * oracleCnt / 3 && !signDepositToken[txId][dataHash].emitted) {
            emit MultiSignForDepositToken(dataHash, txId);
            signDepositToken[txId][dataHash].emitted = true;
        }
    }

    function multiSignForWithdrawTRX(address from, uint256 value, bytes32 txData, bytes32 txId, bytes32 sign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(from, value, txData));
        if (signWithdrawTrx[txId][dataHash].oracleSigned[msg.sender]) {
            return;
        }
        if (signWithdrawTrx[txId][dataHash].signCnt == 0) {
            signWithdrawTrx[txId][dataHash].from = from;
            signWithdrawTrx[txId][dataHash].value = value;
            signWithdrawTrx[txId][dataHash].txData = txData;
        }

        signWithdrawTrx[txId][dataHash].oracleSigned[msg.sender] = true;
        signWithdrawTrx[txId][dataHash].signs.push(sign);
        signWithdrawTrx[txId][dataHash].signCnt += 1;

        if (signWithdrawTrx[txId][dataHash].signCnt > 2 * oracleCnt / 3 && !signWithdrawTrx[txId][dataHash].emitted) {
            emit MultiSignForWithdrawTRX(dataHash, txId);
            signWithdrawTrx[txId][dataHash].emitted = true;
        }
    }

    function multiSignForWithdrawTRC10(address from, uint256 trc10, uint256 value, bytes32 txData, bytes32 txId, bytes32 sign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(from, trc10, value, txData));
        if (signWithdrawTrc10[txId][dataHash].oracleSigned[msg.sender]) {
            return;
        }
        if (signWithdrawTrc10[txId][dataHash].signCnt == 0) {
            signWithdrawTrc10[txId][dataHash].from = from;
            signWithdrawTrc10[txId][dataHash].trc10 = trc10;
            signWithdrawTrc10[txId][dataHash].value = value;
            signWithdrawTrc10[txId][dataHash].txData = txData;
        }

        signWithdrawTrc10[txId][dataHash].oracleSigned[msg.sender] = true;
        signWithdrawTrc10[txId][dataHash].signs.push(sign);
        signWithdrawTrc10[txId][dataHash].signCnt += 1;

        if (signWithdrawTrc10[txId][dataHash].signCnt > 2 * oracleCnt / 3 && !signWithdrawTrc10[txId][dataHash].emitted) {
            emit MultiSignForWithdrawTRC10(dataHash, txId);
            signWithdrawTrc10[txId][dataHash].emitted = true;
        }
    }

    function multiSignForWithdrawToken(address from, uint256 valueOrTokenId, uint256 _type, bytes32 txData, bytes32 txId, bytes32 sign) public onlyOracle {
        bytes32 dataHash = keccak256(abi.encodePacked(from, valueOrTokenId, _type, txData));
        if (signWithdrawToken[txId][dataHash].oracleSigned[msg.sender]) {
            return;
        }
        if (signWithdrawToken[txId][dataHash].signCnt == 0) {
            signWithdrawToken[txId][dataHash].from = from;
            signWithdrawToken[txId][dataHash].valueOrTokenId = valueOrTokenId;
            signWithdrawToken[txId][dataHash]._type = _type;
            signWithdrawToken[txId][dataHash].txData = txData;
        }

        signWithdrawToken[txId][dataHash].oracleSigned[msg.sender] = true;
        signWithdrawToken[txId][dataHash].signs.push(sign);
        signWithdrawToken[txId][dataHash].signCnt += 1;

        if (signWithdrawToken[txId][dataHash].signCnt > 2 * oracleCnt / 3 && !signWithdrawToken[txId][dataHash].emitted) {
            emit MultiSignForWithdrawToken(dataHash, txId);
            signWithdrawToken[txId][dataHash].emitted = true;
        }
    }
}
