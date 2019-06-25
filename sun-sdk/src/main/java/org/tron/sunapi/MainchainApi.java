package org.tron.sunapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.sunapi.request.DeployContractRequest;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.response.DeployContractResponse;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunapi.response.TriggerContractResponse;

public class MainchainApi extends Chain{

  public int test = 0;

  public SunNetworkResponse<Integer> init(String config, String priKey) {
    return super.init(config, priKey, true);
  }

  public SunNetworkResponse<DeployContractResponse> deployContract(DeployContractRequest request) {
    return super.deployContract(request);
  }

  public SunNetworkResponse<TriggerContractResponse> triggerContract(TriggerContractRequest request) {
    return super.triggerContract(request);
  }

  public SunNetworkResponse<TransactionResponse> createProposal(HashMap<Long, Long> parametersMap) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if(parametersMap == null || parametersMap.isEmpty()) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    try {
      TransactionResponse result = super.getServerApi().createProposal(parametersMap);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    }

    return resp;
  }

  public SunNetworkResponse<ProposalList> listProposals() {
    SunNetworkResponse<ProposalList> resp = new SunNetworkResponse<>();

    Optional<ProposalList> result = super.getServerApi().listProposals();
    if (result.isPresent()) {
      ProposalList proposalList = result.get();
      resp.success(proposalList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

}
