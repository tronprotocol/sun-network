package org.tron.sunapi;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Hex;
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
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.protos.Contract;
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
import org.tron.sunserver.WalletApi;

@Slf4j
public class Chain implements  ChainInterface{
  @Getter
  private WalletApi serverApi;

  public SunNetworkResponse<Integer> init(String config, String priKey, boolean isMainChain) {
    SunNetworkResponse<Integer> ret = new SunNetworkResponse<>();
    byte[] temp =  org.tron.keystore.StringUtils.hexs2Bytes(priKey.getBytes());

    if (!WalletApi.priKeyValid(temp)) {
      ret.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }
    serverApi = new WalletApi(config, temp, isMainChain);

    return ret.success(0);
  }

  public SunNetworkResponse<DeployContractResponse> deployContract(DeployContractRequest request) {
    SunNetworkResponse<DeployContractResponse> resp = new SunNetworkResponse();

    String contractName = request.getContractName();
    String abiStr = request.getAbiStr();
    String codeStr = request.getCodeStr();
    String constructorStr = request.getConstructorStr();
    String argsStr = request.getArgsStr();
    boolean isHex = request.isHex();
    long feeLimit = request.getFeeLimit();
    long consumeUserResourcePercent = request.getConsumeUserResourcePercent();
    long originEnergyLimit = request.getOriginEnergyLimit();
    if (consumeUserResourcePercent > 100 || consumeUserResourcePercent < 0) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return resp;
    }
    if (originEnergyLimit <= 0) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return resp;
    }

    try {
      if (StringUtils.isEmpty(constructorStr)) {
        if (isHex) {
          codeStr += argsStr;
        } else {
          codeStr += Hex.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
        }
      }

      long trx = request.getTrx();
      long tokenValue = request.getTokenValue();
      String tokenId = request.getTokenId();
      String libraryAddressPair = request.getLibraryAddressPair();
      String compilerVersion = request.getCompilerVersion();

      String contractAddress = serverApi.deployContract(contractName, abiStr, codeStr, feeLimit, trx,
          consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId, libraryAddressPair,
          compilerVersion);
      if(StringUtils.isEmpty(contractAddress)) {
        resp.failed(ErrorCodeEnum.ERROR_UNKNOWN);
      } else {
        resp.success(new DeployContractResponse(contractAddress));
      }
    } catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    } catch(EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }


  public SunNetworkResponse<TransactionResponse> triggerContract(TriggerContractRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse();

    String contractAddrStr = request.getContractAddrStr();
    String methodStr = request.getMethodStr();
    String argsStr = request.getArgsStr();
    boolean isHex = request.isHex();
    long feeLimit = request.getFeeLimit();
    long callValue = request.getCallValue();
    long tokenCallValue = request.getTokenCallValue();
    String tokenId = request.getTokenId();
    if (argsStr.equalsIgnoreCase("#")) {
      argsStr = "";
    }
    if (tokenId.equalsIgnoreCase("#")) {
      tokenId = "";
    }
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, isHex));
      byte[] contractAddress = WalletApi.decodeFromBase58Check(contractAddrStr);

      TransactionResponse result = serverApi
          .triggerContract(contractAddress, callValue, input, feeLimit, tokenCallValue, tokenId);
      resp.setData(result);
      if (StringUtils.isEmpty(result.getTrxId())) {
        resp.failed(ErrorCodeEnum.FAILED);
      } else {
        resp.success(result);
      }
    } catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    } catch(EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }


  public SunNetworkResponse<String>  getAddress() {
    SunNetworkResponse<String> resp = new SunNetworkResponse<>();

    String address = serverApi.encode58Check(serverApi.getAddress());
    resp.success(address);

    return resp;
  }

  public SunNetworkResponse<Long>  getBalance() {
    SunNetworkResponse<Long> resp = new SunNetworkResponse<>();

    Account account = serverApi.queryAccount();
    if (account == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ACCOUNT);
    } else {
      long balance = account.getBalance();
      resp.success(balance);
    }

    return resp;
  }

  public SunNetworkResponse<Account> getAccount(String address){
    SunNetworkResponse<Account> resp = new SunNetworkResponse<>();
    byte[] addressBytes = serverApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return null;
    }

    Account account = serverApi.queryAccount(addressBytes);
    resp.success(account);
    return resp;
  }

  public SunNetworkResponse<Account> getAccountById( String accountId) {
    SunNetworkResponse<Account> resp = new SunNetworkResponse<>();

    Account account = serverApi.queryAccountById(accountId);
    if (account == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    } else {
      resp.success(account);
    }

    return  resp;
  }

  public SunNetworkResponse<Integer> updateAccount(String accountName) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(accountName)) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
      return resp;
    }
    byte[] accountNameBytes = ByteArray.fromString(accountName);

    try {
      boolean ret = serverApi.updateAccount(accountNameBytes);
      if (ret) {
        resp.success(0);
      } else {
        resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
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

  public SunNetworkResponse<Integer> setAccountId(String accountId){
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(accountId)) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
      return resp;
    }
    byte[] accountIdBytes = ByteArray.fromString(accountId);

    try {
      boolean ret = serverApi.setAccountId(accountIdBytes);
      if (ret) {
        resp.success(0);
      } else {
        resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      }
    }catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    }

    return resp;
  }

  public SunNetworkResponse<Integer> updateAsset(UpdateAssetRequest request) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    String newLimitString = request.getNewLimitString();
    String newPublicLimitString = request.getNewPublicLimitString();
    String description = request.getDescription();
    String url = request.getUrl();

    byte[] descriptionBytes = ByteArray.fromString(description);
    byte[] urlBytes = ByteArray.fromString(url);
    long newLimit = new Long(newLimitString);
    long newPublicLimit = new Long(newPublicLimitString);

    try {
      boolean ret = serverApi.updateAsset(descriptionBytes, urlBytes, newLimit, newPublicLimit);
      if (ret) {
        resp.success(0);
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

  public SunNetworkResponse<AssetIssueList> getAssetIssueByAccount(String address) {
    SunNetworkResponse<AssetIssueList> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return resp;
    }

    Optional<AssetIssueList> result = serverApi.getAssetIssueByAccount(addressBytes);
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      resp.success(assetIssueList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<AccountNetMessage> getAccountNet(String address) {
    SunNetworkResponse<AccountNetMessage> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    AccountNetMessage result = serverApi.getAccountNet(addressBytes);
    if (result == null) {
      resp.failed(ErrorCodeEnum.FAILED);
    } else {
      resp.success(result);
    }

    return resp;
  }

  public SunNetworkResponse<AccountResourceMessage> getAccountResource(String address) {
    SunNetworkResponse<AccountResourceMessage> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    AccountResourceMessage result = serverApi.getAccountResource(addressBytes);
    if (result == null) {
      resp.failed(ErrorCodeEnum.FAILED);
    } else {
      resp.success(result);
    }

    return resp;
  }

  // In 3.2 version, this function will return null if there are two or more asset with the same token name,
  // so please use getAssetIssueById or getAssetIssueListByName. This function just remains for compatibility.
  public SunNetworkResponse<AssetIssueContract> getAssetIssueByName(String assetName) {
    SunNetworkResponse<AssetIssueContract> resp = new SunNetworkResponse<>();

    AssetIssueContract assetIssueContract = serverApi.getAssetIssueByName(assetName);
    if (assetIssueContract == null) {
      resp.failed(ErrorCodeEnum.FAILED);
    } else {
      resp.success(assetIssueContract);
    }

    return resp;
  }

  public SunNetworkResponse<AssetIssueList> getAssetIssueListByName(String assetName) {
    SunNetworkResponse<AssetIssueList> resp = new SunNetworkResponse<>();

    Optional<AssetIssueList> result = serverApi.getAssetIssueListByName(assetName);
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      resp.success(assetIssueList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<AssetIssueContract> getAssetIssueById(String assetId) {
    SunNetworkResponse<AssetIssueContract> resp = new SunNetworkResponse<>();

    AssetIssueContract assetIssueContract = serverApi.getAssetIssueById(assetId);
    if (assetIssueContract != null) {
      resp.success(assetIssueContract);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<Integer> sendCoin(String toAddress, String amountStr)  {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    long amount = new Long(amountStr);
    byte[] to = WalletApi.decodeFromBase58Check(toAddress);
    if (to == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return resp;
    }

    try {
      boolean result = serverApi.sendCoin(to, amount);
      if (result) {
        resp.success(0);
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


  public SunNetworkResponse<Integer> transferAsset(String toAddress, String assertName, String amountStr) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    byte[] to = WalletApi.decodeFromBase58Check(toAddress);
    if (to == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    long amount = new Long(amountStr);
    try {
      boolean result = serverApi.transferAsset(to, assertName.getBytes(), amount);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer> participateAssetIssue(String toAddress, String assertName, String amountStr) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    byte[] to = WalletApi.decodeFromBase58Check(toAddress);
    if (to == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    long amount = Long.parseLong(amountStr);
    try {
      boolean result = serverApi.participateAssetIssue(to, assertName.getBytes(), amount);
      if (result) {
        resp.success(0);
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


  public boolean assetIssueProc(String name, long totalSupply, int trxNum, int icoNum, int precision,
      long startTime, long endTime, int voteScore, String description, String url,
      long freeNetLimit, long publicFreeNetLimit, HashMap<String, String> frozenSupply)
      throws CipherException, IOException, CancelException {


    Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(serverApi.getAddress()));
    builder.setName(ByteString.copyFrom(name.getBytes()));

    if (totalSupply <= 0) {
      return false;
    }
    builder.setTotalSupply(totalSupply);

    if (trxNum <= 0) {
      return false;
    }
    builder.setTrxNum(trxNum);

    if (icoNum <= 0) {
      return false;
    }
    builder.setNum(icoNum);

    if (precision < 0) {
      return false;
    }
    builder.setPrecision(precision);

    long now = System.currentTimeMillis();
    if (startTime <= now) {
      return false;
    }
    if (endTime <= startTime) {
      return false;
    }

    if (freeNetLimit < 0) {
      return false;
    }
    if (publicFreeNetLimit < 0) {
      return false;
    }

    builder.setStartTime(startTime);
    builder.setEndTime(endTime);
    builder.setVoteScore(voteScore);
    builder.setDescription(ByteString.copyFrom(description.getBytes()));
    builder.setUrl(ByteString.copyFrom(url.getBytes()));
    builder.setFreeAssetNetLimit(freeNetLimit);
    builder.setPublicFreeAssetNetLimit(publicFreeNetLimit);

    for (String daysStr : frozenSupply.keySet()) {
      String amountStr = frozenSupply.get(daysStr);
      long amount = Long.parseLong(amountStr);
      long days = Long.parseLong(daysStr);
      Contract.AssetIssueContract.FrozenSupply.Builder frozenSupplyBuilder
          = Contract.AssetIssueContract.FrozenSupply.newBuilder();
      frozenSupplyBuilder.setFrozenAmount(amount);
      frozenSupplyBuilder.setFrozenDays(days);
      builder.addFrozenSupply(frozenSupplyBuilder.build());
    }

    return serverApi.createAssetIssue(builder.build());
  }

  public SunNetworkResponse<Integer> assetIssue(AssertIssueRequest request) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    String name = request.getName();
    String totalSupplyStr = request.getTotalSupplyStr();
    String trxNumStr = request.getTrxNumStr();
    String icoNumStr = request.getIcoNumStr();
    String precisionStr = request.getPrecisionStr();
    String startYyyyMmDd = request.getStartYyyyMmDd();
    String endYyyyMmDd = request.getEndYyyyMmDd();
    String description = request.getDescription();
    String url = request.getUrl();
    String freeNetLimitPerAccount = request.getFreeNetLimitPerAccount();
    String publicFreeNetLimitString = request.getPublicFreeNetLimitString();
    HashMap<String, String> frozenSupply = request.getFrozenSupply();

    long totalSupply = new Long(totalSupplyStr);
    int trxNum = new Integer(trxNumStr);
    int icoNum = new Integer(icoNumStr);
    int precision = new Integer(precisionStr);
    Date startDate = Utils.strToDateLong(startYyyyMmDd);
    Date endDate = Utils.strToDateLong(endYyyyMmDd);
    long startTime = startDate.getTime();
    long endTime = endDate.getTime();
    long freeAssetNetLimit = new Long(freeNetLimitPerAccount);
    long publicFreeNetLimit = new Long(publicFreeNetLimitString);

    try {

      boolean result = assetIssueProc(name, totalSupply, trxNum, icoNum, precision, startTime,
          endTime,
          0, description, url, freeAssetNetLimit, publicFreeNetLimit, frozenSupply);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer> createAccount(String address) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    try {
      boolean result = serverApi.createAccount(addressBytes);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer> createWitness(String url) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    try {
      boolean result = serverApi.createWitness(url.getBytes());
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer> updateWitness(String url) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    try {
      boolean result = serverApi.updateWitness(url.getBytes());
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<WitnessList> listWitnesses() {
    SunNetworkResponse<WitnessList> resp = new SunNetworkResponse<>();

    Optional<WitnessList> result = serverApi.listWitnesses();
    if (result.isPresent()) {
      WitnessList witnessList = result.get();
      resp.success(witnessList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<AssetIssueList> getAssetIssueList() {
    SunNetworkResponse<AssetIssueList> resp = new SunNetworkResponse<>();

    Optional<AssetIssueList> result = serverApi.getAssetIssueList();
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      resp.success(assetIssueList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<AssetIssueList> getAssetIssueList(int offset, int limit) {
    SunNetworkResponse<AssetIssueList> resp = new SunNetworkResponse<>();

    Optional<AssetIssueList> result = serverApi.getAssetIssueList(offset, limit);
    if (result.isPresent()) {
      AssetIssueList assetIssueList = result.get();
      resp.success(assetIssueList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<ProposalList> getProposalsListPaginated(int offset, int limit) {
    SunNetworkResponse<ProposalList> resp = new SunNetworkResponse<>();

    Optional<ProposalList> result = serverApi.getProposalListPaginated(offset, limit);
    if (result.isPresent()) {
      ProposalList proposalList = result.get();
      resp.success(proposalList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<ExchangeList> getExchangesListPaginated(int offset, int limit) {
    SunNetworkResponse<ExchangeList> resp = new SunNetworkResponse<>();

    Optional<ExchangeList> result = serverApi.getExchangeListPaginated(offset, limit);
    if (result.isPresent()) {
      ExchangeList exchangeList = result.get();
      resp.success(exchangeList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<NodeList> listNodes() {
    SunNetworkResponse<NodeList> resp = new SunNetworkResponse<>();

    Optional<NodeList> result = serverApi.listNodes();
    if (result.isPresent()) {
      NodeList nodeList = result.get();
      resp.success(nodeList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<Block> getBlock(long blockNum) {
    SunNetworkResponse<Block> resp = new SunNetworkResponse<>();

    Block block = serverApi.getBlock(blockNum);
    resp.success(block);

    return resp;
  }

  public SunNetworkResponse<Long> getTransactionCountByBlockNum(long blockNum) {
    SunNetworkResponse<Long> resp = new SunNetworkResponse<>();

    long count = serverApi.getTransactionCountByBlockNum(blockNum);
    resp.success(count);

    return resp;
  }

  public SunNetworkResponse<Integer> voteWitness(HashMap<String, String> witness)  {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    if(witness == null || witness.isEmpty()) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    try {
      boolean result = serverApi.voteWitness(witness);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer>  freezeBalance(FreezeBalanceRequest request) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    long frozen_balance = request.getFrozen_balance();
    long frozen_duration = request.getFrozen_duration();
    int resourceCode = request.getResourceCode();
    String receiverAddress = request.getReceiverAddress();

    try {
      boolean result = serverApi.freezeBalance(frozen_balance, frozen_duration, resourceCode,
          receiverAddress);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer> buyStorage(long quantity) {
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    try {
      boolean result = serverApi.buyStorage(quantity);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<Integer> buyStorageBytes(long bytes){
    SunNetworkResponse<Integer> resp = new SunNetworkResponse<>();

    try {
      boolean result = serverApi.buyStorageBytes(bytes);
      if (result) {
        resp.success(0);
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

  public SunNetworkResponse<TransactionResponse> sellStorage(long storageBytes) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.sellStorage(storageBytes);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    }  catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    }

    return resp;
  }


  public SunNetworkResponse<TransactionResponse> unfreezeBalance(int resourceCode, String receiverAddress) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.unfreezeBalance(resourceCode, receiverAddress);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    }  catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    }

    return resp;
  }


  public SunNetworkResponse<TransactionResponse> unfreezeAsset() {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.unfreezeAsset();
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


  public SunNetworkResponse<TransactionResponse> approveProposal(long id, boolean is_add_approval)  {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.approveProposal(id, is_add_approval);
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

  public SunNetworkResponse<TransactionResponse> deleteProposal(long id) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.deleteProposal(id);
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

  public SunNetworkResponse<Proposal> getProposal(String id) {
    SunNetworkResponse<Proposal> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(id)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<Proposal> result = serverApi.getProposal(id);
    if (result.isPresent()) {
      Proposal proposal = result.get();
      resp.success(proposal);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }


  public SunNetworkResponse<DelegatedResourceList> getDelegatedResource(String fromAddress, String toAddress) {
    SunNetworkResponse<DelegatedResourceList> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(fromAddress) || StringUtils.isEmpty(toAddress) ) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<DelegatedResourceList> result = serverApi.getDelegatedResource(fromAddress, toAddress);
    if (result.isPresent()) {
      DelegatedResourceList delegatedResourceList = result.get();
      resp.success(delegatedResourceList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(String address) {
    SunNetworkResponse<DelegatedResourceAccountIndex> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(address))  {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<DelegatedResourceAccountIndex> result = serverApi.getDelegatedResourceAccountIndex(address);
    if (result.isPresent()) {
      DelegatedResourceAccountIndex delegatedResourceAccountIndex = result.get();
      resp.success(delegatedResourceAccountIndex);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }


  public SunNetworkResponse<TransactionResponse> exchangeCreate(ExchangeCreateRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();
    if (request == null || StringUtils.isEmpty(request.getFirstTokenId()) || StringUtils.isEmpty(request.getSecondTokenId())) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] firstTokenId     = request.getFirstTokenId().getBytes();
    long firstTokenBalance  = request.getFirstTokenBalance();
    byte[] secondTokenId    = request.getSecondTokenId().getBytes();
    long secondTokenBalance = request.getSecondTokenBalance();

    try {
      TransactionResponse result = serverApi.exchangeCreate(firstTokenId, firstTokenBalance,
          secondTokenId, secondTokenBalance);
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

  public SunNetworkResponse<TransactionResponse>  exchangeInject(long exchangeId, String tokenIdStr, long quant) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(tokenIdStr)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] tokenId = tokenIdStr.getBytes();
    try {
      TransactionResponse result = serverApi.exchangeInject(exchangeId, tokenId, quant);
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

  public SunNetworkResponse<TransactionResponse> exchangeWithdraw(long exchangeId, String tokenIdStr, long quant) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(tokenIdStr)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] tokenId = tokenIdStr.getBytes();
    try {
      TransactionResponse result = serverApi.exchangeWithdraw(exchangeId, tokenId, quant);
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

  public SunNetworkResponse<TransactionResponse> exchangeTransaction(ExchangeTransactionRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(request.getTokenId())) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    long exchangeId = request.getExchangeId();
    byte[] tokenId  = request.getTokenId().getBytes();
    long quant      = request.getQuant();
    long expected   = request.getExpected();

    try {
      TransactionResponse result = serverApi.exchangeTransaction(exchangeId, tokenId, quant, expected);
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

  public SunNetworkResponse<ExchangeList> listExchanges() {
    SunNetworkResponse<ExchangeList> resp = new SunNetworkResponse<>();

    Optional<ExchangeList> result = serverApi.listExchanges();
    if (result.isPresent()) {
      ExchangeList exchangeList = result.get();
      resp.success(exchangeList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<Exchange> getExchange(String id) {
    SunNetworkResponse<Exchange> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(id)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<Exchange> result = serverApi.getExchange(id);
    if (result.isPresent()) {
      Exchange exchange = result.get();
      resp.success(exchange);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse>  withdrawBalance()  {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.withdrawBalance();
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

  public SunNetworkResponse<NumberMessage> getTotalTransaction() {
    SunNetworkResponse<NumberMessage> resp = new SunNetworkResponse<>();

    NumberMessage totalTransition = serverApi.getTotalTransaction();

    return resp.success(totalTransition);
  }

  public SunNetworkResponse<String> getNextMaintenanceTime() {
    SunNetworkResponse<String> resp = new SunNetworkResponse<>();

    NumberMessage nextMaintenanceTime = serverApi.getNextMaintenanceTime();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = formatter.format(nextMaintenanceTime.getNum());

    return resp.success(date);
  }

  public SunNetworkResponse<Transaction> getTransactionById(String trxId) {
    SunNetworkResponse<Transaction> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(trxId)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<Transaction> result = serverApi.getTransactionById(trxId);
    if (result.isPresent()) {
      Transaction transaction = result.get();
      resp.success(transaction);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionInfo> getTransactionInfoById(String trxId) {
    SunNetworkResponse<TransactionInfo> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(trxId)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<TransactionInfo> result = serverApi.getTransactionInfoById(trxId);
    if (result.isPresent()) {
      TransactionInfo transactionInfo = result.get();
      resp.success(transactionInfo);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionListExtention> getTransactionsFromThis(String address, int offset, int limit) {
    SunNetworkResponse<TransactionListExtention> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<TransactionListExtention> result = serverApi.getTransactionsFromThis2(addressBytes, offset, limit);
    if (result.isPresent()) {
      TransactionListExtention transactionList = result.get();
      resp.success(transactionList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionListExtention> getTransactionsToThis(String address, int offset, int limit) {
    SunNetworkResponse<TransactionListExtention> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<TransactionListExtention> result = serverApi.getTransactionsToThis2(addressBytes, offset, limit);
    if (result.isPresent()) {
      TransactionListExtention transactionList = result.get();
      resp.success(transactionList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<Block> getBlockById(String blockID) {
    SunNetworkResponse<Block> resp = new SunNetworkResponse<>();

    Optional<Block> result = serverApi.getBlockById(blockID);
    if (result.isPresent()) {
      Block block = result.get();
      resp.success(block);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<BlockListExtention> getBlockByLimitNext(long start, long end) {
    SunNetworkResponse<BlockListExtention> resp = new SunNetworkResponse<>();

    Optional<BlockListExtention> result = serverApi.getBlockByLimitNext2(start, end);
    if (result.isPresent()) {
      BlockListExtention blockList = result.get();
      resp.success(blockList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<BlockListExtention> getBlockByLatestNum(long num) {
    SunNetworkResponse<BlockListExtention> resp = new SunNetworkResponse<>();

    Optional<BlockListExtention> result = serverApi.getBlockByLatestNum2(num);
    if (result.isPresent()) {
      BlockListExtention blockList = result.get();
      resp.success(blockList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> updateSetting(String address, long consumeUserResourcePercent) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] contractAddress = WalletApi.decodeFromBase58Check(address);
    if(contractAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    if (consumeUserResourcePercent > 100 || consumeUserResourcePercent < 0) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse result = serverApi.updateSetting(contractAddress, consumeUserResourcePercent);
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

  public SunNetworkResponse<TransactionResponse> updateEnergyLimit(String address, long originEnergyLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] contractAddress = WalletApi.decodeFromBase58Check(address);
    if(contractAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    if (originEnergyLimit < 0) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse result = serverApi.updateEnergyLimit(contractAddress, originEnergyLimit);
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

  public SunNetworkResponse<SmartContract> getContract(String address) {
    SunNetworkResponse<SmartContract> resp = new SunNetworkResponse<>();

    byte[] addressBytes = WalletApi.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    SmartContract contractDeployContract = serverApi.getContract(addressBytes);
    if (contractDeployContract != null) {
      resp.success(contractDeployContract);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }


  public SunNetworkResponse<AddressPrKeyPairMessage> generateAddress() {
    SunNetworkResponse<AddressPrKeyPairMessage> resp = new SunNetworkResponse<>();

    AddressPrKeyPairMessage result = serverApi.generateAddress();
    if (null != result) {
      resp.success(result);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> updateAccountPermission(String address, String permissionJson) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if(StringUtils.isEmpty(permissionJson)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] ownerAddress = WalletApi.decodeFromBase58Check(address);
    if (ownerAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try{
      TransactionResponse ret = serverApi.accountPermissionUpdate(ownerAddress, permissionJson);
      resp.setData(ret);
      if (ret.getResult()) {
        resp.success(ret);
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

  public SunNetworkResponse<TransactionSignWeight> getTransactionSignWeight(String transactionStr)  {
    SunNetworkResponse<TransactionSignWeight> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
    } catch(InvalidProtocolBufferException e) {
      return resp.failed(ErrorCodeEnum.EXCEPTION_INVALID_PROTOCOL_BUFFER);
    }

    TransactionSignWeight transactionSignWeight = serverApi.getTransactionSignWeight(transaction);
    if (transactionSignWeight != null) {
      resp.success(transactionSignWeight);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionApprovedList> getTransactionApprovedList(String transactionStr) {
    SunNetworkResponse<TransactionApprovedList> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
    } catch(InvalidProtocolBufferException e) {
      return resp.failed(ErrorCodeEnum.EXCEPTION_INVALID_PROTOCOL_BUFFER);
    }

    TransactionApprovedList transactionApprovedList = serverApi.getTransactionApprovedList(transaction);
    if (transactionApprovedList != null) {
      resp.success(transactionApprovedList);

    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return  resp;
  }

  public SunNetworkResponse<Transaction>  addTransactionSign(String transactionStr) {
    SunNetworkResponse<Transaction> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      }
    } catch(InvalidProtocolBufferException e) {
      return resp.failed(ErrorCodeEnum.EXCEPTION_INVALID_PROTOCOL_BUFFER);
    }

    try {
      transaction = serverApi.addTransactionSign(transaction);
      if (transaction != null) {
        resp.success(transaction);
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

  public SunNetworkResponse<TransactionResponse>  broadcastTransaction(String transactionStr) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      }
    } catch (InvalidProtocolBufferException e) {
      return resp.failed(ErrorCodeEnum.EXCEPTION_INVALID_PROTOCOL_BUFFER);
    }

    TransactionResponse ret = serverApi.broadcastTransaction(transaction);
    resp.setData(ret);
    if (ret.getResult()) {
      resp.success(ret);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }


}
