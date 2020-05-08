package org.tron.sunapi.request;

import lombok.Data;

@Data
public class ExchangeCreateRequest {
  String firstTokenId;
  long firstTokenBalance;
  String secondTokenId;
  long secondTokenBalance;

  public ExchangeCreateRequest() {
    this.firstTokenId       = null;
    this.firstTokenBalance  = 0;
    this.secondTokenId      = null;
    this.secondTokenBalance = 0;
  }

}
