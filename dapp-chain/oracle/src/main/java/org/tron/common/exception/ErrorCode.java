package org.tron.common.exception;

public enum ErrorCode {
  CHECK_TRANSACTION_SUCCESS(1, "tx: %s, check transaction result success"),
  CHECK_TRANSACTION_FAIL(2,
      "tx: %s, check transaction fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  BROADCAST_TRANSACTION_SUCCESS(3, "tx: %s, broadcast transaction result success"),
  BROADCAST_TRANSACTION_FAIL(4,
      "tx: %s, broadcast transaction  fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  CREATE_TRANSACTION_SUCCESS(5, "tx: %s, create transaction result success"),
  CREATE_TRANSACTION_FAIL(6,
      "tx: %s, create transaction  fail, please resolve this problem by reviewing and inspecting logs of oracle");
  private int code;
  private String msg;

  ErrorCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getMsg(String txId) {
    return String.format(CHECK_TRANSACTION_SUCCESS.msg, txId);
  }

}
