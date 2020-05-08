pragma solidity ^0.4.25;
import "./Address.sol";


contract Ownership {
    using Address for address;
    
    address public owner;
    bool public paused = false;
    mapping(address => uint256)  internal  admins;

    event OwnershipTransferred(address indexed previousOwner, address indexed newOwner);
    event SetAdmin(address, uint256);
    event GamePaused(address);
    event GameUnPaused(address);
    event Ownerkilled(address, uint256);

    constructor() public {
        owner = msg.sender;
        admins[owner] = 1;
    }

    modifier onlyOwner(){
        require(msg.sender == owner);
        _;
    }
    
    /*
     * only human is allowed to call this contract
     */
    modifier isHuman() {
        require((bytes32(msg.sender)) == (bytes32(tx.origin)));
        _;
    }
    
    /*
     * only human is allowed to call this contract
     */
    modifier notContract(address addr_) {
        require(!addr_.isContract());
        _;
    }

    modifier whenNotPaused() {
        require(!paused);
        _;
    }

    modifier whenPaused {
        require(paused);
        _;
    }

    function isPaused() view public onlyOwner returns(bool) {
        return paused;
    }

    function pause() public onlyOwner {
        paused = true;
        emit GamePaused(msg.sender);
    }

    function unPause() public onlyOwner {
        paused = false;
        emit GameUnPaused(msg.sender);
    }
    
    function transferOwner(address newOwner) public onlyOwner {
        require(newOwner != address(0));
        emit OwnershipTransferred(owner, newOwner);
        owner = newOwner;
    }
}

contract KillableOwnership is Ownership {
    
    function ownerkill() public onlyOwner {
        emit Ownerkilled(msg.sender, address(this).balance);
        selfdestruct(owner);
    }
}