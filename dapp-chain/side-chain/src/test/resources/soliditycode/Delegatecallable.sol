import "./Versionable.sol";

contract Delegatecallable is Versionable {
    address public logicAddress;

    event DelegateResult(bool result, bytes msg);
    event LogicAddressChanged(address oldAddress, address newAddress);
    modifier goDelegateCall {
        _;
    }
    function changeLogicAddress(address newLogicAddress) internal {
        string memory newVersion;
        if (newLogicAddress == address(0)) {
            newVersion = initVersion;
        } else {
            newVersion = Versionable(newLogicAddress).getCodeVersion();
        }
        emit ChangeVersion(codeVersion, newVersion);
        emit LogicAddressChanged(logicAddress, newLogicAddress);
        codeVersion = newVersion;
        logicAddress = newLogicAddress;
    }
}