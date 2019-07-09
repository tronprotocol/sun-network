package org.tron.walletcli;

import com.google.protobuf.ByteString;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import lombok.Getter;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockExtention;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.Utils;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.keystore.StringUtils;
import org.tron.keystore.Wallet;
import org.tron.keystore.WalletFile;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.ChainParameters;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Proposal;
import org.tron.protos.Protocol.Transaction;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunapi.response.TransactionResponse.ResponseType;
import org.tron.sunserver.ServerApi;

public class WalletApiWrapper {

  private static final Logger logger = LoggerFactory.getLogger("WalletApiWrapper");
  @Getter
  private ServerApi serverApi;
  private ServerApi mainChainServerApi;
  private ServerApi sideChainServerApi;
  private SunNetwork sdk;

  private WalletApi wallet;


  public WalletApiWrapper() {
    sdk = new SunNetwork();
    SunNetworkResponse<Integer> ret = sdk.init("config.conf");
    if(ret.getData() != 0) {
      System.out.println("Failed to init sdk");
    }

    mainChainServerApi = sdk.mainChainService.getServerApi();
    sideChainServerApi = sdk.sideChainService.getServerApi();
    serverApi = mainChainServerApi;
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
    if (!ServerApi.priKeyValid(priKey)) {
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

    System.out.println("Please input your password.");
    char[] password = Utils.inputPassword(false);
    byte[] passwd = StringUtils.char2Byte(password);
    StringUtils.clear(password);
    wallet.checkPassword(passwd);

    if (wallet == null) {
      System.out.println("Warning: Login failed, Please registerWallet or importWallet first !!");
      return false;
    }

    WalletFile walletFile = wallet.getCurrentWalletFile();
    ECKey myKey = Wallet.decrypt(passwd, walletFile);
    byte[] priKey = myKey.getPrivKeyBytes();
    mainChainServerApi.initPrivateKey(priKey);
    sideChainServerApi.initPrivateKey(priKey);

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
    serverApi = mainChainServerApi;
  }

  public void switch2Side() {
    serverApi = sideChainServerApi;
  }

  public boolean isMainChain() {
    return serverApi == mainChainServerApi;
  }

  public String getAddress() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: GetAddress failed,  Please login first !!");
      return null;
    }

    return ServerApi.encode58Check(serverApi.getAddress());
  }

  public Account queryAccount() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: QueryAccount failed,  Please login first !!");
      return null;
    }

    return serverApi.queryAccount();
  }

  public boolean sendCoin(String toAddress, long amount)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: SendCoin failed,  Please login first !!");
      return false;
    }
    byte[] to = ServerApi.decodeFromBase58Check(toAddress);
    if (to == null) {
      return false;
    }

    return serverApi.sendCoin(to, amount);
  }

  public boolean transferAsset(String toAddress, String assertName, long amount)
    throws IOException, CipherException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: TransferAsset failed,  Please login first !!");
      return false;
    }
    byte[] to = ServerApi.decodeFromBase58Check(toAddress);
    if (to == null) {
      return false;
    }

    return serverApi.transferAsset(to, assertName.getBytes(), amount);
  }

  public boolean participateAssetIssue(String toAddress, String assertName,
    long amount) throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: TransferAsset failed,  Please login first !!");
      return false;
    }
    byte[] to = ServerApi.decodeFromBase58Check(toAddress);
    if (to == null) {
      return false;
    }

    return serverApi.participateAssetIssue(to, assertName.getBytes(), amount);
  }

  public boolean assetIssue(String name, long totalSupply, int trxNum, int icoNum, int precision,
    long startTime, long endTime, int voteScore, String description, String url,
    long freeNetLimit, long publicFreeNetLimit, HashMap<String, String> frozenSupply)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: assetIssue failed,  Please login first !!");
      return false;
    }

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

    TransactionResponse resp = serverApi.createAssetIssue(builder.build());
    printResponseInfo(resp);

    return resp.result;
  }

  public boolean createAccount(String address)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createAccount failed,  Please login first !!");
      return false;
    }

    byte[] addressBytes = ServerApi.decodeFromBase58Check(address);
    return serverApi.createAccount(addressBytes);
  }

  public AddressPrKeyPairMessage generateAddress() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createAccount failed,  Please login first !!");
      return null;
    }
    return serverApi.generateAddress();
  }


  public boolean createWitness(String url) throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createWitness failed,  Please login first !!");
      return false;
    }

    return serverApi.createWitness(url.getBytes());
  }

  public boolean updateWitness(String url) throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateWitness failed,  Please login first !!");
      return false;
    }

    return serverApi.updateWitness(url.getBytes());
  }

  public Block getBlock(long blockNum) {
    return serverApi.getBlock(blockNum);
  }

  public long getTransactionCountByBlockNum(long blockNum) {
    return serverApi.getTransactionCountByBlockNum(blockNum);
  }

  public BlockExtention getBlock2(long blockNum) {
    return serverApi.getBlock2(blockNum);
  }

  public boolean voteWitness(HashMap<String, String> witness)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: VoteWitness failed,  Please login first !!");
      return false;
    }

    return serverApi.voteWitness(witness);
  }

  public Optional<WitnessList> listWitnesses() {
    try {
      return serverApi.listWitnesses();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<AssetIssueList> getAssetIssueList() {
    try {
      return serverApi.getAssetIssueList();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<AssetIssueList> getAssetIssueList(long offset, long limit) {
    try {
      return serverApi.getAssetIssueList(offset, limit);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public AssetIssueContract getAssetIssueByName(String assetName) {
    return serverApi.getAssetIssueByName(assetName);
  }

  public Optional<AssetIssueList> getAssetIssueListByName(String assetName) {
    try {
      return serverApi.getAssetIssueListByName(assetName);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public AssetIssueContract getAssetIssueById(String assetId) {
    return serverApi.getAssetIssueById(assetId);
  }

  public Optional<ProposalList> getProposalListPaginated(long offset, long limit) {
    try {
      return serverApi.getProposalListPaginated(offset, limit);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }


  public Optional<ExchangeList> getExchangeListPaginated(long offset, long limit) {
    try {
      return serverApi.getExchangeListPaginated(offset, limit);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }


  public Optional<NodeList> listNodes() {
    try {
      return serverApi.listNodes();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public GrpcAPI.NumberMessage getTotalTransaction() {
    return serverApi.getTotalTransaction();
  }

  public GrpcAPI.NumberMessage getNextMaintenanceTime() {
    return serverApi.getNextMaintenanceTime();
  }

  public boolean updateAccount(byte[] accountNameBytes)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateAccount failed, Please login first !!");
      return false;
    }

    return serverApi.updateAccount(accountNameBytes);
  }

  public boolean setAccountId(byte[] accountIdBytes)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: setAccount failed, Please login first !!");
      return false;
    }

    return serverApi.setAccountId(accountIdBytes);
  }


  public boolean updateAsset(byte[] description, byte[] url, long newLimit,
    long newPublicLimit) throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateAsset failed, Please login first !!");
      return false;
    }

    return serverApi.updateAsset(description, url, newLimit, newPublicLimit);
  }

  public boolean freezeBalance(long frozen_balance, long frozen_duration, int resourceCode,
    String receiverAddress)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: freezeBalance failed, Please login first !!");
      return false;
    }

    return serverApi.freezeBalance(frozen_balance, frozen_duration, resourceCode, receiverAddress);
  }

  public boolean buyStorage(long quantity)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: buyStorage failed, Please login first !!");
      return false;
    }

    return serverApi.buyStorage(quantity);
  }

  public boolean buyStorageBytes(long bytes)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: buyStorageBytes failed, Please login first !!");
      return false;
    }

    return serverApi.buyStorageBytes(bytes);
  }

  public boolean sellStorage(long storageBytes)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: sellStorage failed, Please login first !!");
      return false;
    }

    TransactionResponse resp =  serverApi.sellStorage(storageBytes);
    printResponseInfo(resp);
    return resp.result;
  }


  public boolean unfreezeBalance(int resourceCode, String receiverAddress)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeBalance failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.unfreezeBalance(resourceCode, receiverAddress);
    printResponseInfo(resp);
    return resp.result;
  }


  public boolean unfreezeAsset() throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: unfreezeAsset failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.unfreezeAsset();
    printResponseInfo(resp);
    return resp.result;
  }

  public boolean withdrawBalance() throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: withdrawBalance failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.withdrawBalance();
    printResponseInfo(resp);
    return resp.result;
  }

  public boolean createProposal(HashMap<Long, Long> parametersMap)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createProposal failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.createProposal(parametersMap);
    printResponseInfo(resp);
    return resp.result;
  }

  public boolean sideChainCreateProposal(HashMap<Long, String> parametersMap)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createProposal failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.sideChainCreateProposal(parametersMap);
    printResponseInfo(resp);
    return resp.result;
  }


  public Optional<ProposalList> getProposalsList() {
    try {
      return serverApi.listProposals();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<SideChainProposalList> sideChainGetProposalsList() {
    try {
      return serverApi.sideChainListProposals();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<Proposal> getProposals(String id) {
    try {
      return serverApi.getProposal(id);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ExchangeList> getExchangeList() {
    try {
      return serverApi.listExchanges();
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<Exchange> getExchange(String id) {
    try {
      return serverApi.getExchange(id);
    } catch (Exception ex) {
      ex.printStackTrace();
      return Optional.empty();
    }
  }

  public Optional<ChainParameters> getChainParameters() {
    try {
      return serverApi.getChainParameters();
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

    TransactionResponse resp = serverApi.approveProposal(id, is_add_approval);
    printResponseInfo(resp);
    return resp.result;
  }

  public boolean deleteProposal(long id)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: deleteProposal failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.deleteProposal(id);

    return resp.result;
  }

  public boolean exchangeCreate(byte[] firstTokenId, long firstTokenBalance,
    byte[] secondTokenId, long secondTokenBalance)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeCreate failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.exchangeCreate(firstTokenId, firstTokenBalance,
      secondTokenId, secondTokenBalance);

    printResponseInfo(resp);
    return resp.result;
  }

  public boolean exchangeInject(long exchangeId, byte[] tokenId, long quant)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeInject failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.exchangeInject(exchangeId, tokenId, quant);
    return resp.result;
  }

  public boolean exchangeWithdraw(long exchangeId, byte[] tokenId, long quant)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeWithdraw failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.exchangeWithdraw(exchangeId, tokenId, quant);
    printResponseInfo(resp);
    return resp.result;
  }

  public boolean exchangeTransaction(long exchangeId, byte[] tokenId, long quant, long expected)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: exchangeTransaction failed, Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.exchangeTransaction(exchangeId, tokenId, quant, expected);
    printResponseInfo(resp);
    return resp.result;
  }

  public boolean updateSetting(byte[] contractAddress, long consumeUserResourcePercent)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateSetting failed,  Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.updateSetting(contractAddress, consumeUserResourcePercent);
    printResponseInfo(resp);

    return resp.result;
  }

  public boolean updateEnergyLimit(byte[] contractAddress, long originEnergyLimit)
    throws CipherException, IOException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: updateSetting failed,  Please login first !!");
      return false;
    }
    TransactionResponse resp = serverApi.updateEnergyLimit(contractAddress, originEnergyLimit);
    printResponseInfo(resp);
    return resp.result;
  }

  public void printResponseInfo(TransactionResponse resp) {
    System.out.println("Transaction response information:" );
    if(resp.getResponseType() == ResponseType.TRANSACTION_NORMAL) {
      System.out.println("response code is: " + resp.getRespCode());
      System.out.println("response result is: " + resp.getResult());
      System.out.println("response message is: " + resp.getMessage());
      System.out.println("response transaction id is: " + resp.getTrxId());
    } else if(resp.getResponseType() == ResponseType.TRANSACTION_CONSTANT) {
      System.out.println("response constantCode is: " + resp.getConstantResult());
      System.out.println("response constantResult is: " + resp.getConstantResult());
      System.out.println("response transaction id is: " + resp.getTrxId());
    } else {
      System.out.println("!!! unknown response type !!!");
    }
  }

  public boolean deployContract(String name, String abiStr, String codeStr,
    long feeLimit, long value, long consumeUserResourcePercent, long originEnergyLimit,
    long tokenValue, String tokenId, String libraryAddressPair, String compilerVersion) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: createContract failed,  Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.deployContract(name, abiStr, codeStr, feeLimit, value,
        consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId, libraryAddressPair, compilerVersion);
    printResponseInfo(resp);
    if(resp.getResult()) {
      Optional<Transaction>  txOp = serverApi.getTransactionById(resp.getTrxId());
      if (txOp.isPresent()) {
        Transaction tx = txOp.get();
        byte[] contractAddress = serverApi.generateContractAddress(tx);
        System.out.println("Your smart contract address will be: " + ServerApi.encode58Check(contractAddress));
      } else {
        logger.info("getTransactionById " + resp.getTrxId() + " failed !!");
      }
    }

    return resp.getResult();
  }

  public boolean callContract(byte[] contractAddress, long callValue, byte[] data, long feeLimit,
    long tokenValue, String tokenId)  {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: callContract failed,  Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.triggerContract(contractAddress, callValue, data, feeLimit, tokenValue, tokenId);
    printResponseInfo(resp);

    return !org.apache.commons.lang3.StringUtils.isEmpty(resp.getTrxId());
  }

  public boolean callContractAndCheck(byte[] contractAddress, long callValue, byte[] data, long feeLimit,
                              long tokenValue, String tokenId) {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: callContractAndCheck failed,  Please login first !!");
      return false;
    }

    TransactionResponse resp = serverApi.triggerContract(contractAddress, callValue, data, feeLimit, tokenValue, tokenId);
    printResponseInfo(resp);

    String trxId = resp.getTrxId();
    if (org.apache.commons.lang3.StringUtils.isEmpty(trxId)) {
      return false;
    }

    return serverApi.checkTxInfo(trxId);
  }

  public boolean accountPermissionUpdate(byte[] ownerAddress, String permission)
    throws IOException, CipherException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: accountPermissionUpdate failed,  Please login first !!");
      return false;
    }
    TransactionResponse resp = serverApi.accountPermissionUpdate(ownerAddress, permission);
    printResponseInfo(resp);
    return resp.result;
  }


  public Transaction addTransactionSign(Transaction transaction)
    throws IOException, CipherException, CancelException {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: addTransactionSign failed,  Please login first !!");
      return null;
    }
    return serverApi.addTransactionSign(transaction);
  }



  public byte[] sideSignTokenData(String tokenAddress, String value) {

    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: addTransactionSign failed,  Please login first !!");
      return null;
    }

    return null; //TODO
  }

  public byte[] getSideTokenAddress(String mainAddress) throws EncodingException {

    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: addTransactionSign failed,  Please login first !!");
      return null;
    }

    return serverApi.getSideTokenAddress(mainAddress);
  }

  public String calcMaincontractAddress(String trxHash)
  {
    byte[] ownerAddress = serverApi.getAddress();
    // get tx hash
    byte[] txRawDataHash = Hex.decode(trxHash);

    // combine
    byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
    System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
    System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

    return ServerApi.encode58Check(Hash.sha3omit12(combined));
  }

  public void sideGetMappingAddress(byte[] sideGateway, String mainContractAddress)
          throws  EncodingException {

    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: sideGetMappingAddress failed,  Please login first !!");
      return ;
    }

    String contractAddress = serverApi.sideGetMappingAddress(sideGateway, mainContractAddress);

    System.out.println("sideContractAddress is " + contractAddress);
  }

  public byte[] getSideGatewayAddress() {
    if (wallet == null || !wallet.isLoginState()) {
      logger.warn("Warning: addTransactionSign failed,  Please login first !!");
      return null;
    }

    return serverApi.getSideGatewayAddress();
  }

}
