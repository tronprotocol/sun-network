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
    event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes txData);
    event WithdrawTRC20(address from, uint256 value, address mainChainAddress, bytes txData);
    event WithdrawTRC721(address from, uint256 tokenId, address mainChainAddress, bytes txData);
    event WithdrawTRX(address from, uint256 value, bytes txData);

    // TODO: type enum
    mapping(address => address) public mainToSideContractMap;
    mapping(address => address) public sideToMainContractMap;
    mapping(uint256 => bool) public trc10Map;
    mapping(address => bool) public oracles;
    address public owner;
    address public sunTokenAddress;
    address mintTRXContract = 0x10000;
    address mintTRC10Contract = 0x10001;

    constructor (address _oracle) public {
        owner = msg.sender;
        oracles[_oracle] = true;
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
    function withdrawTRC10(bytes memory txData) payable public {
        require(trc10Map[msg.tokenid], "trc10Map[msg.tokenid] == false");
        // burn
        address(0).transferToken(msg.tokenvalue, msg.tokenid);
        emit WithdrawTRC10(msg.sender, msg.tokenvalue, msg.tokenid, txData);
    }

    // 8. withdrawTRC20
    function onTRC20Received(address from, uint256 value, bytes txData) public returns (bytes4) {
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");
        DAppTRC20(sideChainAddress).burn(value);
        emit WithdrawTRC20(from, value, mainChainAddress, txData);

        return _TRC20_RECEIVED;
    }

    // 9. withdrawTRC721
    function onTRC721Received(address from, uint256 tokenId, bytes txData) public returns (bytes4) {
        address sideChainAddress = msg.sender;
        address mainChainAddress = sideToMainContractMap[sideChainAddress];
        require(mainChainAddress != address(0), "the trc721 must have been deposited");
        // burn
        DAppTRC721(sideChainAddress).burn(tokenId);
        emit WithdrawTRC721(from, tokenId, mainChainAddress, txData);
        return _TRC721_RECEIVED;
    }

    // 10. withdrawTRX
    function withdrawTRX(bytes memory txData) payable public {
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
}
