pragma solidity 0.5.9;

contract InteractionContract {


    mapping(address => uint256) public allowableAddressAmountMap;

    address payable public owner;

    constructor () public {
        owner = msg.sender;
    }


    modifier isOwner {
        require(msg.sender == owner, "msg.sender != owner");
        _;
    }


    function deposit () payable public  {
        allowableAddressAmountMap[msg.sender]+= msg.value;
    }

    function withdraw () public {
        msg.sender.transfer(allowableAddressAmountMap[msg.sender]);
        allowableAddressAmountMap[msg.sender]=0;
    }

    function withdrawByOwner () public isOwner {
        owner.transfer(address(this).balance);
    }

}