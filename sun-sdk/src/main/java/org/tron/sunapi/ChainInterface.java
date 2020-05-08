package org.tron.sunapi;

import java.util.HashMap;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockExtention;
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
import org.tron.protos.Protocol.ChainParameters;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Proposal;
import org.tron.protos.Protocol.SideChainParameters;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.sunapi.request.AssertIssueRequest;
import org.tron.sunapi.request.DeployContractRequest;
import org.tron.sunapi.request.ExchangeCreateRequest;
import org.tron.sunapi.request.ExchangeTransactionRequest;
import org.tron.sunapi.request.TriggerConstantContractRequest;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.response.TransactionResponse;

public interface ChainInterface {

  //SmartContract

  SunNetworkResponse<TransactionResponse> deployContract(DeployContractRequest request);

  SunNetworkResponse<TransactionResponse> triggerContract(TriggerContractRequest request);

  SunNetworkResponse<TransactionResponse> triggerConstantContract(
      TriggerConstantContractRequest request);

  SunNetworkResponse<TransactionResponse> updateSetting(String address,
      long consumeUserResourcePercent);

  SunNetworkResponse<TransactionResponse> updateEnergyLimit(String address, long originEnergyLimit);

  SunNetworkResponse<SmartContract> getContract(String address);


  //account
  SunNetworkResponse<byte[]> getAddress();

  SunNetworkResponse<Long> getBalance();

  SunNetworkResponse<Account> getAccount(String address);

  SunNetworkResponse<Account> getAccountById(String accountId);

  SunNetworkResponse<TransactionResponse> updateAccount(String accountName);

  SunNetworkResponse<TransactionResponse> setAccountId(String accountId);

  SunNetworkResponse<TransactionResponse> createAccount(String address);

  SunNetworkResponse<TransactionListExtention> getTransactionsFromThis(String address, int offset,
      int limit);

  SunNetworkResponse<TransactionListExtention> getTransactionsToThis(String address, int offset,
      int limit);

  SunNetworkResponse<AddressPrKeyPairMessage> generateAddress();

  //AssetIssue
  SunNetworkResponse<TransactionResponse> updateAsset(String newLimitString,
      String newPublicLimitString,
      String description, String url);

  SunNetworkResponse<AssetIssueList> getAssetIssueByAccount(String address);

  SunNetworkResponse<AssetIssueContract> getAssetIssueByName(String assetName);

  SunNetworkResponse<AssetIssueList> getAssetIssueListByName(String assetName);

  SunNetworkResponse<AssetIssueContract> getAssetIssueById(String assetId);

  SunNetworkResponse<TransactionResponse> transferAsset(String toAddress, String assertName,
      long amount);

  SunNetworkResponse<TransactionResponse> participateAssetIssue(String toAddress, String assertName,
      long amount);

  SunNetworkResponse<TransactionResponse> assetIssue(AssertIssueRequest request);

  SunNetworkResponse<AssetIssueList> getAssetIssueList();

  SunNetworkResponse<AssetIssueList> getAssetIssueList(long offset, long limit);


  //witness
  SunNetworkResponse<TransactionResponse> createWitness(String url);

  SunNetworkResponse<TransactionResponse> updateWitness(String url);

  SunNetworkResponse<WitnessList> listWitnesses();

  SunNetworkResponse<TransactionResponse> voteWitness(HashMap<String, String> witness);

  SunNetworkResponse<TransactionResponse> withdrawBalance();

  SunNetworkResponse<ProposalList> getProposalsListPaginated(long offset, long limit);

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

  SunNetworkResponse<ExchangeList> getExchangesListPaginated(long offset, long limit);

  //System
  SunNetworkResponse<NodeList> listNodes();

  SunNetworkResponse<BlockExtention> getBlock(long blockNum);

  SunNetworkResponse<Long> getTransactionCountByBlockNum(long blockNum);

  SunNetworkResponse<String> getNextMaintenanceTime();

  SunNetworkResponse<NumberMessage> getTotalTransaction();

  SunNetworkResponse<Transaction> getTransactionById(String txid);

  SunNetworkResponse<TransactionInfo> getTransactionInfoById(String trxId);

  SunNetworkResponse<Block> getBlockById(String blockID);

  SunNetworkResponse<BlockListExtention> getBlockByLimitNext(long start, long end);

  SunNetworkResponse<ChainParameters> getChainParameters();

  SunNetworkResponse<SideChainParameters> getSideChainParameters();

  SunNetworkResponse<Boolean> checkTrxResult(String txId);

  SunNetworkResponse<BlockListExtention> getBlockByLatestNum(long num);

  //resource
  SunNetworkResponse<AccountResourceMessage> getAccountResource(String address);

  SunNetworkResponse<AccountNetMessage> getAccountNet(String address);

  SunNetworkResponse<DelegatedResourceList> getDelegatedResource(String fromAddress,
      String toAddress);

  SunNetworkResponse<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(
      String address);

  SunNetworkResponse<TransactionResponse> freezeBalance(long frozen_balance, long frozen_duration,
      int resourceCode, String receiverAddress);

  SunNetworkResponse<TransactionResponse> fundInject(long amount);

  SunNetworkResponse<TransactionResponse> unfreezeBalance(int resourceCode, String receiverAddress);

  SunNetworkResponse<TransactionResponse> unfreezeAsset();

  //multiSign
  SunNetworkResponse<TransactionSignWeight> getTransactionSignWeight(String transactionStr);

  SunNetworkResponse<TransactionApprovedList> getTransactionApprovedList(String transactionStr);

  SunNetworkResponse<Transaction> addTransactionSign(String transactionStr);

  SunNetworkResponse<TransactionResponse> updateAccountPermission(String address,
      String permissionJson);

  //core
  SunNetworkResponse<TransactionResponse> sendCoin(String toAddress, long amount);

  SunNetworkResponse<TransactionResponse> broadcastTransaction(String transactionStr);

  //offline functions
  AddressPrKeyPairMessage generateAddressOffline();

}
