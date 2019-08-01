package org.tron.sunapi.response;

import lombok.Data;

@Data
public class TriggerContractResponse {
  public String trxId;

  public TriggerContractResponse(String trxId) {
    this.trxId = trxId;
  }
}
