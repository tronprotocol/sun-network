package org.tron.sunapi.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class FreezeBalanceRequest {
  long frozen_balance;
  long frozen_duration;
  int resourceCode;
  String receiverAddress;

  public FreezeBalanceRequest() {
    this.frozen_balance = 0;
    this.frozen_duration = 0;
    this.resourceCode = 0;
    this.receiverAddress = null;
  }

}
