pragma solidity ^0.4.25;
import "./common/SafeMath.sol";
import "./common/Ownership.sol";

/**
* DonateNowGame
*/
contract DonateNow is  KillableOwnership {
    using SafeMath for uint256;
    
    struct Item {
        address addr;
        uint256 amount;
        string  msg;
    }
    uint256 public index;
    uint256 public totalDonation;

    mapping(uint256 => Item) public items;

    event Donate(address _addr, uint256 _amount, uint256 _index);
    
    constructor() public payable {}
  
    function donate(string _msg) isHuman whenNotPaused public  payable returns (uint256) {
        require(msg.value > 0);
        totalDonation = totalDonation.add(msg.value);
        index = index.add(1);
        items[index] = Item(msg.sender, msg.value, _msg);
       
        emit Donate(msg.sender, msg.value, index);
        return index;
    }

    function check(uint256 _index) public view returns (address, uint256, string) {
        require(_index <= index);
        return (items[_index].addr, items[_index].amount, items[_index].msg);
    }

}

