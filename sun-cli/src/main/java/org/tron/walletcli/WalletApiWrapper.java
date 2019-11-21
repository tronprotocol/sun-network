package org.tron.walletcli;

import static org.tron.common.utils.ByteArray.toHexString;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
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
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionListExtention;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.AddressUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
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
import org.tron.sunapi.ChainInterface;
import org.tron.sunapi.ErrorCodeEnum;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.AssertIssueRequest;
import org.tron.sunapi.request.DeployContractRequest;
import org.tron.sunapi.request.ExchangeCreateRequest;
import org.tron.sunapi.request.ExchangeTransactionRequest;
import org.tron.sunapi.request.TriggerConstantContractRequest;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunapi.response.TransactionResponse.ResponseType;
import org.tron.walletcli.keystore.StringUtils;
import org.tron.walletcli.keystore.Wallet;
import org.tron.walletcli.keystore.WalletFile;
import org.tron.walletcli.utils.Utils;

public class WalletApiWrapper {

  private static final Logger logger = LoggerFactory.getLogger("WalletApiWrapper");
  @Getter
  private SunNetwork sdk;
  private static boolean isMainChain = true;

  private WalletApi wallet;


  public WalletApiWrapper() {
    sdk = new SunNetwork();
    SunNetworkResponse<Integer> ret = sdk
        .init(new ServerConfigImpl("config.conf"), new MultiSignTransactionImpl());
    if (ret.getData() != 0) {
      System.out.println("Failed to init sdk");
    }

  }

  public ChainInterface getChainInterface() {
    if (isMainChain) {
      return sdk.getMainChainService();
    } else {
      return sdk.getSideChainService();
    }
  }


  public String registerWallet(char[] password) throws CipherException, IOException {
    if (!WalletApi.passwordValid(password)) {
      return null;
    }

    byte[] passwd = StringUtils.char2Byte(password);

    WalletFile walletFile = WalletApi.CreateWalletFile(passwd);
    StringUtils.clear(passwd);

    String keystoreName = WalletApi.store2Keystore(walletFile);
    logout();
    return keystoreName;
  }

  public String importWallet(char[] password, byte[] priKey) throws CipherException, IOException {
    if (!WalletApi.passwordValid(password)) {
      return null;
    }
    if (!Utils.priKeyValid(priKey)) {
      return null;
    }

    byte[] passwd = StringUtils.char2Byte(password);

    WalletFile walletFile = WalletApi.CreateWalletFile(passwd, priKey);
    StringUtils.clear(passwd);

    String keystoreName = WalletApi.store2Keystore(walletFile);
    logout();
    return keystoreName;
  }

  public boolean changePassword(char[] oldPassword, char[] newPassword)
      throws IOException, CipherException {
    logout();
    if (!WalletApi.passwordValid(newPassword)) {
      logger.warn("Warning: ChangePassword failed, NewPassword is invalid !!");
      return false;
    }

    byte[] oldPasswd = StringUtils.char2Byte(oldPassword);
    byte[] newPasswd = StringUtils.char2Byte(newPassword);

    boolean result = WalletApi.changeKeystorePassword(oldPasswd, newPasswd);
    StringUtils.clear(oldPasswd);
    StringUtils.clear(newPasswd);

    return result;
  }

  public boolean login() throws IOException, CipherException {
    logout();
    wallet = WalletApi.loadWalletFromKeystore();

    if (wallet == null) {
      System.out.println("Warning: Login failed, Please registerWallet or importWallet first !!");
      return false;
    }

    System.out.println("Please input your password.");
    char[] password = Utils.inputPassword(false);
    byte[] passwd = StringUtils.char2Byte(password);
    StringUtils.clear(password);
    wallet.checkPassword(passwd);

    WalletFile walletFile = wallet.getCurrentWalletFile();
    ECKey myKey = Wallet.decrypt(passwd, walletFile);

    byte[] priKey = myKey.getPrivKeyBytes();
    SunNetworkResponse<Integer> resp = sdk.setPrivateKey(toHexString(priKey));
    if (!resp.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      System.out.println("set private key failed, key: " + toHexString(priKey));
      return false;
    }

    StringUtils.clear(passwd);

    wallet.setLogin();
    return true;
  }

  public void logout() {
    if (wallet != null) {
      wallet.logout();
      wallet = null;
    }
    //Neddn't logout
  }

  //password is current, will be enc by password2.
  public byte[] backupWallet() throws IOException, CipherException {
    if (wallet == null || !wallet.isLoginState()) {
      wallet = WalletApi.loadWalletFromKeystore();
      if (wallet == null) {
        System.out.println("Warning: BackupWallet failed, no wallet can be backup !!");
        return null;
      }
    }

    System.out.println("Please input your password.");
    char[] password = Utils.inputPassword(false);
    byte[] passwd = StringUtils.char2Byte(password);
    StringUtils.clear(password);
    byte[] privateKey = wallet.getPrivateBytes(passwd);
    StringUtils.clear(passwd);

    return privateKey;
  }

  public void switch2Main() {
    isMainChain = true;
  }

  public void switch2Side() {
    isMainChain = false;
  }

  public boolean isMainChain() {
    return isMainChain;
  }

  public String getAddress() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: GetAddress failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<byte[]> resp = getChainInterface().getAddress();
    if (resp.getData() == null) {
      return null;
    }

    String address = AddressUtil.encode58Check(resp.getData());
    return address;
  }


  public SunNetworkResponse<Long> getBalance() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: QueryAccount failed,  Please login first !!");
      return null;
    }

    return getChainInterface().getBalance();
  }

  public SunNetworkResponse<Account> getAccount(String address) {

    SunNetworkResponse<Account> result = getChainInterface().getAccount(address);

    return result;
  }

  public SunNetworkResponse<TransactionResponse> sendCoin(String toAddress, long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: SendCoin failed,  Please login first !!");
      return null;
    }
    byte[] to = AddressUtil.decodeFromBase58Check(toAddress);
    if (to == null) {
      return null;
    }

    return getChainInterface().sendCoin(toAddress, amount);

  }

  public SunNetworkResponse<TransactionResponse> transferAsset(String toAddress, String assertName,
      long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: TransferAsset failed,  Please login first !!");
      return null;
    }

    return getChainInterface().transferAsset(toAddress, assertName, amount);
  }

  public SunNetworkResponse<TransactionResponse> participateAssetIssue(String toAddress,
      String assertName,
      long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: TransferAsset failed,  Please login first !!");
      return null;
    }

    return getChainInterface().participateAssetIssue(toAddress, assertName, amount);
  }

  public boolean assetIssue(String name, String totalSupplyStr, String trxNumStr, String icoNumStr,
      String precisionStr,
      String startYyyyMmDd, String endYyyyMmDd, String description, String url,
      String freeNetLimitPerAccount,
      String publicFreeNetLimitString, HashMap<String, String> frozenSupply) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: assetIssue failed,  Please login first !!");
      return false;
    }

    AssertIssueRequest request = new AssertIssueRequest();
    request.setName(name);
    request.setTotalSupplyStr(totalSupplyStr);
    request.setTrxNumStr(trxNumStr);
    request.setIcoNumStr(icoNumStr);
    request.setPrecisionStr(precisionStr);
    request.setStartYyyyMmDd(startYyyyMmDd);
    request.setEndYyyyMmDd(endYyyyMmDd);
    request.setDescription(description);
    request.setUrl(url);
    request.setFreeNetLimitPerAccount(freeNetLimitPerAccount);
    request.setPublicFreeNetLimitString(publicFreeNetLimitString);
    request.setFrozenSupply(frozenSupply);

    SunNetworkResponse<TransactionResponse> SnResp = getChainInterface().assetIssue(request);
    TransactionResponse resp = SnResp.getData();
    printResponseInfo(resp);
    if (resp != null) {
      return resp.result;
    }

    return false;
  }

  public SunNetworkResponse<TransactionResponse> createAccount(String address) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createAccount failed,  Please login first !!");
      return null;
    }

    return getChainInterface().createAccount(address);
  }


  public Account queryAccountById(String accountId) {
    SunNetworkResponse<Account> resp = getChainInterface().getAccountById(accountId);

    return resp.getData();
  }

  public AddressPrKeyPairMessage generateAddress() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createAccount failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<AddressPrKeyPairMessage> resp = getChainInterface().generateAddress();
    if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
      return resp.getData();
    }

    return null;
  }

  public AddressPrKeyPairMessage generateAddressOffline() {
    return getSdk().mainChainService.generateAddressOffline();
  }

  public SunNetworkResponse<TransactionResponse> createWitness(String url) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createWitness failed,  Please login first !!");
      return null;
    }

    return getChainInterface().createWitness(url);
  }

  public SunNetworkResponse<TransactionResponse> updateWitness(String url) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateWitness failed,  Please login first !!");
      return null;
    }

    return getChainInterface().updateWitness(url);
  }

  public BlockExtention getBlock(long blockNum) {
    SunNetworkResponse<BlockExtention> resp = getChainInterface().getBlock(blockNum);
    if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
      return resp.getData();
    }

    return null;
  }

  public long getTransactionCountByBlockNum(long blockNum) {
    SunNetworkResponse<Long> resp = getChainInterface().getTransactionCountByBlockNum(blockNum);

    return resp.getData();
  }

  public SunNetworkResponse<TransactionResponse> voteWitness(HashMap<String, String> witness) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: VoteWitness failed,  Please login first !!");
      return null;
    }

    return getChainInterface().voteWitness(witness);
  }

  public WitnessList listWitnesses() {
    try {
      SunNetworkResponse<WitnessList> resp = getChainInterface().listWitnesses();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        return resp.getData();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public AssetIssueList getAssetIssueList() {
    try {
      SunNetworkResponse<AssetIssueList> resp = getChainInterface().getAssetIssueList();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        return resp.getData();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public AssetIssueList getAssetIssueList(long offset, long limit) {
    try {
      SunNetworkResponse<AssetIssueList> resp = getChainInterface()
          .getAssetIssueList(offset, limit);
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        return resp.getData();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public AssetIssueContract getAssetIssueByName(String assetName) {
    SunNetworkResponse<AssetIssueContract> resp = getChainInterface()
        .getAssetIssueByName(assetName);
    if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
      return resp.getData();
    }

    return null;
  }

  public AssetIssueList getAssetIssueListByName(String assetName) {
    try {
      SunNetworkResponse<AssetIssueList> resp = getChainInterface()
          .getAssetIssueListByName(assetName);
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        return resp.getData();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return null;
  }

  public AssetIssueContract getAssetIssueById(String assetId) {
    SunNetworkResponse<AssetIssueContract> resp = getChainInterface().getAssetIssueById(assetId);

    if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
      return resp.getData();
    } else {
      return null;
    }
  }

  public Optional<ProposalList> getProposalListPaginated(long offset, long limit) {
    try {
      SunNetworkResponse<ProposalList> resp = getChainInterface()
          .getProposalsListPaginated(offset, limit);
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }


  public Optional<ExchangeList> getExchangeListPaginated(long offset, long limit) {
    try {
      SunNetworkResponse<ExchangeList> resp = getChainInterface()
          .getExchangesListPaginated(offset, limit);
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }


  public Optional<NodeList> listNodes() {
    try {
      SunNetworkResponse<NodeList> resp = getChainInterface().listNodes();
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public GrpcAPI.NumberMessage getTotalTransaction() {
    SunNetworkResponse<NumberMessage> resp = getChainInterface().getTotalTransaction();
    return resp.getData();
  }

  public String getNextMaintenanceTime() {
    SunNetworkResponse<String> resp = getChainInterface().getNextMaintenanceTime();
    return resp.getData();
  }

  public SunNetworkResponse<TransactionResponse> updateAccount(String accountName) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateAccount failed, Please login first !!");
      return null;
    }

    return getChainInterface().updateAccount(accountName);
  }

  public SunNetworkResponse<TransactionResponse> setAccountId(String accountId) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: setAccount failed, Please login first !!");
      return null;
    }

    return getChainInterface().setAccountId(accountId);
  }


  public SunNetworkResponse<TransactionResponse> updateAsset(String newLimitString,
      String newPublicLimitString,
      String description, String url) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateAsset failed, Please login first !!");
      return null;
    }

    return getChainInterface().updateAsset(newLimitString, newPublicLimitString, description, url);
  }

  public SunNetworkResponse<TransactionResponse> freezeBalance(long frozen_balance,
      long frozen_duration, int resourceCode,
      String receiverAddress) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: freezeBalance failed, Please login first !!");
      return null;
    }

    return getChainInterface()
        .freezeBalance(frozen_balance, frozen_duration, resourceCode, receiverAddress);
  }

  public boolean unfreezeBalance(int resourceCode, String receiverAddress) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeBalance failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .unfreezeBalance(resourceCode, receiverAddress);
    TransactionResponse txResp = resp.getData();
    if (txResp != null) {
      printResponseInfo(txResp);
      return txResp.getResult();
    }
    return false;
  }

  public boolean fundInject(long amount) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeBalance failed, Please login first !!");
      return false;
    }
    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .fundInject(amount);
    TransactionResponse txResp = resp.getData();
    if (txResp != null) {
      printResponseInfo(txResp);
      return txResp.getResult();
    }
    return false;
  }

  public boolean unfreezeAsset() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeAsset failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface().unfreezeAsset();
    TransactionResponse txResp = resp.getData();
    if (txResp != null) {
      printResponseInfo(txResp);
      return txResp.getResult();
    }
    return false;
  }

  public boolean withdrawBalance() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: withdrawBalance failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface().withdrawBalance();
    TransactionResponse txResp = resp.getData();
    if (txResp != null) {
      printResponseInfo(txResp);
      return txResp.getResult();
    }

    return false;
  }

  public boolean createProposal(HashMap<Long, Long> parametersMap) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createProposal failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = sdk.getMainChainService()
        .createProposal(parametersMap);
    TransactionResponse txResp = resp.getData();
    if (txResp != null) {
      printResponseInfo(txResp);
      return txResp.getResult();
    }

    return false;
  }

  public boolean sideChainCreateProposal(HashMap<Long, String> parametersMap) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createProposal failed, Please login first !!");
      return false;
    }
    SunNetworkResponse<TransactionResponse> resp = sdk.getSideChainService()
        .createProposal(parametersMap);
    TransactionResponse txResp = resp.getData();
    if (txResp != null) {
      printResponseInfo(txResp);
      return txResp.getResult();
    }

    return false;
  }


  public Optional<ProposalList> getProposalsList() {
    try {
      SunNetworkResponse<ProposalList> resp = sdk.getMainChainService().listProposals();
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<SideChainProposalList> sideChainGetProposalsList() {
    try {
      SunNetworkResponse<SideChainProposalList> resp = sdk.getSideChainService().listProposals();
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ExchangeList> getExchangeList() {
    try {
      SunNetworkResponse<ExchangeList> resp = getChainInterface().listExchanges();
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<Exchange> getExchange(String id) {
    try {
      SunNetworkResponse<Exchange> resp = getChainInterface().getExchange(id);
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ChainParameters> getChainParameters() {
    try {
      SunNetworkResponse<ChainParameters> resp = getChainInterface().getChainParameters();
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<SideChainParameters> getSideChainParameters() {
    try {
      SunNetworkResponse<SideChainParameters> resp = getChainInterface().getSideChainParameters();
      return Optional.ofNullable(resp.getData());
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public boolean approveProposal(long id, boolean is_add_approval)
      throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: approveProposal failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .approveProposal(id, is_add_approval);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean deleteProposal(long id)
      throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: deleteProposal failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface().deleteProposal(id);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean exchangeCreate(String firstTokenId, long firstTokenBalance,
      String secondTokenId, long secondTokenBalance) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeCreate failed, Please login first !!");
      return false;
    }

    ExchangeCreateRequest request = new ExchangeCreateRequest();
    request.setFirstTokenId(firstTokenId);
    request.setSecondTokenId(secondTokenId);
    request.setFirstTokenBalance(firstTokenBalance);
    request.setSecondTokenBalance(secondTokenBalance);

    SunNetworkResponse<TransactionResponse> resp = getChainInterface().exchangeCreate(request);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean exchangeInject(long exchangeId, String tokenId, long quant) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeInject failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .exchangeInject(exchangeId, tokenId, quant);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean exchangeWithdraw(long exchangeId, String tokenId, long quant)
      throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeWithdraw failed, Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .exchangeWithdraw(exchangeId, tokenId, quant);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean exchangeTransaction(long exchangeId, String tokenId, long quant, long expected)
      throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeTransaction failed, Please login first !!");
      return false;
    }

    ExchangeTransactionRequest request = new ExchangeTransactionRequest();
    request.setExchangeId(exchangeId);
    request.setTokenId(tokenId);
    request.setQuant(quant);
    request.setExpected(expected);

    SunNetworkResponse<TransactionResponse> resp = getChainInterface().exchangeTransaction(request);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean updateSetting(String contractAddress, long consumeUserResourcePercent)
      throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateSetting failed,  Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .updateSetting(contractAddress, consumeUserResourcePercent);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public boolean updateEnergyLimit(String contractAddress, long originEnergyLimit)
      throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateSetting failed,  Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .updateEnergyLimit(contractAddress, originEnergyLimit);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public void printResponseInfo(TransactionResponse resp) {
    System.out.println("Transaction response information:");
    if (resp == null) {
      System.out.println("Transaction response is null.");
      return;
    }

    if (resp.getResponseType() == ResponseType.TRANSACTION_NORMAL) {
      System.out.println("response code is: " + resp.getRespCode());
      System.out.println("response result is: " + resp.getResult());
      System.out.println("response message is: " + resp.getMessage());
      System.out.println("response transaction id is: " + resp.getTrxId());
    } else if (resp.getResponseType() == ResponseType.TRANSACTION_CONSTANT) {
      System.out.println("response constantCode is: " + resp.getConstantResult());
      System.out.println("response constantResult is: " + resp.getConstantResult());
      System.out.println("response transaction id is: " + resp.getTrxId());
    } else {
      System.out.println("!!! unknown response type !!!");
    }
  }

  public boolean deployContract(String name, String abiStr, String codeStr,
      String constructorStr, String argsStr, boolean isHex,
      long feeLimit, long value, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId, String libraryAddressPair, String compilerVersion) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createContract failed,  Please login first !!");
      return false;
    }

    DeployContractRequest request = new DeployContractRequest();
    request.setContractName(name);
    request.setAbiStr(abiStr);
    request.setCodeStr(codeStr);
    request.setConstructorStr(constructorStr);
    request.setArgsStr(argsStr);
    request.setHex(isHex);
    request.setFeeLimit(feeLimit);
    request.setValue(value);
    request.setConsumeUserResourcePercent(consumeUserResourcePercent);
    request.setOriginEnergyLimit(originEnergyLimit);
    request.setTokenValue(tokenValue);
    request.setTokenId(tokenId);
    request.setLibraryAddressPair(libraryAddressPair);
    request.setCompilerVersion(compilerVersion);

    SunNetworkResponse<TransactionResponse> sunNetworkresp = getChainInterface()
        .deployContract(request);
    logger.info("sun network response code is: " + sunNetworkresp.getDesc());

    TransactionResponse resp = sunNetworkresp.getData();
    printResponseInfo(resp);
    if (resp != null) {
      if (resp.getResult()) {
        SunNetworkResponse<Transaction> txResp = getChainInterface()
            .getTransactionById(resp.getTrxId());
        Transaction tx = txResp.getData();
        if (tx != null) {

          byte[] txRawDataHash = Sha256Hash.of(tx.getRawData().toByteArray()).getBytes();
          SunNetworkResponse<byte[]> addressResp = getChainInterface().getAddress();
          byte[] ownerAddress = addressResp.getData();

          byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
          System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
          System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

          byte[] contractAddress = Hash.sha3omit12(combined);
          System.out.println(
              "Your smart contract address will be: " + AddressUtil.encode58Check(contractAddress));
        } else {
          logger.info("getTransactionById " + resp.getTrxId() + " failed !!");
        }
      }

      return resp.getResult();
    }
    return false;
  }

  public boolean callContract(String contractAddress, long callValue, String methodStr,
      String argsStr, boolean isHex, long feeLimit, long tokenValue, String tokenId) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: callContract failed,  Please login first !!");
      return false;
    }

    TriggerContractRequest request = new TriggerContractRequest();
    request.setContractAddrStr(contractAddress);
    request.setCallValue(callValue);
    request.setMethodStr(methodStr);
    request.setArgsStr(argsStr);
    request.setHex(isHex);
    request.setFeeLimit(feeLimit);
    request.setTokenCallValue(tokenValue);
    request.setTokenId(tokenId);
    SunNetworkResponse<TransactionResponse> sunNetworkresp = getChainInterface()
        .triggerContract(request);
    logger.info("sun network response code is: " + sunNetworkresp.getDesc());

    TransactionResponse resp = sunNetworkresp.getData();
    printResponseInfo(resp);
    if (resp != null) {
      return resp.getResult();
    }

    return false;
  }

  public boolean callConstantContract(String contractAddress, String methodStr,
      String argsStr, boolean isHex, long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: callConstantContract failed,  Please login first !!");
      return false;
    }

    TriggerConstantContractRequest request = new TriggerConstantContractRequest();
    request.setContractAddrStr(contractAddress);
    request.setMethodStr(methodStr);
    request.setArgsStr(argsStr);
    request.setHex(isHex);
    request.setFeeLimit(feeLimit);
    SunNetworkResponse<TransactionResponse> sunNetworkresp = getChainInterface()
        .triggerConstantContract(request);
    logger.info("sun network response code is: " + sunNetworkresp.getDesc());

    TransactionResponse resp = sunNetworkresp.getData();
    printResponseInfo(resp);
    if (resp != null) {
      return resp.getResult();
    }

    return false;
  }

  public boolean callContractAndCheck(String contractAddress, long callValue, String methodStr,
      String argsStr,
      boolean isHex, long feeLimit, long tokenValue, String tokenId) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: callContractAndCheck failed,  Please login first !!");
      return false;
    }

    TriggerContractRequest request = new TriggerContractRequest();
    request.setContractAddrStr(contractAddress);
    request.setCallValue(callValue);
    request.setMethodStr(methodStr);
    request.setHex(isHex);
    request.setArgsStr(argsStr);
    request.setFeeLimit(feeLimit);
    request.setTokenCallValue(tokenValue);
    request.setTokenId(tokenId);
    SunNetworkResponse<TransactionResponse> sunNetworkResp = getChainInterface()
        .triggerContract(request);
    logger.info("sun network response code is: " + sunNetworkResp.getDesc());
    TransactionResponse resp = sunNetworkResp.getData();

    printResponseInfo(resp);

    String trxId = resp.getTrxId();
    if (org.apache.commons.lang3.StringUtils.isEmpty(trxId)) {
      return false;
    }

    SunNetworkResponse<Boolean> checkResp = getChainInterface().checkTrxResult(trxId);

    return checkResp.getData();
  }

  public boolean accountPermissionUpdate(String ownerAddress, String permission)
      throws IOException, CipherException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: accountPermissionUpdate failed,  Please login first !!");
      return false;
    }

    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .updateAccountPermission(ownerAddress, permission);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);
    if (txResp != null) {
      return txResp.getResult();
    }

    return false;
  }

  public Transaction addTransactionSign(String transaction) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: addTransactionSign failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<Transaction> resp = getChainInterface().addTransactionSign(transaction);
    return resp.getData();
  }


  public void sideGetMappingAddress(byte[] sideGateway, String mainContractAddress) {

    SunNetworkResponse<String> resp;
    resp = sdk.getSideChainService().sideGetMappingAddress(sideGateway, mainContractAddress);

    String contractAddress = resp.getData();
    System.out.println("sideContractAddress is " + contractAddress);
  }

  public SunNetworkResponse<TransactionResponse> depositTrx(long num, long depositFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: depositTrx failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().depositTrx(num, depositFee, feeLimit);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> depositTrc10(
      String tokenId, long tokenValue, long depositFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: depositTrc10 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().depositTrc10(tokenId, tokenValue, depositFee, feeLimit);
    TransactionResponse txResp = resp.getData();
    printResponseInfo(txResp);

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> depositTrc20(String contractAddrStr, String num,
      long depositFee, long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: depositTrc20 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().depositTrc20(contractAddrStr, num, depositFee, feeLimit);
    List<TransactionResponse> list = resp.getDataList();
    for (TransactionResponse txResp : list) {
      printResponseInfo(txResp);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> depositTrc721(String contractAddrStr, String num,
      long depositFee, long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: depositTrc20 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService()
        .depositTrc721(contractAddrStr, num, depositFee, feeLimit);
    List<TransactionResponse> list = resp.getDataList();
    for (TransactionResponse txResp : list) {
      printResponseInfo(txResp);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> mappingTrc20(String trxHash, long mappingFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: mappingTrc20 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().mappingTrc20(trxHash, mappingFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> mappingTrc721(String trxHash, long mappingFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: mappingTrc721 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().mappingTrc721(trxHash, mappingFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrx(long trxNum, long withdrawFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: withdrawTrx failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().withdrawTrx(trxNum, withdrawFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc10(String tokenId, long tokenValue,
      long withdrawFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: withdrawTrc10 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().withdrawTrc10(tokenId, tokenValue, withdrawFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc20(String contractAddrStr, String value,
      long withdrawFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: withdrawTrc20 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().withdrawTrc20(contractAddrStr, value, withdrawFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc721(String contractAddrStr,
      String value, long withdrawFee, long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: withdrawTrc721 failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().withdrawTrc721(contractAddrStr, value, withdrawFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> retryDeposit(String nonce, long retryFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: retry deposit failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().retryDeposit(nonce, retryFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> retryWithdraw(String nonce, long retryFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: retry withdraw failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().retryWithdraw(nonce, retryFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> retryMapping(String nonce, long retryFee,
      long feeLimit) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: retry mapping failed,  Please login first !!");
      return null;
    }

    SunNetworkResponse<TransactionResponse> resp;
    resp = sdk.getCrossChainService().retryMapping(nonce, retryFee, feeLimit);
    printResponseInfo(resp.getData());

    return resp;
  }

  public Optional<AssetIssueList> getAssetIssueByAccount(String address) {
    SunNetworkResponse<AssetIssueList> resp = getChainInterface().getAssetIssueByAccount(address);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public AccountNetMessage getAccountNet(String address) {
    SunNetworkResponse<AccountNetMessage> resp = getChainInterface().getAccountNet(address);
    logger.info("sun network response code is: " + resp.getDesc());

    return resp.getData();
  }

  public AccountResourceMessage getAccountResource(String address) {
    SunNetworkResponse<AccountResourceMessage> resp = getChainInterface()
        .getAccountResource(address);
    logger.info("sun network response code is: " + resp.getDesc());

    return resp.getData();
  }

  public Optional<Proposal> getProposal(String id) {
    SunNetworkResponse<Proposal> resp = getChainInterface().getProposal(id);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<DelegatedResourceList> getDelegatedResource(String fromAddress,
      String toAddress) {
    SunNetworkResponse<DelegatedResourceList> resp = getChainInterface()
        .getDelegatedResource(fromAddress, toAddress);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }


  public Optional<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(String address) {
    SunNetworkResponse<DelegatedResourceAccountIndex> resp = getChainInterface()
        .getDelegatedResourceAccountIndex(address);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<Transaction> getTransactionById(String txid) {
    SunNetworkResponse<Transaction> resp = getChainInterface().getTransactionById(txid);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }


  public Optional<TransactionInfo> getTransactionInfoById(String txid) {
    SunNetworkResponse<TransactionInfo> resp = getChainInterface().getTransactionInfoById(txid);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<TransactionListExtention> getTransactionsFromThis(String address, int offset,
      int limit) {
    SunNetworkResponse<TransactionListExtention> resp = getChainInterface()
        .getTransactionsFromThis(address, offset, limit);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<TransactionListExtention> getTransactionsToThis(String address, int offset,
      int limit) {
    SunNetworkResponse<TransactionListExtention> resp = getChainInterface()
        .getTransactionsToThis(address, offset, limit);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<Block> getBlockById(String blockID) {
    SunNetworkResponse<Block> resp = getChainInterface().getBlockById(blockID);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<BlockListExtention> getBlockByLimitNext(long start, long end) {
    SunNetworkResponse<BlockListExtention> resp = getChainInterface()
        .getBlockByLimitNext(start, end);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public Optional<BlockListExtention> getBlockByLatestNum(long num) {
    SunNetworkResponse<BlockListExtention> resp = getChainInterface().getBlockByLatestNum(num);
    logger.info("sun network response code is: " + resp.getDesc());

    return Optional.ofNullable(resp.getData());
  }

  public SmartContract getContract(String address) {
    SunNetworkResponse<SmartContract> resp = getChainInterface().getContract(address);
    logger.info("sun network response code is: " + resp.getDesc());

    return resp.getData();
  }

  public TransactionSignWeight getTransactionSignWeight(String transactionStr) {
    SunNetworkResponse<TransactionSignWeight> resp = getChainInterface()
        .getTransactionSignWeight(transactionStr);
    logger.info("sun network response code is: " + resp.getDesc());

    return resp.getData();
  }

  public TransactionApprovedList getTransactionApprovedList(String transactionStr) {
    SunNetworkResponse<TransactionApprovedList> resp = getChainInterface()
        .getTransactionApprovedList(transactionStr);
    logger.info("sun network response code is: " + resp.getDesc());

    return resp.getData();
  }

  public TransactionResponse broadcastTransaction(String transactionStr) {
    SunNetworkResponse<TransactionResponse> resp = getChainInterface()
        .broadcastTransaction(transactionStr);
    logger.info("sun network response code is: " + resp.getDesc());

    return resp.getData();
  }

}
