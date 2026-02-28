package org.tron.sunapi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunserver.IMultiTransactionSign;

public class MainchainApi extends Chain {

  /**
   * @param config the configuration path
   * @param priKey the private key of user
   * @return the result of init main chain
   * @author sun-network
   */
  public SunNetworkResponse<Integer> init(IServerConfig config, String priKey,
      IMultiTransactionSign multiTransactionSign) {
    return super.init(config, priKey, true, multiTransactionSign);
  }

  /**
   * @return the result of init main chain
   * @author sun-network
   */
  public SunNetworkResponse<Integer> init(IServerConfig config,
      IMultiTransactionSign multiTransactionSign) {
    return super.init(config, true, multiTransactionSign);
  }

  /**
   * @param parametersMap the id and content of proposal
   * @return the result of creating proposal
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> createProposal(HashMap<Long, Long> parametersMap) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (parametersMap == null || parametersMap.isEmpty()) {
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
