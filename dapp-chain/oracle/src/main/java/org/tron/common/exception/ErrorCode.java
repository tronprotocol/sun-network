package org.tron.common.exception;

public enum ErrorCode {
  CHECK_TRANSACTION_SUCCESS(1,
      "tx: %s, check transaction result success"),
  CHECK_TRANSACTION_FAIL(2,
      "tx: %s, fail, please resolve this problem by reviewing and inspecting logs of oracle");
  private int code;
  private String msg;

  ErrorCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public static String getCheckTransactionSuccess(String txId) {
    return String.format(CHECK_TRANSACTION_SUCCESS.msg, txId);
  }

  public static String getCheckTransactionFail(String txId) {
    return String.format(CHECK_TRANSACTION_FAIL.msg, txId);
  }
}
