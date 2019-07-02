package stest.tron.wallet.common.client.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Wallet;
import org.tron.keystore.WalletFile;
import org.tron.protos.Contract;
import org.tron.protos.Contract.CreateSmartContract;
import org.tron.protos.Contract.CreateSmartContract.Builder;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;


public class PublicMethed {

  Wallet wallet = new Wallet();
  //Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  private static final String FilePath = "Wallet";
  private static List<WalletFile> walletFile = new ArrayList<>();
  private static final Logger logger = LoggerFactory.getLogger("TestLogger");
  //private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  //private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;


  /**
   * constructor.
   */

  public static SmartContract.ABI jsonStr2Abi(String jsonStr) {
    if (jsonStr == null) {
      return null;
    }

    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElementRoot = jsonParser.parse(jsonStr);
    JsonArray jsonRoot = jsonElementRoot.getAsJsonArray();
    SmartContract.ABI.Builder abiBuilder = SmartContract.ABI.newBuilder();
    for (int index = 0; index < jsonRoot.size(); index++) {
      JsonElement abiItem = jsonRoot.get(index);
      boolean anonymous = abiItem.getAsJsonObject().get("anonymous") != null
          ? abiItem.getAsJsonObject().get("anonymous").getAsBoolean() : false;
      final boolean constant = abiItem.getAsJsonObject().get("constant") != null
          ? abiItem.getAsJsonObject().get("constant").getAsBoolean() : false;
      final String name = abiItem.getAsJsonObject().get("name") != null
          ? abiItem.getAsJsonObject().get("name").getAsString() : null;
      JsonArray inputs = abiItem.getAsJsonObject().get("inputs") != null
          ? abiItem.getAsJsonObject().get("inputs").getAsJsonArray() : null;
      final JsonArray outputs = abiItem.getAsJsonObject().get("outputs") != null
          ? abiItem.getAsJsonObject().get("outputs").getAsJsonArray() : null;
      String type = abiItem.getAsJsonObject().get("type") != null
          ? abiItem.getAsJsonObject().get("type").getAsString() : null;
      final boolean payable = abiItem.getAsJsonObject().get("payable") != null
          ? abiItem.getAsJsonObject().get("payable").getAsBoolean() : false;
      final String stateMutability = abiItem.getAsJsonObject().get("stateMutability") != null
          ? abiItem.getAsJsonObject().get("stateMutability").getAsString() : null;
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
      if (inputs != null) {
        for (int j = 0; j < inputs.size(); j++) {
          JsonElement inputItem = inputs.get(j);
          if (inputItem.getAsJsonObject().get("name") == null
              || inputItem.getAsJsonObject().get("type") == null) {
            logger.error("Input argument invalid due to no name or no type!");
            return null;
          }
          String inputName = inputItem.getAsJsonObject().get("name").getAsString();
          String inputType = inputItem.getAsJsonObject().get("type").getAsString();
          SmartContract.ABI.Entry.Param.Builder paramBuilder = SmartContract.ABI.Entry.Param
              .newBuilder();
          JsonElement indexed = inputItem.getAsJsonObject().get("indexed");

          paramBuilder.setIndexed((indexed == null) ? false : indexed.getAsBoolean());
          paramBuilder.setName(inputName);
          paramBuilder.setType(inputType);
          entryBuilder.addInputs(paramBuilder.build());
        }
      }

      /* { outputs : optional } */
      if (outputs != null) {
        for (int k = 0; k < outputs.size(); k++) {
          JsonElement outputItem = outputs.get(k);
          if (outputItem.getAsJsonObject().get("name") == null
              || outputItem.getAsJsonObject().get("type") == null) {
            logger.error("Output argument invalid due to no name or no type!");
            return null;
          }
          String outputName = outputItem.getAsJsonObject().get("name").getAsString();
          String outputType = outputItem.getAsJsonObject().get("type").getAsString();
          SmartContract.ABI.Entry.Param.Builder paramBuilder = SmartContract.ABI.Entry.Param
              .newBuilder();
          JsonElement indexed = outputItem.getAsJsonObject().get("indexed");

          paramBuilder.setIndexed((indexed == null) ? false : indexed.getAsBoolean());
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

  /**
   * constructor.
   */

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

  /**
   * constructor.
   */

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

  /**
   * constructor.
   */

  public static byte[] getFinalAddress(String priKey) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    WalletClient walletClient;
    walletClient = new WalletClient(priKey);
    //walletClient.init(0);
    return walletClient.getAddress();
  }

  /**
   * constructor.
   */

  public static boolean printAddress(String key) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    logger.info(key);
    logger.info(ByteArray.toHexString(getFinalAddress(key)));
    logger.info(Base58.encode58Check(getFinalAddress(key)));
    return true;
  }


  /**
   * constructor.
   */

  public static boolean waitProduceNextBlock(WalletGrpc.WalletBlockingStub
      blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    final Long currentNum = currentBlock.getBlockHeader().getRawData().getNumber();

    Block nextBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long nextNum = nextBlock.getBlockHeader().getRawData().getNumber();

    Integer wait = 0;
    logger.info("Block num is " + Long.toString(currentBlock
        .getBlockHeader().getRawData().getNumber()));
    while (nextNum <= currentNum + 1 && wait <= 15) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      logger.info("Wait to produce next block");
      nextBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
      nextNum = nextBlock.getBlockHeader().getRawData().getNumber();
      if (wait == 15) {
        logger.info("These 45 second didn't produce a block,please check.");
        return false;
      }
      wait++;
    }
    logger.info("quit normally");
    return true;
  }


  /**
   * constructor.
   */

  public static Boolean sendcoin(byte[] to, long amount, byte[] owner, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    //String priKey = testKey002;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    Integer times = 0;
    while (times++ <= 2) {

      Contract.TransferContract.Builder builder = Contract.TransferContract.newBuilder();
      ByteString bsTo = ByteString.copyFrom(to);
      ByteString bsOwner = ByteString.copyFrom(owner);
      builder.setToAddress(bsTo);
      builder.setOwnerAddress(bsOwner);
      builder.setAmount(amount);

      Contract.TransferContract contract = builder.build();
      Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        logger.info("transaction ==null");
        continue;
      }
      transaction = signTransaction(ecKey, transaction);
      GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
      return response.getResult();
    }
    return false;

  }


  /**
   * constructor.
   */

  public static Account queryAccount(byte[] address, WalletGrpc
      .WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString addressBs = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBs).build();
    return blockingStubFull.getAccount(request);
  }


  /**
   * constructor.
   */

  public static Protocol.Transaction signTransaction(ECKey ecKey,
      Protocol.Transaction transaction) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    if (ecKey == null || ecKey.getPrivKey() == null) {
      //logger.warn("Warning: Can't sign,there is no private key !!");
      return null;
    }
    transaction = TransactionUtils.setTimestamp(transaction);
    return TransactionUtils.sign(transaction, ecKey);
  }


  public static Protocol.Transaction signTransaction(ECKey ecKey,
      Protocol.Transaction transaction, byte[] mainGateWay, boolean isSideChain) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    if (ecKey == null || ecKey.getPrivKey() == null) {
      //logger.warn("Warning: Can't sign,there is no private key !!");
      return null;
    }
    transaction = TransactionUtils.setTimestamp(transaction);

    return TransactionUtils.sign(transaction, ecKey, mainGateWay, isSideChain);
  }


  /**
   * constructor.
   */
  public static GrpcAPI.Return broadcastTransaction(Transaction transaction,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    int i = 10;
    GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
    while (response.getResult() == false && response.getCode() == response_code.SERVER_BUSY
        && i > 0) {
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      i--;
      response = blockingStubFull.broadcastTransaction(transaction);
      logger.info("repeate times = " + (10 - i));
    }

    if (response.getResult() == false) {
      logger.info("Code = " + response.getCode());
      logger.info("Message = " + response.getMessage().toStringUtf8());
    }
    return response;
  }


  public static String triggerContract(byte[] contractAddress, long callValue, byte[] data,
      long feeLimit,
      long tokenValue, String tokenId, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    byte[] owner = ownerAddress;

    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    Contract.TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 &&
        transactionExtention.getConstantResult(0) != null &&
        transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
//    System.out.println("transaction hex string is " + Utils.printTransaction(transaction));
//    System.out.println(Utils.printTransaction(transactionExtention));
    transaction = signTransaction(ecKey, transaction);

    ByteString txid = ByteString.copyFrom(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == true) {
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
    }

    return null;
  }

  public static String triggerContractSideChain(byte[] contractAddress, byte[] mainGateWay,
      long callValue, byte[] data, long feeLimit,
      long tokenValue, String tokenId, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    byte[] owner = ownerAddress;

    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    Contract.TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 &&
        transactionExtention.getConstantResult(0) != null &&
        transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
//    System.out.println("transaction hex string is " + Utils.printTransaction(transaction));
//    System.out.println(Utils.printTransaction(transactionExtention));
    transaction = signTransaction(ecKey, transaction, mainGateWay, false);

    ByteString txid = ByteString.copyFrom(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == true) {
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
    }

    return null;
  }


  public static Optional<TransactionInfo> getTransactionInfoById(String txId, WalletGrpc
      .WalletBlockingStub blockingStubFull) {
    ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txId));
    BytesMessage request = BytesMessage.newBuilder().setValue(bsTxid).build();
    TransactionInfo transactionInfo;
    transactionInfo = blockingStubFull.getTransactionInfoById(request);
    return Optional.ofNullable(transactionInfo);
  }

  /**
   * constructor.
   */
   // should not have in side-chain
//  public static Boolean createAssetIssue(byte[] address, String name, Long totalSupply,
//      Integer trxNum, Integer icoNum, Long startTime, Long endTime, Integer voteScore,
//      String description, String url, Long freeAssetNetLimit, Long publicFreeAssetNetLimit,
//      Long fronzenAmount, Long frozenDay, String priKey,
//      WalletGrpc.WalletBlockingStub blockingStubFull) {
//    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
//    ECKey temKey = null;
//    try {
//      BigInteger priK = new BigInteger(priKey, 16);
//      temKey = ECKey.fromPrivate(priK);
//    } catch (Exception ex) {
//      ex.printStackTrace();
//    }
//    ECKey ecKey = temKey;
//    try {
//      Contract.AssetIssueContract.Builder builder = Contract.AssetIssueContract.newBuilder();
//      builder.setOwnerAddress(ByteString.copyFrom(address));
//      builder.setName(ByteString.copyFrom(name.getBytes()));
//      builder.setTotalSupply(totalSupply);
//      builder.setTrxNum(trxNum);
//      builder.setNum(icoNum);
//      builder.setStartTime(startTime);
//      builder.setEndTime(endTime);
//      builder.setVoteScore(voteScore);
//      builder.setDescription(ByteString.copyFrom(description.getBytes()));
//      builder.setUrl(ByteString.copyFrom(url.getBytes()));
//      builder.setFreeAssetNetLimit(freeAssetNetLimit);
//      builder.setPublicFreeAssetNetLimit(publicFreeAssetNetLimit);
//      Contract.AssetIssueContract.FrozenSupply.Builder frozenBuilder = Contract.AssetIssueContract
//          .FrozenSupply.newBuilder();
//      frozenBuilder.setFrozenAmount(fronzenAmount);
//      frozenBuilder.setFrozenDays(frozenDay);
//      builder.addFrozenSupply(0, frozenBuilder);
//
//      Protocol.Transaction transaction = blockingStubFull.createAssetIssue(builder.build());
//      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
//        logger.info("transaction == null");
//        return false;
//      }
//      transaction = signTransaction(ecKey, transaction);
//
//      GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
//
//      return response.getResult();
//    } catch (Exception ex) {
//      ex.printStackTrace();
//      return false;
//    }
//  }


  /**
   * constructor.
   */
  public static Long getAssetIssueValue(byte[] accountAddress, ByteString assetIssueId,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Long assetIssueCount = 0L;
    Account contractAccount = queryAccount(accountAddress, blockingStubFull);
    Map<String, Long> createAssetIssueMap = contractAccount.getAssetV2Map();
    for (Map.Entry<String, Long> entry : createAssetIssueMap.entrySet()) {
      if (assetIssueId.toStringUtf8().equals(entry.getKey())) {
        assetIssueCount = entry.getValue();
      }
    }
    return assetIssueCount;
  }


  public static GrpcAPI.Return triggerContractForReturn(byte[] contractAddress, long callValue,
      byte[] data,
      long feeLimit,
      long tokenValue, String tokenId, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    byte[] owner = ownerAddress;
    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    Contract.TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 &&
        transactionExtention.getConstantResult(0) != null &&
        transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
      return null;
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
//    System.out.println("transaction hex string is " + Utils.printTransaction(transaction));
//    System.out.println(Utils.printTransaction(transactionExtention));
    transaction = signTransaction(ecKey, transaction);

    ByteString txid = ByteString.copyFrom(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response;
  }


  /**
   * constructor.
   */
  public static String deployContractAndGetTransactionInfoById(String contractName,
      String abiString, String code, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, long originEnergyLimit, String tokenId, long tokenValue,
      String libraryAddress, String priKey, byte[] ownerAddress,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    byte[] owner = ownerAddress;
    SmartContract.ABI abi = jsonStr2Abi(abiString);
    if (abi == null) {
      logger.error("abi is null");
      return null;
    }
    //byte[] codeBytes = Hex.decode(code);
    SmartContract.Builder builder = SmartContract.newBuilder();
    builder.setName(contractName);
    builder.setOriginAddress(ByteString.copyFrom(owner));
    builder.setAbi(abi);
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent);
    builder.setOriginEnergyLimit(originEnergyLimit);

    if (value != 0) {

      builder.setCallValue(value);
    }

    byte[] byteCode;
    if (null != libraryAddress) {
      byteCode = replaceLibraryAddress(code, libraryAddress);
    } else {
      byteCode = Hex.decode(code);
    }
    builder.setBytecode(ByteString.copyFrom(byteCode));

    Builder contractBuilder = CreateSmartContract.newBuilder();
    contractBuilder.setOwnerAddress(ByteString.copyFrom(owner));
    contractBuilder.setCallTokenValue(tokenValue);
    contractBuilder.setTokenId(Long.parseLong(tokenId));
    CreateSmartContract contractDeployContract = contractBuilder
        .setNewContract(builder.build()).build();

    TransactionExtention transactionExtention = blockingStubFull
        .deployContract(contractDeployContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return null;
    }

    final TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    transaction = signTransaction(ecKey, transaction);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    }
  }

  /**
   * constructor.
   */
  private static byte[] replaceLibraryAddress(String code, String libraryAddressPair) {

    String[] libraryAddressList = libraryAddressPair.split("[,]");

    for (int i = 0; i < libraryAddressList.length; i++) {
      String cur = libraryAddressList[i];

      int lastPosition = cur.lastIndexOf(":");
      if (-1 == lastPosition) {
        throw new RuntimeException("libraryAddress delimit by ':'");
      }
      String libraryName = cur.substring(0, lastPosition);
      String addr = cur.substring(lastPosition + 1);
      String libraryAddressHex = ByteArray.toHexString(Wallet.decodeFromBase58Check(addr))
          .substring(2);

      String repeated = new String(new char[40 - libraryName.length() - 2]).replace("\0", "_");
      String beReplaced = "__" + libraryName + repeated;
      Matcher m = Pattern.compile(beReplaced).matcher(code);
      code = m.replaceAll(libraryAddressHex);
    }

    return Hex.decode(code);
  }

  /**
   * constructor.
   */
  public static byte[] generateContractAddress(Transaction trx, byte[] owneraddress) {

    // get owner address
    // this address should be as same as the onweraddress in trx, DONNOT modify it
    byte[] ownerAddress = owneraddress;

    // get tx hash
    byte[] txRawDataHash = Sha256Hash.of(trx.getRawData().toByteArray()).getBytes();

    // combine
    byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
    System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
    System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

    return Hash.sha3omit12(combined);

  }


  /**
   * constructor.
   */
  public static boolean transferAsset(byte[] to, byte[] assertName, long amount, byte[] address,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    Contract.TransferAssetContract.Builder builder = Contract.TransferAssetContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsName = ByteString.copyFrom(assertName);
    ByteString bsOwner = ByteString.copyFrom(address);
    builder.setToAddress(bsTo);
    builder.setAssetName(bsName);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    Contract.TransferAssetContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.transferAsset(contract);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      if (transaction == null) {
        logger.info("transaction == null");
      } else {
        logger.info("transaction.getRawData().getContractCount() == 0");
      }
      return false;
    }
    transaction = signTransaction(ecKey, transaction);

    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }

  /**
   * constructor.
   */
  public static String depositTrc(
      String contractAddrStr, String mainGatewayAddr, String methodStr,
      String depositMethodStr, String num, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    long callValue = 0;
    long tokenCallValue = 0;
    String tokenId = "";
    String argsStr = "\"" + mainGatewayAddr + "\",\"" + num + "\"";

    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
    byte[] contractAddress = WalletClient.decodeFromBase58Check(contractAddrStr);

    String trxId = triggerContract(contractAddress, callValue, input, feeLimit, tokenCallValue,
        tokenId, ownerAddress, priKey, blockingStubFull);
    boolean result = org.apache.commons.lang3.StringUtils.isNoneEmpty(trxId);
    if (result) {
      System.out.println("approve successfully.\n");

      byte[] depositContractAddr = WalletClient.decodeFromBase58Check(mainGatewayAddr);
      String depositArgStr = num + ",\"" + contractAddrStr + "\"";
      byte[] depositInput = Hex.decode(AbiUtil.parseMethod(depositMethodStr, depositArgStr, false));

      String Trxid = triggerContract(depositContractAddr, callValue, depositInput, feeLimit,
          tokenCallValue,
          tokenId, ownerAddress, priKey, blockingStubFull);
      return Trxid;
    } else {
      logger.info("approve failed.\n");
      return null;
    }
  }

  /**
   * constructor.
   */
  public static String depositTrc20(String trc20ContractAddress, String mainGatewayAddress,
      long tokenValue,
      long feeLimit, byte[] ownerAddress, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {

    String contractAddrStr = trc20ContractAddress;  //main trc20 contract address
    String methodStr = "approve(address,uint256)";
    String mainGatewayAddr = mainGatewayAddress; //main gateway contract address
    String num = Long.toString(tokenValue);
    String depositMethodStr = "depositTRC20(uint256,address)";

    return depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit,
        ownerAddress, priKey, blockingStubFull);
  }

  /**
   * constructor.
   */
  public static void depositTrc721(String trc20ContractAddress, String mainGatewayAddress,
      long tokenValue,
      long feeLimit, byte[] ownerAddress, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {

    String contractAddrStr = trc20ContractAddress;  //main trc20 contract address
    String methodStr = "approve(address,uint256)";
    String mainGatewayAddr = mainGatewayAddress; //main gateway contract address
    String num = Long.toString(tokenValue);
    String depositMethodStr = "depositTRC721(uint256,address)";

    depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit,
        ownerAddress, priKey, blockingStubFull);

  }

  /**
   * constructor.
   */
  public static HashMap<String, String> mappingTrc20(byte[] sideGatewayAddress,
      byte[] mainGatewayAddress,
      String trxHash, String name, String symbol, String decimal, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    byte[] contractAddrStr = sideGatewayAddress;  //side gateway
    String methodStr = "deployDAppTRC20AndMapping(bytes,string,string,uint8)";
    String argsStr = "\"" + trxHash + "\",\"" + name + "\",\"" + symbol + "\",\"" + decimal + "\"";

    return mapingTrc(sideGatewayAddress, mainGatewayAddress, methodStr, argsStr, trxHash, feeLimit,
        ownerAddress,
        priKey, blockingStubFull);
  }

  /**
   * constructor.
   */

  public static HashMap<String, String> mapingTrc(
      byte[] sideGatewayAddress, byte[] mainGatewayAddress, String methodStr, String argsStr,
      String trxHash, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    HashMap<String, String> map = new HashMap<String, String>();
    long callValue = 0;
    long tokenCallValue = 0;
    String tokenId = "";

    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
    byte[] contractAddress = sideGatewayAddress;

    boolean result = true;
    String trxId = triggerContractSideChain(contractAddress, mainGatewayAddress, callValue, input,
        feeLimit,
        tokenCallValue,
        tokenId, ownerAddress, priKey, blockingStubFull);
    if (org.apache.commons.lang3.StringUtils.isEmpty(trxId)) {
      result = false;
    }
    result = checkTxInfo(trxId, blockingStubFull);

    if (result) {
      System.out.println("mappingTrc successfully.\n");
      map.put("TID", trxId);
      // get tx hash
      byte[] txRawDataHash = Hex.decode(trxHash);

      // combine
      byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
      System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
      System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

      String mainContractAddress = WalletClient.encode58Check(Hash.sha3omit12(combined));

      byte[] input1 = Hex.decode(
          AbiUtil.parseMethod("mainToSideContractMap(address)", "\"" + mainContractAddress + "\"",
              false));
      Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
      builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
      builder.setContractAddress(
          ByteString.copyFrom(sideGatewayAddress));
      builder.setData(ByteString.copyFrom(input1));
      builder.setCallValue(callValue);
      if (tokenId != null && tokenId != "") {
        builder.setCallTokenValue(0);
        builder.setTokenId(Long.parseLong("0"));
      }
      Contract.TriggerSmartContract triggerContract = builder.build();
      TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
      byte[] data = transactionExtention.getConstantResult(0).toByteArray();
      byte[] address = Arrays.copyOfRange(data, 12, data.length);

      if (address.length == 20) {
        byte[] newAddress = new byte[21];
        byte[] temp = new byte[]{0x41};
        System.arraycopy(temp, 0, newAddress, 0, temp.length);
        System.arraycopy(address, 0, newAddress, temp.length, address.length);
        address = newAddress;
      }
      System.out.println(
          "sideContractAddress is " + WalletClient.encode58Check(address));
      map.put("SideContract", WalletClient.encode58Check(address));

    } else {
      System.out.println("please confirm the result in side chain after 60s.");
    }

    return map;
  }

  /**
   * constructor.
   */
  public static boolean checkTxInfo(String txId, WalletGrpc.WalletBlockingStub blockingStubFull) {
    try {
      System.out.println("wait 3s for check result. ");
      Thread.sleep(3_000);
      System.out.println("trx id: " + txId);
      Optional<TransactionInfo> transactionInfo = getTransactionInfoById(txId, blockingStubFull);
      TransactionInfo info = transactionInfo.get();
      if (info.getBlockTimeStamp() != 0L) {
        if (info.getResult().equals(TransactionInfo.code.SUCESS)) {
          return true;
        }
      }

      //retry
      int maxRetry = 3;
      for (int i = 0; i < maxRetry; i++) {
        Thread.sleep(1_000);
        System.out.println("will retry {} time(s): " + i + 1);
        transactionInfo = getTransactionInfoById(txId, blockingStubFull);
        info = transactionInfo.get();
        if (info.getBlockTimeStamp() != 0L) {
          if (info.getResult().equals(TransactionInfo.code.SUCESS)) {
            return true;
          }
        }
      }
    } catch (InterruptedException e) {
      System.out.println("sleep error" + (e.getMessage()));
      return false;
    }

    return false;
  }

  /**
   * constructor.
   */
  public static String deployContractWithConstantParame(String contractName, String abiString,
      String code, String constructorStr, String argsStr, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, long originEnergyLimit, String tokenId, long tokenValue,
      String libraryAddress, String priKey, byte[] ownerAddress,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    SmartContract.ABI abi = jsonStr2Abi(abiString);
    if (abi == null) {
      logger.error("abi is null");
      return null;
    }

    code += Hex.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
    byte[] owner = ownerAddress;
    SmartContract.Builder builder = SmartContract.newBuilder();
    builder.setName(contractName);
    builder.setOriginAddress(ByteString.copyFrom(owner));
    builder.setAbi(abi);
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent);
    builder.setOriginEnergyLimit(originEnergyLimit);

    if (value != 0) {

      builder.setCallValue(value);
    }

    byte[] byteCode;
    if (null != libraryAddress) {
      byteCode = replaceLibraryAddress(code, libraryAddress);
    } else {
      byteCode = Hex.decode(code);
    }
    builder.setBytecode(ByteString.copyFrom(byteCode));

    Builder contractBuilder = CreateSmartContract.newBuilder();
    contractBuilder.setOwnerAddress(ByteString.copyFrom(owner));
    contractBuilder.setCallTokenValue(tokenValue);
    contractBuilder.setTokenId(Long.parseLong(tokenId));
    CreateSmartContract contractDeployContract = contractBuilder.setNewContract(builder.build())
        .build();

    TransactionExtention transactionExtention = blockingStubFull
        .deployContract(contractDeployContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return null;
    }

    final TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    transaction = signTransaction(ecKey, transaction);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    }
  }

  /**
   * constructor.
   */
  public static String deploySideContractWithConstantParame(String contractName, String abiString,
      String code, String constructorStr, String argsStr, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, long originEnergyLimit, String tokenId, long tokenValue,
      String libraryAddress, String priKey, byte[] ownerAddress,String mainGateway,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    SmartContract.ABI abi = jsonStr2Abi(abiString);
    if (abi == null) {
      logger.error("abi is null");
      return null;
    }

    code += Hex.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
    byte[] owner = ownerAddress;
    SmartContract.Builder builder = SmartContract.newBuilder();
    builder.setName(contractName);
    builder.setOriginAddress(ByteString.copyFrom(owner));
    builder.setAbi(abi);
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent);
    builder.setOriginEnergyLimit(originEnergyLimit);

    if (value != 0) {

      builder.setCallValue(value);
    }

    byte[] byteCode;
    if (null != libraryAddress) {
      byteCode = replaceLibraryAddress(code, libraryAddress);
    } else {
      byteCode = Hex.decode(code);
    }
    builder.setBytecode(ByteString.copyFrom(byteCode));

    Builder contractBuilder = CreateSmartContract.newBuilder();
    contractBuilder.setOwnerAddress(ByteString.copyFrom(owner));
    contractBuilder.setCallTokenValue(tokenValue);
    contractBuilder.setTokenId(Long.parseLong(tokenId));
    CreateSmartContract contractDeployContract = contractBuilder.setNewContract(builder.build())
        .build();

    TransactionExtention transactionExtention = blockingStubFull
        .deployContract(contractDeployContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return null;
    }

    final TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    transaction = signTransaction(ecKey, transaction,WalletClient.decodeFromBase58Check(mainGateway),false);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return ByteArray.toHexString(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    }
  }
  /**
   * constructor.
   */
  public static String deployContractWithConstantParame(String contractName, String abiString,
      String code, String constructorStr, String argsStr, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, String libraryAddress, String priKey, byte[] ownerAddress,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    return deployContractWithConstantParame(contractName, abiString, code, constructorStr, argsStr,
        data, feeLimit, value, consumeUserResourcePercent, 1000L, "0", 0L,
        libraryAddress, priKey, ownerAddress, blockingStubFull);
  }

  /**
   * constructor.
   */
  public static String deploySideContractWithConstantParame(String contractName, String abiString,
      String code, String constructorStr, String argsStr, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, String libraryAddress, String priKey, byte[] ownerAddress,String mainGateway,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    return deploySideContractWithConstantParame(contractName, abiString, code, constructorStr, argsStr,
        data, feeLimit, value, consumeUserResourcePercent, 1000L, "0", 0L,
        libraryAddress, priKey, ownerAddress, mainGateway, blockingStubFull);
  }

  /**
   * constructor.
   */
  public static TransactionExtention triggerContractForTransactionExtention(byte[] contractAddress,
      long callValue,
      byte[] data,
      long feeLimit,
      long tokenValue, String tokenId, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    byte[] owner = ownerAddress;
    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    Contract.TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    return transactionExtention;


  }


  /**
   * constructor.
   */
  public static String withdrawTrx(
      String mainGatewayAddr, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long tokenCallValue = 0;
    String tokenId = "";

    byte[] txData1 = PublicMethed.sideSignTrxData(callValue, ownerAddress, priKey,
        WalletClient.decodeFromBase58Check(mainGatewayAddr), blockingStubFull, 0,
        0, "0");
    String methodStr1 = "withdrawTRX(bytes)";

    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "\"" + Hex.toHexString(txData1) + "\"", false));

    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input1,
            feeLimit, tokenCallValue, tokenId, ownerAddress, priKey, blockingsideStubFull);
    return txid1;
  }


  /**
   * constructor.
   */
  public static GrpcAPI.Return withdrawTrxForReturn(
      String mainGatewayAddr, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long tokenCallValue = 0;
    String tokenId = "";

    byte[] txData1 = PublicMethed.sideSignTrxData(callValue, ownerAddress, priKey,
        WalletClient.decodeFromBase58Check(mainGatewayAddr), blockingStubFull, 0,
        0, "0");
    String methodStr1 = "withdrawTRX(bytes)";

    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "\"" + Hex.toHexString(txData1) + "\"", false));

    Return aReturn = PublicMethed
        .triggerContractSideChainForReturn(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input1,
            feeLimit, tokenCallValue, tokenId, ownerAddress, priKey, blockingsideStubFull);
    return aReturn;
  }


  public static GrpcAPI.Return triggerContractSideChainForReturn(byte[] contractAddress,
      byte[] mainGateWay,
      long callValue, byte[] data, long feeLimit,
      long tokenValue, String tokenId, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    byte[] owner = ownerAddress;

    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    Contract.TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 &&
        transactionExtention.getConstantResult(0) != null &&
        transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
      return null;
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

    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return null;
    }
    transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
//    System.out.println("transaction hex string is " + Utils.printTransaction(transaction));
//    System.out.println(Utils.printTransaction(transactionExtention));
    transaction = signTransaction(ecKey, transaction, mainGateWay, false);

    ByteString txid = ByteString.copyFrom(Sha256Hash.hash(transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response;
  }

  public static byte[] sideSignTrxData(long trxNum, byte[] ownerAddress, String priKey,
      byte[] mainGatewayAddress, WalletGrpc.WalletBlockingStub blockingStubFull, long callValue,
      long tokenValue, String tokenId) {
    byte[] input = Hex.decode(
        AbiUtil.parseMethod("nonces(address)", "\"" + Base58.encode58Check(ownerAddress) + "\"",
            false));

    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
    builder.setContractAddress(ByteString.copyFrom(mainGatewayAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    Contract.TriggerSmartContract triggerContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    byte[] nonceTemp = transactionExtention.getConstantResult(0).toByteArray();
    DataWord nonce = new DataWord(nonceTemp);

    byte[] amountTemp = ByteArray.fromLong(trxNum);
    DataWord amount = new DataWord(amountTemp);

    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(mainGatewayAddress, 1, mainGatewayAddress.length),
            nonce.getData(),
            amount.getData());
    ECKey myKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      myKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ECKey.ECDSASignature signature = myKey.sign(Hash.sha3(data));

    return signature.toByteArray();
  }

  /**
   * constructor.
   */

  public static Optional<Transaction> getTransactionById(String txId,
      WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubFull) {
    ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txId));
    BytesMessage request = BytesMessage.newBuilder().setValue(bsTxid).build();
    Transaction transaction = blockingStubFull.getTransactionById(request);

    return Optional.ofNullable(transaction);
  }

  /**
   * constructor.
   */

  public static Optional<Transaction> getTransactionById(String txId,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txId));
    BytesMessage request = BytesMessage.newBuilder().setValue(bsTxid).build();
    Transaction transaction = blockingStubFull.getTransactionById(request);
    return Optional.ofNullable(transaction);
  }

  /**
   * constructor.
   */

  public static SmartContract getContract(byte[] address, WalletGrpc
      .WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString byteString = ByteString.copyFrom(address);
    BytesMessage bytesMessage = BytesMessage.newBuilder().setValue(byteString).build();
    Integer i = 0;
    while (blockingStubFull.getContract(bytesMessage).getName().isEmpty() && i++ < 4) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    logger.info("contract name is " + blockingStubFull.getContract(bytesMessage).getName());
    logger.info("contract address is " + WalletClient.encode58Check(address));
    return blockingStubFull.getContract(bytesMessage);
  }

  /**
   * constructor.
   */

  public static boolean sideChainCreateProposal(byte[] ownerAddress, String priKey,String mainGatewayAddress,
      HashMap<Long, String> parametersMap, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    byte[] owner = ownerAddress;
    Contract.SideChainProposalCreateContract.Builder builder = Contract.SideChainProposalCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.putAllParameters(parametersMap);

    Contract.SideChainProposalCreateContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.sideChainProposalCreate(contract);
    if (transactionExtention == null) {
      return false;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return false;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return false;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    byte[] mainGateway = WalletClient.decodeFromBase58Check(mainGatewayAddress);
    transaction = signTransaction(ecKey, transaction,mainGateway,false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response.getResult();
  }

  /**
   * constructor.
   */

  public static boolean approveProposal(byte[] ownerAddress, String priKey, String mainGatewayAddress, long id,
      boolean isAddApproval, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    byte[] owner = ownerAddress;
    Contract.ProposalApproveContract.Builder builder = Contract.ProposalApproveContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setProposalId(id);
    builder.setIsAddApproval(isAddApproval);
    Contract.ProposalApproveContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.proposalApprove(contract);
    if (transactionExtention == null) {
      return false;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return false;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return false;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    byte[] mainGateway = WalletClient.decodeFromBase58Check(mainGatewayAddress);
    transaction = signTransaction(ecKey, transaction,mainGateway,false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }

}