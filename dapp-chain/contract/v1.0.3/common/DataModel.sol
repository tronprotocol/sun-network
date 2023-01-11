contract DataModel {
    enum TokenKind {
        TRX, // 0
        TRC10, // 1
        TRC20, // 2
        TRC721       // 3
    }
    enum Status {
        SUCCESS, // 0
        LOCKING, // 1
        FAIL, // 2
        REFUNDED        // 3
    }
}
