import "./VersionableForSidechain.sol";

contract Delegatecallable is Versionable {
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
        string memory newVersion = Versionable(newLoginAddress).getCodeVersion();
        emit ChangeVersion(codeVersion, newVersion);
        emit LogicAddressChanged(logicAddress, newLoginAddress);
        codeVersion = newVersion;
        logicAddress = newLoginAddress;
    }
}
