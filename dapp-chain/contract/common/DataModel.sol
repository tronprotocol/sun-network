pragma solidity ^0.4.24;

contract DataModel {
    enum TokenKind {
        TRX,
        TRC10,
        TRC20,
        TRC721
    }
    enum Status {
        LOCKING,
        SUCCESS,
        FAIL,
        REFUNDED
    }
}
