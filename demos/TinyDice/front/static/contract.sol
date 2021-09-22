pragma solidity ^0.4.24;

/**
 * Math operations with safety checks
 */
contract SafeMath {
    function safeMul(uint256 a, uint256 b) internal pure returns (uint256) {
        uint256 c = a * b;
        _assert(a == 0 || c / a == b);
        return c;
    }

    function safeDiv(uint256 a, uint256 b) internal pure returns (uint256) {
        _assert(b > 0);
        uint256 c = a / b;
        _assert(a == b * c + a % b);
        return c;
    }

    function safeSub(uint256 a, uint256 b) internal pure returns (uint256) {
        _assert(b <= a);
        return a - b;
    }

    function safeAdd(uint256 a, uint256 b) internal pure returns (uint256) {
        uint256 c = a + b;
        _assert(c>=a && c>=b);
        return c;
    }

    function _assert(bool assertion) internal pure {
        if (!assertion) {
            revert();
        }
    }
}

contract Ownable {
    address public owner;
    bool public paused = false;

    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);

    constructor() public {
        owner = msg.sender;
    }

    modifier onlyOwner() {
        require(msg.sender == owner);
        _;
    }

    function transferOwnership(address newOwner) public onlyOwner {
        require(newOwner != address(0));
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }

    modifier whenNotPaused() {
        require(!paused);
        _;
    }

    modifier whenPaused {
        require(paused);
        _;
    }

    function pause() external onlyOwner whenNotPaused {
        paused = true;
    }

    function unpause() public onlyOwner whenPaused {
        paused = false;
    }
}

/**
*  TronBet
*/
contract TronBet is Ownable, SafeMath {
    uint256   [5] winPercent = [17, 33, 50, 67, 84];
    uint256   [5] rewardPercent = [560, 280, 190, 140, 115];

    uint256 public totalRoll = 0;
    uint256 public totalReward = 0;
    uint256 public minWager = 1000000;
    uint256 public maxWager = 10000000000;
    address [100] lastUser;
    uint256 lastUserIndex = 0;

    mapping (address => uint256) luckPoint;
    mapping (address => uint256) luckPool;
    mapping (address => uint256) reward;


    event RollDice(address _addr, uint256 _wp, uint256 _random, uint256 _reward);
    event NotEnough(address _addr, uint256 _totalReward, uint256 _contractBalance);
    event Refund(address _addr, uint256 _amount);
    event Deposit(uint256 _amount);
    event Withdraw(uint256 _amount);
    event Log(uint256 _random1, uint256 _random2, uint256 _random);

    constructor() public payable {}

    function deposit() public payable onlyOwner whenNotPaused {
        emit Deposit(msg.value);
    }

    function withdraw(uint256 _amount) public onlyOwner whenNotPaused returns(uint256) {
        require(_amount > 0);
        if (!msg.sender.send(_amount)) {
            revert();
        }
        emit Withdraw(_amount);
        return _amount;
    }

    function setMinMax(uint256 _min, uint256 _max) public onlyOwner whenNotPaused {
        require(_min > 0);
        require(_max > _min);
        minWager = _min;
        maxWager = _max;
    }

    function rollDice(uint256 _point) public payable whenNotPaused returns (uint256) {
        require(_point >= 1 && _point <= 5);
        require(msg.value >= minWager && msg.value <= maxWager);

        totalRoll += 1;

        lastUser[lastUserIndex % 100] = msg.sender;
        lastUserIndex ++;
        uint256 amount = msg.value;

        uint256 random1 = uint256(blockhash(block.number-1));
        uint256 random2 = uint256(lastUser[random1 % 100]);
        uint256 random3 = uint256(block.coinbase);
        uint256 seed = (random1 + random2 + random3 + now) % 100 + 1;
        uint256 random =  uint256(keccak256(seed, msg.sender, totalRoll)) % 100 + 1;
        emit Log(random3, seed, random);

        uint256 _reward = 0;
        uint256 _wp = winPercent[_point - 1];
        if (random < _wp) {
            _reward = safeDiv(safeMul(amount, rewardPercent[_point - 1]), 100);
            reward[msg.sender] = safeAdd(reward[msg.sender], _reward);
            luckPoint[msg.sender] = 0;
            luckPool[msg.sender] = 0;
        } else {
            if (luckPoint[msg.sender] == 2) {
                _reward = luckPool[msg.sender];
                luckPool[msg.sender] = safeAdd(luckPool[msg.sender], safeDiv(amount, 10));
                luckPoint[msg.sender] = luckPoint[msg.sender] + 1;
            } else if (luckPoint[msg.sender] == 5) {
                _reward = safeMul(luckPool[msg.sender], 2);
                luckPool[msg.sender] = 0;
                luckPoint[msg.sender] = 0;
            } else {
                luckPool[msg.sender] = safeAdd(luckPool[msg.sender], safeDiv(amount, 10));
                luckPoint[msg.sender] = luckPoint[msg.sender] + 1;
            }
        }

        if ((totalReward + _reward) > address(this).balance) {
            emit NotEnough(msg.sender, totalReward + _reward, address(this).balance);
            return _reward;
        }

        reward[msg.sender] = safeAdd(reward[msg.sender], _reward);
        totalReward = totalReward + _reward;

        emit RollDice(msg.sender, _wp, random, _reward);
        return _reward;
    }

    function refund() public whenNotPaused returns(uint256) {
        uint256 amount = reward[msg.sender];
        require(amount > 0);
        reward[msg.sender] = 0;
        if (!msg.sender.send(amount)) {
            revert();
        }
        lastUser[lastUserIndex % 100] = msg.sender;
        lastUserIndex ++;
        emit Refund(msg.sender, amount);
        return amount;
    }

    function getReward() public view returns(uint256) {
        return reward[msg.sender];
    }

    function getLuckPoint() public view returns(uint256) {
        return luckPoint[msg.sender];
    }

    function getLuckPool() public view returns(uint256) {
        return luckPool[msg.sender];
    }

    function getContractBalance() public view returns(uint256) {
        return address(this).balance;
    }

}