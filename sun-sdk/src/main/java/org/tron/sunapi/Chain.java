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
import org.apache.commons.lang3.StringUtils;
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
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.AddressUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.protos.Contract;
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
import org.tron.sunserver.IMultiTransactionSign;
import org.tron.sunserver.ServerApi;

@Slf4j
public class Chain implements ChainInterface {

  @Getter
  private ServerApi serverApi;

  /**
   * @param config      the environment configuration path
   * @param priKey      the private key of user
   * @param isMainChain main chain or side chain
   * @return the result of initialize
   * @author sun-network
   */

  public SunNetworkResponse<Integer> init(IServerConfig config, String priKey, boolean isMainChain,
      IMultiTransactionSign multiTransactionSign) {
    SunNetworkResponse<Integer> ret = new SunNetworkResponse<>();
    byte[] temp = ByteArray.fromHexString(priKey);

    if (!Utils.priKeyValid(temp)) {
      ret.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }
    serverApi = new ServerApi(config, temp, isMainChain, multiTransactionSign);

    return ret.success(0);
  }

  public SunNetworkResponse<Integer> init(IServerConfig config, boolean isMainChain,
      IMultiTransactionSign multiTransactionSign) {
    SunNetworkResponse<Integer> ret = new SunNetworkResponse<>();

    serverApi = new ServerApi(config, isMainChain, multiTransactionSign);

    return ret.success(0);
  }

  public SunNetworkResponse<Integer> setPrivateKey(String priKey) {
    SunNetworkResponse<Integer> ret = new SunNetworkResponse<>();
    byte[] temp = ByteArray.fromHexString(priKey);

    if (!Utils.priKeyValid(temp)) {
      ret.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    serverApi.initPrivateKey(temp);

    return ret.success(0);
  }

  /**
   * @param request request of deploy contract
   * @return the response of deploy contract which contains the address of contract deployed
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> deployContract(DeployContractRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

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
      if (StringUtils.isNotEmpty(constructorStr) && !constructorStr.equals("#")) {
        if (isHex) {
          codeStr += argsStr;
        } else {
          codeStr += ByteArray.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
        }
      }

      long value = request.getValue();
      long tokenValue = request.getTokenValue();
      String tokenId = request.getTokenId();
      String libraryAddressPair = request.getLibraryAddressPair();
      String compilerVersion = request.getCompilerVersion();

      TransactionResponse ret = serverApi
          .deployContract(contractName, abiStr, codeStr, feeLimit, value,
              consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId,
              libraryAddressPair,
              compilerVersion);
      resp.setData(ret);
      if (ret.getResult()) {
        resp.success(ret);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @param request request of trigger contract
   * @return the response of trigger contract which contains the id of transaction
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> triggerContract(TriggerContractRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String contractAddrStr = request.getContractAddrStr();
    String methodStr = request.getMethodStr();
    String argsStr = request.getArgsStr();
    boolean isHex = request.isHex();
    long feeLimit = request.getFeeLimit();
    long callValue = request.getCallValue();
    long tokenCallValue = request.getTokenCallValue();
    String tokenId = request.getTokenId();
    if (StringUtils.isEmpty(argsStr) || argsStr.equalsIgnoreCase("#")) {
      argsStr = "";
    }
    if (StringUtils.isEmpty(tokenId) || tokenId.equalsIgnoreCase("#")) {
      tokenId = "";
    }
    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, argsStr, isHex));
      byte[] contractAddress = AddressUtil.decodeFromBase58Check(contractAddrStr);

      TransactionResponse result = serverApi
          .triggerContract(contractAddress, callValue, input, feeLimit, tokenCallValue, tokenId);
      resp.setData(result);
      if (StringUtils.isEmpty(result.getTrxId())) {
        resp.failed(ErrorCodeEnum.FAILED);
      } else {
        resp.success(result);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @param request request of trigger contract
   * @return the response of trigger contract which contains the id of transaction
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> triggerConstantContract(
      TriggerConstantContractRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String contractAddrStr = request.getContractAddrStr();
    String methodStr = request.getMethodStr();
    String argsStr = request.getArgsStr();
    boolean isHex = request.isHex();
    long feeLimit = request.getFeeLimit();
    if (StringUtils.isEmpty(argsStr) || argsStr.equalsIgnoreCase("#")) {
      argsStr = "";
    }
    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, argsStr, isHex));
      byte[] contractAddress = AddressUtil.decodeFromBase58Check(contractAddrStr);

      TransactionResponse result = serverApi
          .triggerConstantContract(contractAddress, input, feeLimit);
      resp.setData(result);
      if (StringUtils.isEmpty(result.getTrxId())) {
        resp.failed(ErrorCodeEnum.FAILED);
      } else {
        resp.success(result);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return address
   * @author sun-network
   */
  public SunNetworkResponse<byte[]> getAddress() {
    SunNetworkResponse<byte[]> resp = new SunNetworkResponse<>();

    byte[] address = serverApi.getAddress();
    resp.success(address);

    return resp;
  }

  /**
   * @return balance
   * @author sun-network
   */
  public SunNetworkResponse<Long> getBalance() {
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

  /**
   * @return account information
   * @author sun-network
   */
  public SunNetworkResponse<Account> getAccount(String address) {
    SunNetworkResponse<Account> resp = new SunNetworkResponse<>();
    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
    if (addressBytes == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return null;
    }

    Account account = serverApi.queryAccount(addressBytes);
    resp.success(account);
    return resp;
  }

  /**
   * @param accountId account ID
   * @return account information
   * @author sun-network
   */
  public SunNetworkResponse<Account> getAccountById(String accountId) {
    SunNetworkResponse<Account> resp = new SunNetworkResponse<>();

    Account account = serverApi.queryAccountById(accountId);
    if (account == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    } else {
      resp.success(account);
    }

    return resp;
  }

  /**
   * @param accountName accountName
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> updateAccount(String accountName) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(accountName)) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
      return resp;
    }
    byte[] accountNameBytes = ByteArray.fromString(accountName);

    try {
      TransactionResponse result = serverApi.updateAccount(accountNameBytes);
      resp.setData(result);
      if (StringUtils.isEmpty(result.getTrxId())) {
        resp.failed(ErrorCodeEnum.FAILED);
      } else {
        resp.success(result);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @param accountId the account ID
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> setAccountId(String accountId) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(accountId)) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
      return resp;
    }
    byte[] accountIdBytes = ByteArray.fromString(accountId);

    try {
      TransactionResponse result = serverApi.setAccountId(accountIdBytes);
      resp.setData(result);
      if (StringUtils.isEmpty(result.getTrxId())) {
        resp.failed(ErrorCodeEnum.FAILED);
      } else {
        resp.success(result);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  //TODO main

  /**
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> updateAsset(String newLimitString,
      String newPublicLimitString,
      String description, String url) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] descriptionBytes = ByteArray.fromString(description);
    byte[] urlBytes = ByteArray.fromString(url);
    long newLimit = new Long(newLimitString);
    long newPublicLimit = new Long(newPublicLimitString);

    try {
      TransactionResponse result = serverApi
          .updateAsset(descriptionBytes, urlBytes, newLimit, newPublicLimit);
      resp.setData(result);
      if (StringUtils.isEmpty(result.getTrxId())) {
        resp.failed(ErrorCodeEnum.FAILED);
      } else {
        resp.success(result);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return asset issue list
   * @author sun-network
   */
  public SunNetworkResponse<AssetIssueList> getAssetIssueByAccount(String address) {
    SunNetworkResponse<AssetIssueList> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
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

  /**
   * @return asset issue list
   * @author sun-network
   */
  public SunNetworkResponse<AccountNetMessage> getAccountNet(String address) {
    SunNetworkResponse<AccountNetMessage> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
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

  /**
   * @return account resource
   * @author sun-network
   */
  public SunNetworkResponse<AccountResourceMessage> getAccountResource(String address) {
    SunNetworkResponse<AccountResourceMessage> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
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

  /**
   * @return asset issue
   * @author sun-network
   */
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

  /**
   * @return asset issue list
   * @author sun-network
   */
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

  /**
   * @param assetId the asset ID
   * @return asset issue
   * @author sun-network
   */
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

  /**
   * @param toAddress the destination address
   * @param amount    the amount of trx
   * @return the result of send
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> sendCoin(String toAddress, long amount) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] to = AddressUtil.decodeFromBase58Check(toAddress);
    if (to == null) {
      resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      return resp;
    }

    try {
      TransactionResponse result = serverApi.sendCoin(to, amount);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }
    return resp;
  }

  /**
   * @param toAddress  the destination address
   * @param assertName the asset name
   * @param amount     the amount of asset
   * @return the result of transfer
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> transferAsset(String toAddress, String assertName,
      long amount) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] to = AddressUtil.decodeFromBase58Check(toAddress);
    if (to == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse result = serverApi.transferAsset(to, assertName.getBytes(), amount);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @param toAddress  the destination address
   * @param assertName the asset name
   * @param amount     the amount of asset
   * @return the result of participating asset issue
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<TransactionResponse> participateAssetIssue(String toAddress,
      String assertName, long amount) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    byte[] to = AddressUtil.decodeFromBase58Check(toAddress);
    if (to == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse result = serverApi
          .participateAssetIssue(to, assertName.getBytes(), amount);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }


  private TransactionResponse assetIssueProc(String name, long totalSupply, int trxNum, int icoNum,
      int precision,
      long startTime, long endTime, int voteScore, String description, String url,
      long freeNetLimit, long publicFreeNetLimit, HashMap<String, String> frozenSupply) {

    Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(serverApi.getAddress()));
    builder.setName(ByteString.copyFrom(name.getBytes()));

    if (totalSupply <= 0) {
      return null;
    }
    builder.setTotalSupply(totalSupply);

    if (trxNum <= 0) {
      return null;
    }
    builder.setTrxNum(trxNum);

    if (icoNum <= 0) {
      return null;
    }
    builder.setNum(icoNum);

    if (precision < 0) {
      return null;
    }
    builder.setPrecision(precision);

    long now = System.currentTimeMillis();
    if (startTime <= now) {
      return null;
    }
    if (endTime <= startTime) {
      return null;
    }

    if (freeNetLimit < 0) {
      return null;
    }
    if (publicFreeNetLimit < 0) {
      return null;
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

  /**
   * @param request the request of assert issue
   * @return the result of creating assert issue
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<TransactionResponse> assetIssue(AssertIssueRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

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

    TransactionResponse result = assetIssueProc(name, totalSupply, trxNum, icoNum, precision,
        startTime,
        endTime, 0, description, url, freeAssetNetLimit, publicFreeNetLimit, frozenSupply);
    resp.setData(result);
    if (result == null || result.getResult() != true) {
      resp.failed(ErrorCodeEnum.FAILED);
    } else {
      resp.success(result);
    }

    return resp;
  }

  /**
   * @param address the account address
   * @return the result of creating account
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> createAccount(String address) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse result = serverApi.createAccount(addressBytes);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the result of creating witness
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> createWitness(String url) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.createWitness(url.getBytes());
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> updateWitness(String url) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.updateWitness(url.getBytes());
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the witness list
   * @author sun-network
   */
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

  /**
   * @return the asset issue list
   * @author sun-network
   */
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

  /**
   * @param offset the offset from the first asset
   * @param limit  the number of asset issues
   * @return assert issue list
   * @author sun-network
   */
  public SunNetworkResponse<AssetIssueList> getAssetIssueList(long offset, long limit) {
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

  /**
   * @param offset the offset from the first proposal
   * @param limit  the number of proposal
   * @return proposal list
   * @author sun-network
   */
  public SunNetworkResponse<ProposalList> getProposalsListPaginated(long offset, long limit) {
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

  /**
   * @param offset the offset from the first exchange
   * @param limit  the number of exchange
   * @return exchange list
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<ExchangeList> getExchangesListPaginated(long offset, long limit) {
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

  /**
   * @return node list
   * @author sun-network
   */
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

  /**
   * @param blockNum the block number
   * @return Block
   * @author sun-network
   */
  public SunNetworkResponse<BlockExtention> getBlock(long blockNum) {
    SunNetworkResponse<BlockExtention> resp = new SunNetworkResponse<>();

    BlockExtention block = serverApi.getBlock2(blockNum);
    resp.success(block);

    return resp;
  }

  /**
   * @param blockNum the block number
   * @return the count of transaction
   * @author sun-network
   */
  public SunNetworkResponse<Long> getTransactionCountByBlockNum(long blockNum) {
    SunNetworkResponse<Long> resp = new SunNetworkResponse<>();

    long count = serverApi.getTransactionCountByBlockNum(blockNum);
    resp.success(count);

    return resp;
  }

  /**
   * @param witness the vote information which contains address and vote count
   * @return the result of vote
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> voteWitness(HashMap<String, String> witness) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (witness == null || witness.isEmpty()) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    try {
      TransactionResponse result = serverApi.voteWitness(witness);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the result of freeze balance
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> freezeBalance(long frozen_balance,
      long frozen_duration,
      int resourceCode, String receiverAddress) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi
          .freezeBalance(frozen_balance, frozen_duration, resourceCode,
              receiverAddress);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the result of inject fund
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> fundInject(long amount) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.fundInject(amount);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @param resourceCode    the resource code
   * @param receiverAddress the receive address
   * @return the result of unfreeze balance
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> unfreezeBalance(int resourceCode,
      String receiverAddress) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.unfreezeBalance(resourceCode, receiverAddress);
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the result of unfreeze asset
   * @author sun-network
   */
  //TODO main
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
   * @param id       the proposal id
   * @param approval the proposal or not
   * @return the result of approving proposal
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> approveProposal(long id, boolean approval) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.approveProposal(id, approval);
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
   * @param id the id of proposal
   * @return the result of deleting proposal
   * @author sun-network
   */
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
   * @param id the id of proposal
   * @return the proposal
   * @author sun-network
   */
  public SunNetworkResponse<Proposal> getProposal(String id) {
    SunNetworkResponse<Proposal> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(id)) {
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

  /**
   * @param fromAddress the from address
   * @param toAddress   the address delegated
   * @return the delegated resource list
   * @author sun-network
   */
  public SunNetworkResponse<DelegatedResourceList> getDelegatedResource(String fromAddress,
      String toAddress) {
    SunNetworkResponse<DelegatedResourceList> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(fromAddress) || StringUtils.isEmpty(toAddress)) {
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

  /**
   * @return the delegated resource account index
   * @author sun-network
   */
  public SunNetworkResponse<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(
      String address) {
    SunNetworkResponse<DelegatedResourceAccountIndex> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(address)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<DelegatedResourceAccountIndex> result = serverApi
        .getDelegatedResourceAccountIndex(address);
    if (result.isPresent()) {
      DelegatedResourceAccountIndex delegatedResourceAccountIndex = result.get();
      resp.success(delegatedResourceAccountIndex);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @param request the request of exchange
   * @return the result of creating exchange
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<TransactionResponse> exchangeCreate(ExchangeCreateRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();
    if (request == null || StringUtils.isEmpty(request.getFirstTokenId()) || StringUtils
        .isEmpty(request.getSecondTokenId())) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] firstTokenId = request.getFirstTokenId().getBytes();
    long firstTokenBalance = request.getFirstTokenBalance();
    byte[] secondTokenId = request.getSecondTokenId().getBytes();
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
   * @param exchangeId the exchange id
   * @param tokenIdStr the token id
   * @param quantity   the quantity
   * @return the result of injecting exchange
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<TransactionResponse> exchangeInject(long exchangeId, String tokenIdStr,
      long quantity) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(tokenIdStr)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] tokenId = tokenIdStr.getBytes();
    try {
      TransactionResponse result = serverApi.exchangeInject(exchangeId, tokenId, quantity);
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
   * @param exchangeId the exchange id
   * @param tokenIdStr the token id
   * @param quantity   the quantity
   * @return the result of withdrawing exchange
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<TransactionResponse> exchangeWithdraw(long exchangeId,
      String tokenIdStr, long quantity) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(tokenIdStr)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] tokenId = tokenIdStr.getBytes();
    try {
      TransactionResponse result = serverApi.exchangeWithdraw(exchangeId, tokenId, quantity);
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
   * @param request the request of exchange request
   * @return the result of exchange transaction
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<TransactionResponse> exchangeTransaction(
      ExchangeTransactionRequest request) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(request.getTokenId())) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    long exchangeId = request.getExchangeId();
    byte[] tokenId = request.getTokenId().getBytes();
    long quant = request.getQuant();
    long expected = request.getExpected();

    try {
      TransactionResponse result = serverApi
          .exchangeTransaction(exchangeId, tokenId, quant, expected);
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
   * @return exchange list
   * @author sun-network
   */
  //TODO main
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

  /**
   * @param id the exchange id
   * @return exchange
   * @author sun-network
   */
  //TODO main
  public SunNetworkResponse<Exchange> getExchange(String id) {
    SunNetworkResponse<Exchange> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(id)) {
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

  /**
   * @return the result of withdrawing balance
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawBalance() {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    try {
      TransactionResponse result = serverApi.withdrawBalance();
      resp.setData(result);
      if (result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  /**
   * @return the total amount of transaction
   * @author sun-network
   */
  public SunNetworkResponse<NumberMessage> getTotalTransaction() {
    SunNetworkResponse<NumberMessage> resp = new SunNetworkResponse<>();

    NumberMessage totalTransition = serverApi.getTotalTransaction();

    return resp.success(totalTransition);
  }

  /**
   * @return the next maintenance time
   * @author sun-network
   */
  public SunNetworkResponse<String> getNextMaintenanceTime() {
    SunNetworkResponse<String> resp = new SunNetworkResponse<>();

    NumberMessage nextMaintenanceTime = serverApi.getNextMaintenanceTime();
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String date = formatter.format(nextMaintenanceTime.getNum());

    return resp.success(date);
  }

  /**
   * @param trxId the transaction ID
   * @return transaction
   * @author sun-network
   */
  public SunNetworkResponse<Transaction> getTransactionById(String trxId) {
    SunNetworkResponse<Transaction> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(trxId)) {
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

  /**
   * @param trxId the transaction ID
   * @return transaction information
   * @author sun-network
   */
  public SunNetworkResponse<TransactionInfo> getTransactionInfoById(String trxId) {
    SunNetworkResponse<TransactionInfo> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(trxId)) {
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

  /**
   * @param address the account address
   * @param offset  the offset from first transaction
   * @param limit   the number of transaction
   * @return transaction list extension
   * @author sun-network
   */
  public SunNetworkResponse<TransactionListExtention> getTransactionsFromThis(String address,
      int offset, int limit) {
    SunNetworkResponse<TransactionListExtention> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<TransactionListExtention> result = serverApi
        .getTransactionsFromThis2(addressBytes, offset, limit);
    if (result.isPresent()) {
      TransactionListExtention transactionList = result.get();
      resp.success(transactionList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @param address the account address
   * @param offset  the offset from first transaction
   * @param limit   the number of transaction
   * @return transaction list extension
   * @author sun-network
   */
  public SunNetworkResponse<TransactionListExtention> getTransactionsToThis(String address,
      int offset, int limit) {
    SunNetworkResponse<TransactionListExtention> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
    if (addressBytes == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    Optional<TransactionListExtention> result = serverApi
        .getTransactionsToThis2(addressBytes, offset, limit);
    if (result.isPresent()) {
      TransactionListExtention transactionList = result.get();
      resp.success(transactionList);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @param blockID the block ID
   * @return Block
   * @author sun-network
   */
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

  /**
   * @param start the start number
   * @param end   the end number
   * @return Block list extension
   * @author sun-network
   */
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

  /**
   * @param num the latest number
   * @return Block list extension
   * @author sun-network
   */
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

  /**
   * @param address                    the account address
   * @param consumeUserResourcePercent the percent of user resource consuming
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> updateSetting(String address,
      long consumeUserResourcePercent) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] contractAddress = AddressUtil.decodeFromBase58Check(address);
    if (contractAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    if (consumeUserResourcePercent > 100 || consumeUserResourcePercent < 0) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse result = serverApi
          .updateSetting(contractAddress, consumeUserResourcePercent);
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
   * @param address           the account address
   * @param originEnergyLimit the limit of origin energy
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> updateEnergyLimit(String address,
      long originEnergyLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    byte[] contractAddress = AddressUtil.decodeFromBase58Check(address);
    if (contractAddress == null) {
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
   * @param address the contact address
   * @return smart contract information
   * @author sun-network
   */
  public SunNetworkResponse<SmartContract> getContract(String address) {
    SunNetworkResponse<SmartContract> resp = new SunNetworkResponse<>();

    byte[] addressBytes = AddressUtil.decodeFromBase58Check(address);
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

  /**
   * @return the address pair
   * @author sun-network
   */
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

  /**
   * @return the address pair offline
   * @author sun-network
   */
  public AddressPrKeyPairMessage generateAddressOffline() {
    ECKey ecKey = new ECKey(Utils.getRandom());
    byte[] priKey = ecKey.getPrivKeyBytes();
    byte[] address = ecKey.getAddress();
    String priKeyStr = org.apache.commons.codec.binary.Hex.encodeHexString(priKey);
    String base58check = AddressUtil.encode58Check(address);
    return AddressPrKeyPairMessage.newBuilder().setAddress(base58check).setPrivateKey(priKeyStr)
        .build();
  }

  /**
   * @param address        the account address
   * @param permissionJson the permission information
   * @return the result of update
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> updateAccountPermission(String address,
      String permissionJson) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<>();

    if (StringUtils.isEmpty(permissionJson)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    byte[] ownerAddress = AddressUtil.decodeFromBase58Check(address);
    if (ownerAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    try {
      TransactionResponse ret = serverApi.accountPermissionUpdate(ownerAddress, permissionJson);
      resp.setData(ret);
      if (ret.getResult()) {
        resp.success(ret);
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
   * @param transactionStr the transaction string
   * @return the sign weight of transaction
   * @author sun-network
   */
  public SunNetworkResponse<TransactionSignWeight> getTransactionSignWeight(String transactionStr) {
    SunNetworkResponse<TransactionSignWeight> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
    } catch (InvalidProtocolBufferException e) {
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

  /**
   * @return the  approved list of transaction
   * @author sun-network * @param transactionStr the transaction string
   */
  public SunNetworkResponse<TransactionApprovedList> getTransactionApprovedList(
      String transactionStr) {
    SunNetworkResponse<TransactionApprovedList> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
    } catch (InvalidProtocolBufferException e) {
      return resp.failed(ErrorCodeEnum.EXCEPTION_INVALID_PROTOCOL_BUFFER);
    }

    TransactionApprovedList transactionApprovedList = serverApi
        .getTransactionApprovedList(transaction);
    if (transactionApprovedList != null) {
      resp.success(transactionApprovedList);

    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @param transactionStr the transaction string
   * @return the transaction added signature
   * @author sun-network
   */
  public SunNetworkResponse<Transaction> addTransactionSign(String transactionStr) {
    SunNetworkResponse<Transaction> resp = new SunNetworkResponse<>();

    Transaction transaction;
    try {
      transaction = Transaction.parseFrom(ByteArray.fromHexString(transactionStr));
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
      }
    } catch (InvalidProtocolBufferException e) {
      return resp.failed(ErrorCodeEnum.EXCEPTION_INVALID_PROTOCOL_BUFFER);
    }

    transaction = serverApi.addTransactionSign(transaction);
    if (transaction != null) {
      resp.success(transaction);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @param transactionStr the transaction string
   * @return the result of broadcast
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> broadcastTransaction(String transactionStr) {
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

  /**
   * @return the result of broadcast
   * @author sun-network
   */
  public SunNetworkResponse<ChainParameters> getChainParameters() {
    SunNetworkResponse<ChainParameters> resp = new SunNetworkResponse<>();

    Optional<ChainParameters> result = serverApi.getChainParameters();
    if (result.isPresent()) {
      ChainParameters chainParameters = result.get();
      resp.success(chainParameters);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @return the result of broadcast
   * @author sun-network
   */
  public SunNetworkResponse<SideChainParameters> getSideChainParameters() {
    SunNetworkResponse<SideChainParameters> resp = new SunNetworkResponse<>();

    Optional<SideChainParameters> result = serverApi.getSideChainParameters();
    if (result.isPresent()) {
      SideChainParameters sideChainParameters = result.get();
      resp.success(sideChainParameters);
    } else {
      resp.failed(ErrorCodeEnum.FAILED);
    }

    return resp;
  }

  /**
   * @param txId the transaction ID
   * @return the result of broadcast
   * @author sun-network
   */
  public SunNetworkResponse<Boolean> checkTrxResult(String txId) {
    SunNetworkResponse<Boolean> resp = new SunNetworkResponse<Boolean>();

    boolean result = serverApi.checkTxInfo(txId);
    resp.success(result);

    return resp;
  }

}
