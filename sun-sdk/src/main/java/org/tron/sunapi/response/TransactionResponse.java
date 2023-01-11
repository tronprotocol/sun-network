package org.tron.sunapi.response;

import lombok.Data;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.protos.Protocol.Transaction.Result.code;

@Data
public class TransactionResponse {

  /**
   * the response type, constant transaction or normal transaction
   */
  public ResponseType responseType;

  /**
   * the response code for normal transaction, see response_code
   */
  public response_code respCode;

  /**
   * the result for normal transaction, success or fail
   */
  public boolean result;

  /**
   * the result for normal transaction
   */
  public String message;

  /**
   * the transaction ID
   */
  public String trxId;

  /**
   * the response code for constant transaction
   */
  public code constantCode;

  /**
   * the result for constant transaction
   */
  public String constantResult;

  public enum ResponseType {
    TRANSACTION_NORMAL(0),

    TRANSACTION_CONSTANT(1);

    private int type;
    ResponseType(int type) {
      this.type = type;
    }
  }

  public boolean getResult() {
    return result;
  }

  public  TransactionResponse(String message) {
    this.respCode = null;
    this.result   = false;
    this.message  = message;
    this.trxId    = null;
    this.constantResult = null;
    this.responseType   = ResponseType.TRANSACTION_NORMAL;
  }

  public  TransactionResponse(Return returnCode) {
    this.respCode = returnCode.getCode();
    this.result   = returnCode.getResult();
    this.message  = returnCode.getMessage().toStringUtf8();
    this.trxId    = null;
    this.constantResult = null;
    this.responseType   = ResponseType.TRANSACTION_NORMAL;
  }

  public  TransactionResponse(Return returnCode, String trxId) {
    this.respCode = returnCode.getCode();
    this.result   = returnCode.getResult();
    this.message  = returnCode.getMessage().toStringUtf8();
    this.trxId    = trxId;
    this.constantResult = null;
    this.responseType   = ResponseType.TRANSACTION_NORMAL;
  }

  public  TransactionResponse(boolean result, code c, String trxId, String constantResult) {
    this.result         = result;
    this.trxId          = trxId;
    this.constantCode   = c;
    this.constantResult = constantResult;
    this.responseType   = ResponseType.TRANSACTION_CONSTANT;
  }

}
