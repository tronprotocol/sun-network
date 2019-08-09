package org.tron.sunapi.service;

import org.tron.protos.Protocol;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.DeployContractRequest;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.response.DeployContractResponse;
import org.tron.sunapi.response.TransactionResponse;

public interface SmartContract {
  SunNetworkResponse<DeployContractResponse> deployContract(DeployContractRequest request);

  SunNetworkResponse<TransactionResponse> triggerContract(TriggerContractRequest request);

  SunNetworkResponse<TransactionResponse> updateSetting(String address, long consumeUserResourcePercent);

  SunNetworkResponse<TransactionResponse> updateEnergyLimit(String address, long originEnergyLimit);

  SunNetworkResponse<Protocol.SmartContract> getContract(String address);
}
