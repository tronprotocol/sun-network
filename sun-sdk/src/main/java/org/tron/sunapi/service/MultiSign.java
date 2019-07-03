package org.tron.sunapi.service;

import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.protos.Protocol.Transaction;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.response.TransactionResponse;

public interface MultiSign {
  SunNetworkResponse<TransactionSignWeight> getTransactionSignWeight(String transactionStr);

  SunNetworkResponse<TransactionApprovedList> getTransactionApprovedList(String transactionStr);

  SunNetworkResponse<Transaction>  addTransactionSign(String transactionStr);

  SunNetworkResponse<TransactionResponse> updateAccountPermission(String address, String permissionJson);

}
