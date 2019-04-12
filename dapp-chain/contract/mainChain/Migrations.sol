pragma solidity ^0.4.25;

 library SafeMath {
    /**
    * @dev Multiplies two numbers, throws on overflow.
    */
    function mul(uint256 a, uint256 b) 
        internal 
        pure 
        returns (uint256 c) 
    {
        if (a == 0) {
            return 0;
        }
        c = a * b;
        require(c / a == b, "SafeMath mul failed");
        return c;
    }

    /**
    * @dev Integer division of two numbers, truncating the quotient.
    */
    function div(uint256 a, uint256 b) internal pure returns (uint256) {
        // assert(b > 0); // Solidity automatically throws when dividing by 0
        uint256 c = a / b;
        // assert(a == b * c + a % b); // There is no case in which this doesn't hold
        return c;
    }
    
    /**
    * @dev Subtracts two numbers, throws on overflow (i.e. if subtrahend is greater than minuend).
    */
    function sub(uint256 a, uint256 b)
        internal
        pure
        returns (uint256) 
    {
        require(b <= a, "SafeMath sub failed");
        return a - b;
    }

    /**
    * @dev Adds two numbers, throws on overflow.
    */
    function add(uint256 a, uint256 b)
        internal
        pure
        returns (uint256 c) 
    {
        c = a + b;
        require(c >= a, "SafeMath add failed");
        return c;
    }
    
    /**
     * @dev gives square root of given x.
     */
    function sqrt(uint256 x)
        internal
        pure
        returns (uint256 y) 
    {
        uint256 z = ((add(x,1)) / 2);
        y = x;
        while (z < y) 
        {
            y = z;
            z = ((add((x / z),z)) / 2);
        }
    }
    
    /**
     * @dev gives square. multiplies x by x
     */
    function sq(uint256 x)
        internal
        pure
        returns (uint256)
    {
        return (mul(x,x));
    }
    
    /**
     * @dev x to the power of y 
     */
    function pwr(uint256 x, uint256 y)
        internal 
        pure 
        returns (uint256)
    {
        if (x==0)
            return (0);
        else if (y==0)
            return (1);
        else 
        {
            uint256 z = x;
            for (uint256 i=1; i < y; i++)
                z = mul(z,x);
            return (z);
        }
    }
}

library SDDatasets {
    struct Player {
        uint256 planCount;
        mapping(uint256=>PalyerPlan) plans;
    }
    
    struct PalyerPlan {
        uint256 planId;
        uint256 startTime;
        uint256 startTimeSec;
        uint256 invested;
        uint256 atTimeSec;
        uint256 payTrx;
        bool isClose;
    }

    struct Plan {
        uint256 interest;
        uint256 dayRange;
    }    
}

contract TronDeposit {
    using SafeMath              for *;

    uint256 public totalInvestedTron = 0;
    uint256 public donatedTRX = 0;

    uint256 public totalWithdrawals = 0;
    
    mapping (address => bool) public registered;
    uint256 public userCount = 0;

    mapping (address => SDDatasets.Player) public player_;
    mapping (uint256 => SDDatasets.Plan) private plan_;
    
    mapping (address => uint256) public affiliateCommision;
    mapping (address => uint256) public affiliateCommisionEarned;
    
    mapping (address => uint256) public totalEarnedTRX;
    
    function GetPlanByAddress() public 
        view returns(uint256[],uint256[],uint256[],uint256[],uint256[],bool[])
    {
        uint256[] memory planIds = new  uint256[] (player_[msg.sender].planCount);
        uint256[] memory startTimeSecs = new  uint256[] (player_[msg.sender].planCount);
        uint256[] memory investeds = new  uint256[] (player_[msg.sender].planCount);
        uint256[] memory atTimeSecs = new  uint256[] (player_[msg.sender].planCount);
        uint256[] memory payTrxs = new  uint256[] (player_[msg.sender].planCount);
        bool[] memory isCloses = new  bool[] (player_[msg.sender].planCount);
        
        for(uint i = 0; i < player_[msg.sender].planCount; i++) {
            planIds[i] = player_[msg.sender].plans[i].planId;
            startTimeSecs[i] = player_[msg.sender].plans[i].startTimeSec;
            investeds[i] = player_[msg.sender].plans[i].invested;
            atTimeSecs[i] = player_[msg.sender].plans[i].atTimeSec;
            payTrxs[i] = player_[msg.sender].plans[i].payTrx;
            isCloses[i] = player_[msg.sender].plans[i].isClose;
        }
        
        return
        (
            planIds,
            startTimeSecs,
            investeds,
            atTimeSecs,
            payTrxs,
            isCloses
        );
    }

    function donateTRX() public payable {
        donatedTRX = SafeMath.add(msg.value, donatedTRX);
    }
    
    function register_(address customer) private{
        require(registered[customer] != true);
        player_[customer].planCount = 0;
        registered[customer] = true;
        userCount = SafeMath.add(userCount, 1);
    }
    
    function deposit(uint256 planNumber, address affiliate) public payable {
        require(now > contractStartTime, "Contract is not active yet!");

        /* disables the ability to change start time */
        contractStarted = true;

        require(planNumber >= 1 && 4 >= planNumber);
        require(msg.value >= 10000000, "invest amount error, min depost 10 trx");

        uint256 investment = SafeMath.div(SafeMath.mul(msg.value, 99), 100);
        donatedTRX = SafeMath.add(SafeMath.sub(msg.value, investment), donatedTRX);
        
        //first
        if(registered[msg.sender] != true) {
            register_(msg.sender);
        }
        
        // record block number and invested amount
        uint256 planCount = player_[msg.sender].planCount;
        player_[msg.sender].plans[planCount].planId = planNumber;
        player_[msg.sender].plans[planCount].startTime = now;
        player_[msg.sender].plans[planCount].startTimeSec = now;
        player_[msg.sender].plans[planCount].atTimeSec = now;
        player_[msg.sender].plans[planCount].invested = investment;
        player_[msg.sender].plans[planCount].payTrx = 0;
        player_[msg.sender].plans[planCount].isClose = false;
        
        player_[msg.sender].planCount = player_[msg.sender].planCount.add(1);

        totalInvestedTron = SafeMath.add(totalInvestedTron, msg.value);
        
        if(affiliate != 0x0 && affiliate != msg.sender){
            affiliateCommision[affiliate] = SafeMath.add(affiliateCommision[affiliate], SafeMath.div(SafeMath.mul(msg.value, 6), 100));
        }
    }
    
    function getAffiliateProfit() view public returns(uint256) {
        return affiliateCommision[msg.sender];
    }
    
    function getTotalAffiliateProfitEarned() view public returns(uint256) {
        return affiliateCommisionEarned[msg.sender];
    }
    
    function withdrawAffiliateCommision() public {
        require(affiliateCommision[msg.sender] > 0);
        uint256 commision = affiliateCommision[msg.sender];
        affiliateCommision[msg.sender] = 0;
        affiliateCommisionEarned[msg.sender] = SafeMath.add(affiliateCommisionEarned[msg.sender], commision);
        totalWithdrawals = SafeMath.add(totalWithdrawals, commision);
        msg.sender.transfer(commision);
    }
    
    function withdraw() public payable {
        require(msg.value == 0, "withdraw fee is 0 trx, please set the exact amount");

        require(registered[msg.sender] == true);
        
        uint256 totalCredit = 0;

        for(uint i = 0; i < player_[msg.sender].planCount; i++) {
            if (player_[msg.sender].plans[i].isClose) {
                continue;
            }

            SDDatasets.Plan plan = plan_[player_[msg.sender].plans[i].planId];
            
            bool bClose = false;
            uint256 calSec = now;
            if (plan.dayRange > 0) {
                
                uint256 endTime = player_[msg.sender].plans[i].startTimeSec.add(plan.dayRange*60*60*24);
                if (now >= endTime){
                    calSec = endTime;
                    bClose = true;
                }
            }
            
            uint256 amount = player_[msg.sender].plans[i].invested * plan.interest / 10000 * (calSec - player_[msg.sender].plans[i].atTimeSec) / (60*60*24);

            totalCredit = SafeMath.add(totalCredit, amount);

            // record block number and invested amount (msg.value) of this transaction
            player_[msg.sender].plans[i].atTimeSec = calSec;
            player_[msg.sender].plans[i].isClose = bClose;
            player_[msg.sender].plans[i].payTrx += amount;
        }
        
        require(totalCredit > 0);
        
        affiliateCommision[developerAddress] = SafeMath.add(affiliateCommision[developerAddress], SafeMath.div(SafeMath.mul(totalCredit, 5), 100));
        affiliateCommision[promoter1] = SafeMath.add(affiliateCommision[promoter1], SafeMath.div(SafeMath.mul(totalCredit, 2), 100));
        affiliateCommision[promoter2] = SafeMath.add(affiliateCommision[promoter2], SafeMath.div(SafeMath.mul(totalCredit, 2), 100));
        affiliateCommision[promoter3] = SafeMath.add(affiliateCommision[promoter3], SafeMath.div(SafeMath.mul(totalCredit, 1), 100));
        
        totalEarnedTRX[msg.sender] = SafeMath.add(totalEarnedTRX[msg.sender], totalCredit);

        totalWithdrawals = SafeMath.add(totalWithdrawals, totalCredit);
        
        msg.sender.transfer(totalCredit);
    }
    
    function totalEarnedTRX() view public returns(uint256) {
        return totalEarnedTRX[msg.sender];
    }
    
    function getCreditBalance() view public returns(uint256) {
        
        uint256 totalCredit = 0;

        for(uint i = 0; i < player_[msg.sender].planCount; i++) {
            if (player_[msg.sender].plans[i].isClose) {
                continue;
            }

            SDDatasets.Plan plan = plan_[player_[msg.sender].plans[i].planId];
            
            uint256 amount = player_[msg.sender].plans[i].invested * plan.interest / 10000 * (now - player_[msg.sender].plans[i].atTimeSec) / (60*60*24);
            
            totalCredit = SafeMath.add(totalCredit, amount);
        }
        
        return totalCredit;
    }

    address public developerAddress;
    address public promoter1;
    address public promoter2;
    address public promoter3;

    bool public contractStarted = false;
    uint256 public contractStartTime = 1554069600;

    /* If the start time needs to be delayed, this can be called. */
    function setStartTime(uint256 newStartTime) public {
        require(msg.sender == developerAddress && contractStarted == false, "Contract already active.");
        contractStartTime = newStartTime;
    }

    constructor(address devAddr, address promoter1_, address promoter2_, address promoter3_) public {
        plan_[1] = SDDatasets.Plan(460, 0);
        plan_[2] = SDDatasets.Plan(560, 35);
        plan_[3] = SDDatasets.Plan(660, 20);
        plan_[4] = SDDatasets.Plan(760, 16);

        developerAddress = devAddr;
        promoter1 = promoter1_;
        promoter2 = promoter2_;
        promoter3 = promoter3_;
    }
}