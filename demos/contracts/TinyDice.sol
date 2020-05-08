pragma solidity ^0.4.25;
import "./common/SafeMath.sol";
import "./common/Ownership.sol";

/**
* TinyDiceGame
*/
contract TinyDice is  KillableOwnership {
    using SafeMath for uint256;
    
    uint256 constant precision = 1000000;
    uint256 constant public yi = 100000000;
    
    uint256 _R = 98 * precision; //返现率

    uint256 public index = 0;
    uint256 public batchIndex = 0;
    
    struct Item {
        address addr;
        uint256 amount;
        uint256 point;
        uint256 payList;
        uint256 randList;
    }

    mapping(uint256 => Item) public items;
    bytes32 private defaultSalt;

    event Bet(address _addr, uint256 _amount, uint256 _index);
    event WithDraw(address _addr, uint256 _amount);
    event UserWin(uint256, address _addr, uint256 _amount, uint256 _point, uint256 _random, uint256 _P, uint256 _O, uint256 _W);
    event UserLose(uint256, address _addr, uint256 _amount, uint256 _point, uint256 _random);
    
    constructor() public payable {
        defaultSalt = keccak256(abi.encodePacked(owner));
    }
  
    function bet(uint256 _point) isHuman whenNotPaused public  payable returns (uint256) {
        require(_point < 97 && _point > 1);
        require(msg.value >= 1 * precision && msg.value <= 1000 * precision);
        index = index.add(1);
        items[index] = Item(msg.sender, msg.value, _point, 0, 0);
       
        emit Bet(msg.sender, msg.value, index);
        return index;
    }

    function rtu() public returns (uint256, uint256, uint256, uint256) {
        require(batchIndex < index);
        uint256 currIndex = batchIndex.add(1);
        Item storage item = items[currIndex];
        
        // allowd overflow here.
        defaultSalt = bytes32(uint256(defaultSalt) + item.point + msg.value); 
        uint256 random = uint256(keccak256(abi.encodePacked(defaultSalt, blockhash(block.number - 1)))).mod(100);
        
        uint256 payOut = 0;
        
        if (random < item.point) {
            // win chance
            uint256 _P = item.point.sub(1);
            // Odds = Bonus Ratio / Winning Probability
            uint256 _O = _R.div(_P);
            // Winned
            uint256 _W = item.amount.mul(_O).div(precision);// safeDiv(safeMul(item.amount, _O), precision);
            payOut = _W;
            emit UserWin(currIndex, item.addr, item.amount, item.point, random, _P, _O, _W);
        } else {
            emit UserLose(currIndex, item.addr, item.amount, item.point, random);
        }
        
        if (payOut > 0) {
            address(item.addr).transfer(payOut);
        }
        
        item.payList = payOut;
        item.randList = random;
        
        batchIndex = currIndex;
        return (batchIndex, random, item.amount, payOut);
    }

    function check(uint256 _index) public view returns (address, uint256, uint256, uint256, uint256) {
        require(_index <= index);
        Item memory item = items[_index];
        return (item.addr, item.amount, item.point, item.randList, item.payList);
    }

    function stat() public view returns (uint256, uint256) {
        return (index, batchIndex);
    }

}

