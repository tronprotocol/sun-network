pragma solidity ^0.4.24;

import "./ownership/Ownable.sol";
import "./ECVerify.sol";


contract ValidatorManagerContract is Ownable {
    using ECVerify for bytes32;

    mapping(address => bool) public allowedTokens;
    mapping(address => uint256) public nonces;
    mapping(address => bool) validators;

    uint8 threshold_num;
    uint8 threshold_denom;
    uint256 public numValidators;
    uint256 public nonce; // used for replay protection when adding/removing validators

    event AddedValidator(address validator);
    event RemovedValidator(address validator);

    modifier onlyValidator() {require(checkValidator(msg.sender));
        _;}

    constructor (address[] _validators, uint8 _threshold_num, uint8 _threshold_denom) public {
        uint256 length = _validators.length;
        require(length > 0);

        threshold_num = _threshold_num;
        threshold_denom = _threshold_denom;
        for (uint256 i = 0; i < length; i++) {
            require(_validators[i] != address(0));
            validators[_validators[i]] = true;
            emit AddedValidator(_validators[i]);
        }
        numValidators = _validators.length;
    }

    modifier isVerifiedByValidator(uint256 num, address contractAddress, bytes sig) {
        // prevent replay attacks by adding the nonce in the sig
        // if a validator signs an invalid nonce,
        // it won't pass the signature verification
        // since the nonce in the hash is stored in the contract
        bytes32 hash = keccak256(abi.encodePacked(msg.sender, contractAddress, nonces[msg.sender], num));
        address sender = hash.recover(sig);
        require(sender == msg.sender, "Message not signed by a validator");
        _;
        nonces[msg.sender]++;
        // increment nonce after execution
    }

    function checkValidator(address _address) public view returns (bool) {
        // owner is a permanent validator
        if (_address == owner) {
            return true;
        }
        return validators[_address];
    }

    //    function addValidator(address validator, uint8[] v, bytes32[] r, bytes32[] s)
    //    external
    //    {
    //        require(!validators[validator], "Already a validator");
    //
    //        // Check that we have enough signatures
    //        bytes32 message = keccak256(abi.encodePacked("add", validator, nonce));
    //        checkThreshold(message, v, r, s);
    //
    //        // Add validator and increment nonce
    //        validators[validator] = true;
    //        numValidators++;
    //        nonce++;
    //        emit AddedValidator(validator);
    //    }

    //    function removeValidator(address validator, uint8[] v, bytes32[] r, bytes32[] s)
    //    external
    //    {
    //        require(validators[validator], "Not a validator");
    //        // The last validator may not remove himself
    //        require(numValidators > 1);
    //
    //        // Check that we have enough signatures
    //        bytes32 message = keccak256(abi.encodePacked("remove", validator, nonce));
    //        checkThreshold(message, v, r, s);
    //
    //        delete validators[validator];
    //        numValidators--;
    //        nonce++;
    //        emit RemovedValidator(validator);
    //    }

    // Can't pass bytes[] to use the whole sig due to ABI enc
    //, so we need to send v,r,s params
    //    function checkThreshold(bytes32 message, uint8[] v, bytes32[] r, bytes32[] s) private view {
    //        require(v.length > 0 && v.length == r.length && r.length == s.length,
    //            "Incorrect number of params");
    //        require(v.length >= (threshold_num * numValidators / threshold_denom),
    //            "Not enough votes");
    //
    //        bytes32 hash = keccak256(abi.encodePacked("\x19Ethereum Signed Message:\n32", message));
    //        uint256 sig_length = v.length;
    //
    //        // Check that all addresess were from validators
    //        // Prevent duplicates by requiring that the sender sigs
    //        // get submitted in increasing order
    //        // influenced by
    //        // https://github.com/christianlundkvist/simple-multisig
    //        address lastAdd = address(0);
    //        for (uint256 i = 0; i < sig_length; i++) {
    //            address signer = ecrecover(hash, v[i], r[i], s[i]);
    //            require(signer > lastAdd && validators[signer], "Not signed by a validator");
    //            lastAdd = signer;
    //        }
    //    }

    function toggleToken(address _token) public onlyValidator {
        allowedTokens[_token] = !allowedTokens[_token];
    }
}
