package org.tron.sunapi.request;

import lombok.Data;

@Data
public class ExchangeTransactionRequest {

  long exchangeId;
  String tokenId;
  long quant;
  long expected;

  public ExchangeTransactionRequest() {
    this.exchangeId = 0;
    this.tokenId = null;
    this.quant = 0;
    this.expected = 0;
  }
}
