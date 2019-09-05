contract Delegatecallable {
    address public logicAddress;

    event DelegateResult(bool result, bytes msg);
    event LogicAddressChanged(address oldAddress, address newAddress);
    modifier goDelegateCall {
        if (logicAddress != address(0)) {
            (bool result,bytes memory mesg) = logicAddress.delegatecall(msg.data);
            emit DelegateResult(result, mesg);
            return;
        }
        _;
    }
    function changeLogicAddress(address newLoginAddress) internal {
        emit LogicAddressChanged(logicAddress, newLoginAddress);
        logicAddress = newLoginAddress;
    }
}
