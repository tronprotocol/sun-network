package org.tron.sunapi.service;

import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.protos.Protocol.Exchange;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.ExchangeCreateRequest;
import org.tron.sunapi.request.ExchangeTransactionRequest;
import org.tron.sunapi.response.TransactionResponse;

public interface Exchanges {
  SunNetworkResponse<TransactionResponse> exchangeCreate(ExchangeCreateRequest request);

  SunNetworkResponse<TransactionResponse>  exchangeInject(long exchangeId, String tokenIdStr, long quant);

  SunNetworkResponse<TransactionResponse> exchangeWithdraw(long exchangeId, String tokenIdStr, long quant);

  SunNetworkResponse<TransactionResponse> exchangeTransaction(ExchangeTransactionRequest request);

  SunNetworkResponse<ExchangeList> listExchanges();

  SunNetworkResponse<Exchange> getExchange(String id);

  SunNetworkResponse<ExchangeList> getExchangesListPaginated(int offset, int limit);
}
