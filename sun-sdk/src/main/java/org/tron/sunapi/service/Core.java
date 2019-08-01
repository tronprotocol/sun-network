package org.tron.sunapi.service;

import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.response.TransactionResponse;

public interface Core {
  SunNetworkResponse<Integer> sendCoin(String toAddress, String amountStr);

  SunNetworkResponse<TransactionResponse>  broadcastTransaction(String transactionStr);

}
