package org.tron.common;

public enum MessageCode {
  CHECK_TRANSACTION_SUCCESS(1, "%s tx: %s, check transaction result success"),
  CHECK_TRANSACTION_FAIL(2,
      "%s tx: %s, check transaction fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  BROADCAST_TRANSACTION_SUCCESS(3, "%s tx: %s, broadcast transaction result success"),
  BROADCAST_TRANSACTION_FAIL(4,
      "%s tx: %s, broadcast transaction  fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  CREATE_TRANSACTION_SUCCESS(5, "%s tx: %s, create transaction result success"),
  CREATE_TRANSACTION_FAIL(6,
      "%s nonce key : %s, create transaction  fail, please resolve this problem by reviewing and inspecting logs of oracle"),
  NONCE_HAS_BE_SUCCEED(7, "%s, the nonce %s has be processed successfully"),
  NONCE_IS_PROCESSING(8, "%s, the nonce %s is processing");
  private int code;
  private String msg;

  MessageCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }

  public String getMsg(String chain, String key) {
    return String.format(msg, chain, key);
  }

}
