package org.tron.sunapi.request;

import java.util.HashMap;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AssertIssueRequest {
  public String name;
  public String totalSupplyStr;
  public String trxNumStr;
  public String icoNumStr;
  public String precisionStr;
  public String startYyyyMmDd;
  public String endYyyyMmDd;
  public String description ;
  public String url;
  public String freeNetLimitPerAccount;
  public String publicFreeNetLimitString;
  public HashMap<String, String> frozenSupply;

  public AssertIssueRequest() {
    this.totalSupplyStr = null;
    this.trxNumStr = null;
    this.icoNumStr = null;
    this.precisionStr = null;
    this.startYyyyMmDd = null;
    this.endYyyyMmDd = null;
    this.description = null;
    this.url = null;
    this.freeNetLimitPerAccount = null;
    this.publicFreeNetLimitString = null;
    this.frozenSupply = new HashMap<>();
  }
}
