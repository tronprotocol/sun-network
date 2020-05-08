package org.tron.sunapi.service;

import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.DelegatedResourceList;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.FreezeBalanceRequest;
import org.tron.sunapi.response.TransactionResponse;

public interface Resource {
  SunNetworkResponse<AccountResourceMessage> getAccountResource(String address);

  SunNetworkResponse<AccountNetMessage> getAccountNet(String address);

  SunNetworkResponse<DelegatedResourceList> getDelegatedResource(String fromAddress, String toAddress);

  SunNetworkResponse<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(String address);

  SunNetworkResponse<Integer>  freezeBalance(FreezeBalanceRequest request);

  SunNetworkResponse<TransactionResponse> unfreezeBalance(int resourceCode, String receiverAddress);

  SunNetworkResponse<TransactionResponse> unfreezeAsset();
}
