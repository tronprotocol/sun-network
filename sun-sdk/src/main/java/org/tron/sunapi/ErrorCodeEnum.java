package org.tron.sunapi;

public enum ErrorCodeEnum {

  SUCCESS("0", "success"),
  FAILED("1", "failed"),

  //common error
  COMMON_PARAM_EMPTY("1001", "parameter empty"),
  COMMON_PARAM_ERROR("1002", "parameter error"),
  COMMON_PARAM_ACCOUNT("1003", "account error"),
  COMMON_MAIN_CHAIN_PARAM_ERROR("1004", "main chain parameter error"),
  COMMON_SIDE_CHAIN_PARAM_ERROR("1005", "side chain parameter error"),
  COMMON_SIDE_CHAIN_INVALID_GATEWAY("1006", "side chain invalid gateway"),
  COMMON_MAIN_CHAIN_INVALID_GATEWAY("1007", "main chain invalid gateway"),

  //exception
  EXCEPTION_CIPHER("2001", "cipher exception"),
  EXCEPTION_UNKNOWN("2002", "unknown exception"),
  EXCEPTION_ENCODING("2003", "encoding exception"),
  EXCEPTION_CANCEL("2004", "cancel exception"),
  EXCEPTION_IO("2005", "cipher exception"),
  EXCEPTION_INVALID_PROTOCOL_BUFFER("2006", "invalid protocol buffer"),

  //
  ERROR_UNKNOWN("1000001", "unknown error");

  private String code;
  private String desc;

  ErrorCodeEnum(String code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  public String getCode() {
    return this.code;
  }

  public String getDesc() {
    return desc;
  }

  @Override
  public String toString() {
    return "ErrorCodeEnum{" + "code='" + code + '\'' + ", desc='" + desc + '\'' + '}';
  }


}
