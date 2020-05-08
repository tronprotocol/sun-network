package org.tron.sunserver;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.AssetIssueList;
import org.tron.api.GrpcAPI.BlockExtention;
import org.tron.api.GrpcAPI.BlockList;
import org.tron.api.GrpcAPI.BlockListExtention;
import org.tron.api.GrpcAPI.DelegatedResourceList;
import org.tron.api.GrpcAPI.EasyTransferResponse;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.NodeList;
import org.tron.api.GrpcAPI.ProposalList;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.GrpcAPI.TransactionApprovedList;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.GrpcAPI.TransactionListExtention;
import org.tron.api.GrpcAPI.TransactionSignWeight;
import org.tron.api.GrpcAPI.TransactionSignWeight.Result.response_code;
import org.tron.api.GrpcAPI.WitnessList;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.crypto.Sha256Hash;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.AddressUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.MUtil;
import org.tron.common.utils.TransactionUtils;
import org.tron.core.config.Parameter.CommonConstant;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
import org.tron.core.exception.EncodingException;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Contract.BuyStorageBytesContract;
import org.tron.protos.Contract.BuyStorageContract;
import org.tron.protos.Contract.CreateSmartContract;
import org.tron.protos.Contract.FreezeBalanceContract;
import org.tron.protos.Contract.FundInjectContract;
import org.tron.protos.Contract.SellStorageContract;
import org.tron.protos.Contract.UnfreezeAssetContract;
import org.tron.protos.Contract.UnfreezeBalanceContract;
import org.tron.protos.Contract.UpdateEnergyLimitContract;
import org.tron.protos.Contract.UpdateSettingContract;
import org.tron.protos.Contract.WithdrawBalanceContract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.ChainParameters;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Key;
import org.tron.protos.Protocol.Permission;
import org.tron.protos.Protocol.Proposal;
import org.tron.protos.Protocol.SideChainParameters;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionSign;
import org.tron.protos.Protocol.Witness;
import org.tron.sunapi.IServerConfig;
import org.tron.sunapi.response.TransactionResponse;

@Slf4j(topic = "ServerApi")
public class ServerApi {

  private byte[] address;
  private static byte addressPreFixByte = CommonConstant.ADD_PRE_FIX_BYTE_TESTNET;
  private static int rpcVersion = 0;
  private static int sideRpcVersion = 0;
  @Getter
  private static byte[] sideGatewayAddress;
  @Getter
  private static byte[] mainGatewayAddress;

  private static byte[] sideChainId;
  private ECKey priEcKey;

  private boolean isMainChain;
  private GrpcClient rpcCli;
  @Getter
  private IMultiTransactionSign multiTransactionSign;

  public static GrpcClient initMain(IServerConfig config) {

    if ("mainnet".equalsIgnoreCase(config.getMainNetType())) {
      ServerApi.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    } else {
      ServerApi.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_TESTNET);
    }

    rpcVersion = config.getMainRPCVersion();

    if (!AddressUtil.addressValid(config.getMainGatewayAddress())) {
      //TODO logger
    }
    mainGatewayAddress = config.getMainGatewayAddress();

    return new GrpcClient(config.getMainFullNode(), config.getMainSolidityNode());
  }

  public static GrpcClient initSide(IServerConfig config) {
    if ("mainnet".equalsIgnoreCase(config.getSideNetType())) {
      ServerApi.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    } else {
      ServerApi.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_TESTNET);
    }

    sideRpcVersion = config.getSideRPCVersion();

    if (!AddressUtil.addressValid(config.getSideGatewayAddress())) {
      //TODO logger
    }
    sideGatewayAddress = config.getSideGatewayAddress();
    sideChainId = config.getSideChainId();

    return new GrpcClient(config.getSideFullNode(), config.getSideSolidityNode());
  }

  public boolean isMainChain() {
    return isMainChain;
  }

  public static byte getAddressPreFixByte() {
    return addressPreFixByte;
  }

  public static void setAddressPreFixByte(byte addressPreFixByte) {
    ServerApi.addressPreFixByte = addressPreFixByte;
  }

  public static int getRpcVersion() {
    return rpcVersion;
  }

  //  store private key
  public void initPrivateKey(byte[] priKey) {
    this.priEcKey = ECKey.fromPrivate(priKey);

    this.address = priEcKey.getAddress();
  }

  public ServerApi(IServerConfig config, byte[] priKey, boolean isMainChain,
      IMultiTransactionSign multiTransactionSign) {
    this.isMainChain = isMainChain;
    this.multiTransactionSign = multiTransactionSign;
    if (isMainChain) {
      rpcCli = initMain(config);
    } else {
      rpcCli = initSide(config);
    }

    initPrivateKey(priKey);
    this.address = priEcKey.getAddress();
  }

  public ServerApi(IServerConfig config, boolean isMainChain,
      IMultiTransactionSign multiTransactionSign) {
    this.isMainChain = isMainChain;
    this.multiTransactionSign = multiTransactionSign;
    if (isMainChain) {
      rpcCli = initMain(config);
    } else {
      rpcCli = initSide(config);
    }
  }

  public ECKey getEcKey() {
    return priEcKey;
  }

  public byte[] getPrivateBytes(byte[] password) throws CipherException, IOException {
    return priEcKey.getPrivKeyBytes();
  }

  public byte[] getAddress() {
    return address;
  }

  public byte[] getTrc10Address(String trc10) throws EncodingException {
    byte[] input = ByteArray.fromHexString(
        AbiUtil.parseMethod("mainToSideTRC10Map(uint256)", trc10, false));
    Contract.TriggerSmartContract triggerContract = triggerCallContract(getAddress(),
        sideGatewayAddress, 0, input, 0, "0");
    TransactionExtention transactionExtention = rpcCli.triggerContract(triggerContract);  //rpcside
    byte[] trc10Address = transactionExtention.getConstantResult(0).toByteArray();
    return MUtil.convertToTronAddress(new DataWord(trc10Address).getLast20Bytes());
  }

  public byte[] getSideTokenAddress(String mainAddress) throws EncodingException {
    byte[] input = ByteArray.fromHexString(
        AbiUtil.parseMethod("mainToSideContractMap(address)", "\"" + mainAddress + "\"", false));
    Contract.TriggerSmartContract triggerContract = triggerCallContract(getAddress(),
        sideGatewayAddress, 0, input, 0, "0");
    TransactionExtention transactionExtention = rpcCli.triggerContract(triggerContract);  //side
    byte[] sideAddress = transactionExtention.getConstantResult(0).toByteArray();
    return MUtil.convertToTronAddress(new DataWord(sideAddress).getLast20Bytes());
  }


  public String sideGetMappingAddress(byte[] sideGateway, String mainContractAddress)
      throws EncodingException {
    String contractAddress = null;
    byte[] input = ByteArray.fromHexString(
        AbiUtil.parseMethod("mainToSideContractMap(address)", "\"" + mainContractAddress + "\"",
            false));

    Contract.TriggerSmartContract triggerContract = triggerCallContract(getAddress(),
        sideGateway, 0, input, 0, "0");
    TransactionExtention transactionExtention = rpcCli.triggerContract(triggerContract);
    if (transactionExtention != null && !transactionExtention.getConstantResultList().isEmpty()) {
      byte[] data = transactionExtention.getConstantResult(0).toByteArray();
      byte[] address = Arrays.copyOfRange(data, 12, data.length);

      contractAddress = AddressUtil.encode58Check(ByteArray.convertToTronAddress(address));
    }

    return contractAddress;

  }

  public Account queryAccount() {
    return queryAccount(getAddress());
  }

  public Account queryAccount(byte[] address) {
    return rpcCli.queryAccount(address);//call rpc
  }

  public Account queryAccountById(String accountId) {
    return rpcCli.queryAccountById(accountId);
  }

  private byte[] getCurrentChainId() {
    if (isMainChain()) {
      return new byte[0];
    }

    return sideChainId;
  }

  private Transaction signTransaction(Transaction transaction) {
    if (transaction.getRawData().getTimestamp() == 0) {
      transaction = TransactionUtils.setTimestamp(transaction);
    }
    transaction = TransactionUtils.setExpirationTime(transaction);

    if (Objects.isNull(multiTransactionSign)) {
      transaction = TransactionUtils
          .sign(transaction, this.getEcKey(), getCurrentChainId(), isMainChain());
      return transaction;
    }
    transaction = multiTransactionSign.setPermissionId(transaction);
    TransactionSignWeight weight;
    while (true) {
      weight = getTransactionSignWeight(transaction);
      if (weight.getResult().getCode() == response_code.ENOUGH_PERMISSION) {
        break;
      } else if (weight.getResult().getCode() == response_code.NOT_ENOUGH_PERMISSION) {
        Transaction transactionRet = getMultiTransactionSign()
            .addTransactionSign(transaction, weight, getCurrentChainId());
        if (Objects.isNull(transactionRet)) {
          return null;
        }
        transaction = transactionRet;
      } else {
        return null;
      }
    }
    return transaction;
  }

  private TransactionResponse processTransactionExt2(TransactionExtention transactionExtention) {
    if (transactionExtention == null) {
      return new TransactionResponse("Transaction extension is null");
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      return new TransactionResponse(ret);
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      return new TransactionResponse("Transaction is empty");
    }

    transaction = signTransaction(transaction);
    if (Objects.isNull(transaction)) {
      logger.info("Sign transaction cancelled");
      return new TransactionResponse("Sign transaction cancelled");
    }
    ByteString txid = ByteString.copyFrom(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();

    Return response = rpcCli.broadcastTransaction(transaction);
    if (response.getResult()) {
      return new TransactionResponse(response,
          ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    }

    return new TransactionResponse(response);
  }

  private TransactionResponse processTransaction(Transaction transaction) {
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      return new TransactionResponse("Transaction is empty");
    }
    transaction = signTransaction(transaction);
    if (Objects.isNull(transaction)) {
      logger.info("Sign transaction cancelled");
      return new TransactionResponse("Sign transaction cancelled");
    }
    GrpcAPI.Return response = rpcCli.broadcastTransaction(transaction);

    return new TransactionResponse(response);
  }

  //Warning: do not invoke this interface provided by others.
  public TransactionExtention signTransactionByApi2(Transaction transaction,
      byte[] privateKey) throws CancelException {
    transaction = TransactionUtils.setExpirationTime(transaction);
    transaction = getMultiTransactionSign().setPermissionId(transaction);
    TransactionSign.Builder builder = TransactionSign.newBuilder();
    builder.setPrivateKey(ByteString.copyFrom(privateKey));
    builder.setTransaction(transaction);
    return rpcCli.signTransaction2(builder.build());
  }

  //Warning: do not invoke this interface provided by others.
  public TransactionExtention addSignByApi(Transaction transaction,
      byte[] privateKey) throws CancelException {
    transaction = TransactionUtils.setExpirationTime(transaction);
    transaction = getMultiTransactionSign().setPermissionId(transaction);
    TransactionSign.Builder builder = TransactionSign.newBuilder();
    builder.setPrivateKey(ByteString.copyFrom(privateKey));
    builder.setTransaction(transaction);
    return rpcCli.addSign(builder.build());
  }

  public TransactionSignWeight getTransactionSignWeight(Transaction transaction) {
    return rpcCli.getTransactionSignWeight(transaction);
  }

  public TransactionApprovedList getTransactionApprovedList(Transaction transaction) {
    return rpcCli.getTransactionApprovedList(transaction);
  }

  //Warning: do not invoke this interface provided by others.
  public byte[] createAdresss(byte[] passPhrase) {
    return rpcCli.createAdresss(passPhrase);
  }

  //Warning: do not invoke this interface provided by others.
  public EasyTransferResponse easyTransfer(byte[] passPhrase, byte[] toAddress,
      long amount) {
    return rpcCli.easyTransfer(passPhrase, toAddress, amount);
  }

  //Warning: do not invoke this interface provided by others.
  public EasyTransferResponse easyTransferByPrivate(byte[] privateKey, byte[] toAddress,
      long amount) {
    return rpcCli.easyTransferByPrivate(privateKey, toAddress, amount);
  }

  //Warning: do not invoke this interface provided by others.
  public EasyTransferResponse easyTransferAsset(byte[] passPhrase, byte[] toAddress,
      String assetId, long amount) {
    return rpcCli.easyTransferAsset(passPhrase, toAddress, assetId, amount);
  }

  //Warning: do not invoke this interface provided by others.
  public EasyTransferResponse easyTransferAssetByPrivate(byte[] privateKey,
      byte[] toAddress, String assetId, long amount) {
    return rpcCli.easyTransferAssetByPrivate(privateKey, toAddress, assetId, amount);
  }

  public TransactionResponse sendCoin(byte[] to, long amount) {
    byte[] owner = getAddress();
    Contract.TransferContract contract = createTransferContract(to, owner, amount);

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);
    return processTransactionExt2(transactionExtention);

  }

  public TransactionResponse updateAccount(byte[] accountNameBytes) {
    byte[] owner = getAddress();
    Contract.AccountUpdateContract contract = createAccountUpdateContract(accountNameBytes, owner);

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);
    return processTransactionExt2(transactionExtention);

  }

  public TransactionResponse setAccountId(byte[] accountIdBytes) {
    byte[] owner = getAddress();
    Contract.SetAccountIdContract contract = createSetAccountIdContract(accountIdBytes, owner);
    Transaction transaction = rpcCli.createTransaction(contract);
    return processTransaction(transaction);
  }


  public TransactionResponse updateAsset(byte[] description, byte[] url, long newLimit,
      long newPublicLimit) {
    byte[] owner = getAddress();
    Contract.UpdateAssetContract contract
        = createUpdateAssetContract(owner, description, url, newLimit, newPublicLimit);

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse transferAsset(byte[] to, byte[] assertName, long amount) {
    byte[] owner = getAddress();
    Contract.TransferAssetContract contract = createTransferAssetContract(to, assertName, owner,
        amount);

    TransactionExtention transactionExtention = rpcCli.createTransferAssetTransaction2(contract);
    return processTransactionExt2(transactionExtention);

  }

  public TransactionResponse participateAssetIssue(byte[] to, byte[] assertName, long amount) {
    byte[] owner = getAddress();
    Contract.ParticipateAssetIssueContract contract = participateAssetIssueContract(to, assertName,
        owner, amount);

    TransactionExtention transactionExtention = rpcCli
        .createParticipateAssetIssueTransaction2(contract);
    return processTransactionExt2(transactionExtention);

  }

  public boolean broadcastTransaction(byte[] transactionBytes)
      throws InvalidProtocolBufferException {
    Transaction transaction = Transaction.parseFrom(transactionBytes);
    GrpcAPI.Return response = rpcCli.broadcastTransaction(transaction);
    return response.getResult();
  }

  public TransactionResponse broadcastTransaction(Transaction transaction) {
    GrpcAPI.Return response = rpcCli.broadcastTransaction(transaction);

    return new TransactionResponse(response);
  }

  public TransactionResponse createAssetIssue(Contract.AssetIssueContract contract) {
    TransactionExtention transactionExtention = rpcCli.createAssetIssue2(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse createAccount(byte[] address) {
    byte[] owner = getAddress();
    Contract.AccountCreateContract contract = createAccountCreateContract(owner, address);

    TransactionExtention transactionExtention = rpcCli.createAccount2(contract);
    return processTransactionExt2(transactionExtention);
  }

  //Warning: do not invoke this interface provided by others.
  public AddressPrKeyPairMessage generateAddress() {
    EmptyMessage.Builder builder = EmptyMessage.newBuilder();
    return rpcCli.generateAddress(builder.build());
  }

  public TransactionResponse createWitness(byte[] url)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.WitnessCreateContract contract = createWitnessCreateContract(owner, url);

    TransactionExtention transactionExtention = rpcCli.createWitness2(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse updateWitness(byte[] url)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.WitnessUpdateContract contract = createWitnessUpdateContract(owner, url);
    TransactionExtention transactionExtention = rpcCli.updateWitness2(contract);
    return processTransactionExt2(transactionExtention);
  }

  public Block getBlock(long blockNum) {
    return rpcCli.getBlock(blockNum);
  }

  public BlockExtention getBlock2(long blockNum) {
    return rpcCli.getBlock2(blockNum);
  }

  public long getTransactionCountByBlockNum(long blockNum) {
    return rpcCli.getTransactionCountByBlockNum(blockNum);
  }

  public TransactionResponse voteWitness(HashMap<String, String> witness) {
    byte[] owner = getAddress();
    Contract.VoteWitnessContract contract = createVoteWitnessContract(owner, witness);

    TransactionExtention transactionExtention = rpcCli.voteWitnessAccount2(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.TransferContract createTransferContract(byte[] to, byte[] owner,
      long amount) {
    Contract.TransferContract.Builder builder = Contract.TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    return builder.build();
  }

  public static Contract.TransferAssetContract createTransferAssetContract(byte[] to,
      byte[] assertName, byte[] owner,
      long amount) {
    Contract.TransferAssetContract.Builder builder = Contract.TransferAssetContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsName = ByteString.copyFrom(assertName);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setAssetName(bsName);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    return builder.build();
  }

  public static Contract.ParticipateAssetIssueContract participateAssetIssueContract(byte[] to,
      byte[] assertName, byte[] owner,
      long amount) {
    Contract.ParticipateAssetIssueContract.Builder builder = Contract.ParticipateAssetIssueContract
        .newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsName = ByteString.copyFrom(assertName);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setAssetName(bsName);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    return builder.build();
  }

  public static Contract.AccountUpdateContract createAccountUpdateContract(byte[] accountName,
      byte[] address) {
    Contract.AccountUpdateContract.Builder builder = Contract.AccountUpdateContract.newBuilder();
    ByteString basAddreess = ByteString.copyFrom(address);
    ByteString bsAccountName = ByteString.copyFrom(accountName);
    builder.setAccountName(bsAccountName);
    builder.setOwnerAddress(basAddreess);

    return builder.build();
  }

  public static Contract.SetAccountIdContract createSetAccountIdContract(byte[] accountId,
      byte[] address) {
    Contract.SetAccountIdContract.Builder builder = Contract.SetAccountIdContract.newBuilder();
    ByteString bsAddress = ByteString.copyFrom(address);
    ByteString bsAccountId = ByteString.copyFrom(accountId);
    builder.setAccountId(bsAccountId);
    builder.setOwnerAddress(bsAddress);

    return builder.build();
  }


  public static Contract.UpdateAssetContract createUpdateAssetContract(
      byte[] address,
      byte[] description,
      byte[] url,
      long newLimit,
      long newPublicLimit
  ) {
    Contract.UpdateAssetContract.Builder builder =
        Contract.UpdateAssetContract.newBuilder();
    ByteString basAddreess = ByteString.copyFrom(address);
    builder.setDescription(ByteString.copyFrom(description));
    builder.setUrl(ByteString.copyFrom(url));
    builder.setNewLimit(newLimit);
    builder.setNewPublicLimit(newPublicLimit);
    builder.setOwnerAddress(basAddreess);

    return builder.build();
  }

  public static Contract.AccountCreateContract createAccountCreateContract(byte[] owner,
      byte[] address) {
    Contract.AccountCreateContract.Builder builder = Contract.AccountCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setAccountAddress(ByteString.copyFrom(address));

    return builder.build();
  }

  public static Contract.WitnessCreateContract createWitnessCreateContract(byte[] owner,
      byte[] url) {
    Contract.WitnessCreateContract.Builder builder = Contract.WitnessCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setUrl(ByteString.copyFrom(url));

    return builder.build();
  }

  public static Contract.WitnessUpdateContract createWitnessUpdateContract(byte[] owner,
      byte[] url) {
    Contract.WitnessUpdateContract.Builder builder = Contract.WitnessUpdateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setUpdateUrl(ByteString.copyFrom(url));

    return builder.build();
  }

  public static Contract.VoteWitnessContract createVoteWitnessContract(byte[] owner,
      HashMap<String, String> witness) {
    Contract.VoteWitnessContract.Builder builder = Contract.VoteWitnessContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    for (String addressBase58 : witness.keySet()) {
      String value = witness.get(addressBase58);
      long count = Long.parseLong(value);
      Contract.VoteWitnessContract.Vote.Builder voteBuilder = Contract.VoteWitnessContract.Vote
          .newBuilder();
      byte[] address = AddressUtil.decodeFromBase58Check(addressBase58);
      if (address == null) {
        continue;
      }
      voteBuilder.setVoteAddress(ByteString.copyFrom(address));
      voteBuilder.setVoteCount(count);
      builder.addVotes(voteBuilder.build());
    }

    return builder.build();
  }

  public Optional<WitnessList> listWitnesses() {
    Optional<WitnessList> result = rpcCli.listWitnesses();
    if (result.isPresent()) {
      WitnessList witnessList = result.get();
      List<Witness> list = witnessList.getWitnessesList();
      List<Witness> newList = new ArrayList<>();
      newList.addAll(list);
      newList.sort(new Comparator<Witness>() {
        @Override
        public int compare(Witness o1, Witness o2) {
          return Long.compare(o2.getVoteCount(), o1.getVoteCount());
        }
      });
      WitnessList.Builder builder = WitnessList.newBuilder();
      newList.forEach(witness -> builder.addWitnesses(witness));
      result = Optional.of(builder.build());
    }
    return result;
  }

  public Optional<AssetIssueList> getAssetIssueList() {
    return rpcCli.getAssetIssueList();
  }

  public Optional<AssetIssueList> getAssetIssueList(long offset, long limit) {
    return rpcCli.getAssetIssueList(offset, limit);
  }

  public Optional<ProposalList> getProposalListPaginated(long offset, long limit) {
    return rpcCli.getProposalListPaginated(offset, limit);
  }

  public Optional<ExchangeList> getExchangeListPaginated(long offset, long limit) {
    return rpcCli.getExchangeListPaginated(offset, limit);
  }


  public Optional<NodeList> listNodes() {
    return rpcCli.listNodes();
  }

  public Optional<AssetIssueList> getAssetIssueByAccount(byte[] address) {
    return rpcCli.getAssetIssueByAccount(address);
  }

  public AccountNetMessage getAccountNet(byte[] address) {
    return rpcCli.getAccountNet(address);
  }

  public AccountResourceMessage getAccountResource(byte[] address) {
    return rpcCli.getAccountResource(address);
  }

  public AssetIssueContract getAssetIssueByName(String assetName) {
    return rpcCli.getAssetIssueByName(assetName);
  }

  public Optional<AssetIssueList> getAssetIssueListByName(String assetName) {
    return rpcCli.getAssetIssueListByName(assetName);
  }

  public AssetIssueContract getAssetIssueById(String assetId) {
    return rpcCli.getAssetIssueById(assetId);
  }

  public GrpcAPI.NumberMessage getTotalTransaction() {
    return rpcCli.getTotalTransaction();
  }

  public GrpcAPI.NumberMessage getNextMaintenanceTime() {
    return rpcCli.getNextMaintenanceTime();
  }

  public Optional<TransactionListExtention> getTransactionsFromThis2(byte[] address,
      int offset,
      int limit) {
    return rpcCli.getTransactionsFromThis2(address, offset, limit);
  }
//  public static GrpcAPI.NumberMessage getTransactionsFromThisCount(byte[] address) {
//    return rpcCli.getTransactionsFromThisCount(address);
//  }


  public Optional<TransactionListExtention> getTransactionsToThis2(byte[] address,
      int offset,
      int limit) {
    return rpcCli.getTransactionsToThis2(address, offset, limit);
  }
//  public static GrpcAPI.NumberMessage getTransactionsToThisCount(byte[] address) {
//    return rpcCli.getTransactionsToThisCount(address);
//  }

  public Optional<Transaction> getTransactionById(String txID) {
    return rpcCli.getTransactionById(txID);
  }

  public Optional<TransactionInfo> getTransactionInfoById(String txID) {
    return rpcCli.getTransactionInfoById(txID);
  }

  public TransactionResponse freezeBalance(long frozen_balance, long frozen_duration,
      int resourceCode,
      String receiverAddress) {
    Contract.FreezeBalanceContract contract = createFreezeBalanceContract(frozen_balance,
        frozen_duration, resourceCode, receiverAddress);

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse fundInject(long amount) {
    Contract.FundInjectContract contract = createFundInjectContract(amount);
    TransactionExtention transactionExtention = rpcCli.createTransaction(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse buyStorage(long quantity) {
    Contract.BuyStorageContract contract = createBuyStorageContract(quantity);
    TransactionExtention transactionExtention = rpcCli.createTransaction(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse buyStorageBytes(long bytes) {
    Contract.BuyStorageBytesContract contract = createBuyStorageBytesContract(bytes);
    TransactionExtention transactionExtention = rpcCli.createTransaction(contract);
    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse sellStorage(long storageBytes) {
    Contract.SellStorageContract contract = createSellStorageContract(storageBytes);
    TransactionExtention transactionExtention = rpcCli.createTransaction(contract);
    return processTransactionExt2(transactionExtention);

  }

  private FreezeBalanceContract createFreezeBalanceContract(long frozen_balance,
      long frozen_duration, int resourceCode, String receiverAddress) {
    byte[] address = getAddress();
    Contract.FreezeBalanceContract.Builder builder = Contract.FreezeBalanceContract.newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setFrozenBalance(frozen_balance)
        .setFrozenDuration(frozen_duration).setResourceValue(resourceCode);

    if (receiverAddress != null && !receiverAddress.equals("")) {
      ByteString receiverAddressBytes = ByteString.copyFrom(
          Objects.requireNonNull(AddressUtil.decodeFromBase58Check(receiverAddress)));
      builder.setReceiverAddress(receiverAddressBytes);
    }
    return builder.build();
  }

  private FundInjectContract createFundInjectContract(long amount) {
    byte[] address = getAddress();
    Contract.FundInjectContract.Builder builder = Contract.FundInjectContract.newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setAmount(amount);
    return builder.build();
  }

  private BuyStorageContract createBuyStorageContract(long quantity) {
    byte[] address = getAddress();
    Contract.BuyStorageContract.Builder builder = Contract.BuyStorageContract.newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setQuant(quantity);

    return builder.build();
  }

  private BuyStorageBytesContract createBuyStorageBytesContract(long bytes) {
    byte[] address = getAddress();
    Contract.BuyStorageBytesContract.Builder builder = Contract.BuyStorageBytesContract
        .newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setBytes(bytes);

    return builder.build();
  }

  private SellStorageContract createSellStorageContract(long storageBytes) {
    byte[] address = getAddress();
    Contract.SellStorageContract.Builder builder = Contract.SellStorageContract.newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setStorageBytes(storageBytes);

    return builder.build();
  }

  public TransactionResponse unfreezeBalance(int resourceCode, String receiverAddress) {
    UnfreezeBalanceContract contract = createUnfreezeBalanceContract(resourceCode, receiverAddress);

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);

    return processTransactionExt2(transactionExtention);
  }


  private UnfreezeBalanceContract createUnfreezeBalanceContract(int resourceCode,
      String receiverAddress) {
    byte[] address = getAddress();
    Contract.UnfreezeBalanceContract.Builder builder = Contract.UnfreezeBalanceContract
        .newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddreess).setResourceValue(resourceCode);

    if (receiverAddress != null && !receiverAddress.equals("")) {
      ByteString receiverAddressBytes = ByteString.copyFrom(
          Objects.requireNonNull(AddressUtil.decodeFromBase58Check(receiverAddress)));
      builder.setReceiverAddress(receiverAddressBytes);
    }

    return builder.build();
  }

  public TransactionResponse unfreezeAsset() throws CipherException, IOException, CancelException {
    Contract.UnfreezeAssetContract contract = createUnfreezeAssetContract();

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);
    return processTransactionExt2(transactionExtention);
  }

  private UnfreezeAssetContract createUnfreezeAssetContract() {
    byte[] address = getAddress();
    Contract.UnfreezeAssetContract.Builder builder = Contract.UnfreezeAssetContract
        .newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddreess);
    return builder.build();
  }

  public TransactionResponse withdrawBalance() {
    Contract.WithdrawBalanceContract contract = createWithdrawBalanceContract();

    TransactionExtention transactionExtention = rpcCli.createTransaction2(contract);
    return processTransactionExt2(transactionExtention);
  }

  private WithdrawBalanceContract createWithdrawBalanceContract() {
    byte[] address = getAddress();
    Contract.WithdrawBalanceContract.Builder builder = Contract.WithdrawBalanceContract
        .newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddreess);

    return builder.build();
  }

  public Optional<Block> getBlockById(String blockID) {
    return rpcCli.getBlockById(blockID);
  }

  public Optional<BlockList> getBlockByLimitNext(long start, long end) {
    return rpcCli.getBlockByLimitNext(start, end);
  }

  public Optional<BlockListExtention> getBlockByLimitNext2(long start, long end) {
    return rpcCli.getBlockByLimitNext2(start, end);
  }

  public Optional<BlockList> getBlockByLatestNum(long num) {
    return rpcCli.getBlockByLatestNum(num);
  }

  public Optional<BlockListExtention> getBlockByLatestNum2(long num) {
    return rpcCli.getBlockByLatestNum2(num);
  }

  public TransactionResponse createProposal(HashMap<Long, Long> parametersMap)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ProposalCreateContract contract = createProposalCreateContract(owner, parametersMap);
    TransactionExtention transactionExtention = rpcCli.proposalCreate(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.SideChainProposalCreateContract sideCreateProposalCreateContract(
      byte[] owner,
      HashMap<Long, String> parametersMap) {
    Contract.SideChainProposalCreateContract.Builder builder = Contract.SideChainProposalCreateContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.putAllParameters(parametersMap);
    return builder.build();
  }

  public TransactionResponse sideChainCreateProposal(HashMap<Long, String> parametersMap)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.SideChainProposalCreateContract contract = sideCreateProposalCreateContract(owner,
        parametersMap);
    TransactionExtention transactionExtention = rpcCli.SideProposalCreate(contract);
    return processTransactionExt2(transactionExtention);
  }

  public Optional<ProposalList> listProposals() {
    return rpcCli.listProposals();
  }

  public Optional<SideChainProposalList> sideChainListProposals() {
    return rpcCli.sideChianListProposals();
  }

  public Optional<Proposal> getProposal(String id) {
    return rpcCli.getProposal(id);
  }

  public Optional<DelegatedResourceList> getDelegatedResource(String fromAddress,
      String toAddress) {
    return rpcCli.getDelegatedResource(fromAddress, toAddress);
  }

  public Optional<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(
      String address) {
    return rpcCli.getDelegatedResourceAccountIndex(address);
  }

  public Optional<ExchangeList> listExchanges() {
    return rpcCli.listExchanges();
  }

  public Optional<Exchange> getExchange(String id) {
    return rpcCli.getExchange(id);
  }

  public Optional<ChainParameters> getChainParameters() {
    return rpcCli.getChainParameters();
  }

  public Optional<SideChainParameters> getSideChainParameters() {
    return rpcCli.getSideChainParameters();
  }


  public static Contract.ProposalCreateContract createProposalCreateContract(byte[] owner,
      HashMap<Long, Long> parametersMap) {
    Contract.ProposalCreateContract.Builder builder = Contract.ProposalCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.putAllParameters(parametersMap);
    return builder.build();
  }

  public TransactionResponse approveProposal(long id, boolean is_add_approval)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ProposalApproveContract contract = createProposalApproveContract(owner, id,
        is_add_approval);
    TransactionExtention transactionExtention = rpcCli.proposalApprove(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.ProposalApproveContract createProposalApproveContract(byte[] owner,
      long id, boolean is_add_approval) {
    Contract.ProposalApproveContract.Builder builder = Contract.ProposalApproveContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setProposalId(id);
    builder.setIsAddApproval(is_add_approval);
    return builder.build();
  }

  public TransactionResponse deleteProposal(long id)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ProposalDeleteContract contract = createProposalDeleteContract(owner, id);
    TransactionExtention transactionExtention = rpcCli.proposalDelete(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.ProposalDeleteContract createProposalDeleteContract(byte[] owner,
      long id) {
    Contract.ProposalDeleteContract.Builder builder = Contract.ProposalDeleteContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setProposalId(id);
    return builder.build();
  }

  public TransactionResponse exchangeCreate(byte[] firstTokenId, long firstTokenBalance,
      byte[] secondTokenId, long secondTokenBalance)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ExchangeCreateContract contract = createExchangeCreateContract(owner, firstTokenId,
        firstTokenBalance, secondTokenId, secondTokenBalance);
    TransactionExtention transactionExtention = rpcCli.exchangeCreate(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.ExchangeCreateContract createExchangeCreateContract(byte[] owner,
      byte[] firstTokenId, long firstTokenBalance,
      byte[] secondTokenId, long secondTokenBalance) {
    Contract.ExchangeCreateContract.Builder builder = Contract.ExchangeCreateContract.newBuilder();
    builder
        .setOwnerAddress(ByteString.copyFrom(owner))
        .setFirstTokenId(ByteString.copyFrom(firstTokenId))
        .setFirstTokenBalance(firstTokenBalance)
        .setSecondTokenId(ByteString.copyFrom(secondTokenId))
        .setSecondTokenBalance(secondTokenBalance);
    return builder.build();
  }

  public TransactionResponse exchangeInject(long exchangeId, byte[] tokenId, long quant)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ExchangeInjectContract contract = createExchangeInjectContract(owner, exchangeId,
        tokenId, quant);
    TransactionExtention transactionExtention = rpcCli.exchangeInject(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.ExchangeInjectContract createExchangeInjectContract(byte[] owner,
      long exchangeId, byte[] tokenId, long quant) {
    Contract.ExchangeInjectContract.Builder builder = Contract.ExchangeInjectContract.newBuilder();
    builder
        .setOwnerAddress(ByteString.copyFrom(owner))
        .setExchangeId(exchangeId)
        .setTokenId(ByteString.copyFrom(tokenId))
        .setQuant(quant);
    return builder.build();
  }

  public TransactionResponse exchangeWithdraw(long exchangeId, byte[] tokenId, long quant)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ExchangeWithdrawContract contract = createExchangeWithdrawContract(owner, exchangeId,
        tokenId, quant);
    TransactionExtention transactionExtention = rpcCli.exchangeWithdraw(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.ExchangeWithdrawContract createExchangeWithdrawContract(byte[] owner,
      long exchangeId, byte[] tokenId, long quant) {
    Contract.ExchangeWithdrawContract.Builder builder = Contract.ExchangeWithdrawContract
        .newBuilder();
    builder
        .setOwnerAddress(ByteString.copyFrom(owner))
        .setExchangeId(exchangeId)
        .setTokenId(ByteString.copyFrom(tokenId))
        .setQuant(quant);
    return builder.build();
  }

  public TransactionResponse exchangeTransaction(long exchangeId, byte[] tokenId, long quant,
      long expected)
      throws CipherException, IOException, CancelException {
    byte[] owner = getAddress();
    Contract.ExchangeTransactionContract contract = createExchangeTransactionContract(owner,
        exchangeId, tokenId, quant, expected);
    TransactionExtention transactionExtention = rpcCli.exchangeTransaction(contract);
    return processTransactionExt2(transactionExtention);
  }

  public static Contract.ExchangeTransactionContract createExchangeTransactionContract(byte[] owner,
      long exchangeId, byte[] tokenId, long quant, long expected) {
    Contract.ExchangeTransactionContract.Builder builder = Contract.ExchangeTransactionContract
        .newBuilder();
    builder
        .setOwnerAddress(ByteString.copyFrom(owner))
        .setExchangeId(exchangeId)
        .setTokenId(ByteString.copyFrom(tokenId))
        .setQuant(quant)
        .setExpected(expected);
    return builder.build();
  }


  public static SmartContract.ABI.Entry.EntryType getEntryType(String type) {
    switch (type) {
      case "constructor":
        return SmartContract.ABI.Entry.EntryType.Constructor;
      case "function":
        return SmartContract.ABI.Entry.EntryType.Function;
      case "event":
        return SmartContract.ABI.Entry.EntryType.Event;
      case "fallback":
        return SmartContract.ABI.Entry.EntryType.Fallback;
      default:
        return SmartContract.ABI.Entry.EntryType.UNRECOGNIZED;
    }
  }

  public static SmartContract.ABI.Entry.StateMutabilityType getStateMutability(
      String stateMutability) {
    switch (stateMutability) {
      case "pure":
        return SmartContract.ABI.Entry.StateMutabilityType.Pure;
      case "view":
        return SmartContract.ABI.Entry.StateMutabilityType.View;
      case "nonpayable":
        return SmartContract.ABI.Entry.StateMutabilityType.Nonpayable;
      case "payable":
        return SmartContract.ABI.Entry.StateMutabilityType.Payable;
      default:
        return SmartContract.ABI.Entry.StateMutabilityType.UNRECOGNIZED;
    }
  }

  public static SmartContract.ABI jsonStr2ABI(String jsonStr) {
    if (jsonStr == null) {
      return null;
    }

    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElementRoot = jsonParser.parse(jsonStr);
    JsonArray jsonRoot = jsonElementRoot.getAsJsonArray();
    SmartContract.ABI.Builder abiBuilder = SmartContract.ABI.newBuilder();
    for (int index = 0; index < jsonRoot.size(); index++) {
      JsonElement abiItem = jsonRoot.get(index);
      boolean anonymous = abiItem.getAsJsonObject().get("anonymous") != null ?
          abiItem.getAsJsonObject().get("anonymous").getAsBoolean() : false;
      boolean constant = abiItem.getAsJsonObject().get("constant") != null ?
          abiItem.getAsJsonObject().get("constant").getAsBoolean() : false;
      String name = abiItem.getAsJsonObject().get("name") != null ?
          abiItem.getAsJsonObject().get("name").getAsString() : null;
      JsonArray inputs = abiItem.getAsJsonObject().get("inputs") != null ?
          abiItem.getAsJsonObject().get("inputs").getAsJsonArray() : null;
      JsonArray outputs = abiItem.getAsJsonObject().get("outputs") != null ?
          abiItem.getAsJsonObject().get("outputs").getAsJsonArray() : null;
      String type = abiItem.getAsJsonObject().get("type") != null ?
          abiItem.getAsJsonObject().get("type").getAsString() : null;
      boolean payable = abiItem.getAsJsonObject().get("payable") != null ?
          abiItem.getAsJsonObject().get("payable").getAsBoolean() : false;
      String stateMutability = abiItem.getAsJsonObject().get("stateMutability") != null ?
          abiItem.getAsJsonObject().get("stateMutability").getAsString() : null;
      if (type == null) {
        logger.error("No type!");
        return null;
      }
      if (!type.equalsIgnoreCase("fallback") && null == inputs) {
        logger.error("No inputs!");
        return null;
      }

      SmartContract.ABI.Entry.Builder entryBuilder = SmartContract.ABI.Entry.newBuilder();
      entryBuilder.setAnonymous(anonymous);
      entryBuilder.setConstant(constant);
      if (name != null) {
        entryBuilder.setName(name);
      }

      /* { inputs : optional } since fallback function not requires inputs*/
      if (null != inputs) {
        for (int j = 0; j < inputs.size(); j++) {
          JsonElement inputItem = inputs.get(j);
          if (inputItem.getAsJsonObject().get("name") == null ||
              inputItem.getAsJsonObject().get("type") == null) {
            logger.error("Input argument invalid due to no name or no type!");
            return null;
          }
          String inputName = inputItem.getAsJsonObject().get("name").getAsString();
          String inputType = inputItem.getAsJsonObject().get("type").getAsString();
          Boolean inputIndexed = false;
          if (inputItem.getAsJsonObject().get("indexed") != null) {
            inputIndexed = Boolean
                .valueOf(inputItem.getAsJsonObject().get("indexed").getAsString());
          }
          SmartContract.ABI.Entry.Param.Builder paramBuilder = SmartContract.ABI.Entry.Param
              .newBuilder();
          paramBuilder.setIndexed(inputIndexed);
          paramBuilder.setName(inputName);
          paramBuilder.setType(inputType);
          entryBuilder.addInputs(paramBuilder.build());
        }
      }

      /* { outputs : optional } */
      if (outputs != null) {
        for (int k = 0; k < outputs.size(); k++) {
          JsonElement outputItem = outputs.get(k);
          if (outputItem.getAsJsonObject().get("name") == null ||
              outputItem.getAsJsonObject().get("type") == null) {
            logger.error("Output argument invalid due to no name or no type!");
            return null;
          }
          String outputName = outputItem.getAsJsonObject().get("name").getAsString();
          String outputType = outputItem.getAsJsonObject().get("type").getAsString();
          Boolean outputIndexed = false;
          if (outputItem.getAsJsonObject().get("indexed") != null) {
            outputIndexed = Boolean
                .valueOf(outputItem.getAsJsonObject().get("indexed").getAsString());
          }
          SmartContract.ABI.Entry.Param.Builder paramBuilder = SmartContract.ABI.Entry.Param
              .newBuilder();
          paramBuilder.setIndexed(outputIndexed);
          paramBuilder.setName(outputName);
          paramBuilder.setType(outputType);
          entryBuilder.addOutputs(paramBuilder.build());
        }
      }

      entryBuilder.setType(getEntryType(type));
      entryBuilder.setPayable(payable);
      if (stateMutability != null) {
        entryBuilder.setStateMutability(getStateMutability(stateMutability));
      }

      abiBuilder.addEntrys(entryBuilder.build());
    }

    return abiBuilder.build();
  }

  public static Contract.UpdateSettingContract createUpdateSettingContract(byte[] owner,
      byte[] contractAddress, long consumeUserResourcePercent) {

    Contract.UpdateSettingContract.Builder builder = Contract.UpdateSettingContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent);
    return builder.build();
  }

  public static Contract.UpdateEnergyLimitContract createUpdateEnergyLimitContract(
      byte[] owner,
      byte[] contractAddress, long originEnergyLimit) {

    Contract.UpdateEnergyLimitContract.Builder builder = Contract.UpdateEnergyLimitContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setOriginEnergyLimit(originEnergyLimit);
    return builder.build();
  }

  public static CreateSmartContract createContractDeployContract(String contractName,
      byte[] address,
      String ABI, String code, long value, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId,
      String libraryAddressPair, String compilerVersion) {
    SmartContract.ABI abi = jsonStr2ABI(ABI);
    if (abi == null) {
      logger.error("abi is null");
      return null;
    }

    SmartContract.Builder builder = SmartContract.newBuilder();
    builder.setName(contractName);
    builder.setOriginAddress(ByteString.copyFrom(address));
    builder.setAbi(abi);
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent)
        .setOriginEnergyLimit(originEnergyLimit);

    if (value != 0) {

      builder.setCallValue(value);
    }
    byte[] byteCode;
    if (null != libraryAddressPair) {
      byteCode = replaceLibraryAddress(code, libraryAddressPair, compilerVersion);
    } else {
      byteCode = Hex.decode(code);
    }

    builder.setBytecode(ByteString.copyFrom(byteCode));
    CreateSmartContract.Builder createSmartContractBuilder = CreateSmartContract.newBuilder();
    createSmartContractBuilder.setOwnerAddress(ByteString.copyFrom(address)).
        setNewContract(builder.build());
    if (!StringUtils.isEmpty(tokenId) && !tokenId.equalsIgnoreCase("#")) {
      createSmartContractBuilder.setCallTokenValue(tokenValue).setTokenId(Long.parseLong(tokenId));
    }
    return createSmartContractBuilder.build();
  }

  private static byte[] replaceLibraryAddress(String code, String libraryAddressPair,
      String compilerVersion) {

    String[] libraryAddressList = libraryAddressPair.split("[,]");

    for (int i = 0; i < libraryAddressList.length; i++) {
      String cur = libraryAddressList[i];

      int lastPosition = cur.lastIndexOf(":");
      if (-1 == lastPosition) {
        throw new RuntimeException("libraryAddress delimit by ':'");
      }
      String libraryName = cur.substring(0, lastPosition);
      String addr = cur.substring(lastPosition + 1);
      String libraryAddressHex;
      try {
        libraryAddressHex = (new String(Hex.encode(AddressUtil.decodeFromBase58Check(addr)),
            "US-ASCII")).substring(2);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);  // now ignore
      }

      String beReplaced;
      if (compilerVersion == null) {
        //old version
        String repeated = new String(new char[40 - libraryName.length() - 2]).replace("\0", "_");
        beReplaced = "__" + libraryName + repeated;
      } else if (compilerVersion.equalsIgnoreCase("v5")) {
        //0.5.4 version
        String libraryNameKeccak256 = ByteArray
            .toHexString(Hash.sha3(ByteArray.fromString(libraryName))).substring(0, 34);
        beReplaced = "__\\$" + libraryNameKeccak256 + "\\$__";
      } else {
        throw new RuntimeException("unknown compiler version.");
      }

      Matcher m = Pattern.compile(beReplaced).matcher(code);
      code = m.replaceAll(libraryAddressHex);
    }

    return Hex.decode(code);
  }

  public static Contract.TriggerSmartContract triggerCallContract(byte[] address,
      byte[] contractAddress,
      long callValue, byte[] data, long tokenValue, String tokenId) {
    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(address));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    return builder.build();
  }

  public static Contract.TriggerSmartContract triggerCallConstantContract(byte[] address,
      byte[] contractAddress, byte[] data) {
    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(address));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    return builder.build();
  }

  public byte[] generateContractAddress(Transaction trx) {

    // get owner address
    // this address should be as same as the onweraddress in trx, DONNOT modify it
    byte[] ownerAddress = getAddress();

    // get tx hash
    byte[] txRawDataHash = Sha256Hash.of(trx.getRawData().toByteArray()).getBytes();

    // combine
    byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
    System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
    System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

    return Hash.sha3omit12(combined);
  }

  public TransactionResponse updateSetting(byte[] contractAddress, long consumeUserResourcePercent)
      throws IOException, CipherException, CancelException {
    byte[] owner = getAddress();
    UpdateSettingContract updateSettingContract = createUpdateSettingContract(owner,
        contractAddress, consumeUserResourcePercent);

    TransactionExtention transactionExtention = rpcCli.updateSetting(updateSettingContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      logger.info("RPC create trx failed!");
      if (transactionExtention != null) {
        logger.info("Code = " + transactionExtention.getResult().getCode());
        logger.info("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return new TransactionResponse("RPC create trx failed!");
    }

    return processTransactionExt2(transactionExtention);

  }

  public TransactionResponse updateEnergyLimit(byte[] contractAddress, long originEnergyLimit)
      throws IOException, CipherException, CancelException {
    byte[] owner = getAddress();
    UpdateEnergyLimitContract updateEnergyLimitContract = createUpdateEnergyLimitContract(
        owner,
        contractAddress, originEnergyLimit);

    TransactionExtention transactionExtention = rpcCli
        .updateEnergyLimit(updateEnergyLimitContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      logger.info("RPC create trx failed!");
      if (transactionExtention != null) {
        logger.info("Code = " + transactionExtention.getResult().getCode());
        logger.info("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return new TransactionResponse("RPC create trx failed!");
    }

    return processTransactionExt2(transactionExtention);

  }

  public TransactionResponse deployContract(String contractName, String ABI, String code,
      long feeLimit, long value, long consumeUserResourcePercent, long originEnergyLimit,
      long tokenValue, String tokenId, String libraryAddressPair, String compilerVersion) {
    byte[] owner = getAddress();
    CreateSmartContract contractDeployContract = createContractDeployContract(contractName, owner,
        ABI, code, value, consumeUserResourcePercent, originEnergyLimit, tokenValue, tokenId,
        libraryAddressPair, compilerVersion);

    TransactionExtention transactionExtention = rpcCli.deployContract(contractDeployContract);
    if (transactionExtention == null) {
      return new TransactionResponse("RPC create trx failed!");
    }

    if (!transactionExtention.getResult().getResult()) {
      return new TransactionResponse(transactionExtention.getResult());
    }

    TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
    Transaction.Builder transBuilder = Transaction.newBuilder();
    Transaction.raw.Builder rawBuilder = transactionExtention.getTransaction().getRawData()
        .toBuilder();
    rawBuilder.setFeeLimit(feeLimit);
    transBuilder.setRawData(rawBuilder);
    for (int i = 0; i < transactionExtention.getTransaction().getSignatureCount(); i++) {
      ByteString s = transactionExtention.getTransaction().getSignature(i);
      transBuilder.setSignature(i, s);
    }
    for (int i = 0; i < transactionExtention.getTransaction().getRetCount(); i++) {
      Result r = transactionExtention.getTransaction().getRet(i);
      transBuilder.setRet(i, r);
    }
    texBuilder.setTransaction(transBuilder);
    texBuilder.setResult(transactionExtention.getResult());
    texBuilder.setTxid(transactionExtention.getTxid());
    transactionExtention = texBuilder.build();

    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse triggerContract(byte[] contractAddress, long callValue, byte[] data,
      long feeLimit,
      long tokenValue, String tokenId) {
    byte[] owner = getAddress();
    Contract.TriggerSmartContract triggerContract = triggerCallContract(owner, contractAddress,
        callValue, data, tokenValue, tokenId);
    TransactionExtention transactionExtention = rpcCli.triggerContract(triggerContract);
    if (transactionExtention == null) {
      return new TransactionResponse("RPC create trx failed!");
    }

    if (!transactionExtention.getResult().getResult()) {
      return new TransactionResponse(transactionExtention.getResult());
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 &&
        transactionExtention.getConstantResult(0) != null &&
        transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();

      //Return ret = transactionExtention.getResult();
      logger.info("message:" + transaction.getRet(0).getRet());
      logger.info(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      logger.info("Result:" + Hex.toHexString(result));
      String trxId = ByteArray.toHexString(transactionExtention.getTxid().toByteArray());

      return new TransactionResponse(true, transaction.getRet(0).getRet(), trxId,
          Hex.toHexString(result));

    }

    TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
    Transaction.Builder transBuilder = Transaction.newBuilder();
    Transaction.raw.Builder rawBuilder = transactionExtention.getTransaction().getRawData()
        .toBuilder();
    rawBuilder.setFeeLimit(feeLimit);
    transBuilder.setRawData(rawBuilder);
    for (int i = 0; i < transactionExtention.getTransaction().getSignatureCount(); i++) {
      ByteString s = transactionExtention.getTransaction().getSignature(i);
      transBuilder.setSignature(i, s);
    }
    for (int i = 0; i < transactionExtention.getTransaction().getRetCount(); i++) {
      Result r = transactionExtention.getTransaction().getRet(i);
      transBuilder.setRet(i, r);
    }
    texBuilder.setTransaction(transBuilder);
    texBuilder.setResult(transactionExtention.getResult());
    texBuilder.setTxid(transactionExtention.getTxid());
    transactionExtention = texBuilder.build();

    return processTransactionExt2(transactionExtention);
  }

  public TransactionResponse triggerConstantContract(byte[] contractAddress, byte[] data,
      long feeLimit) {
    byte[] owner = getAddress();
    Contract.TriggerSmartContract triggerContract = triggerCallConstantContract(owner,
        contractAddress,
        data);
    TransactionExtention transactionExtention = rpcCli.triggerConstantContract(triggerContract);
    if (transactionExtention == null) {
      return new TransactionResponse("RPC create trx failed!");
    }

    if (!transactionExtention.getResult().getResult()) {
      return new TransactionResponse(transactionExtention.getResult());
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 &&
        transactionExtention.getConstantResult(0) != null &&
        transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();

      logger.info("message:" + transaction.getRet(0).getRet());
      logger.info(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      logger.info("Result:" + Hex.toHexString(result));
      String trxId = ByteArray.toHexString(transactionExtention.getTxid().toByteArray());

      return new TransactionResponse(true, transaction.getRet(0).getRet(), trxId,
          Hex.toHexString(result));

    } else {
      return new TransactionResponse("constant result is null");
    }
  }

  public boolean checkTxInfo(String txId) {
    try {
      logger.info("wait 3s for check result. ");
      Thread.sleep(3_000);
      logger.info("trx id: " + txId);
      Optional<TransactionInfo> transactionInfo = rpcCli.getTransactionInfoById(txId);
      TransactionInfo info = transactionInfo.get();
      if (info.getBlockTimeStamp() != 0L) {
        if (info.getResult().equals(TransactionInfo.code.SUCESS)) {
          return true;
        }
      }

      //retry
      int maxRetry = 3;
      for (int i = 0; i < maxRetry; i++) {
        Thread.sleep(2_000);
        logger.info("will retry {} time(s): " + i + 1);
        transactionInfo = rpcCli.getTransactionInfoById(txId);
        info = transactionInfo.get();
        if (info.getBlockTimeStamp() != 0L) {
          if (info.getResult().equals(TransactionInfo.code.SUCESS)) {
            return true;
          }
        }
      }
    } catch (InterruptedException e) {
      logger.error("sleep error" + (e.getMessage()));
      return false;
    }

    return false;
  }

  public SmartContract getContract(byte[] address) {
    return rpcCli.getContract(address);
  }


  public TransactionResponse accountPermissionUpdate(byte[] owner, String permissionJson)
      throws CipherException, IOException, CancelException {
    Contract.AccountPermissionUpdateContract contract = createAccountPermissionContract(owner,
        permissionJson);
    TransactionExtention transactionExtention = rpcCli.accountPermissionUpdate(contract);
    return processTransactionExt2(transactionExtention);
  }

  private Permission json2Permission(JSONObject json) {
    Permission.Builder permissionBuilder = Permission.newBuilder();
    if (json.containsKey("type")) {
      int type = json.getInteger("type");
      permissionBuilder.setTypeValue(type);
    }
    if (json.containsKey("permission_name")) {
      String permission_name = json.getString("permission_name");
      permissionBuilder.setPermissionName(permission_name);
    }
    if (json.containsKey("threshold")) {
      long threshold = json.getLong("threshold");
      permissionBuilder.setThreshold(threshold);
    }
    if (json.containsKey("parent_id")) {
      int parent_id = json.getInteger("parent_id");
      permissionBuilder.setParentId(parent_id);
    }
    if (json.containsKey("operations")) {
      byte[] operations = ByteArray.fromHexString(json.getString("operations"));
      permissionBuilder.setOperations(ByteString.copyFrom(operations));
    }
    if (json.containsKey("keys")) {
      JSONArray keys = json.getJSONArray("keys");
      List<Key> keyList = new ArrayList<>();
      for (int i = 0; i < keys.size(); i++) {
        Key.Builder keyBuilder = Key.newBuilder();
        JSONObject key = keys.getJSONObject(i);
        String address = key.getString("address");
        long weight = key.getLong("weight");
        keyBuilder.setAddress(ByteString.copyFrom(AddressUtil.decodeFromBase58Check(address)));
        keyBuilder.setWeight(weight);
        keyList.add(keyBuilder.build());
      }
      permissionBuilder.addAllKeys(keyList);
    }
    return permissionBuilder.build();
  }

  public Contract.AccountPermissionUpdateContract createAccountPermissionContract(
      byte[] owner, String permissionJson) {
    Contract.AccountPermissionUpdateContract.Builder builder =
        Contract.AccountPermissionUpdateContract.newBuilder();

    JSONObject permissions = JSONObject.parseObject(permissionJson);
    JSONObject owner_permission = permissions.getJSONObject("owner_permission");
    JSONObject witness_permission = permissions.getJSONObject("witness_permission");
    JSONArray active_permissions = permissions.getJSONArray("active_permissions");

    if (owner_permission != null) {
      Permission ownerPermission = json2Permission(owner_permission);
      builder.setOwner(ownerPermission);
    }
    if (witness_permission != null) {
      Permission witnessPermission = json2Permission(witness_permission);
      builder.setWitness(witnessPermission);
    }
    if (active_permissions != null) {
      List<Permission> activePermissionList = new ArrayList<>();
      for (int j = 0; j < active_permissions.size(); j++) {
        JSONObject permission = active_permissions.getJSONObject(j);
        activePermissionList.add(json2Permission(permission));
      }
      builder.addAllActives(activePermissionList);
    }
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    return builder.build();
  }

  public Transaction addTransactionSign(Transaction transaction) {
    if (transaction.getRawData().getTimestamp() == 0) {
      transaction = TransactionUtils.setTimestamp(transaction);
    }
    transaction = TransactionUtils.setExpirationTime(transaction);
    transaction = getMultiTransactionSign().setPermissionId(transaction);

    transaction = TransactionUtils
        .sign(transaction, this.getEcKey(), getCurrentChainId(), isMainChain());

    return transaction;
  }

}
