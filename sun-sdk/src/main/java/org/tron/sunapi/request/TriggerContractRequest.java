package org.tron.sunapi.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class TriggerContractRequest  {
  public String contractAddrStr;
  public String methodStr;
  public String argsStr;
  public boolean isHex;
  public long feeLimit;
  public long callValue;
  public long tokenCallValue;
  public String tokenId;

  public TriggerContractRequest() {
    this.contractAddrStr = null;
    this.methodStr = null;
    this.argsStr = null;
    this.isHex = false;
    this.feeLimit = 0;
    this.callValue = 0;
    this.tokenCallValue = 0;
    this.tokenId = null;
  }


}
