package org.tron.sunapi.service;

import java.util.HashMap;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.protos.Protocol.Proposal;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.response.TransactionResponse;

public interface Witness {
  SunNetworkResponse<Integer> createWitness(String url);

  SunNetworkResponse<Integer> updateWitness(String url);

  SunNetworkResponse<WitnessList> listWitnesses();

  SunNetworkResponse<Integer> voteWitness(HashMap<String, String> witness);

  SunNetworkResponse<TransactionResponse>  withdrawBalance();

  SunNetworkResponse<ProposalList> getProposalsListPaginated(int offset, int limit);

  SunNetworkResponse<TransactionResponse> approveProposal(long id, boolean is_add_approval);

  SunNetworkResponse<TransactionResponse> deleteProposal(long id);

  SunNetworkResponse<Proposal> getProposal(String id);
}
