pragma solidity ^0.4.24;


library ECVerify {

    function recover(bytes32 hash, bytes signature) internal pure returns (address) {
        uint8 v;
        bytes32 r;
        bytes32 s;
        assembly {
            r := mload(add(signature, 32))
            s := mload(add(signature, 64))
            v := and(mload(add(signature, 65)), 255)
        }
        if(v<27){
            v+=27;
        }
        return ecrecover(hash, v, r, s);
    }

    function ecverify(bytes32 hash, bytes sig, address signer) internal pure returns (bool) {
        return signer == recover(hash, sig);
    }
}
