package org.tron.sunapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.sunapi.response.TransactionResponse;

public class SidechainApi extends Chain{

  public SunNetworkResponse<Integer> init(String config, String priKey) {
    return super.init(config, priKey, false);
  }

  public SunNetworkResponse<TransactionResponse> createProposal(HashMap<Long, String> parametersMap) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if(parametersMap == null || parametersMap.isEmpty()) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    try {
      TransactionResponse result = super.getServerApi().sideChainCreateProposal(parametersMap);
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

  public SunNetworkResponse<SideChainProposalList> listProposals() {
    SunNetworkResponse<SideChainProposalList> resp = new SunNetworkResponse<>();

    Optional<SideChainProposalList> result = super.getServerApi().sideChainListProposals();
    if (result.isPresent()) {
      SideChainProposalList proposalList = result.get();
      resp.success(proposalList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }



}
