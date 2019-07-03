package org.tron.sunapi.service.impl;

import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.TransactionListExtention;
import org.tron.protos.Protocol;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.service.Account;

public class AccountImpl implements Account {

  @Override
  public SunNetworkResponse<String> getAddress() {
    return null;
  }

  @Override
  public SunNetworkResponse<Long> getBalance() {
    return null;
  }

  @Override
  public SunNetworkResponse<Protocol.Account> getAccount(String address) {
    return null;
  }

  @Override
  public SunNetworkResponse<Protocol.Account> getAccountById(String accountId) {
    return null;
  }

  @Override
  public SunNetworkResponse<Integer> updateAccount(String accountName) {
    return null;
  }

  @Override
  public SunNetworkResponse<Integer> setAccountId(String accountId) {
    return null;
  }

  @Override
  public SunNetworkResponse<Integer> createAccount(String address) {
    return null;
  }

  @Override
  public SunNetworkResponse<TransactionListExtention> getTransactionsFromThis(String address,
      int offset, int limit) {
    return null;
  }

  @Override
  public SunNetworkResponse<TransactionListExtention> getTransactionsToThis(String address,
      int offset, int limit) {
    return null;
  }

  @Override
  public SunNetworkResponse<AddressPrKeyPairMessage> generateAddress() {
    return null;
  }
}
