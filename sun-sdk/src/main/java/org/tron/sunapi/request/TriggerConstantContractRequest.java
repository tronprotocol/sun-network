package org.tron.sunapi.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class TriggerConstantContractRequest {

  public String contractAddrStr;
  public String methodStr;
  public String argsStr;
  public boolean isHex;
  public long feeLimit;

  public TriggerConstantContractRequest() {
    this.contractAddrStr = null;
    this.methodStr = null;
    this.argsStr = null;
    this.isHex = false;
    this.feeLimit = 0;
  }


}
