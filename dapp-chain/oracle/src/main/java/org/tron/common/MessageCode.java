package org.tron.common;

public enum MessageCode {
  CHECK_TRANSACTION_SUCCESS(1, "tx: %s, check transaction result success"),
  CHECK_TRANSACTION_FAIL(2,
      "tx: %s, check transaction fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  BROADCAST_TRANSACTION_SUCCESS(3, "tx: %s, broadcast transaction result success"),
  BROADCAST_TRANSACTION_FAIL(4,
      "tx: %s, broadcast transaction  fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  CREATE_TRANSACTION_SUCCESS(5, "tx: %s, create transaction result success"),
  CREATE_TRANSACTION_FAIL(6,
      "nonce key : %s, create transaction  fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  NONCE_HAS_BE_SUCCEED(7, "the nonce %s has be processed successfully"),
  NONCE_IS_PROCESSING(8, "the nonce %s is processing");
  private int code;
  private String msg;

  MessageCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getMsg(String arg) {
    return String.format(CHECK_TRANSACTION_SUCCESS.msg, arg);
  }

}
