package org.tron.sunapi;

import java.util.HashMap;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockListExtention;
import org.tron.api.GrpcAPI.DelegatedResourceList;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionListExtention;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Proposal;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.sunapi.request.AssertIssueRequest;
import org.tron.sunapi.request.DeployContractRequest;
import org.tron.sunapi.request.ExchangeCreateRequest;
import org.tron.sunapi.request.ExchangeTransactionRequest;
import org.tron.sunapi.request.FreezeBalanceRequest;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.request.UpdateAssetRequest;
import org.tron.sunapi.response.DeployContractResponse;
import org.tron.sunapi.response.TransactionResponse;

public interface ChainInterface {

  //SmartContract
  SunNetworkResponse<DeployContractResponse> deployContract(DeployContractRequest request);

  SunNetworkResponse<TransactionResponse> triggerContract(TriggerContractRequest request);

  SunNetworkResponse<TransactionResponse> updateSetting(String address,
      long consumeUserResourcePercent);

  SunNetworkResponse<TransactionResponse> updateEnergyLimit(String address, long originEnergyLimit);

  SunNetworkResponse<SmartContract> getContract(String address);

  //account
  SunNetworkResponse<String> getAddress();

  SunNetworkResponse<Long> getBalance();

  SunNetworkResponse<Account> getAccount(String address);

  SunNetworkResponse<Account> getAccountById(String accountId);

  SunNetworkResponse<Integer> updateAccount(String accountName);

  SunNetworkResponse<Integer> setAccountId(String accountId);

  SunNetworkResponse<Integer> createAccount(String address);

  SunNetworkResponse<TransactionListExtention> getTransactionsFromThis(String address, int offset,
      int limit);

  SunNetworkResponse<TransactionListExtention> getTransactionsToThis(String address, int offset,
      int limit);

  SunNetworkResponse<AddressPrKeyPairMessage> generateAddress();

  //AssetIssue
  SunNetworkResponse<Integer> updateAsset(UpdateAssetRequest request);

  SunNetworkResponse<AssetIssueList> getAssetIssueByAccount(String address);

  SunNetworkResponse<AssetIssueContract> getAssetIssueByName(String assetName);

  SunNetworkResponse<AssetIssueList> getAssetIssueListByName(String assetName);

  SunNetworkResponse<AssetIssueContract> getAssetIssueById(String assetId);

  SunNetworkResponse<Integer> transferAsset(String toAddress, String assertName, long amount);

<<<<<<

  <HEAD
  SunNetworkResponse<Integer> participateAssetIssue(String toAddress, String assertName,
      String amountStr);
=======

  SunNetworkResponse<Integer> participateAssetIssue(String toAddress, String assertName,
      long amount);
>>>>>>>8afb481fb2c8d89751ee3fe01f63f181cc71d3a0

  SunNetworkResponse<Integer> assetIssue(AssertIssueRequest request);

  SunNetworkResponse<AssetIssueList> getAssetIssueList();

  SunNetworkResponse<AssetIssueList> getAssetIssueList(int offset, int limit);


  //witness
  SunNetworkResponse<Integer> createWitness(String url);

  SunNetworkResponse<Integer> updateWitness(String url);

  SunNetworkResponse<WitnessList> listWitnesses();

  SunNetworkResponse<Integer> voteWitness(HashMap<String, String> witness);

  SunNetworkResponse<TransactionResponse> withdrawBalance();

  SunNetworkResponse<ProposalList> getProposalsListPaginated(int offset, int limit);

  SunNetworkResponse<TransactionResponse> approveProposal(long id, boolean is_add_approval);

  SunNetworkResponse<TransactionResponse> deleteProposal(long id);

  SunNetworkResponse<Proposal> getProposal(String id);

  //Exchanges
  SunNetworkResponse<TransactionResponse> exchangeCreate(ExchangeCreateRequest request);

  SunNetworkResponse<TransactionResponse> exchangeInject(long exchangeId, String tokenIdStr,
      long quant);

  SunNetworkResponse<TransactionResponse> exchangeWithdraw(long exchangeId, String tokenIdStr,
      long quant);

  SunNetworkResponse<TransactionResponse> exchangeTransaction(ExchangeTransactionRequest request);

  SunNetworkResponse<ExchangeList> listExchanges();

  SunNetworkResponse<Exchange> getExchange(String id);

  SunNetworkResponse<ExchangeList> getExchangesListPaginated(int offset, int limit);

  //System
  SunNetworkResponse<NodeList> listNodes();

  SunNetworkResponse<Block> getBlock(long blockNum);

  SunNetworkResponse<Long> getTransactionCountByBlockNum(long blockNum);

  SunNetworkResponse<String> getNextMaintenanceTime();

  SunNetworkResponse<NumberMessage> getTotalTransaction();

  SunNetworkResponse<Transaction> getTransactionById(String txid);

  SunNetworkResponse<TransactionInfo> getTransactionInfoById(String trxId);

  SunNetworkResponse<Block> getBlockById(String blockID);

  SunNetworkResponse<BlockListExtention> getBlockByLimitNext(long start, long end);

  //resource
  SunNetworkResponse<AccountResourceMessage> getAccountResource(String address);

  SunNetworkResponse<AccountNetMessage> getAccountNet(String address);

  SunNetworkResponse<DelegatedResourceList> getDelegatedResource(String fromAddress,
      String toAddress);

  SunNetworkResponse<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(
      String address);

  SunNetworkResponse<Integer> freezeBalance(FreezeBalanceRequest request);

  SunNetworkResponse<TransactionResponse> unfreezeBalance(int resourceCode, String receiverAddress);

  SunNetworkResponse<TransactionResponse> unfreezeAsset();

  //multiSign
  SunNetworkResponse<TransactionSignWeight> getTransactionSignWeight(String transactionStr);

  SunNetworkResponse<TransactionApprovedList> getTransactionApprovedList(String transactionStr);

  SunNetworkResponse<Transaction> addTransactionSign(String transactionStr);

  SunNetworkResponse<TransactionResponse> updateAccountPermission(String address,
      String permissionJson);

  //core
  SunNetworkResponse<Integer> sendCoin(String toAddress, long amount);

  SunNetworkResponse<TransactionResponse> broadcastTransaction(String transactionStr);

}
