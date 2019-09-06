pragma experimental ABIEncoderV2;

import "./ITRC20Receiver.sol";
import "./ITRC721Receiver.sol";
import "./DAppTRC20.sol";
import "./DAppTRC721.sol";
import "./ECVerify.sol";
import "./DataModel.sol";
import "./Ownable.sol";

contract SideChainGateway is ITRC20Receiver, ITRC721Receiver, Ownable {
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


    event DeployDAppTRC20AndMapping(address mainChainAddress, address sideChainAddress, uint256 nonce);
    event DeployDAppTRC721AndMapping(address mainChainAddress, address sideChainAddress, uint256 nonce);

    event DepositTRC10(address to, trcToken tokenId, uint256 value, uint256 nonce);
    event DepositTRC20(address to, address sideChainAddress, uint256 value, uint256 nonce);
    event DepositTRC721(address to, address sideChainAddress, uint256 uId, uint256 nonce);
    event DepositTRX(address to, uint256 value, uint256 nonce);

    event WithdrawTRC10(address from, trcToken tokenId, uint256 value, uint256 nonce);
    event WithdrawTRC20(address from, address mainChainAddress, uint256 value, uint256 nonce);
    event WithdrawTRC721(address from, address mainChainAddress, uint256 uId, uint256 nonce);
    event WithdrawTRX(address from, uint256 value, uint256 nonce);

    event MultiSignForWithdrawTRC10(address from, trcToken tokenId, uint256 value, uint256 nonce);
    event MultiSignForWithdrawTRC20(address from, address mainChainAddress, uint256 value, uint256 nonce);
    event MultiSignForWithdrawTRC721(address from, address mainChainAddress, uint256 uId, uint256 nonce);
    event MultiSignForWithdrawTRX(address from, uint256 value, uint256 nonce);

    uint256 public numOracles;
    address public sunTokenAddress;
    address mintTRXContract = address(0x10000);
    address mintTRC10Contract = address(0x10001);
    uint256 public withdrawMinTrx = 1;
    uint256 public withdrawMinTrc10 = 1;
    uint256 public withdrawMinTrc20 = 1;
    uint256 public withdrawFee = 0;
    uint256 public retryFee = 0;
    uint256 public bonus;
    bool public pause;
    bool public stop;

    mapping(address => address) public mainToSideContractMap;
    mapping(address => address) public sideToMainContractMap;
    address[] public mainContractList;
    mapping(uint256 => bool) public tokenIdMap;
    mapping(address => bool) public oracles;

    mapping(uint256 => SignMsg) public depositSigns;
    mapping(uint256 => SignMsg) public withdrawSigns;
    mapping(uint256 => SignMsg) public mappingSigns;
    mapping(address => SignMsg) public changeLogicSigns;

    WithdrawMsg[] userWithdrawList;

    struct SignMsg {
        mapping(address => bool) oracleSigned;
        bytes[] signs;
        address[] signOracles;
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

    modifier onlyOracle {
        require(oracles[msg.sender], "oracles[msg.sender] is false");
        _;
    }

    modifier isHuman() {
        require(msg.sender == tx.origin, "not allow contract");
        _;
    }

    modifier checkForTrc10(uint256 tokenId, uint256 tokenValue) {
        require(tokenId == uint256(msg.tokenid), "tokenId != msg.tokenid");
        require(tokenValue == msg.tokenvalue, "tokenValue != msg.tokenvalue");
        _;
    }

    modifier onlyNotPause {
        require(!pause, "pause is true");
        _;
    }

    modifier onlyNotStop {
        require(!stop, "stop is true");
        _;
    }

    function getWithdrawSigns(uint256 nonce) view public returns (bytes[] memory, address[] memory) {
        return (withdrawSigns[nonce].signs, withdrawSigns[nonce].signOracles);
    }

    function addOracle(address _oracle) public onlyOwner {
        require(_oracle != address(0), "this address cannot be zero");
        require(!oracles[_oracle], "_oracle is oracle");
        oracles[_oracle] = true;
        numOracles++;
    }

    function delOracle(address _oracle) public onlyOwner {
        require(oracles[_oracle], "_oracle is not oracle");
        oracles[_oracle] = false;
        numOracles--;
    }

    function setSunTokenAddress(address _sunTokenAddress) public onlyOwner {
        require(_sunTokenAddress != address(0), "_sunTokenAddress == address(0)");
        sunTokenAddress = _sunTokenAddress;
    }

    // 1. deployDAppTRC20AndMapping
    function multiSignForDeployDAppTRC20AndMapping(address mainChainAddress, string memory name,
        string memory symbol, uint8 decimals, uint256 nonce)
    public onlyNotStop onlyOracle
    {
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        bool needMapping = multiSignForMapping(nonce);
        if (needMapping) {
            deployDAppTRC20AndMapping(mainChainAddress, name, symbol, decimals, nonce);
        }
    }

    function deployDAppTRC20AndMapping(address mainChainAddress, string memory name,
        string memory symbol, uint8 decimals, uint256 nonce) internal
    {
        require(mainToSideContractMap[mainChainAddress] == address(0), "TRC20 contract is mapped");
        address sideChainAddress = address(new DAppTRC20(address(this), name, symbol, decimals));
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC20AndMapping(mainChainAddress, sideChainAddress, nonce);
        mainContractList.push(mainChainAddress);
    }

    // 2. deployDAppTRC721AndMapping
    function multiSignForDeployDAppTRC721AndMapping(address mainChainAddress, string memory name,
        string memory symbol, uint256 nonce)
    public onlyNotStop onlyOracle
    {
        require(mainChainAddress != sunTokenAddress, "mainChainAddress == sunTokenAddress");
        bool needMapping = multiSignForMapping(nonce);
        if (needMapping) {
            deployDAppTRC721AndMapping(mainChainAddress, name, symbol, nonce);
        }
    }

    function deployDAppTRC721AndMapping(address mainChainAddress, string memory name,
        string memory symbol, uint256 nonce) internal
    {
        require(mainToSideContractMap[mainChainAddress] == address(0), "TRC721 contract is mapped");
        address sideChainAddress = address(new DAppTRC721(address(this), name, symbol));
        mainToSideContractMap[mainChainAddress] = sideChainAddress;
        sideToMainContractMap[sideChainAddress] = mainChainAddress;
        emit DeployDAppTRC721AndMapping(mainChainAddress, sideChainAddress, nonce);
        mainContractList.push(mainChainAddress);
    }

    function multiSignForMapping(uint256 nonce) internal returns (bool) {
        SignMsg storage _signMsg = mappingSigns[nonce];
        if (_signMsg.oracleSigned[msg.sender]) {
            return false;
        }
        _signMsg.oracleSigned[msg.sender] = true;
        _signMsg.signCnt += 1;

        if (!_signMsg.success && _signMsg.signCnt > numOracles * 2 / 3) {
            _signMsg.success = true;
            return true;
        }
        return false;
    }

    // 3. depositTRC10
    function multiSignForDepositTRC10(address payable to, trcToken tokenId,
        uint256 value, bytes32 name, bytes32 symbol, uint8 decimals, uint256 nonce)
    public onlyNotStop onlyOracle
    {
        require(tokenId > 1000000 && tokenId < 2000000, "tokenId <= 1000000 or tokenId >= 2000000");
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRC10(to, tokenId, value, name, symbol, decimals, nonce);
        }
    }

    function depositTRC10(address payable to, trcToken _tokenId,
        uint256 value, bytes32 name, bytes32 symbol, uint8 decimals, uint256 nonce) internal
    {
        uint256 tokenId = uint256(_tokenId);
        if (tokenIdMap[tokenId] == false) {
            tokenIdMap[tokenId] = true;
        }
        mintTRC10Contract.call(abi.encode(value, tokenId, name, symbol, decimals));
        to.transferToken(value, tokenId);
        emit DepositTRC10(to, tokenId, value, nonce);
    }

    // 4. depositTRC20
    function multiSignForDepositTRC20(address to, address mainChainAddress,
        uint256 value, uint256 nonce)
    public onlyNotStop onlyOracle
    {
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRC20(to, sideChainAddress, value, nonce);
        }
    }

    function depositTRC20(address to, address sideChainAddress, uint256 value, uint256 nonce) internal {
        IDApp(sideChainAddress).mint(to, value);
        emit DepositTRC20(to, sideChainAddress, value, nonce);
    }

    // 5. depositTRC721
    function multiSignForDepositTRC721(address to, address mainChainAddress, uint256 uId, uint256 nonce)
    public onlyNotStop onlyOracle {
        address sideChainAddress = mainToSideContractMap[mainChainAddress];
        require(sideChainAddress != address(0), "the main chain address hasn't mapped");
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRC721(to, sideChainAddress, uId, nonce);
        }
    }

    function depositTRC721(address to, address sideChainAddress, uint256 uId, uint256 nonce) internal {
        IDApp(sideChainAddress).mint(to, uId);
        emit DepositTRC721(to, sideChainAddress, uId, nonce);
    }

    // 6. depositTRX
    function multiSignForDepositTRX(address payable to, uint256 value, uint256 nonce) public onlyNotStop onlyOracle {
        bool needDeposit = multiSignForDeposit(nonce);
        if (needDeposit) {
            depositTRX(to, 2 * value, nonce);
        }
    }

    function depositTRX(address payable to, uint256 value, uint256 nonce) internal {
        mintTRXContract.call(abi.encode(value));
        to.transfer(value);
        emit DepositTRX(to, value, nonce);
    }

    function multiSignForDeposit(uint256 nonce) internal returns (bool) {
        SignMsg storage _signMsg = depositSigns[nonce];
        if (_signMsg.oracleSigned[msg.sender]) {
            return false;
        }
        _signMsg.oracleSigned[msg.sender] = true;
        _signMsg.signCnt += 1;

        if (!_signMsg.success && _signMsg.signCnt > numOracles * 2 / 3) {
            _signMsg.success = true;
            return true;
        }
        return false;
    }

    // 7. withdrawTRC10
    function withdrawTRC10(uint256 tokenId, uint256 tokenValue) payable
    public checkForTrc10(tokenId, tokenValue) onlyNotPause onlyNotStop isHuman
    returns (uint256 r)
    {
        require(tokenIdMap[uint256(msg.tokenid)], "tokenIdMap[`msg.tokenid] == false");
        require(msg.tokenvalue >= withdrawMinTrc10, "tokenvalue must be >= withdrawMinTrc10");
        require(msg.value >= withdrawFee, "value must be >= withdrawFee");
        if (msg.value > withdrawFee) {
            msg.sender.transfer(msg.value - withdrawFee);
        }
        if (msg.value > 0) {
            bonus += withdrawFee;
        }
        userWithdrawList.push(WithdrawMsg(msg.sender, address(0), msg.tokenid, msg.tokenvalue, DataModel.TokenKind.TRC10, DataModel.Status.SUCCESS));
        // burn
        address(0).transferToken(msg.tokenvalue, msg.tokenid);
        emit WithdrawTRC10(msg.sender, msg.tokenid, msg.tokenvalue, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRC10(uint256 nonce, bytes memory oracleSign)
    public onlyNotStop onlyOracle {
        if (!countMultiSignForWithdraw(nonce, oracleSign)) {
            return;
        }

        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.tokenId, withdrawMsg.valueOrUid, nonce));
        if (countSuccessSignForWithdraw(nonce, dataHash)) {
            emit MultiSignForWithdrawTRC10(withdrawMsg.user, withdrawMsg.tokenId, withdrawMsg.valueOrUid, nonce);
        }
    }

    // 8. withdrawTRC20
    function onTRC20Received(address from, uint256 value) payable
    public onlyNotPause onlyNotStop returns (uint256 r) {
        address mainChainAddress = sideToMainContractMap[msg.sender];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");
        require(value >= withdrawMinTrc20, "value must be >= withdrawMinTrc20");
        if (msg.value > 0) {
            bonus += msg.value;
        }
        userWithdrawList.push(WithdrawMsg(from, mainChainAddress, 0, value, DataModel.TokenKind.TRC20, DataModel.Status.SUCCESS));

        // burn
        DAppTRC20(msg.sender).burn(value);
        emit WithdrawTRC20(from, mainChainAddress, value, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRC20(uint256 nonce, bytes memory oracleSign)
    public onlyNotStop onlyOracle {
        if (!countMultiSignForWithdraw(nonce, oracleSign)) {
            return;
        }

        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce));
        if (countSuccessSignForWithdraw(nonce, dataHash)) {
            emit MultiSignForWithdrawTRC20(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
        }
    }

    // 9. withdrawTRC721
    function onTRC721Received(address from, uint256 uId) payable
    public onlyNotPause onlyNotStop returns (uint256 r) {
        address mainChainAddress = sideToMainContractMap[msg.sender];
        require(mainChainAddress != address(0), "mainChainAddress == address(0)");

        if (msg.value > 0) {
            bonus += msg.value;
        }
        userWithdrawList.push(WithdrawMsg(from, mainChainAddress, 0, uId, DataModel.TokenKind.TRC721, DataModel.Status.SUCCESS));

        // burn
        DAppTRC721(msg.sender).burn(uId);
        emit WithdrawTRC721(from, mainChainAddress, uId, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRC721(uint256 nonce, bytes memory oracleSign)
    public onlyNotStop onlyOracle {
        if (!countMultiSignForWithdraw(nonce, oracleSign)) {
            return;
        }

        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce));
        if (countSuccessSignForWithdraw(nonce, dataHash)) {
            emit MultiSignForWithdrawTRC721(withdrawMsg.user, withdrawMsg.mainChainAddress, withdrawMsg.valueOrUid, nonce);
        }
    }

    // 10. withdrawTRX
    function withdrawTRX() payable public onlyNotPause onlyNotStop isHuman returns (uint256 r) {
        require(msg.value >= withdrawMinTrx + withdrawFee, "value must be >= withdrawMinTrx+withdrawFee");
        if (msg.value > 0) {
            bonus += withdrawFee;
        }
        uint256 withdrawValue = msg.value - withdrawFee;
        userWithdrawList.push(WithdrawMsg(msg.sender, address(0), 0, withdrawValue, DataModel.TokenKind.TRX, DataModel.Status.SUCCESS));
        // burn
        address(0).transfer(withdrawValue);
        emit WithdrawTRX(msg.sender, withdrawValue, userWithdrawList.length - 1);
        r = userWithdrawList.length - 1;
    }

    function multiSignForWithdrawTRX(uint256 nonce, bytes memory oracleSign)
    public onlyNotStop onlyOracle {
        if (!countMultiSignForWithdraw(nonce, oracleSign)) {
            return;
        }

        WithdrawMsg storage withdrawMsg = userWithdrawList[nonce];
        bytes32 dataHash = keccak256(abi.encodePacked(withdrawMsg.user, withdrawMsg.valueOrUid, nonce));
        if (countSuccessSignForWithdraw(nonce, dataHash)) {
            emit MultiSignForWithdrawTRX(withdrawMsg.user, withdrawMsg.valueOrUid, nonce);
        }
    }

    function countMultiSignForWithdraw(uint256 nonce, bytes memory oracleSign) internal returns (bool){
        SignMsg storage _signMsg = withdrawSigns[nonce];
        if (_signMsg.oracleSigned[msg.sender]) {
            return false;
        }
        _signMsg.oracleSigned[msg.sender] = true;
        _signMsg.signs.push(oracleSign);
        _signMsg.signOracles.push(msg.sender);
        _signMsg.signCnt += 1;
        if (!_signMsg.success && _signMsg.signCnt > numOracles * 2 / 3) {
            return true;
        }
        return false;
    }

    // 11. retryWithdraw
    function retryWithdraw(uint256 nonce) payable public onlyNotPause onlyNotStop isHuman {
        require(msg.value >= retryFee, "msg.value need  >= retryFee");
        if (msg.value > retryFee) {
            msg.sender.transfer(msg.value - retryFee);
        }
        bonus += retryFee;
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
                emit MultiSignForWithdrawTRX(withdrawMsg.user, withdrawMsg.valueOrUid, nonce);
            } else {
                emit WithdrawTRX(withdrawMsg.user, withdrawMsg.valueOrUid, nonce);

            }
        }
    }

    function setLogicAddress(address _logicAddress) public onlyOracle {
        if (multiSignForDelegate(_logicAddress)) {
            changeLogicAddress(_logicAddress);
        }
    }

    function multiSignForDelegate(address _logicAddress) internal returns (bool) {

        SignMsg storage changeLogicSign = changeLogicSigns[_logicAddress];
        if (changeLogicSign.oracleSigned[msg.sender]) {
            return false;
        }
        changeLogicSign.oracleSigned[msg.sender] = true;
        changeLogicSign.signCnt += 1;

        if (!changeLogicSign.success && changeLogicSign.signCnt > numOracles * 2 / 3) {
            changeLogicSign.success = true;
            return true;
        }
        return false;
    }

    function countSuccessSignForWithdraw(uint256 nonce, bytes32 dataHash) internal returns (bool) {
        SignMsg storage _signMsg = withdrawSigns[nonce];
        if (_signMsg.success) {
            return false;
        }
        bytes32 ret = multivalidatesign(dataHash, _signMsg.signs, _signMsg.signOracles);
        uint256 count = countSuccess(ret);
        if (count > numOracles * 2 / 3) {
            _signMsg.success = true;
            return true;
        }
        return false;
    }

    function countSuccess(bytes32 ret) internal returns (uint256 count) {
        uint256 _num = uint256(ret);
        for (; _num > 0; ++count) {_num &= (_num - 1);}
        return count;
    }

    function() onlyNotPause onlyNotStop payable external {
        revert("not allow function fallback");
    }

    function setPause(bool isPause) external onlyOwner {
        pause = isPause;
    }

    function setStop(bool isStop) external onlyOwner {
        stop = isStop;
    }

    function setWithdrawMinTrx(uint256 minValue) external onlyOwner {
        withdrawMinTrx = minValue;
    }

    function setWithdrawMinTrc10(uint256 minValue) external onlyOwner {
        withdrawMinTrc10 = minValue;
    }

    function setWithdrawMinTrc20(uint256 minValue) external onlyOwner {
        withdrawMinTrc20 = minValue;
    }

    function setWithdrawFee(uint256 fee) external onlyOwner {
        require(fee <= 100_000_000, "less than 100 TRX");
        withdrawFee = fee;
    }

    function getWithdrawFee() view public returns (uint256) {
        return withdrawFee;
    }

    function getMainContractList() view public returns (address[] memory) {
        return mainContractList;
    }

    function getWithdrawMsg(uint256 nonce) view public returns (address, address, uint256, uint256, uint256, uint256){
        WithdrawMsg memory _withdrawMsg = userWithdrawList[nonce];
        return (_withdrawMsg.user, _withdrawMsg.mainChainAddress, uint256(_withdrawMsg.tokenId), _withdrawMsg.valueOrUid,
        uint256(_withdrawMsg._type), uint256(_withdrawMsg.status));

    }

    function mappingDone(uint256 nonce) view public returns (bool) {
        return mappingSigns[nonce].success;
    }

    function setRetryFee(uint256 fee) external onlyOwner {
        require(fee <= 100_000_000, "less than 100 TRX");
        retryFee = fee;
    }

    function mainContractCount() view external returns (uint256) {
        return mainContractList.length;
    }

    function depositDone(uint256 nonce) view external returns (bool r) {
        r = depositSigns[nonce].success;
    }

    function isOracle(address _oracle) view public returns (bool) {
        return oracles[_oracle];
    }

    function getCodeVersion() public returns (string memory) {
        return "1.0.3";
    }
}
