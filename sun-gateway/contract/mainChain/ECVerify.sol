pragma solidity ^0.4.24;


library ECVerify {

    enum SignatureMode {
        EIP712,
        GETH,
        TREZOR
    }

    function recover(bytes32 hash, bytes signature) internal pure returns (address) {
        require(signature.length == 66);
        SignatureMode mode = SignatureMode(uint8(signature[0]));

        uint8 v;
        bytes32 r;
        bytes32 s;
        assembly {
            r := mload(add(signature, 33))
            s := mload(add(signature, 65))
            v := and(mload(add(signature, 66)), 255)
        }

        if (mode == SignatureMode.GETH) {
            hash = keccak256(abi.encodePacked("\x19Ethereum Signed Message:\n32", hash));
        } else if (mode == SignatureMode.TREZOR) {
            hash = keccak256(abi.encodePacked("\x19Ethereum Signed Message:\n\x20", hash));
        }

        return ecrecover(hash, v, r, s);
    }

    function ecverify(bytes32 hash, bytes sig, address signer) internal pure returns (bool) {
        return signer == recover(hash, sig);
    }
}
