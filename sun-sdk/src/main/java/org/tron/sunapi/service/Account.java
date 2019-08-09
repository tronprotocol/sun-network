package org.tron.sunapi.service;

import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.TransactionListExtention;
import org.tron.protos.Protocol;
import org.tron.sunapi.SunNetworkResponse;

public interface Account {
  SunNetworkResponse<String> getAddress();

  SunNetworkResponse<Long>  getBalance();

  SunNetworkResponse<Protocol.Account> getAccount(String address);

  SunNetworkResponse<Protocol.Account> getAccountById( String accountId);

  SunNetworkResponse<Integer> updateAccount(String accountName);

  SunNetworkResponse<Integer> setAccountId(String accountId);

  SunNetworkResponse<Integer> createAccount(String address);

  SunNetworkResponse<TransactionListExtention> getTransactionsFromThis(String address, int offset, int limit);

  SunNetworkResponse<TransactionListExtention> getTransactionsToThis(String address, int offset, int limit);

  SunNetworkResponse<AddressPrKeyPairMessage> generateAddress();
}
