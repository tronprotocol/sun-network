package org.tron.sunapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunserver.IMultiTransactionSign;

public class SidechainApi extends Chain {

  /**
   * @param config the configuration path
   * @param priKey the private key of user
   * @return the result of init side chain
   * @author sun-network
   */
  public SunNetworkResponse<Integer> init(IServerConfig config, String priKey,
      IMultiTransactionSign multiTransactionSign) {
    return super.init(config, priKey, false, multiTransactionSign);
  }

  /**
   * @param config the configuration path
   * @return the result of init side chain
   * @author sun-network
   */
  public SunNetworkResponse<Integer> init(IServerConfig config,
      IMultiTransactionSign multiTransactionSign) {
    return super.init(config, false, multiTransactionSign);
  }

  /**
   * @param parametersMap the id and content of proposal
   * @return the result of creating proposal
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> createProposal(
      HashMap<Long, String> parametersMap) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (parametersMap == null || parametersMap.isEmpty()) {
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
    } catch (IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch (CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    }

    return resp;
  }

  /**
   * @return the proposal list
   * @author sun-network
   */
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


  public SunNetworkResponse<String> sideGetMappingAddress(byte[] sideGateway,
      String mainContractAddress) {
    SunNetworkResponse<String> resp = new SunNetworkResponse<>();

    if (sideGateway == null || StringUtils.isEmpty(mainContractAddress)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    try {
      String result = super.getServerApi().sideGetMappingAddress(sideGateway, mainContractAddress);
      resp.setData(result);

    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }


}
