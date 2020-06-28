package stest.tron.wallet.common.client.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.primitives.Longs;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.netty.util.internal.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.DecryptNotes;
import org.tron.api.GrpcAPI.DecryptNotes.NoteTx;
import org.tron.api.GrpcAPI.DecryptNotesMarked;
import org.tron.api.GrpcAPI.DelegatedResourceList;
import org.tron.api.GrpcAPI.DelegatedResourceMessage;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.ExchangeList;
import org.tron.api.GrpcAPI.IvkDecryptAndMarkParameters;
import org.tron.api.GrpcAPI.IvkDecryptParameters;
import org.tron.api.GrpcAPI.NfParameters;
import org.tron.api.GrpcAPI.Note;
import org.tron.api.GrpcAPI.NoteParameters;
import org.tron.api.GrpcAPI.NumberMessage;
import org.tron.api.GrpcAPI.OvkDecryptParameters;
import org.tron.api.GrpcAPI.PrivateParameters;
import org.tron.api.GrpcAPI.PrivateParametersWithoutAsk;
import org.tron.api.GrpcAPI.ReceiveNote;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.api.GrpcAPI.SpendAuthSigParameters;
import org.tron.api.GrpcAPI.SpendNote;
import org.tron.api.GrpcAPI.SpendResult;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.GrpcAPI.TransactionInfoList;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DBConfig;
import org.tron.common.utils.Hash;
import org.tron.common.utils.Sha256Hash;
import org.tron.core.Wallet;
import org.tron.core.zen.address.DiversifierT;
import org.tron.core.zen.address.ExpandedSpendingKey;
import org.tron.core.zen.address.FullViewingKey;
import org.tron.core.zen.address.IncomingViewingKey;
import org.tron.core.zen.address.PaymentAddress;
import org.tron.core.zen.address.SpendingKey;
import org.tron.keystore.WalletFile;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.DelegatedResourceAccountIndex;
import org.tron.protos.Protocol.Exchange;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;
import org.tron.protos.Protocol.Transaction.Result;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.contract.AccountContract.AccountCreateContract;
import org.tron.protos.contract.AccountContract.AccountPermissionUpdateContract;
import org.tron.protos.contract.AccountContract.AccountUpdateContract;
import org.tron.protos.contract.AccountContract.SetAccountIdContract;
import org.tron.protos.contract.AssetIssueContractOuterClass.TransferAssetContract;
import org.tron.protos.contract.BalanceContract.FreezeBalanceContract;
import org.tron.protos.contract.BalanceContract.TransferContract;
import org.tron.protos.contract.BalanceContract.UnfreezeBalanceContract;
import org.tron.protos.contract.IncentiveContract.FundInjectContract;
import org.tron.protos.contract.ProposalContract.ProposalApproveContract;
import org.tron.protos.contract.ProposalContract.ProposalDeleteContract;
import org.tron.protos.contract.ProposalContract.SideChainProposalCreateContract;
import org.tron.protos.contract.ShieldContract.IncrementalMerkleVoucherInfo;
import org.tron.protos.contract.ShieldContract.OutputPoint;
import org.tron.protos.contract.ShieldContract.OutputPointInfo;
import org.tron.protos.contract.ShieldContract.ShieldedTransferContract;
import org.tron.protos.contract.ShieldContract.SpendDescription;
import org.tron.protos.contract.SmartContractOuterClass.ClearABIContract;
import org.tron.protos.contract.SmartContractOuterClass.CreateSmartContract;
import org.tron.protos.contract.SmartContractOuterClass.CreateSmartContract.Builder;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract.ABI;
import org.tron.protos.contract.SmartContractOuterClass.TriggerSmartContract;
import org.tron.protos.contract.SmartContractOuterClass.UpdateEnergyLimitContract;
import org.tron.protos.contract.SmartContractOuterClass.UpdateSettingContract;
import org.tron.protos.contract.StorageContract.BuyStorageContract;
import org.tron.protos.contract.StorageContract.SellStorageContract;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;


public class PublicMethed {

  //Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  private static final String FilePath = "Wallet";
  private static final Logger logger = LoggerFactory.getLogger("TestLogger");
  private static List<WalletFile> walletFile = new ArrayList<>();
  Wallet wallet = new Wallet();
  //private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  //private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  /**
   * constructor.
   */

  public static ABI jsonStr2Abi(String jsonStr) {
    if (jsonStr == null) {
      return null;
    }

    JsonParser jsonParser = new JsonParser();
    JsonElement jsonElementRoot = jsonParser.parse(jsonStr);
    JsonArray jsonRoot = jsonElementRoot.getAsJsonArray();
    ABI.Builder abiBuilder = ABI.newBuilder();
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
  public static List<String> getStrings(byte[] data) {
    int index = 0;
    List<String> ret = new ArrayList<>();
    while (index < data.length) {
      ret.add(byte2HexStr(data, index, 32));
      index += 32;
    }
    return ret;
  }

  /**
   * constructor.
   */
  public static String byte2HexStr(byte[] b, int offset, int length) {
    StringBuilder ssBuilder = new StringBuilder();
    for (int n = offset; n < offset + length && n < b.length; n++) {
      String stmp = Integer.toHexString(b[n] & 0xFF);
      ssBuilder.append((stmp.length() == 1) ? "0" + stmp : stmp);
    }
    return ssBuilder.toString().toUpperCase().trim();
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

      TransferContract.Builder builder = TransferContract.newBuilder();
      ByteString bsTo = ByteString.copyFrom(to);
      ByteString bsOwner = ByteString.copyFrom(owner);
      builder.setToAddress(bsTo);
      builder.setOwnerAddress(bsOwner);
      builder.setAmount(amount);

      TransferContract contract = builder.build();
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
      Protocol.Transaction transaction, byte[] mainGateWay, boolean isMainchain
  ) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    if (ecKey == null || ecKey.getPrivKey() == null) {
      //logger.warn("Warning: Can't sign,there is no private key !!");
      return null;
    }
    transaction = TransactionUtils.setTimestamp(transaction);
    return TransactionUtils.sign(transaction, ecKey, mainGateWay, isMainchain);
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


  public static String triggerContract(byte[] contractAddress, String method, String argsStr,
      Boolean isHex, long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    return triggerContract(contractAddress, method, argsStr, isHex, callValue, feeLimit,
        "0", 0, ownerAddress, priKey, blockingStubFull);

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

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

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

    ByteString txid = ByteString.copyFrom(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == true) {
      System.out.println(
          "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
    }

    return null;
  }

  public static String triggerContractSideChain(byte[] contractAddress, String method,
      String argsStr,
      Boolean isHex, long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    return triggerContractSideChain(contractAddress, method, argsStr, isHex, callValue, feeLimit,
        "0", 0, ownerAddress, priKey, blockingStubFull);

  }


  public static String triggerContractSideChain(
      byte[] contractAddress, String method, String argsStr,
      Boolean isHex, long callValue, long feeLimit, String tokenId, long tokenValue,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull
  ) {
    //LOADCONF
    byte[] mainGateWay = PublicMethed.getMaingatewayByteAddr();

    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    final ECKey ecKey = temKey;
    if (argsStr.equalsIgnoreCase("#")) {
      logger.info("argsstr is #");
      argsStr = "";
    }

    byte[] owner = ownerAddress;
    byte[] input = Hex.decode(AbiUtil.parseMethod(method, argsStr, isHex));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

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

    ByteString txid = ByteString.copyFrom(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == true) {
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
    }

    return null;


  }


  public static String triggerContractSideChain(byte[] contractAddress, byte[] sideChainId,
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

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

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
    transaction = signTransaction(ecKey, transaction, sideChainId, false);

    ByteString txid = ByteString.copyFrom(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == true) {
      logger.info("txid:" + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
      return ByteArray.toHexString(transactionExtention.getTxid().toByteArray());
    }

    return null;
  }


  public static String triggerContractSideChainfee(byte[] contractAddress, byte[] sideChainId,
      long withdrawfee, byte[] data, long feeLimit,
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

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(withdrawfee);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

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
    transaction = signTransaction(ecKey, transaction, sideChainId, false);

    ByteString txid = ByteString.copyFrom(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    transactionExtention = transactionExtention.toBuilder().setTransaction(transaction)
        .setTxid(txid).build();
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == true) {
      logger.info("txid:" + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
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
    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

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

    ByteString txid = ByteString.copyFrom(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
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
      long consumeUserResourcePercent, String libraryAddress, String priKey, byte[] ownerAddress,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    return deployContractAndGetTransactionInfoById(contractName, abiString, code, data, feeLimit,
        value, consumeUserResourcePercent, 1000L, "0", 0L, libraryAddress,
        priKey, ownerAddress, blockingStubFull);
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
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
    byte[] txRawDataHash = Sha256Hash
        .of(DBConfig.isECKeyCryptoEngine(), trx.getRawData().toByteArray()).getBytes();

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

    TransferAssetContract.Builder builder = TransferAssetContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsName = ByteString.copyFrom(assertName);
    ByteString bsOwner = ByteString.copyFrom(address);
    builder.setToAddress(bsTo);
    builder.setAssetName(bsName);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferAssetContract contract = builder.build();
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
//      String depositArgStr = num + ",\"" + contractAddrStr + "\"";
      String depositArgStr = "\"" + contractAddrStr + "\",\"" + num + "\"";

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
    String depositMethodStr = "depositTRC20(address,uint256)";

    return depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit,
        ownerAddress, priKey, blockingStubFull);
  }

  /**
   * constructor.
   */

  public static String depositTrc721(String trc20ContractAddress, String mainGatewayAddress,
      long tokenValue,
      long feeLimit, byte[] ownerAddress, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {

    String contractAddrStr = trc20ContractAddress;  //main trc20 contract address
    String methodStr = "approve(address,uint256)";
    String mainGatewayAddr = mainGatewayAddress; //main gateway contract address
    String num = Long.toString(tokenValue);
    String depositMethodStr = "depositTRC721(address,uint256)";

    return depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit,
        ownerAddress, priKey, blockingStubFull);

  }

  /**
   * constructor.
   */
  public static String mappingTrc20(byte[] mainGatewayAddress,
      String trxHash, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    String methodStr = "mappingTRC20(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    String trxid = triggerContract(mainGatewayAddress, methodStr, argsStr, false, 0, feeLimit,
        ownerAddress, priKey, blockingStubFull);
    return trxid;
  }

  /**
   * constructor.
   */
  public static String mappingTrc20fee(byte[] mainGatewayAddress,
      String trxHash, long mappingfee, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    String methodStr = "mappingTRC20(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    String trxid = triggerContract(mainGatewayAddress, methodStr, argsStr, false, mappingfee,
        feeLimit,
        ownerAddress, priKey, blockingStubFull);
    return trxid;
  }

  /**
   * constructor.
   */
  public static String mappingTrc721fee(byte[] mainGatewayAddress,
      String trxHash, long mappingfee, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    String methodStr = "mappingTRC721(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    String trxid = triggerContract(mainGatewayAddress, methodStr, argsStr, false, mappingfee,
        feeLimit,
        ownerAddress, priKey, blockingStubFull);
    return trxid;
  }


  /**
   * constructor.
   */
  public static String mappingTrc721(byte[] mainGatewayAddress,
      String trxHash, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    String methodStr = "mappingTRC721(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    String trxid = triggerContract(mainGatewayAddress, methodStr, argsStr, false, 0, feeLimit,
        ownerAddress, priKey, blockingStubFull);
    return trxid;
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
      TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
      builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
      builder.setContractAddress(
          ByteString.copyFrom(sideGatewayAddress));
      builder.setData(ByteString.copyFrom(input1));
      builder.setCallValue(callValue);
      if (tokenId != null && tokenId != "") {
        builder.setCallTokenValue(0);
        builder.setTokenId(Long.parseLong("0"));
      }
      TriggerSmartContract triggerContract = builder.build();
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
        "txid = " + ByteArray.toHexString(Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    }
  }

  /**
   * constructor.
   */
  public static String deploySideContractWithConstantParame(String contractName, String abiString,
      String code, String constructorStr, String argsStr, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, long originEnergyLimit, String tokenId, long tokenValue,
      String libraryAddress, String priKey, byte[] ownerAddress, String mainGateway,
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

    if (!constructorStr.equals("#")) {
      code += Hex.toHexString(AbiUtil.encodeInput(constructorStr, argsStr));
    }
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
    transaction = signTransaction(ecKey, transaction,
        WalletClient.decodeFromBase58Check(mainGateway), false);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    }
  }

  /**
   * constructor.
   */
  public static String deploySideContractWithConstantParame(String contractName, String abiString,
      String code, String constructorStr, String argsStr, String data, Long feeLimit, long value,
      long consumeUserResourcePercent, String libraryAddress, String priKey, byte[] ownerAddress,
      String mainGateway,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    return deploySideContractWithConstantParame(contractName, abiString, code, constructorStr,
        argsStr,
        data, feeLimit, value, consumeUserResourcePercent, 1000L, "0", 0L,
        libraryAddress, priKey, ownerAddress, mainGateway, blockingStubFull);
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
    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    return transactionExtention;
  }

  public static String withdrawTrx1(
      byte[] sideChainId, byte[] sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long tokenCallValue = 0;
    String tokenId = "0";
    String methodStr1 = "withdrawTRX()";

    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "", false));

    String txid1 = PublicMethed
        .triggerContractSideChain(sideGatewayAddress, sideChainId,
            callValue,
            input1,
            feeLimit, tokenCallValue, tokenId, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid:" + txid1);
    return txid1;
  }

  /**
   * constructor.
   */
  public static String withdrawTrx(
      String ChainIdAddress, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long tokenCallValue = 0;
    String tokenId = "0";
    String methodStr1 = "withdrawTRX()";

    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "", false));

    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(ChainIdAddress),
            callValue,
            input1,
            feeLimit, tokenCallValue, tokenId, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid:" + txid1);
    return txid1;
  }

  /**
   * constructor.
   */
  public static String withdrawTrxfee(
      String mainGatewayAddr, String sideGatewayAddress,
      long callvalue, long withdrawfee, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long tokenCallValue = 0;
    String tokenId = "0";
    String methodStr1 = "withdrawTRX()";
    long callValue = callvalue + withdrawfee;
    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "", false));

    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input1,

            feeLimit, tokenCallValue, tokenId, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid:" + txid1);
    return txid1;
  }

  /**
   * constructor.
   */
  public static GrpcAPI.Return withdrawTrxForReturn(
      String sideChainIdAddress, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long tokenCallValue = 0;
    String tokenId = "";

    String methodStr1 = "withdrawTRX()";

    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "", false));
    Return aReturn = PublicMethed
        .triggerContractSideChainForReturn(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(sideChainIdAddress),
            callValue,
            input1,
            feeLimit, tokenCallValue, tokenId, ownerAddress, priKey, blockingsideStubFull);
    return aReturn;
  }


  public static GrpcAPI.Return triggerContractSideChainForReturn(byte[] contractAddress,
      byte[] sideChainId,
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

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();

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
    transaction = signTransaction(ecKey, transaction, sideChainId, false);

    ByteString txid = ByteString.copyFrom(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
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

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
    builder.setContractAddress(ByteString.copyFrom(mainGatewayAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();
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


  public static byte[] sideSignTrc10Data(String trc10, String tokenValue, byte[] ownerAddress,
      String priKey,
      byte[] mainGatewayAddress, WalletGrpc.WalletBlockingStub blockingStubFull, long callValue) {
    byte[] input = Hex.decode(
        AbiUtil.parseMethod("nonces(address)", "\"" + Base58.encode58Check(ownerAddress) + "\"",
            false));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
    builder.setContractAddress(ByteString.copyFrom(mainGatewayAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    if (trc10 != null && trc10 != "") {
      builder.setCallTokenValue(0);
      builder.setTokenId(Long.parseLong("0"));
    }
    TriggerSmartContract triggerContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    byte[] nonceTemp = transactionExtention.getConstantResult(0).toByteArray();
    DataWord nonce = new DataWord(nonceTemp);

    DataWord valueI = new DataWord(new BigInteger(tokenValue, 10).toByteArray());
    DataWord tc10I = new DataWord(new BigInteger(trc10, 10).toByteArray());

    byte[] data = ByteUtil
        .merge(tc10I.getData(), nonce.getData(), valueI.getData());
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


  public static byte[] sideSignTokenData(String tokenAddress, byte[] ownerAddress, String priKey,
      byte[] mainGatewayAddress, WalletGrpc.WalletBlockingStub blockingStubFull, String value,
      long callValue,
      long tokenValue, String tokenId) {
    byte[] input = Hex.decode(
        AbiUtil.parseMethod("nonces(address)", "\"" + Base58.encode58Check(ownerAddress) + "\"",
            false));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(ownerAddress));
    builder.setContractAddress(ByteString.copyFrom(mainGatewayAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    if (tokenId != null && tokenId != "") {
      builder.setCallTokenValue(tokenValue);
      builder.setTokenId(Long.parseLong(tokenId));
    }
    TriggerSmartContract triggerContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    byte[] nonceTemp = transactionExtention.getConstantResult(0).toByteArray();
    DataWord nonce = new DataWord(nonceTemp);

    DataWord valueI = new DataWord(new BigInteger(value, 10).toByteArray());
    byte[] mainTokenAddress = WalletClient.decodeFromBase58Check(tokenAddress);
    if (mainTokenAddress == null || mainTokenAddress.length == 0) {
      System.out.println("Invalide adreess: " + tokenAddress);
      return null;
    }

    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(mainTokenAddress, 1, mainTokenAddress.length), nonce.getData(),
            valueI.getData());
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
  public static GrpcAPI.Return withdrawTrxForReturn(String trc10, String tokenValue,
      String mainGatewayAddr, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {

    byte[] txData1 = PublicMethed.sideSignTrc10Data(trc10, tokenValue, ownerAddress, priKey,
        WalletClient.decodeFromBase58Check(mainGatewayAddr), blockingStubFull, 0);
    String methodStr1 = "withdrawTRC10(bytes)";

    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod(methodStr1, "\"" + Hex.toHexString(txData1) + "\"", false));
    long tokenValue1 = Long.parseLong(tokenValue);

    Return aReturn = PublicMethed
        .triggerContractSideChainForReturn(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input1,
            feeLimit, tokenValue1, trc10, ownerAddress, priKey, blockingsideStubFull);
    return aReturn;
  }


  /**
   * constructor.
   */
  public static String withdrawTrc(
      String mainGatewayAddr, byte[] sideTokenAddress,
      String value, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingsideStubFull) {

    String methodStr = "withdrawal(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, value, false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideTokenAddress,
            WalletClient.decodeFromBase58Check(mainGatewayAddr), 0, input, feeLimit,
            0, "0", ownerAddress, priKey, blockingsideStubFull);
    return withdrawTxid1;
  }

  /**
   * constructor.
   */
  public static String withdrawTrc20(
      String ChainIdAddress, String sideTokenAddress,
      String value, String tokenAddress, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {

    String methodStr1 = "withdrawal(uint256)";

    byte[] input1 = Hex
        .decode(AbiUtil
            .parseMethod(methodStr1, value, false));
    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(tokenAddress),
            WalletClient.decodeFromBase58Check(ChainIdAddress),
            0,
            input1,
            feeLimit, 0, "0", ownerAddress, priKey, blockingsideStubFull);
    return txid1;
  }

  /**
   * constructor.
   */
  public static String withdrawtrc20fee(
      String mainGatewayAddr, String sideTokenAddress,
      String value, String tokenAddress, long feeLimit, byte[] ownerAddress, long callValue,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {

    String methodStr1 = "withdrawal(uint256)";
    byte[] input1 = Hex
        .decode(AbiUtil
            .parseMethod(methodStr1, value, false));
    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(tokenAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input1,
            feeLimit, 0, "0", ownerAddress, priKey, blockingsideStubFull);
    return txid1;
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
  public static String triggerContract(byte[] contractAddress, String method, String argsStr,
      Boolean isHex, long callValue, long feeLimit, String tokenId, long tokenValue,
      byte[] ownerAddress,
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
    if (argsStr.equalsIgnoreCase("#")) {
      logger.info("argsstr is #");
      argsStr = "";
    }

    byte[] owner = ownerAddress;
    byte[] input = Hex.decode(AbiUtil.parseMethod(method, argsStr, isHex));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    builder.setTokenId(Long.parseLong(tokenId));
    builder.setCallTokenValue(tokenValue);
    TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0
        && transactionExtention.getConstantResult(0) != null
        && transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
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
    transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    transaction = signTransaction(ecKey, transaction);
    System.out.println(
        "trigger txid = " + ByteArray
            .toHexString(Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData()
                .toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    }
  }


  /**
   * constructor.
   */

  public static TransactionExtention triggerContractForExtention(byte[] contractAddress,
      String method, String argsStr,
      Boolean isHex, long callValue, long feeLimit, String tokenId, long tokenValue,
      byte[] ownerAddress,
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
    if (argsStr.equalsIgnoreCase("#")) {
      logger.info("argsstr is #");
      argsStr = "";
    }

    byte[] owner = ownerAddress;
    byte[] input = Hex.decode(AbiUtil.parseMethod(method, argsStr, isHex));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    builder.setTokenId(Long.parseLong(tokenId));
    builder.setCallTokenValue(tokenValue);
    TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    return transactionExtention;
  }

  /**
   * constructor.
   */

  public static boolean sideChainCreateProposal(byte[] ownerAddress, String priKey,
      String mainGatewayAddress,
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
    SideChainProposalCreateContract.Builder builder = SideChainProposalCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.putAllParameters(parametersMap);

    SideChainProposalCreateContract contract = builder.build();
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
    transaction = signTransaction(ecKey, transaction, mainGateway, false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response.getResult();
  }

  /**
   * constructor.
   */
  public static boolean approveProposal(byte[] ownerAddress, String priKey,
      String mainGatewayAddress, long id,
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
    ProposalApproveContract.Builder builder = ProposalApproveContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setProposalId(id);
    builder.setIsAddApproval(isAddApproval);
    ProposalApproveContract contract = builder.build();
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
    transaction = signTransaction(ecKey, transaction, mainGateway, false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }

  /**
   * constructor.
   */


  public static String getMaingatewayAddr() {
    return Configuration.getByPath("testng.conf")
        .getString("gateway_address.chainIdAddress");
  }

  /**
   * constructor.
   */

  public static byte[] getMaingatewayByteAddr() {
    return WalletClient.decodeFromBase58Check(getMaingatewayAddr());

  }


  /**
   * constructor.
   */

  public static String sendcoinDelayedGetTxid(byte[] to, long amount, long delaySeconds,
      byte[] owner, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    TransferContract.Builder builder = TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);

    //transaction = TransactionUtils.setDelaySeconds(transaction, delaySeconds);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction ==null");
      return null;
    }
    transaction = signTransaction(ecKey, transaction);
    logger.info("Txid is " + ByteArray
        .toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction
                .getRawData().toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return ByteArray.toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
        .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
  }

  /**
   * constructor.
   */

  public static Boolean cancelDeferredTransactionById(String txid, byte[] owner, String priKey,
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
    /*Contract.CancelDeferredTransactionContract.Builder builder = Contract
      .CancelDeferredTransactionContract.newBuilder();
    builder.setTransactionId(ByteString.copyFrom(ByteArray.fromHexString(txid)));
    builder.setOwnerAddress(ByteString.copyFrom(owner));

    Contract.CancelDeferredTransactionContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull
     .createCancelDeferredTransactionContract(contract);

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
        "Cancel transaction before sign txid = " + ByteArray.toHexString(
        transactionExtention.getTxid().toByteArray()));

    transaction = signTransaction(ecKey, transaction);
    System.out.println(
        "Cancel transaction txid = " + ByteArray.toHexString(transactionExtention
        .getTxid().toByteArray()));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();*/
    return null;
  }

  /**
   * constructor.
   */

  public static String cancelDeferredTransactionByIdGetTxid(String txid, byte[] owner,
      String priKey,
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
    /* Contract.CancelDeferredTransactionContract.Builder builder = Contract
      .CancelDeferredTransactionContract.newBuilder();
    builder.setTransactionId(ByteString.copyFrom(ByteArray.fromHexString(txid)));
    builder.setOwnerAddress(ByteString.copyFrom(owner));

    Contract.CancelDeferredTransactionContract contract = builder.build();
   TransactionExtention transactionExtention = blockingStubFull
     .createCancelDeferredTransactionContract(contract);

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
    System.out.println(
        "Cancel transaction before sign txid = " + ByteArray.toHexString(
        transactionExtention.getTxid().toByteArray()));

    transaction = signTransaction(ecKey, transaction);
    System.out.println(
        "Cancel transaction txid = " + ByteArray.toHexString(transactionExtention
        .getTxid().toByteArray()));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return ByteArray.toHexString(Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));*/
    return null;
  }

  /**
   * constructor.
   */

  public static Boolean sendcoinDelayed(byte[] to, long amount, long delaySeconds, byte[] owner,
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

    TransferContract.Builder builder = TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);

    //transaction = TransactionUtils.setDelaySeconds(transaction, delaySeconds);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction ==null");
      return false;
    }
    transaction = signTransaction(ecKey, transaction);
    logger.info("Txid is " + ByteArray
        .toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction
                .getRawData().toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */

  public static String updateAccountDelayGetTxid(byte[] addressBytes, byte[] accountNameBytes,
      Long delaySeconds, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    AccountUpdateContract.Builder builder = AccountUpdateContract.newBuilder();
    ByteString basAddreess = ByteString.copyFrom(addressBytes);
    ByteString bsAccountName = ByteString.copyFrom(accountNameBytes);

    builder.setAccountName(bsAccountName);
    builder.setOwnerAddress(basAddreess);

    AccountUpdateContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.updateAccount(contract);
    //transaction = TransactionUtils.setDelaySeconds(transaction, delaySeconds);
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("Please check!!! transaction == null");
      return null;
    }
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    logger.info("Txid is " + ByteArray
        .toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction
                .getRawData().toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return ByteArray.toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
        .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
  }


  /**
   * constructor.
   */
  public static HashMap<String, String> getBycodeAbi(String solFile, String contractName) {
    final String compile = Configuration.getByPath("testng.conf")
        .getString("defaultParameter.solidityCompile");

    String dirPath = solFile.substring(solFile.lastIndexOf("/"), solFile.lastIndexOf("."));
    String outputPath = "src/test/resources/soliditycode/output" + dirPath;

    File binFile = new File(outputPath + "/" + contractName + ".bin");
    File abiFile = new File(outputPath + "/" + contractName + ".abi");
    if (binFile.exists()) {
      binFile.delete();
    }
    if (abiFile.exists()) {
      abiFile.delete();
    }

    HashMap<String, String> retMap = new HashMap<>();
    String absolutePath = System.getProperty("user.dir");
    logger.debug("absolutePath: " + absolutePath);
    logger.debug("solFile: " + solFile);
    logger.debug("outputPath: " + outputPath);
    String cmd =
        compile + " --optimize --bin --abi --overwrite " + solFile + " -o "
            + absolutePath + "/" + outputPath;
    logger.debug("cmd: " + cmd);

    String byteCode = null;
    String abI = null;

    // compile solidity file
    try {
      exec(cmd);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    // get byteCode and ABI
    try {
      byteCode = fileRead(outputPath + "/" + contractName + ".bin", false);
      retMap.put("byteCode", byteCode);
      logger.debug("byteCode: " + byteCode);
      abI = fileRead(outputPath + "/" + contractName + ".abi", false);
      retMap.put("abI", abI);
      logger.debug("abI: " + abI);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return retMap;
  }

  /**
   * constructor.
   */
  public static String exec(String command) throws InterruptedException {
    String returnString = "";
    Process pro = null;
    Runtime runTime = Runtime.getRuntime();
    if (runTime == null) {
      logger.error("Create runtime false!");
    }
    try {
      pro = runTime.exec(command);
      BufferedReader input = new BufferedReader(new InputStreamReader(pro.getInputStream()));
      PrintWriter output = new PrintWriter(new OutputStreamWriter(pro.getOutputStream()));
      String line;
      while ((line = input.readLine()) != null) {
        returnString = returnString + line + "\n";
      }
      input.close();
      output.close();
      pro.destroy();
    } catch (IOException ex) {
      logger.error(null, ex);
    }
    return returnString;
  }


  /**
   * constructor.
   */
  public static String fileRead(String filePath, boolean isLibrary) throws Exception {
    File file = new File(filePath);
    FileReader reader = new FileReader(file);
    BufferedReader breader = new BufferedReader(reader);
    StringBuilder sb = new StringBuilder();
    String s = "";
    if (!isLibrary) {
      if ((s = breader.readLine()) != null) {
        sb.append(s);
      }
      breader.close();
    } else {
      String fistLine = breader.readLine();
      breader.readLine();
      if ((s = breader.readLine()) != null && !s.equals("")) {
        s = s.substring(s.indexOf("-> ") + 3);
        sb.append(s + ":");
      } else {
        s = fistLine.substring(fistLine.indexOf("__") + 2, fistLine.lastIndexOf("__"));
        sb.append(s + ":");
      }
      breader.close();
    }
    return sb.toString();
  }

  /**
   * constructor.
   */

  public static String getAddressString(String key) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    return Base58.encode58Check(getFinalAddress(key));
  }

  /**
   * constructor.
   */

  public static Boolean freezeBalanceGetEnergy(byte[] addRess, long freezeBalance,
      long freezeDuration,
      int resourceCode, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    byte[] address = addRess;
    long frozenBalance = freezeBalance;
    long frozenDuration = freezeDuration;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    FreezeBalanceContract.Builder builder = FreezeBalanceContract.newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);

    builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozenBalance)
        .setFrozenDuration(frozenDuration).setResourceValue(resourceCode);

    FreezeBalanceContract contract = builder.build();

    GrpcAPI.TransactionExtention transactionExtention = blockingStubFull.freezeBalance2(contract);
    if (transactionExtention == null) {
      return false;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return false;
    } else {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
    }
    Transaction transaction = transactionExtention.getTransaction();
    transaction = TransactionUtils.setTimestamp(transaction);
//    transaction = TransactionUtils.sign(transaction, ecKey, getMaingatewayByteAddr(), false);
    transaction = signTransaction(ecKey, transaction);

    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */
  public static GrpcAPI.AccountResourceMessage getAccountResource(byte[] address,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString addressBs = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBs).build();
    return blockingStubFull.getAccountResource(request);
  }


  /**
   * constructor.
   */

  public static boolean updateSetting(byte[] contractAddress, long consumeUserResourcePercent,
      String priKey, byte[] ownerAddress, WalletGrpc
      .WalletBlockingStub blockingStubFull) {
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
    UpdateSettingContract.Builder builder = UpdateSettingContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent);

    UpdateSettingContract updateSettingContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull
        .updateSetting(updateSettingContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return false;
    }
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */
  public static byte[] deployContract(String contractName, String abiString, String code,
      String data, Long feeLimit, long value,
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
    ABI abi = jsonStr2Abi(abiString);
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

    byte[] contractAddress = generateContractAddress(transactionExtention.getTransaction(), owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));

    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return contractAddress;
    }
  }

  /**
   * constructor.
   */
  public static byte[] deployContract(String contractName, String abiString, String code,
      String data, Long feeLimit, long value,
      long consumeUserResourcePercent, String libraryAddress, String priKey, byte[] ownerAddress,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    return deployContract(contractName, abiString, code, data, feeLimit, value,
        consumeUserResourcePercent, 1000L, "0", 0L, libraryAddress,
        priKey, ownerAddress, blockingStubFull);
  }

  /**
   * constructor.
   */

  public static byte[] deployContractForLibrary(String contractName, String abiString, String code,
      String data, Long feeLimit, long value,
      long consumeUserResourcePercent, String libraryAddress, String priKey, byte[] ownerAddress,
      String compilerVersion,
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
    builder.setOriginEnergyLimit(1000L);

    if (value != 0) {

      builder.setCallValue(value);
    }

    byte[] byteCode;
    if (null != libraryAddress) {
      if (compilerVersion.equals("v5") || compilerVersion.equals("V5")) {
        byteCode = replaceLibraryAddresscompilerVersion(code, libraryAddress, "v5");
      } else {
        //old version
        byteCode = replaceLibraryAddresscompilerVersion(code, libraryAddress, null);
      }

    } else {
      byteCode = Hex.decode(code);
    }
    builder.setBytecode(ByteString.copyFrom(byteCode));

    Builder contractBuilder = CreateSmartContract.newBuilder();
    contractBuilder.setOwnerAddress(ByteString.copyFrom(owner));
    contractBuilder.setCallTokenValue(0);
    contractBuilder.setTokenId(Long.parseLong("0"));
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

    byte[] contractAddress = generateContractAddress(transactionExtention.getTransaction(), owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    System.out.println(
        "txid = " + ByteArray.toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));

    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return contractAddress;
    }

  }

  /**
   * constructor.
   */
  public static HashMap<String, String> getBycodeAbiForLibrary(String solFile,
      String contractName) {
    HashMap retMap = null;
    String dirPath = solFile.substring(solFile.lastIndexOf("/"), solFile.lastIndexOf("."));
    String outputPath = "src/test/resources/soliditycode/output" + dirPath;
    try {
      retMap = getBycodeAbi(solFile, contractName);
      String library = fileRead(outputPath + "/" + contractName + ".bin", true);
      retMap.put("library", library);
      logger.debug("library: " + library);
    } catch (Exception e) {
      e.printStackTrace();
    }

    return retMap;
  }


  private static byte[] replaceLibraryAddresscompilerVersion(String code, String libraryAddressPair,
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
        libraryAddressHex = (new String(Hex.encode(Wallet.decodeFromBase58Check(addr)),
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

  /**
   * constructor.
   */

  public static Protocol.Account queryAccount(String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    byte[] address;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ECKey ecKey = temKey;
    if (ecKey == null) {
      String pubKey = loadPubKey(); //04 PubKey[128]
      if (StringUtils.isEmpty(pubKey)) {
        logger.warn("Warning: QueryAccount failed, no wallet address !!");
        return null;
      }
      byte[] pubKeyAsc = pubKey.getBytes();
      byte[] pubKeyHex = Hex.decode(pubKeyAsc);
      ecKey = ECKey.fromPublicOnly(pubKeyHex);
    }
    return grpcQueryAccount(ecKey.getAddress(), blockingStubFull);
  }


  /**
   * constructor.
   */

  public static Protocol.Account grpcQueryAccount(byte[] address,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString addressBs = ByteString.copyFrom(address);
    Protocol.Account request = Protocol.Account.newBuilder().setAddress(addressBs).build();
    return blockingStubFull.getAccount(request);
  }

  /**
   * constructor.
   */

  public static String loadPubKey() {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    char[] buf = new char[0x100];
    return String.valueOf(buf, 32, 130);
  }


  /**
   * constructor.
   */
  public static TransactionExtention triggerConstantContractForExtention(byte[] contractAddress,
      String method,
      String argsStr,
      Boolean isHex, long callValue, long feeLimit, String tokenId, long tokenValue,
      byte[] ownerAddress,
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
    if (argsStr.equalsIgnoreCase("#")) {
      logger.info("argsstr is #");
      argsStr = "";
    }

    byte[] owner = ownerAddress;
    byte[] input = Hex.decode(AbiUtil.parseMethod(method, argsStr, isHex));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    builder.setTokenId(Long.parseLong(tokenId));
    builder.setCallTokenValue(tokenValue);
    TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull
        .triggerConstantContract(triggerContract);
    return transactionExtention;

  }


  /**
   * constructor.
   */
  public static String clearContractAbi(byte[] contractAddress,
      byte[] ownerAddress,
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

    ClearABIContract.Builder builder = ClearABIContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));

    ClearABIContract clearAbiContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull
        .clearContractABI(clearAbiContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0
        && transactionExtention.getConstantResult(0) != null
        && transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
      return null;
    }

    final TransactionExtention.Builder texBuilder = TransactionExtention.newBuilder();
    Transaction.Builder transBuilder = Transaction.newBuilder();
    Transaction.raw.Builder rawBuilder = transactionExtention.getTransaction().getRawData()
        .toBuilder();
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    System.out.println(
        "trigger txid = " + ByteArray.toHexString(
            stest.tron.wallet.common.client.utils.Sha256Hash
                .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData()
                    .toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    }
  }


  /**
   * constructor.
   */

  public static Boolean freezeBalance(byte[] addRess, long freezeBalance, long freezeDuration,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    byte[] address = addRess;
    long frozenBalance = freezeBalance;
    long frozenDuration = freezeDuration;
    //String priKey = testKey002;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    Protocol.Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI
        .EmptyMessage.newBuilder().build());
    final Long beforeBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Protocol.Account beforeFronzen = queryAccount(priKey, blockingStubFull);
    Long beforeFrozenBalance = 0L;
    //Long beforeBandwidth     = beforeFronzen.getBandwidth();
    if (beforeFronzen.getFrozenCount() != 0) {
      beforeFrozenBalance = beforeFronzen.getFrozen(0).getFrozenBalance();
      //beforeBandwidth     = beforeFronzen.getBandwidth();
      //logger.info(Long.toString(beforeFronzen.getBandwidth()));
      logger.info(Long.toString(beforeFronzen.getFrozen(0).getFrozenBalance()));
    }

    FreezeBalanceContract.Builder builder = FreezeBalanceContract.newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);

    builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozenBalance)
        .setFrozenDuration(frozenDuration);

    FreezeBalanceContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.freezeBalance(contract);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction = null");
      return false;
    }

    transaction = TransactionUtils.setTimestamp(transaction);
    transaction = TransactionUtils.sign(transaction, ecKey, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response.getResult();
    /*    Long afterBlockNum = 0L;

    while (afterBlockNum < beforeBlockNum) {
      Protocol.Block currentBlock1 = blockingStubFull.getNowBlock(GrpcAPI
          .EmptyMessage.newBuilder().build());
      afterBlockNum = currentBlock1.getBlockHeader().getRawData().getNumber();
    }

    Protocol.Account afterFronzen = queryAccount(priKey, blockingStubFull);
    Long afterFrozenBalance = afterFronzen.getFrozen(0).getFrozenBalance();
    logger.info(Long.toString(afterFronzen.getFrozen(0).getFrozenBalance()));
    logger.info("beforefronen" + beforeFrozenBalance.toString() + "    afterfronzen"
        + afterFrozenBalance.toString());
    Assert.assertTrue(afterFrozenBalance - beforeFrozenBalance == freezeBalance);*/
  }


  public static String sendcoinGetTransactionId(byte[] to, long amount, byte[] owner, String priKey,
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
    //Protocol.Account search = queryAccount(priKey, blockingStubFull);

    TransferContract.Builder builder = TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction ==null");
      return null;
    }
    //Test raw data
    /*    Protocol.Transaction.raw.Builder builder1 = transaction.getRawData().toBuilder();
    builder1.setData(ByteString.copyFromUtf8("12345678"));
    Transaction.Builder builder2 = transaction.toBuilder();
    builder2.setRawData(builder1);
    transaction = builder2.build();*/

    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      //logger.info(ByteArray.toStr(response.getMessage().toByteArray()));
      return null;
    } else {
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    }
  }


  /**
   * constructor.
   */

  public static Boolean unFreezeBalance(byte[] address, String priKey, int resourceCode,
      byte[] receiverAddress, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    UnfreezeBalanceContract.Builder builder = UnfreezeBalanceContract
        .newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddreess).setResourceValue(resourceCode);
    if (receiverAddress != null) {
      ByteString receiverAddressBytes = ByteString.copyFrom(receiverAddress);
      builder.setReceiverAddress(receiverAddressBytes);
    }

    UnfreezeBalanceContract contract = builder.build();
    Transaction transaction = blockingStubFull.unfreezeBalance(contract);
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response.getResult();
  }


  /**
   * constructor.
   */

  public static Boolean freezeBalanceForReceiver(byte[] addRess, long freezeBalance,
      long freezeDuration, int resourceCode, ByteString receiverAddressBytes, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    byte[] address = addRess;
    long frozenBalance = freezeBalance;
    long frozenDuration = freezeDuration;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    FreezeBalanceContract.Builder builder = FreezeBalanceContract.newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);

    builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozenBalance)
        .setFrozenDuration(frozenDuration).setResourceValue(resourceCode);
    builder.setReceiverAddress(receiverAddressBytes);
    FreezeBalanceContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.freezeBalance(contract);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction = null");
      return false;
    }
    transaction = TransactionUtils.setTimestamp(transaction);
    transaction = TransactionUtils.sign(transaction, ecKey, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */
  public static long getFreezeBalanceCount(byte[] accountAddress, String ecKey, Long targetEnergy,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    //Precision change as the entire network freezes
    GrpcAPI.AccountResourceMessage resourceInfo = getAccountResource(accountAddress,
        blockingStubFull);

    Account info = queryAccount(accountAddress, blockingStubFull);

    Account getAccount = queryAccount(ecKey, blockingStubFull);

    long balance = info.getBalance();
    long frozenBalance = info.getAccountResource().getFrozenBalanceForEnergy().getFrozenBalance();
    long totalEnergyLimit = resourceInfo.getTotalEnergyLimit();
    long totalEnergyWeight = resourceInfo.getTotalEnergyWeight();
    long energyUsed = resourceInfo.getEnergyUsed();
    long energyLimit = resourceInfo.getEnergyLimit();

    if (energyUsed > energyLimit) {
      targetEnergy = energyUsed - energyLimit + targetEnergy;
    }

    if (totalEnergyWeight == 0) {
      return 1000_000L;
    }

    // totalEnergyLimit / (totalEnergyWeight + needBalance) = needEnergy / needBalance
    final BigInteger totalEnergyWeightBi = BigInteger.valueOf(totalEnergyWeight);
    long needBalance = totalEnergyWeightBi.multiply(BigInteger.valueOf(1_000_000))
        .multiply(BigInteger.valueOf(targetEnergy))
        .divide(BigInteger.valueOf(totalEnergyLimit - targetEnergy)).longValue();

    logger.info("getFreezeBalanceCount, needBalance: " + needBalance);

    if (needBalance < 1000000L) {
      needBalance = 1000000L;
      logger.info(
          "getFreezeBalanceCount, needBalance less than 1 TRX, modify to: " + needBalance);
    }
    return needBalance;
  }


  /**
   * constructor.
   */

  public static String updateSettingDelayGetTxid(byte[] contractAddress,
      long consumeUserResourcePercent, long delaySeconds,
      String priKey, byte[] ownerAddress, WalletGrpc
      .WalletBlockingStub blockingStubFull) {
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
    UpdateSettingContract.Builder builder = UpdateSettingContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setConsumeUserResourcePercent(consumeUserResourcePercent);

    UpdateSettingContract updateSettingContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull
        .updateSetting(updateSettingContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return null;
    }
    /*    transactionExtention = TransactionUtils.setDelaySecondsToExtension(
    transactionExtention, delaySeconds);
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
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    transaction = signTransaction(ecKey, transaction);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return ByteArray.toHexString(Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));*/
    return null;
  }


  /**
   * constructor.
   */

  public static String setAccountIdDelayGetTxid(byte[] accountIdBytes, long delaySeconds,
      byte[] ownerAddress, String priKey,
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
    SetAccountIdContract.Builder builder = SetAccountIdContract.newBuilder();
    ByteString bsAddress = ByteString.copyFrom(owner);
    ByteString bsAccountId = ByteString.copyFrom(accountIdBytes);
    builder.setAccountId(bsAccountId);
    builder.setOwnerAddress(bsAddress);
    SetAccountIdContract contract = builder.build();
    Transaction transaction = blockingStubFull.setAccountId(contract);
    //transaction = TransactionUtils.setDelaySeconds(transaction, delaySeconds);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction == null");
    }
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    logger.info("Txid is " + ByteArray
        .toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction
                .getRawData().toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return ByteArray.toHexString(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
  }


  /**
   * constructor.
   */
  public static String updateEnergyLimitDelayGetTxid(byte[] contractAddress,
      long originEnergyLimit, long delaySeconds, String priKey, byte[] ownerAddress,
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
    UpdateEnergyLimitContract.Builder builder = UpdateEnergyLimitContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setOriginEnergyLimit(originEnergyLimit);

    UpdateEnergyLimitContract updateEnergyLimitContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull
        .updateEnergyLimit(updateEnergyLimitContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return null;
    }
    //transactionExtention = TransactionUtils.setDelaySecondsToExtension(
    // transactionExtention, delaySeconds);
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
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return ByteArray.toHexString(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
  }


  /**
   * constructor.
   */
  public static boolean updateEnergyLimit(byte[] contractAddress, long originEnergyLimit,
      String priKey, byte[] ownerAddress, WalletGrpc
      .WalletBlockingStub blockingStubFull) {
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
    UpdateEnergyLimitContract.Builder builder = UpdateEnergyLimitContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setOriginEnergyLimit(originEnergyLimit);

    UpdateEnergyLimitContract updateEnergyLimitContract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull
        .updateEnergyLimit(updateEnergyLimitContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create trx failed!");
      if (transactionExtention != null) {
        System.out.println("Code = " + transactionExtention.getResult().getCode());
        System.out
            .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      }
      return false;
    }
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */

  public static String createAccountDelayGetTxid(byte[] ownerAddress, byte[] newAddress,
      Long delaySeconds, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
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
    AccountCreateContract.Builder builder = AccountCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setAccountAddress(ByteString.copyFrom(newAddress));
    AccountCreateContract contract = builder.build();
    Transaction transaction = blockingStubFull.createAccount(contract);
    //transaction = TransactionUtils.setDelaySeconds(transaction, delaySeconds);
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction == null");
    }
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    logger.info("Txid is " + ByteArray
        .toHexString(stest.tron.wallet.common.client.utils.Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction
                .getRawData().toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return ByteArray.toHexString(
        Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));

  }


  /**
   * constructor.
   */
  public static String create2(String[] parameters) {
    if (parameters == null || parameters.length != 3) {
      logger.error("create2 needs 3 parameter:\ncreate2 address code salt");
      return null;
    }

    byte[] address = WalletClient.decodeFromBase58Check(parameters[0]);
    if (!WalletClient.addressValid(address)) {
      logger.error("length of address must be 21 bytes.");
      return null;
    }

    byte[] code = Hex.decode(parameters[1]);
    byte[] temp = Longs.toByteArray(Long.parseLong(parameters[2]));
    if (temp.length != 8) {
      logger.error("Invalid salt!");
      return null;
    }
    byte[] salt = new byte[32];
    System.arraycopy(temp, 0, salt, 24, 8);

    byte[] mergedData = ByteUtil.merge(address, salt, Hash.sha3(code));
    String create2Address = Base58.encode58Check(Hash.sha3omit12(mergedData));

    logger.info("create2 Address: " + create2Address);

    return create2Address;
  }


  /**
   * constructor.
   */
  public static TransactionExtention clearContractAbiForExtention(byte[] contractAddress,
      byte[] ownerAddress,
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

    ClearABIContract.Builder builder = ClearABIContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));

    ClearABIContract clearAbiContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull
        .clearContractABI(clearAbiContract);
    return transactionExtention;

  }


  /**
   * constructor.
   */
  public static GrpcAPI.Return accountPermissionUpdateForResponse(String permissionJson,
      byte[] owner, String priKey,
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

    AccountPermissionUpdateContract.Builder builder = AccountPermissionUpdateContract.newBuilder();

    JSONObject permissions = JSONObject.parseObject(permissionJson);
    JSONObject ownerpermission = permissions.getJSONObject("owner_permission");
    JSONObject witnesspermission = permissions.getJSONObject("witness_permission");
    JSONArray activepermissions = permissions.getJSONArray("active_permissions");

    if (ownerpermission != null) {
      Protocol.Permission ownerPermission = json2Permission(ownerpermission);
      builder.setOwner(ownerPermission);
    }
    if (witnesspermission != null) {
      Protocol.Permission witnessPermission = json2Permission(witnesspermission);
      builder.setWitness(witnessPermission);
    }
    if (activepermissions != null) {
      List<Protocol.Permission> activePermissionList = new ArrayList<>();
      for (int j = 0; j < activepermissions.size(); j++) {
        JSONObject permission = activepermissions.getJSONObject(j);
        activePermissionList.add(json2Permission(permission));
      }
      builder.addAllActives(activePermissionList);
    }
    builder.setOwnerAddress(ByteString.copyFrom(owner));

    AccountPermissionUpdateContract contract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.accountPermissionUpdate(contract);
    if (transactionExtention == null) {
      return null;
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return ret;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return ret;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response;
  }

  private static Protocol.Permission json2Permission(JSONObject json) {
    Protocol.Permission.Builder permissionBuilder = Protocol.Permission.newBuilder();
    if (json.containsKey("type")) {
      int type = json.getInteger("type");
      permissionBuilder.setTypeValue(type);
    }
    if (json.containsKey("permission_name")) {
      String permissionName = json.getString("permission_name");
      permissionBuilder.setPermissionName(permissionName);
    }
    if (json.containsKey("threshold")) {
      //long threshold = json.getLong("threshold");
      long threshold = Long.parseLong(json.getString("threshold"));
      permissionBuilder.setThreshold(threshold);
    }
    if (json.containsKey("parent_id")) {
      int parentId = json.getInteger("parent_id");
      permissionBuilder.setParentId(parentId);
    }
    if (json.containsKey("operations")) {
      byte[] operations = ByteArray.fromHexString(json.getString("operations"));
      permissionBuilder.setOperations(ByteString.copyFrom(operations));
    }
    if (json.containsKey("keys")) {
      JSONArray keys = json.getJSONArray("keys");
      List<Protocol.Key> keyList = new ArrayList<>();
      for (int i = 0; i < keys.size(); i++) {
        Protocol.Key.Builder keyBuilder = Protocol.Key.newBuilder();
        JSONObject key = keys.getJSONObject(i);
        String address = key.getString("address");
        long weight = Long.parseLong(key.getString("weight"));
        //long weight = key.getLong("weight");
        //keyBuilder.setAddress(ByteString.copyFrom(address.getBytes()));
        keyBuilder.setAddress(ByteString.copyFrom(WalletClient.decodeFromBase58Check(address)));
        keyBuilder.setWeight(weight);
        keyList.add(keyBuilder.build());
      }
      permissionBuilder.addAllKeys(keyList);
    }
    return permissionBuilder.build();
  }


  /**
   * constructor.
   */
  public static Transaction addTransactionSign(Transaction transaction, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    ECKey ecKey = temKey;

    Transaction.Builder transactionBuilderSigned = transaction.toBuilder();
    byte[] hash = Sha256Hash
        .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray());

    ECKey.ECDSASignature signature = ecKey.sign(hash);
    ByteString bsSign = ByteString.copyFrom(signature.toByteArray());
    transactionBuilderSigned.addSignature(bsSign);
    transaction = transactionBuilderSigned.build();
    return transaction;
  }


  public static GrpcAPI.TransactionApprovedList getTransactionApprovedList(Transaction transaction,
      WalletGrpc
          .WalletBlockingStub blockingStubFull) {
    return blockingStubFull.getTransactionApprovedList(transaction);
  }


  /**
   * constructor.
   */

  public static boolean waitSolidityNodeSynFullNodeData(WalletGrpc.WalletBlockingStub
      blockingStubFull, WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Block solidityCurrentBlock = blockingStubSolidity.getNowBlock(GrpcAPI.EmptyMessage
        .newBuilder().build());
    Integer wait = 0;
    logger.info("Fullnode block num is " + Long.toString(currentBlock
        .getBlockHeader().getRawData().getNumber()));
    while (solidityCurrentBlock.getBlockHeader().getRawData().getNumber()
        < currentBlock.getBlockHeader().getRawData().getNumber() + 1 && wait <= 10) {
      try {
        Thread.sleep(3000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      logger.info("Soliditynode num is " + Long.toString(solidityCurrentBlock
          .getBlockHeader().getRawData().getNumber()));
      solidityCurrentBlock = blockingStubSolidity.getNowBlock(GrpcAPI.EmptyMessage.newBuilder()
          .build());
      if (wait == 10) {
        logger.info("Didn't syn,skip to next case.");
        return false;
      }
      wait++;
    }
    return true;
  }


  /**
   * constructor.
   */

  public static Account queryAccount(byte[] address, WalletSolidityGrpc
      .WalletSolidityBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString addressBs = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBs).build();
    return blockingStubFull.getAccount(request);
  }


  /**
   * constructor.
   */

  public static boolean sellStorage(long quantity, byte[] address,
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

    SellStorageContract.Builder builder = SellStorageContract.newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setStorageBytes(quantity);
    SellStorageContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.sellStorage(contract);
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
    transaction = signTransaction(ecKey, transaction);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */

  public static boolean buyStorage(long quantity, byte[] address,
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

    BuyStorageContract.Builder builder = BuyStorageContract.newBuilder();
    ByteString byteAddress = ByteString.copyFrom(address);
    builder.setOwnerAddress(byteAddress).setQuant(quantity);
    BuyStorageContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.buyStorage(contract);
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
    transaction = signTransaction(ecKey, transaction);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  public static Optional<GrpcAPI.ExchangeList> getExchangeList(WalletGrpc.WalletBlockingStub
      blockingStubFull) {
    GrpcAPI.ExchangeList exchangeList = blockingStubFull
        .listExchanges(GrpcAPI.EmptyMessage.newBuilder().build());
    return Optional.ofNullable(exchangeList);
  }

  public static Optional<ExchangeList> getExchangeList(WalletSolidityGrpc
      .WalletSolidityBlockingStub blockingStubSolidity) {
    ExchangeList exchangeList = blockingStubSolidity.listExchanges(EmptyMessage.newBuilder()
        .build());
    return Optional.ofNullable(exchangeList);
  }

  /**
   * constructor.
   */

  public static Return sendcoin2(byte[] to, long amount, byte[] owner, String priKey,
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
    //Protocol.Account search = queryAccount(priKey, blockingStubFull);

    TransferContract.Builder builder = TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.createTransaction2(contract);
    if (transactionExtention == null) {
      return transactionExtention.getResult();
    }

    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return ret;
    } else {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return transactionExtention.getResult();
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));

    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      //      logger.info(ByteArray.toStr(response.getMessage().toByteArray()));
      return response;
    }
    return ret;
  }


  /**
   * constructor.
   */

  public static Return freezeBalance2(byte[] addRess, long freezeBalance, long freezeDuration,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    byte[] address = addRess;
    long frozenBalance = freezeBalance;
    long frozenDuration = freezeDuration;
    //String priKey = testKey002;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;
    Protocol.Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI
        .EmptyMessage.newBuilder().build());
    final Long beforeBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Protocol.Account beforeFronzen = queryAccount(priKey, blockingStubFull);
    Long beforeFrozenBalance = 0L;
    //Long beforeBandwidth     = beforeFronzen.getBandwidth();
    if (beforeFronzen.getFrozenCount() != 0) {
      beforeFrozenBalance = beforeFronzen.getFrozen(0).getFrozenBalance();
      //beforeBandwidth     = beforeFronzen.getBandwidth();
      //logger.info(Long.toString(beforeFronzen.getBandwidth()));
      logger.info(Long.toString(beforeFronzen.getFrozen(0).getFrozenBalance()));
    }

    FreezeBalanceContract.Builder builder = FreezeBalanceContract.newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);

    builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozenBalance)
        .setFrozenDuration(frozenDuration);

    FreezeBalanceContract contract = builder.build();

    GrpcAPI.TransactionExtention transactionExtention = blockingStubFull.freezeBalance2(contract);
    if (transactionExtention == null) {
      return transactionExtention.getResult();
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return ret;
    } else {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return transactionExtention.getResult();
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));

    transaction = TransactionUtils.setTimestamp(transaction);
    transaction = TransactionUtils.sign(transaction, ecKey, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    if (response.getResult() == false) {
      return response;
    }

    Long afterBlockNum = 0L;

    while (afterBlockNum < beforeBlockNum) {
      Protocol.Block currentBlock1 = blockingStubFull.getNowBlock(GrpcAPI
          .EmptyMessage.newBuilder().build());
      afterBlockNum = currentBlock1.getBlockHeader().getRawData().getNumber();
    }

    Protocol.Account afterFronzen = queryAccount(priKey, blockingStubFull);
    Long afterFrozenBalance = afterFronzen.getFrozen(0).getFrozenBalance();
    logger.info(Long.toString(afterFronzen.getFrozen(0).getFrozenBalance()));
    logger.info("beforefronen" + beforeFrozenBalance.toString() + "    afterfronzen"
        + afterFrozenBalance.toString());
    Assert.assertTrue(afterFrozenBalance - beforeFrozenBalance == freezeBalance);
    return ret;
  }


  public static long printTransactionRow(Transaction.raw raw) {
    long timestamp = raw.getTimestamp();

    return timestamp;
  }


  /**
   * constructor.
   */

  public static Protocol.Block getBlock(long blockNum,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    GrpcAPI.NumberMessage.Builder builder = GrpcAPI.NumberMessage.newBuilder();
    builder.setNum(blockNum);
    return blockingStubFull.getBlockByNum(builder.build());
  }


  /**
   * constructor.
   */

  public static GrpcAPI.AccountNetMessage getAccountNet(byte[] address,
      WalletGrpc.WalletBlockingStub
          blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString addressBs = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBs).build();
    return blockingStubFull.getAccountNet(request);
  }

  /**
   * constructor.
   */
  public static TransactionExtention mappingTrc721ForExtention(byte[] mainGatewayAddress,
      String trxHash, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {

    String methodStr = "mappingTRC721(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    TransactionExtention transactionExtention = triggerContractForExtention(mainGatewayAddress,
        methodStr, argsStr, false, 0, feeLimit, "0", 0,
        ownerAddress, priKey, blockingStubFull);
    return transactionExtention;
  }

  public static String withdrawTrc10(
      String tokenId, String tokenValue, String mainGatewayAddr, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {

//    byte[] txData1 = PublicMethed.sideSignTrc10Data(trc10, tokenValue, ownerAddress, priKey,
//        WalletClient.decodeFromBase58Check(mainGatewayAddr), blockingStubFull, 0);
    String methodStr = "withdrawTRC10(uint256,uint256)";

    long tokenValue1 = Long.parseLong(tokenValue);
    String inputParam = tokenId + "," + tokenValue;
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input1,
            feeLimit, tokenValue1, tokenId, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid1:" + txid1);
    return txid1;
  }

  public static String withdrawTrc10fee(
      String tokenId, String tokenValue, String mainGatewayAddr, String sideGatewayAddress,
      long callValue, long withdrawfee, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    long callValue1 = callValue + withdrawfee;
//    byte[] txData1 = PublicMethed.sideSignTrc10Data(trc10, tokenValue, ownerAddress, priKey,
//        WalletClient.decodeFromBase58Check(mainGatewayAddr), blockingStubFull, 0);
    String methodStr = "withdrawTRC10(uint256,uint256)";
    long tokenValue1 = Long.parseLong(tokenValue);
    String inputParam = tokenId + "," + tokenValue;

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue1,
            input1,
            feeLimit, tokenValue1, tokenId, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid1:" + txid1);
    return txid1;
  }


  /**
   * constructor.
   */

  public static Return createAccount2(byte[] ownerAddress, byte[] newAddress, String priKey,
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
    AccountCreateContract.Builder builder = AccountCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setAccountAddress(ByteString.copyFrom(newAddress));
    AccountCreateContract contract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.createAccount2(contract);

    if (transactionExtention == null) {
      return transactionExtention.getResult();
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return ret;
    } else {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return transactionExtention.getResult();
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));

    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      //logger.info(ByteArray.toStr(response.getMessage().toByteArray()));
      return response;
    }
    return ret;
  }


  /**
   * constructor.
   */


  public static ArrayList<String> getAddressInfo(String key) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ArrayList<String> accountList = new ArrayList<String>();
    accountList.add(key);
    accountList.add(ByteArray.toHexString(getFinalAddress(key)));
    accountList.add(Base58.encode58Check(getFinalAddress(key)));
    return accountList;
  }

  public static GrpcAPI.Return withdrawTrcForReturn(String tokenId, String tokenValue,
      String ChainIdAddress, String sideGatewayAddress,
      long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {

    String methodStr = "withdrawTRC10(uint256,uint256)";

    long tokenValue1 = Long.parseLong(tokenValue);
    String inputParam = tokenId + "," + tokenValue;
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));

    Return aReturn = PublicMethed
        .triggerContractSideChainForReturn(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(ChainIdAddress),
            callValue,
            input1,
            feeLimit, tokenValue1, tokenId, ownerAddress, priKey, blockingsideStubFull);
    return aReturn;
  }


  /**
   * constructor.
   */

  public static String triggerParamListContract(byte[] contractAddress, String method,
      List<Object> params,
      Boolean isHex, long callValue, long feeLimit, String tokenId, long tokenValue,
      byte[] ownerAddress,
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
    byte[] input = Hex.decode(AbiUtil.parseMethod(method, params));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    builder.setTokenId(Long.parseLong(tokenId));
    builder.setCallTokenValue(tokenValue);
    TriggerSmartContract triggerContract = builder.build();

    GrpcAPI.TransactionExtention transactionExtention = blockingStubFull
        .triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }
    Protocol.Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0
        && transactionExtention.getConstantResult(0) != null
        && transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
      return null;
    }

    final GrpcAPI.TransactionExtention.Builder texBuilder = GrpcAPI.TransactionExtention
        .newBuilder();
    Protocol.Transaction.Builder transBuilder = Protocol.Transaction.newBuilder();
    Protocol.Transaction.raw.Builder rawBuilder = transactionExtention.getTransaction().getRawData()
        .toBuilder();
    rawBuilder.setFeeLimit(feeLimit);
    transBuilder.setRawData(rawBuilder);
    for (int i = 0; i < transactionExtention.getTransaction().getSignatureCount(); i++) {
      ByteString s = transactionExtention.getTransaction().getSignature(i);
      transBuilder.setSignature(i, s);
    }
    for (int i = 0; i < transactionExtention.getTransaction().getRetCount(); i++) {
      Protocol.Transaction.Result r = transactionExtention.getTransaction().getRet(i);
      transBuilder.setRet(i, r);
    }
    texBuilder.setTransaction(transBuilder);
    texBuilder.setResult(transactionExtention.getResult());
    texBuilder.setTxid(transactionExtention.getTxid());
    transactionExtention = texBuilder.build();
    if (transactionExtention == null) {
      return null;
    }
    GrpcAPI.Return ret = transactionExtention.getResult();
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    System.out.println(
        "trigger txid = " + ByteArray.toHexString(
            stest.tron.wallet.common.client.utils.Sha256Hash
                .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData()
                    .toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      return ByteArray.toHexString(
          Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray()));
    }


  }


  /**
   * constructor.
   */

  public static boolean createAccount(byte[] ownerAddress, byte[] newAddress, String priKey,
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
    AccountCreateContract.Builder builder = AccountCreateContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setAccountAddress(ByteString.copyFrom(newAddress));
    AccountCreateContract contract = builder.build();
    Transaction transaction = blockingStubFull.createAccount(contract);
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction == null");
    }
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }

  public static String retryDeposit(
      String mainGatewayAddr,
      String nonce, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    String retryMethodStr = "retryDeposit(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(retryMethodStr, nonce, true));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGatewayAddr),
            0,
            input,
            feeLimit, 0, null, ownerAddress, priKey, blockingStubFull);
    return txid;

  }

  /**
   * constructor.
   */
  public static String retryWithdraw(
      String ChainIdAddress, String sideGatewayAddress, String nonce, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    String retryMethodStr = "retryWithdraw(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(retryMethodStr, nonce, true));

    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(ChainIdAddress),
            0,
            input,
            feeLimit, 0, null, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid1:" + txid1);
    return txid1;

  }


  /**
   * constructor.
   */

  public static Account queryAccountByAddress(byte[] address,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString addressBs = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBs).build();
    return blockingStubFull.getAccount(request);
  }

  /**
   * constructor.
   */

  public static Optional<DelegatedResourceAccountIndex> getDelegatedResourceAccountIndex(
      byte[] address, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);

    ByteString addressBs = ByteString.copyFrom(address);

    BytesMessage bytesMessage = BytesMessage.newBuilder().setValue(addressBs).build();

    DelegatedResourceAccountIndex accountIndex = blockingStubFull
        .getDelegatedResourceAccountIndex(bytesMessage);
    return Optional.ofNullable(accountIndex);
  }

  /**
   * constructor.
   */

  public static Optional<DelegatedResourceList> getDelegatedResource(byte[] fromAddress,
      byte[] toAddress, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ByteString fromAddressBs = ByteString.copyFrom(fromAddress);
    ByteString toAddressBs = ByteString.copyFrom(toAddress);

    DelegatedResourceMessage request = DelegatedResourceMessage.newBuilder()
        .setFromAddress(fromAddressBs)
        .setToAddress(toAddressBs)
        .build();
    DelegatedResourceList delegatedResource = blockingStubFull.getDelegatedResource(request);
    return Optional.ofNullable(delegatedResource);
  }

  /**
   * constructor.
   */
  public static String retryMapping(
      String mainGatewayAddr, String nonce, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    String retryMethodStr = "retryMapping(uint256)";

    byte[] input = Hex.decode(AbiUtil.parseMethod(retryMethodStr, nonce, true));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGatewayAddr),
            0,
            input,
            feeLimit, 0, null, ownerAddress, priKey, blockingStubFull);
    return txid;
  }

  /**
   * constructor.
   */
  public static Return withdrawTrc20ForReturn(
      String sideGatewayAddress, String sideTokenAddress,
      String value, String tokenAddress, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull,
      WalletGrpc.WalletBlockingStub blockingsideStubFull) {

    String methodStr1 = "withdrawal(uint256)";

    byte[] input1 = Hex
        .decode(AbiUtil
            .parseMethod(methodStr1, value, false));
    Return i = PublicMethed
        .triggerContractSideChainForReturn(WalletClient.decodeFromBase58Check(tokenAddress),
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0,
            input1,
            feeLimit, 0, "0", ownerAddress, priKey, blockingsideStubFull);
    return i;
  }

  /**
   * constructor.
   */

  public static boolean updateAccount(byte[] addressBytes, byte[] accountNameBytes, String priKey,
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
    AccountUpdateContract.Builder builder = AccountUpdateContract.newBuilder();
    ByteString basAddreess = ByteString.copyFrom(addressBytes);
    ByteString bsAccountName = ByteString.copyFrom(accountNameBytes);

    builder.setAccountName(bsAccountName);
    builder.setOwnerAddress(basAddreess);

    AccountUpdateContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.updateAccount(contract);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("Please check!!! transaction == null");
      return false;
    }
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }

  /**
   * constructor.
   */
  public static boolean sideChainProposalCreate(byte[] ownerAddress, String priKey,
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
    SideChainProposalCreateContract.Builder builder = SideChainProposalCreateContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.putAllParameters(parametersMap);

    SideChainProposalCreateContract contract = builder.build();
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response.getResult();
  }

  /**
   * constructor.
   */

  public static boolean approveSideProposal(byte[] ownerAddress, String priKey, long id,
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
    ProposalApproveContract.Builder builder = ProposalApproveContract
        .newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setProposalId(id);
    builder.setIsAddApproval(isAddApproval);
    ProposalApproveContract contract = builder.build();
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

    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }

  /**
   * constructor.
   */

  public static boolean deleteSideProposal(byte[] ownerAddress, String priKey, long id,
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
    ProposalDeleteContract.Builder builder = ProposalDeleteContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setProposalId(id);

    ProposalDeleteContract contract = builder.build();
    TransactionExtention transactionExtention = blockingStubFull.proposalDelete(contract);
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
    transaction = signTransaction(ecKey, transaction, getMaingatewayByteAddr(), false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  public static Return fundInjectForReturn(
      byte[] ownerAddress,
      String priKey, long value, byte[] sideChainId,
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
    ByteString addressBs = ByteString.copyFrom(ownerAddress);
    FundInjectContract request = FundInjectContract.newBuilder().setOwnerAddress(addressBs)
        .setAmount(value).build();

    TransactionExtention transactionExtention = blockingStubFull.fundInject(request);
    if (transactionExtention == null) {
      logger.info("transaction ==null");
      return null;

    }

    Return ret = transactionExtention.getResult();
    return ret;
  }

  /**
   * constructor.
   */
  public static boolean fundInject(
      byte[] ownerAddress,
      String priKey, long value, byte[] sideChainId,
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
    ByteString addressBs = ByteString.copyFrom(ownerAddress);
    FundInjectContract request = FundInjectContract.newBuilder().setOwnerAddress(addressBs)
        .setAmount(value).build();

    TransactionExtention transactionExtention = blockingStubFull.fundInject(request);
    if (transactionExtention == null) {
      logger.info("transaction ==null");
      return false;

    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
      return false;

    } else {
      System.out.println("Code = " + ret.getCode());
      System.out.println("Message = " + ret.getMessage().toStringUtf8());
    }

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return false;
    }
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
//    transaction = signTransaction(ecKey, transaction);
    transaction = signTransaction(ecKey, transaction, sideChainId, false);

    GrpcAPI.Return response = blockingStubFull.broadcastTransaction(transaction);
    if (response.getResult() == false) {
      logger.info(" Code = " + response.getCode());
      logger.info("Message = " + response.getMessage().toStringUtf8());

    } else {
      return true;
    }

    return false;
  }


  /**
   * constructor.
   */

  public static Boolean freezeBalanceSideChainGetEnergy(byte[] addRess, long freezeBalance,
      long freezeDuration,
      int resourceCode, String priKey, byte[] sideChainId,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    byte[] address = addRess;
    long frozenBalance = freezeBalance;
    long frozenDuration = freezeDuration;
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    FreezeBalanceContract.Builder builder = FreezeBalanceContract.newBuilder();
    ByteString byteAddreess = ByteString.copyFrom(address);

    builder.setOwnerAddress(byteAddreess).setFrozenBalance(frozenBalance)
        .setFrozenDuration(frozenDuration).setResourceValue(resourceCode);

    FreezeBalanceContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.freezeBalance(contract);

    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction = null");
      return false;
    }
    transaction = TransactionUtils.setTimestamp(transaction);
    transaction = signTransaction(ecKey, transaction, sideChainId, false);
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response.getResult();
  }


  /**
   * constructor.
   */

  public static Boolean sendcoinForSidechain(byte[] to, long amount, byte[] owner, String priKey,
      byte[] sideChainId,
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

      TransferContract.Builder builder = TransferContract.newBuilder();
      ByteString bsTo = ByteString.copyFrom(to);
      ByteString bsOwner = ByteString.copyFrom(owner);
      builder.setToAddress(bsTo);
      builder.setOwnerAddress(bsOwner);
      builder.setAmount(amount);

      TransferContract contract = builder.build();
      Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        logger.info("transaction ==null");
        continue;
      }
      transaction = signTransaction(ecKey, transaction, sideChainId, false);
      GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
      return response.getResult();
    }
    return false;

  }


  /**
   * constructor.
   */
  public static Map<String, String> depositTrc20ForDepositFee(String trc20ContractAddress,
      String mainGatewayAddress,
      long tokenValue, long callValue,
      long feeLimit, byte[] ownerAddress, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {

    String contractAddrStr = trc20ContractAddress;  //main trc20 contract address
    String methodStr = "approve(address,uint256)";
    String mainGatewayAddr = mainGatewayAddress; //main gateway contract address
    String num = Long.toString(tokenValue);
    String depositMethodStr = "depositTRC20(address,uint256)";

    return depositTrcForDepositFee(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr,
        num, callValue, feeLimit,
        ownerAddress, priKey, blockingStubFull);
  }

  /**
   * constructor.
   */
  public static Map<String, String> depositTrcForDepositFee(
      String contractAddrStr, String mainGatewayAddr, String methodStr,
      String depositMethodStr, String num, long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Map<String, String> map = new HashMap<>();
    long tokenCallValue = 0;
    String tokenId = "";
    String argsStr = "\"" + mainGatewayAddr + "\",\"" + num + "\"";

    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
    byte[] contractAddress = WalletClient.decodeFromBase58Check(contractAddrStr);

    String trxId = triggerContract(contractAddress, 0, input, feeLimit, tokenCallValue,
        tokenId, ownerAddress, priKey, blockingStubFull);
    boolean result = org.apache.commons.lang3.StringUtils.isNoneEmpty(trxId);
    if (result) {
      map.put("approveId", trxId);
      System.out.println("approve successfully.\n");

      byte[] depositContractAddr = WalletClient.decodeFromBase58Check(mainGatewayAddr);
//      String depositArgStr = num + ",\"" + contractAddrStr + "\"";
      String depositArgStr = "\"" + contractAddrStr + "\",\"" + num + "\"";

      byte[] depositInput = Hex.decode(AbiUtil.parseMethod(depositMethodStr, depositArgStr, false));

      String Trxid = triggerContract(depositContractAddr, callValue, depositInput, feeLimit,
          tokenCallValue,
          tokenId, ownerAddress, priKey, blockingStubFull);
      map.put("depositId", Trxid);
      return map;
    } else {
      logger.info("approve failed.\n");
      return map;
    }
  }

  /**
   * constructor.
   */

  public static Map<String, String> depositTrc721ForDepositFee(String trc20ContractAddress,
      String mainGatewayAddress,
      long tokenValue, long callValue,
      long feeLimit, byte[] ownerAddress, String priKey,
      WalletGrpc.WalletBlockingStub blockingStubFull) {

    String contractAddrStr = trc20ContractAddress;  //main trc20 contract address
    String methodStr = "approve(address,uint256)";
    String mainGatewayAddr = mainGatewayAddress; //main gateway contract address
    String num = Long.toString(tokenValue);
    String depositMethodStr = "depositTRC721(address,uint256)";

    return depositTrcForDepositFee(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr,
        num, callValue, feeLimit,
        ownerAddress, priKey, blockingStubFull);

  }


  public static String retryDepositForRetryFee(
      String mainGatewayAddr,
      String nonce, long callValue, long feeLimit, byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    String retryMethodStr = "retryDeposit(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(retryMethodStr, nonce, true));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input,
            feeLimit, 0, null, ownerAddress, priKey, blockingStubFull);
    return txid;

  }


  /**
   * constructor.
   */
  public static String retryMappingForRetryFee(
      String mainGatewayAddr, String nonce, long callValue, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    String retryMethodStr = "retryMapping(uint256)";

    byte[] input = Hex.decode(AbiUtil.parseMethod(retryMethodStr, nonce, true));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGatewayAddr),
            callValue,
            input,
            feeLimit, 0, null, ownerAddress, priKey, blockingStubFull);
    return txid;
  }


  /**
   * constructor.
   */
  public static String retryWithdrawForRetryFee(
      String ChainIdAddress, String sideGatewayAddress, String nonce, long callValue, long feeLimit,
      byte[] ownerAddress,
      String priKey, WalletGrpc.WalletBlockingStub blockingsideStubFull) {
    String retryMethodStr = "retryWithdraw(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(retryMethodStr, nonce, true));

    String txid1 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(ChainIdAddress),
            callValue,
            input,
            feeLimit, 0, null, ownerAddress, priKey, blockingsideStubFull);
    logger.info("txid1:" + txid1);
    return txid1;

  }

  /**
   * constructor.
   */
  public static byte[] deployContractForMain(String contractName, String abiString, String code,
      String data, Long feeLimit, long value,
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

    byte[] contractAddress = generateContractAddress(transactionExtention.getTransaction(), owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
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
        "txid = " + ByteArray.toHexString(Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));

    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return contractAddress;
    }
  }

  /**
   * constructor.
   */

  public static boolean checkDepositMsg(String depositNonce, String mainChainAddress,
      byte[] depositAddress, String testKeyFordeposit,
      WalletGrpc.WalletBlockingStub blockingStubFull,
      String[] Msg) {

    if (Msg.length != 7) {
      logger.info("check Msg length not equal 7");
      return false;
    }

    // get DepositMsg
    String methodStr = "getDepositMsg(uint256)";
    String parame = depositNonce + "";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0, input,
            1000000000, 0, "0",
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    String addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    if (!Msg[0].equalsIgnoreCase(addressFinal)) {
      logger.info("Msg[0] not equal, expected: " + Msg[0] + " actual: " + addressFinal);
      return false;
    }

    String depositValue = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    if (!Msg[1].equalsIgnoreCase("" + Integer.parseInt(depositValue, 16))) {
      logger.info("Msg[1] not equal, expected: " + Msg[1] +
          " actual: " + Integer.parseInt(depositValue, 16));
      return false;
    }

    String value1 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    if (!Msg[2].equalsIgnoreCase("" + Integer.parseInt(value1, 16))) {
      logger.info("Msg[2] not equal, expected: " + Msg[2] +
          " actual: " + Integer.parseInt(value1, 16));
      return false;
    }

    String value2 = ContractRestule.substring(1 + 64 * 3 + 23, 64 * 4);
    if (Msg[2].equalsIgnoreCase("0") || Msg[2].equalsIgnoreCase("1")) {
      if (!Msg[3].equalsIgnoreCase("" + Integer.parseInt(value2, 16))) {
        logger.info("Msg[3] not equal, expected: " + Msg[3] +
            " actual: " + Integer.parseInt(value2, 16));
        return false;
      }
    } else {
      addressHex = "41" + value2;
      logger.info("address_hex: " + addressHex);
      addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
      logger.info("address_final: " + addressFinal);
      if (!Msg[3].equalsIgnoreCase(addressFinal)) {
        logger.info("Msg[3] not equal, expected: " + Msg[3] + " actual: " + addressFinal);
        return false;
      }
    }

    String value3 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    if (!Msg[4].equalsIgnoreCase("" + Integer.parseInt(value3, 16))) {
      logger.info("Msg[4] not equal, expected: " + Msg[4] +
          " actual: " + Integer.parseInt(value3, 16));
      return false;
    }

    String value4 = ContractRestule.substring(1 + 64 * 5, 64 * 6);
    if (!Msg[5].equalsIgnoreCase("" + Integer.parseInt(value4, 16))) {
      logger.info("Msg[5] not equal, expected: " + Msg[5] +
          " actual: " + Integer.parseInt(value4, 16));
      return false;
    }

    String value5 = ContractRestule.substring(1 + 64 * 6, 64 * 7);
    if (!Msg[6].equalsIgnoreCase("" + Integer.parseInt(value5, 16))) {
      logger.info("Msg[6] not equal, expected: " + Msg[6] +
          " actual: " + Integer.parseInt(value5, 16));
      return false;
    }

    return true;
  }


  /**
   * constructor.
   */
  public static boolean checkWithdrawMsg(String withdrawNonce, String mainChainAddress,
      byte[] depositAddress, String testKeyFordeposit,
      WalletGrpc.WalletBlockingStub blockingStubFull,
      String[] Msg) {

    if (Msg.length != 6) {
      logger.info("check Msg length not equal 6");
      return false;
    }

    // get DepositMsg
    logger.info("withdrawNonce:" + withdrawNonce);
    String methodStr = "getWithdrawMsg(uint256)";
    String parame = withdrawNonce + "";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0, input,
            1000000000, 0, "0",
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    String addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    if (!Msg[0].equalsIgnoreCase(addressFinal)) {
      logger.info("Msg[0] not equal, expected: " + Msg[0] + " actual: " + addressFinal);
      return false;
    }

    String ownerAddress2 = ContractRestule.substring(1 + 64 * 1 + 23, 64 * 2);
    if (Msg[4].equalsIgnoreCase("0") || Msg[4].equalsIgnoreCase("1")) {
      if (!Msg[1].equalsIgnoreCase("" + Integer.parseInt(ownerAddress2, 16))) {
        logger.info("Msg[1] not equal, expected: " + Msg[1] +
            " actual: " + Integer.parseInt(ownerAddress2, 16));
        return false;
      }
    } else {
      addressHex = "41" + ownerAddress2;
      logger.info("address_hex: " + addressHex);
      addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
      logger.info("address_final: " + addressFinal);
      if (!Msg[1].equalsIgnoreCase(addressFinal)) {
        logger.info("Msg[1] not equal, expected: " + Msg[1] + " actual: " + addressFinal);
        return false;
      }
    }

    String value1 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    if (!Msg[2].equalsIgnoreCase("" + Integer.parseInt(value1, 16))) {
      logger.info("Msg[2] not equal, expected: " + Msg[2] +
          " actual: " + Integer.parseInt(value1, 16));
      return false;
    }

    String value2 = ContractRestule.substring(1 + 64 * 3 + 23, 64 * 4);
    if (!Msg[3].equalsIgnoreCase("" + Integer.parseInt(value2, 16))) {
      logger.info("Msg[3] not equal, expected: " + Msg[3] +
          " actual: " + Integer.parseInt(value2, 16));
      return false;
    }

    String value3 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    if (!Msg[4].equalsIgnoreCase("" + Integer.parseInt(value3, 16))) {
      logger.info("Msg[4] not equal, expected: " + Msg[4] +
          " actual: " + Integer.parseInt(value3, 16));
      return false;
    }

    String value4 = ContractRestule.substring(1 + 64 * 5, 64 * 6);
    if (!Msg[5].equalsIgnoreCase("" + Integer.parseInt(value4, 16))) {
      logger.info("Msg[5] not equal, expected: " + Msg[5] +
          " actual: " + Integer.parseInt(value4, 16));
      return false;
    }

    return true;
  }

  /**
   * constructor.
   */

  public static boolean checkMappingMsg(String mappingNonce, String mainChainAddress,
      byte[] depositAddress, String testKeyFordeposit,
      WalletGrpc.WalletBlockingStub blockingStubFull,
      String[] Msg) {

    if (Msg.length != 3) {
      logger.info("check Msg length not equal 3");
      return false;
    }

    // get MappingtMsg
    String methodStr = "getMappingMsg(uint256)";
    String parame = mappingNonce;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0, input,
            1000000000, 0, "0",
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());
    logger.info("ContractRestule:" + ContractRestule);
    String ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    String addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    if (!Msg[0].equalsIgnoreCase(addressFinal)) {
      logger.info("Msg[0] not equal, expected: " + Msg[0] + " actual: " + addressFinal);
      return false;
    }

    String depositValue = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    if (!Msg[1].equalsIgnoreCase("" + Integer.parseInt(depositValue, 16))) {
      logger.info("Msg[1] not equal, expected: " + Msg[1] +
          " actual: " + Integer.parseInt(depositValue, 16));
      return false;
    }

    return true;
  }

  /**
   * constructor.
   */
  public static byte[] deployContractForSide(String contractName, String abiString, String code,
      String data, Long feeLimit, long value,
      long consumeUserResourcePercent, long originEnergyLimit, String tokenId, long tokenValue,
      String libraryAddress, String priKey, byte[] ownerAddress, byte[] sideChainId,
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

    byte[] contractAddress = generateContractAddress(transactionExtention.getTransaction(), owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
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
    transaction = signTransaction(ecKey, transaction, sideChainId, false);
    System.out.println(
        "txid = " + ByteArray.toHexString(Sha256Hash
            .hash(DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));

    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    if (response.getResult() == false) {
      return null;
    } else {
      //logger.info("brodacast succesfully");
      return contractAddress;
    }
  }

  public static String hexStringToString(String s) {
    if (s == null || s.equals("")) {
      return null;
    }
    s = s.replace(" ", "");
    byte[] baKeyword = new byte[s.length() / 2];
    for (int i = 0; i < baKeyword.length; i++) {
      try {
        baKeyword[i] = (byte) (0xff & Integer.parseInt(
            s.substring(i * 2, i * 2 + 2), 16));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    try {
      s = new String(baKeyword, "gbk");
      new String();
    } catch (Exception e1) {
      e1.printStackTrace();
    }
    return s;
  }

  public static byte[] decode58Check(String input) {
    byte[] decodeCheck = org.tron.common.utils.Base58.decode(input);
    if (decodeCheck.length <= 4) {
      return null;
    }
    byte[] decodeData = new byte[decodeCheck.length - 4];
    System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
    byte[] hash0 = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), decodeData);
    byte[] hash1 = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), hash0);
    if (hash1[0] == decodeCheck[decodeData.length] &&
        hash1[1] == decodeCheck[decodeData.length + 1] &&
        hash1[2] == decodeCheck[decodeData.length + 2] &&
        hash1[3] == decodeCheck[decodeData.length + 3]) {
      return decodeData;
    }
    return null;
  }

  public static String hex2Str(String hex) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < hex.length() - 1; i += 2) {
      String h = hex.substring(i, (i + 2));
      int decimal = Integer.parseInt(h, 16);
      sb.append((char) decimal);
    }
    return sb.toString();
  }

  public static String numToHex64(Long b) {
    return String.format("%064x", b);//2表示需要两个16进行数
  }

  public static Return transferAssetForReturn(byte[] to, byte[] assertName, long amount,
      byte[] address,
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

    TransferAssetContract.Builder builder = TransferAssetContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsName = ByteString.copyFrom(assertName);
    ByteString bsOwner = ByteString.copyFrom(address);
    builder.setToAddress(bsTo);
    builder.setAssetName(bsName);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferAssetContract contract = builder.build();
    TransactionExtention transaction = blockingStubFull.transferAsset2(contract);

    if (transaction == null) {
      return transaction.getResult();
    }
    Return ret = transaction.getResult();
    return ret;
  }

  public static Return sendcoinForReturn(byte[] to, long amount, byte[] owner, String priKey,
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

    TransferContract.Builder builder = TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferContract contract = builder.build();
    TransactionExtention transaction = blockingStubFull.createTransaction2(contract);
    if (transaction == null) {
      return transaction.getResult();
    }
    Return ret = transaction.getResult();
    return ret;
  }

  public static void freedResource(byte[] fromAddress, String priKey, byte[] toAddress,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    long balance = PublicMethed.queryAccount(fromAddress, blockingStubFull).getBalance();
    sendcoin(toAddress, balance - 500000, fromAddress, priKey, blockingStubFull);
  }


  /**
   * constructor.
   */
  public static boolean sendShieldCoin(byte[] publicZenTokenOwnerAddress,
      long fromAmount, ShieldAddressInfo shieldAddressInfo,
      NoteTx noteTx, List<GrpcAPI.Note> shieldOutputList,
      byte[] publicZenTokenToAddress,
      long toAmount, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    PrivateParameters.Builder builder = PrivateParameters.newBuilder();
    if (!ByteUtil.isNullOrZeroArray(publicZenTokenOwnerAddress)) {
      builder.setTransparentFromAddress(ByteString.copyFrom(publicZenTokenOwnerAddress));
      builder.setFromAmount(fromAmount);
    }
    if (!ByteUtil.isNullOrZeroArray(publicZenTokenToAddress)) {
      builder.setTransparentToAddress(ByteString.copyFrom(publicZenTokenToAddress));
      builder.setToAmount(toAmount);
    }

    if (shieldAddressInfo != null) {
      OutputPointInfo.Builder request = OutputPointInfo.newBuilder();

      //ShieldNoteInfo noteInfo = shieldWrapper.getUtxoMapNote().get(shieldInputList.get(i));
      OutputPoint.Builder outPointBuild = OutputPoint.newBuilder();
      outPointBuild.setHash(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
      outPointBuild.setIndex(noteTx.getIndex());
      request.addOutPoints(outPointBuild.build());

      //ShieldNoteInfo noteInfo = shieldWrapper.getUtxoMapNote().get(shieldInputList.get(i));

      //String shieldAddress = noteInfo.getPaymentAddress();
      //ShieldAddressInfo addressInfo =
      //    shieldWrapper.getShieldAddressInfoMap().get(shieldAddress);
      SpendingKey spendingKey = new SpendingKey(shieldAddressInfo.getSk());
      try {
        ExpandedSpendingKey expandedSpendingKey = spendingKey.expandedSpendingKey();
        builder.setAsk(ByteString.copyFrom(expandedSpendingKey.getAsk()));
        builder.setNsk(ByteString.copyFrom(expandedSpendingKey.getNsk()));
        builder.setOvk(ByteString.copyFrom(expandedSpendingKey.getOvk()));
      } catch (Exception e) {
        System.out.println(e);
      }

      Note.Builder noteBuild = Note.newBuilder();
      noteBuild.setPaymentAddress(shieldAddressInfo.getAddress());
      noteBuild.setValue(noteTx.getNote().getValue());
      noteBuild.setRcm(ByteString.copyFrom(noteTx.getNote().getRcm().toByteArray()));
      noteBuild.setMemo(ByteString.copyFrom(noteTx.getNote().getMemo().toByteArray()));

      //System.out.println("address " + noteInfo.getPaymentAddress());
      //System.out.println("value " + noteInfo.getValue());
      //System.out.println("rcm " + ByteArray.toHexString(noteInfo.getR()));
      //System.out.println("trxId " + noteInfo.getTrxId());
      //System.out.println("index " + noteInfo.getIndex());
      //System.out.println("meno " + new String(noteInfo.getMemo()));

      SpendNote.Builder spendNoteBuilder = SpendNote.newBuilder();
      spendNoteBuilder.setNote(noteBuild.build());
      try {
        spendNoteBuilder.setAlpha(ByteString.copyFrom(org.tron.core.zen.note.Note.generateR()));
      } catch (Exception e) {
        System.out.println(e);
      }

      IncrementalMerkleVoucherInfo merkleVoucherInfo = blockingStubFull
          .getMerkleTreeVoucherInfo(request.build());
      spendNoteBuilder.setVoucher(merkleVoucherInfo.getVouchers(0));
      spendNoteBuilder.setPath(merkleVoucherInfo.getPaths(0));

      builder.addShieldedSpends(spendNoteBuilder.build());

    } else {
      byte[] ovk = ByteArray
          .fromHexString("030c8c2bc59fb3eb8afb047a8ea4b028743d23e7d38c6fa30908358431e2314d");
      builder.setOvk(ByteString.copyFrom(ovk));
    }

    if (shieldOutputList.size() > 0) {
      for (int i = 0; i < shieldOutputList.size(); ++i) {
        builder
            .addShieldedReceives(ReceiveNote.newBuilder().setNote(shieldOutputList.get(i)).build());
      }
    }

    TransactionExtention transactionExtention = blockingStubFull
        .createShieldedTransaction(builder.build());
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
    Any any = transaction.getRawData().getContract(0).getParameter();

    try {
      ShieldedTransferContract shieldedTransferContract =
          any.unpack(ShieldedTransferContract.class);
      if (shieldedTransferContract.getFromAmount() > 0 || fromAmount == 321321) {
        transaction = signTransactionForShield(ecKey, transaction);
        System.out.println(
            "trigger txid = " + ByteArray.toHexString(
                stest.tron.wallet.common.client.utils.Sha256Hash.hash(
                    DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
      } else {
        System.out.println(
            "trigger txid = " + ByteArray.toHexString(
                stest.tron.wallet.common.client.utils.Sha256Hash.hash(
                    DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    return broadcastTransaction(transaction, blockingStubFull).getResult();
  }

  /**
   * constructor.
   */
  public static boolean sendShieldCoinWithoutAsk(byte[] publicZenTokenOwnerAddress,
      long fromAmount, ShieldAddressInfo shieldAddressInfo,
      NoteTx noteTx, List<GrpcAPI.Note> shieldOutputList,
      byte[] publicZenTokenToAddress,
      long toAmount, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    PrivateParametersWithoutAsk.Builder builder = PrivateParametersWithoutAsk.newBuilder();
    if (!ByteUtil.isNullOrZeroArray(publicZenTokenOwnerAddress)) {
      builder.setTransparentFromAddress(ByteString.copyFrom(publicZenTokenOwnerAddress));
      builder.setFromAmount(fromAmount);
    }
    if (!ByteUtil.isNullOrZeroArray(publicZenTokenToAddress)) {
      builder.setTransparentToAddress(ByteString.copyFrom(publicZenTokenToAddress));
      builder.setToAmount(toAmount);
    }

    byte[] ask = new byte[32];
    if (shieldAddressInfo != null) {
      OutputPointInfo.Builder request = OutputPointInfo.newBuilder();

      //ShieldNoteInfo noteInfo = shieldWrapper.getUtxoMapNote().get(shieldInputList.get(i));
      OutputPoint.Builder outPointBuild = OutputPoint.newBuilder();
      outPointBuild.setHash(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
      outPointBuild.setIndex(noteTx.getIndex());
      request.addOutPoints(outPointBuild.build());
      IncrementalMerkleVoucherInfo merkleVoucherInfo
          = blockingStubFull.getMerkleTreeVoucherInfo(request.build());
      if (merkleVoucherInfo.getVouchersCount() != 1) {
        System.out.println("Can't get all merkel tree, please check the notes.");
        return false;
      }

      //ShieldNoteInfo noteInfo = shieldWrapper.getUtxoMapNote().get(shieldInputList.get(i));

      //String shieldAddress = noteInfo.getPaymentAddress();
      //ShieldAddressInfo addressInfo =
      //    shieldWrapper.getShieldAddressInfoMap().get(shieldAddress);
      String shieldAddress = noteTx.getNote().getPaymentAddress();
      SpendingKey spendingKey = new SpendingKey(shieldAddressInfo.getSk());
      try {
        ExpandedSpendingKey expandedSpendingKey = spendingKey.expandedSpendingKey();
        System.arraycopy(expandedSpendingKey.getAsk(), 0, ask, 0, 32);
        builder.setAk(ByteString.copyFrom(
            ExpandedSpendingKey.getAkFromAsk(expandedSpendingKey.getAsk())));
        builder.setNsk(ByteString.copyFrom(expandedSpendingKey.getNsk()));
        builder.setOvk(ByteString.copyFrom(expandedSpendingKey.getOvk()));
      } catch (Exception e) {
        System.out.println(e);
      }

      Note.Builder noteBuild = Note.newBuilder();
      noteBuild.setPaymentAddress(shieldAddressInfo.getAddress());
      noteBuild.setValue(noteTx.getNote().getValue());
      noteBuild.setRcm(ByteString.copyFrom(noteTx.getNote().getRcm().toByteArray()));
      noteBuild.setMemo(ByteString.copyFrom(noteTx.getNote().getMemo().toByteArray()));

      //System.out.println("address " + noteInfo.getPaymentAddress());
      //System.out.println("value " + noteInfo.getValue());
      //System.out.println("rcm " + ByteArray.toHexString(noteInfo.getR()));
      //System.out.println("trxId " + noteInfo.getTrxId());
      //System.out.println("index " + noteInfo.getIndex());
      //System.out.println("meno " + new String(noteInfo.getMemo()));

      SpendNote.Builder spendNoteBuilder = SpendNote.newBuilder();
      spendNoteBuilder.setNote(noteBuild.build());
      try {
        spendNoteBuilder.setAlpha(ByteString.copyFrom(org.tron.core.zen.note.Note.generateR()));
      } catch (Exception e) {
        System.out.println(e);
      }

      spendNoteBuilder.setVoucher(merkleVoucherInfo.getVouchers(0));
      spendNoteBuilder.setPath(merkleVoucherInfo.getPaths(0));

      builder.addShieldedSpends(spendNoteBuilder.build());

    } else {
      byte[] ovk = ByteArray
          .fromHexString("030c8c2bc59fb3eb8afb047a8ea4b028743d23e7d38c6fa30908358431e2314d");
      builder.setOvk(ByteString.copyFrom(ovk));
    }

    if (shieldOutputList.size() > 0) {
      for (int i = 0; i < shieldOutputList.size(); ++i) {
        builder
            .addShieldedReceives(ReceiveNote.newBuilder().setNote(shieldOutputList.get(i)).build());
      }
    }

    TransactionExtention transactionExtention = blockingStubFull
        .createShieldedTransactionWithoutSpendAuthSig(builder.build());
    if (transactionExtention == null) {
      System.out.println("sendShieldCoinWithoutAsk failure.");
      return false;
    }
    BytesMessage trxHash = blockingStubFull
        .getShieldTransactionHash(transactionExtention.getTransaction());
    if (trxHash == null || trxHash.getValue().toByteArray().length != 32) {
      System.out.println("sendShieldCoinWithoutAsk get transaction hash failure.");
      return false;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRawData().getContract(0).getType()
        != ContractType.ShieldedTransferContract) {
      System.out.println("This method only for ShieldedTransferContract, please check!");
      return false;
    }
    Any any = transaction.getRawData().getContract(0).getParameter();
    Transaction transaction1 = transactionExtention.getTransaction();
    try {
      ShieldedTransferContract shieldContract = any.unpack(ShieldedTransferContract.class);
      List<SpendDescription> spendDescList = shieldContract.getSpendDescriptionList();
      ShieldedTransferContract.Builder contractBuild
          = shieldContract.toBuilder().clearSpendDescription();
      for (int i = 0; i < spendDescList.size(); i++) {

        SpendAuthSigParameters.Builder builder1 = SpendAuthSigParameters.newBuilder();
        builder1.setAsk(ByteString.copyFrom(ask));
        builder1.setTxHash(ByteString.copyFrom(trxHash.getValue().toByteArray()));
        builder1.setAlpha(builder.getShieldedSpends(i).getAlpha());
        SpendDescription.Builder spendDescription = spendDescList.get(i).toBuilder();
        BytesMessage authSig = blockingStubFull.createSpendAuthSig(builder1.build());
        spendDescription.setSpendAuthoritySignature(
            ByteString.copyFrom(authSig.getValue().toByteArray()));

        contractBuild.addSpendDescription(spendDescription.build());
      }

      Transaction.raw.Builder rawBuilder
          = transaction.toBuilder().getRawDataBuilder().clearContract()
          .addContract(
              Transaction.Contract.newBuilder().setType(ContractType.ShieldedTransferContract)
                  .setParameter(
                      Any.pack(contractBuild.build())).build());

      transaction = transaction.toBuilder().clearRawData().setRawData(rawBuilder).build();

      transactionExtention = transactionExtention.toBuilder().setTransaction(transaction).build();

      if (transactionExtention == null) {
        return false;
      }
      Return ret = transactionExtention.getResult();
      if (!ret.getResult()) {
        System.out.println("Code = " + ret.getCode());
        System.out.println("Message = " + ret.getMessage().toStringUtf8());
        return false;
      }
      transaction1 = transactionExtention.getTransaction();
      if (transaction == null || transaction.getRawData().getContractCount() == 0) {
        System.out.println("Transaction is empty");
        return false;
      }
      System.out.println(
          "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));

      if (transaction1.getRawData().getContract(0).getType()
          != ContractType.ShieldedTransferContract) {
        transaction1 = signTransaction(ecKey, transaction1);
      } else {
        Any any1 = transaction1.getRawData().getContract(0).getParameter();
        ShieldedTransferContract shieldedTransferContract =
            any1.unpack(ShieldedTransferContract.class);
        if (shieldedTransferContract.getFromAmount() > 0) {
          transaction1 = signTransactionForShield(ecKey, transaction1);
          System.out.println(
              "trigger txid = " + ByteArray.toHexString(
                  stest.tron.wallet.common.client.utils.Sha256Hash.hash(
                      DBConfig.isECKeyCryptoEngine(), transaction1.getRawData().toByteArray())));
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    System.out.println(
        "trigger txid = " + ByteArray.toHexString(
            stest.tron.wallet.common.client.utils.Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
                transaction1.getRawData().toByteArray())));
    return broadcastTransaction(transaction1, blockingStubFull).getResult();
  }


  /**
   * constructor.
   */
  public static List<Note> addShieldOutputList(List<Note> shieldOutList, String shieldToAddress,
      String toAmountString, String menoString) {
    String shieldAddress = shieldToAddress;
    String amountString = toAmountString;
    if (menoString.equals("null")) {
      menoString = "";
    }
    long shieldAmount = 0;
    if (!StringUtil.isNullOrEmpty(amountString)) {
      shieldAmount = Long.valueOf(amountString);
    }

    Note.Builder noteBuild = Note.newBuilder();
    noteBuild.setPaymentAddress(shieldAddress);
    noteBuild.setPaymentAddress(shieldAddress);
    noteBuild.setValue(shieldAmount);
    try {
      noteBuild.setRcm(ByteString.copyFrom(org.tron.core.zen.note.Note.generateR()));
    } catch (Exception e) {
      System.out.println(e);
    }
    noteBuild.setMemo(ByteString.copyFrom(menoString.getBytes()));
    shieldOutList.add(noteBuild.build());
    //logger.info(shieldOutList.toString());
    return shieldOutList;
  }

  /**
   * constructor.
   */
  public static Optional<ShieldAddressInfo> generateShieldAddress() {
    ShieldAddressInfo addressInfo = new ShieldAddressInfo();
    try {
      DiversifierT diversifier = DiversifierT.random();
      SpendingKey spendingKey = SpendingKey.random();
      FullViewingKey fullViewingKey = spendingKey.fullViewingKey();
      IncomingViewingKey incomingViewingKey = fullViewingKey.inViewingKey();
      PaymentAddress paymentAddress = incomingViewingKey.address(diversifier).get();

      addressInfo.setSk(spendingKey.getValue());
      addressInfo.setD(diversifier);
      addressInfo.setIvk(incomingViewingKey.getValue());
      addressInfo.setOvk(fullViewingKey.getOvk());
      addressInfo.setPkD(paymentAddress.getPkD());

      if (addressInfo.validateCheck()) {
        return Optional.of(addressInfo);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return Optional.empty();
  }

  /**
   * constructor.
   */
  public static DecryptNotes listShieldNote(Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    logger.info(ByteArray.toHexString(shieldAddressInfo.get().ivk));
    IvkDecryptParameters.Builder builder = IvkDecryptParameters.newBuilder();
    builder.setStartBlockIndex(startBlockNum);
    builder.setEndBlockIndex(currentBlockNum + 1);
    builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
    DecryptNotes notes = blockingStubFull.scanNoteByIvk(builder.build());
    logger.info(notes.toString());
    return notes;
  }

  /**
   * constructor.
   */
  public static DecryptNotes getShieldNotesByIvk(Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    //startBlockNum = 0L;
    logger.info("ivk:" + ByteArray.toHexString(shieldAddressInfo.get().ivk));
    IvkDecryptParameters.Builder builder = IvkDecryptParameters.newBuilder();
    builder.setStartBlockIndex(startBlockNum + 1);
    builder.setEndBlockIndex(currentBlockNum + 1);
    builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
    DecryptNotes notes = blockingStubFull.scanNoteByIvk(builder.build());
    logger.info(notes.toString());
    return notes;
  }

  /**
   * constructor.
   */
  public static DecryptNotesMarked getShieldNotesAndMarkByIvk(
      Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    //startBlockNum = 0L;
    logger.info("ivk:" + ByteArray.toHexString(shieldAddressInfo.get().ivk));
    try {
      IvkDecryptAndMarkParameters.Builder builder = IvkDecryptAndMarkParameters.newBuilder();
      builder.setStartBlockIndex(startBlockNum + 1);
      builder.setEndBlockIndex(currentBlockNum + 1);
      builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
      builder.setAk(ByteString.copyFrom(shieldAddressInfo.get().getFullViewingKey().getAk()));
      builder.setNk(ByteString.copyFrom(shieldAddressInfo.get().getFullViewingKey().getNk()));
      DecryptNotesMarked decryptNotes = blockingStubFull.scanAndMarkNoteByIvk(builder.build());
      logger.info(decryptNotes.toString());
      return decryptNotes;
    } catch (Exception e) {
      logger.info(e.toString());
      return null;
    }
  }

  /**
   * constructor.
   */
  public static DecryptNotesMarked getShieldNotesAndMarkByIvkOnSolidity(
      Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity) {
    Block currentBlock = blockingStubSolidity
        .getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    //startBlockNum = 0L;
    logger.info("ivk:" + ByteArray.toHexString(shieldAddressInfo.get().ivk));
    try {
      IvkDecryptAndMarkParameters.Builder builder = IvkDecryptAndMarkParameters.newBuilder();
      builder.setStartBlockIndex(startBlockNum + 1);
      builder.setEndBlockIndex(currentBlockNum + 1);
      builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
      builder.setAk(ByteString.copyFrom(shieldAddressInfo.get().getFullViewingKey().getAk()));
      builder.setNk(ByteString.copyFrom(shieldAddressInfo.get().getFullViewingKey().getNk()));
      DecryptNotesMarked decryptNotes = blockingStubSolidity.scanAndMarkNoteByIvk(builder.build());
      logger.info(decryptNotes.toString());
      return decryptNotes;
    } catch (Exception e) {
      logger.info(e.toString());
      return null;
    }
  }

  /**
   * constructor.
   */
  public static Integer getShieldNotesCount(Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();

    if (currentBlockNum < 100) {
      IvkDecryptParameters.Builder builder = IvkDecryptParameters.newBuilder();
      builder.setStartBlockIndex(0);
      builder.setEndBlockIndex(currentBlockNum);
      builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
      DecryptNotes notes = blockingStubFull.scanNoteByIvk(builder.build());
      logger.info(notes.toString());
      return notes.getNoteTxsCount();
    }
    Integer count = 0;
    Long startBlockNum = 0L;
    while (startBlockNum + 100 < currentBlockNum) {
      IvkDecryptParameters.Builder builder = IvkDecryptParameters.newBuilder();
      builder.setStartBlockIndex(startBlockNum);
      builder.setEndBlockIndex(startBlockNum + 100);
      builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
      DecryptNotes notes = blockingStubFull.scanNoteByIvk(builder.build());
      count = count + notes.getNoteTxsCount();
      startBlockNum = startBlockNum + 100;
    }
    IvkDecryptParameters.Builder builder = IvkDecryptParameters.newBuilder();
    builder.setStartBlockIndex(startBlockNum);
    builder.setEndBlockIndex(currentBlockNum);
    builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
    DecryptNotes notes = blockingStubFull.scanNoteByIvk(builder.build());
    count = count + notes.getNoteTxsCount();
    return count;

  }

  /**
   * constructor.
   */
  public static DecryptNotes getShieldNotesByIvkOnSolidity(
      Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity) {
    Block currentBlock = blockingStubSolidity
        .getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    IvkDecryptParameters.Builder builder = IvkDecryptParameters.newBuilder();
    builder.setStartBlockIndex(startBlockNum);
    builder.setEndBlockIndex(currentBlockNum);
    builder.setIvk(ByteString.copyFrom(shieldAddressInfo.get().getIvk()));
    DecryptNotes notes = blockingStubSolidity.scanNoteByIvk(builder.build());
    logger.info(notes.toString());
    return notes;
  }


  /**
   * constructor.
   */
  public static DecryptNotes getShieldNotesByOvk(Optional<ShieldAddressInfo> shieldAddressInfo,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    Block currentBlock = blockingStubFull.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    logger.info("ovk:" + ByteArray.toHexString(shieldAddressInfo.get().ovk));
    OvkDecryptParameters.Builder builder = OvkDecryptParameters.newBuilder();
    builder.setStartBlockIndex(startBlockNum + 1);
    builder.setEndBlockIndex(currentBlockNum + 1);
    builder.setOvk(ByteString.copyFrom(shieldAddressInfo.get().getOvk()));
    DecryptNotes notes = blockingStubFull.scanNoteByOvk(builder.build());
    logger.info(notes.toString());
    return notes;
  }

  /**
   * constructor.
   */
  public static DecryptNotes getShieldNotesByOvkOnSolidity(
      Optional<ShieldAddressInfo> shieldAddressInfo, WalletSolidityGrpc
      .WalletSolidityBlockingStub blockingStubSolidity) {
    Block currentBlock = blockingStubSolidity
        .getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    Long currentBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    Long startBlockNum = 0L;
    if (currentBlockNum > 100) {
      startBlockNum = currentBlockNum - 100;
    }
    OvkDecryptParameters.Builder builder = OvkDecryptParameters.newBuilder();
    builder.setStartBlockIndex(startBlockNum);
    builder.setEndBlockIndex(currentBlockNum);
    builder.setOvk(ByteString.copyFrom(shieldAddressInfo.get().getOvk()));
    DecryptNotes notes = blockingStubSolidity.scanNoteByOvk(builder.build());
    logger.info(notes.toString());
    return notes;
  }


  /**
   * constructor.
   */
  public static String getMemo(Note note) {
    return ZenUtils.getMemo(note.getMemo().toByteArray());
  }

  /**
   * constructor.
   */
  public static SpendResult getSpendResult(
      ShieldAddressInfo shieldAddressInfo, NoteTx noteTx,
      WalletGrpc.WalletBlockingStub blockingStubFull) {

    OutputPointInfo.Builder request = OutputPointInfo.newBuilder();
    OutputPoint.Builder outPointBuild = OutputPoint.newBuilder();
    outPointBuild.setHash(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
    outPointBuild.setIndex(noteTx.getIndex());
    request.addOutPoints(outPointBuild.build());
    Optional<IncrementalMerkleVoucherInfo> merkleVoucherInfo = Optional.of(blockingStubFull
        .getMerkleTreeVoucherInfo(request.build()));

    if (merkleVoucherInfo.isPresent() && merkleVoucherInfo.get().getVouchersCount() > 0) {
      NoteParameters.Builder builder = NoteParameters.newBuilder();
      try {
        builder.setAk(ByteString.copyFrom(shieldAddressInfo.getFullViewingKey().getAk()));
        builder.setNk(ByteString.copyFrom(shieldAddressInfo.getFullViewingKey().getNk()));
        logger.info("AK:" + ByteArray.toHexString(shieldAddressInfo.getFullViewingKey().getAk()));
        logger.info("NK:" + ByteArray.toHexString(shieldAddressInfo.getFullViewingKey().getNk()));
      } catch (Exception e) {
        Assert.assertTrue(1 == 1);
      }

      Note.Builder noteBuild = Note.newBuilder();
      noteBuild.setPaymentAddress(shieldAddressInfo.getAddress());
      noteBuild.setValue(noteTx.getNote().getValue());
      noteBuild.setRcm(ByteString.copyFrom(noteTx.getNote().getRcm().toByteArray()));
      noteBuild.setMemo(ByteString.copyFrom(noteTx.getNote().getMemo().toByteArray()));
      builder.setNote(noteBuild.build());
      builder.setTxid(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
      builder.setIndex(noteTx.getIndex());
      //builder.setVoucher(merkleVoucherInfo.getVouchers(0));

      SpendResult result = blockingStubFull.isSpend(builder.build());
      return result;

    }
    return null;

  }

  /**
   * constructor.
   */
  public static SpendResult getSpendResultOnSolidity(
      ShieldAddressInfo shieldAddressInfo, NoteTx noteTx, WalletSolidityGrpc
      .WalletSolidityBlockingStub blockingStubSolidity) {
    OutputPointInfo.Builder request = OutputPointInfo.newBuilder();
    OutputPoint.Builder outPointBuild = OutputPoint.newBuilder();
    outPointBuild.setHash(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
    outPointBuild.setIndex(noteTx.getIndex());
    request.addOutPoints(outPointBuild.build());
    Optional<IncrementalMerkleVoucherInfo> merkleVoucherInfo = Optional.of(blockingStubSolidity
        .getMerkleTreeVoucherInfo(request.build()));

    if (merkleVoucherInfo.isPresent() && merkleVoucherInfo.get().getVouchersCount() > 0) {
      NoteParameters.Builder builder = NoteParameters.newBuilder();
      try {
        builder.setAk(ByteString.copyFrom(shieldAddressInfo.getFullViewingKey().getAk()));
        builder.setNk(ByteString.copyFrom(shieldAddressInfo.getFullViewingKey().getNk()));
      } catch (Exception e) {
        Assert.assertTrue(1 == 1);
      }
      Note.Builder noteBuild = Note.newBuilder();
      noteBuild.setPaymentAddress(shieldAddressInfo.getAddress());
      noteBuild.setValue(noteTx.getNote().getValue());
      noteBuild.setRcm(ByteString.copyFrom(noteTx.getNote().getRcm().toByteArray()));
      noteBuild.setMemo(ByteString.copyFrom(noteTx.getNote().getMemo().toByteArray()));
      builder.setNote(noteBuild.build());
      builder.setTxid(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
      builder.setIndex(noteTx.getIndex());
      //builder.setVoucher(merkleVoucherInfo.getVouchers(0));

      SpendResult result = blockingStubSolidity.isSpend(builder.build());
      return result;
    }
    return null;
  }

  /**
   * constructor.
   */
  public static String getShieldNullifier(ShieldAddressInfo shieldAddressInfo,
      NoteTx noteTx, WalletGrpc.WalletBlockingStub blockingStubFull) {
    OutputPointInfo.Builder request = OutputPointInfo.newBuilder();
    OutputPoint.Builder outPointBuild = OutputPoint.newBuilder();
    outPointBuild.setHash(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
    outPointBuild.setIndex(noteTx.getIndex());
    request.addOutPoints(outPointBuild.build());
    IncrementalMerkleVoucherInfo merkleVoucherInfo
        = blockingStubFull.getMerkleTreeVoucherInfo(request.build());
    if (merkleVoucherInfo.getVouchersCount() < 1) {
      System.out.println("get merkleVoucherInfo failure.");
      return null;
    }
    Note.Builder noteBuild = Note.newBuilder();
    noteBuild.setPaymentAddress(shieldAddressInfo.getAddress());
    noteBuild.setValue(noteTx.getNote().getValue());
    noteBuild.setRcm(ByteString.copyFrom(noteTx.getNote().getRcm().toByteArray()));
    noteBuild.setMemo(ByteString.copyFrom(noteTx.getNote().getMemo().toByteArray()));

    String shieldAddress = noteTx.getNote().getPaymentAddress();
    SpendingKey spendingKey = new SpendingKey(shieldAddressInfo.getSk());
    try {
      //TODO
      FullViewingKey fullViewingKey = spendingKey.fullViewingKey();
      NfParameters.Builder builder = NfParameters.newBuilder();
      builder.setNote(noteBuild.build());
      builder.setVoucher(merkleVoucherInfo.getVouchers(0));
      builder.setAk(ByteString.copyFrom(fullViewingKey.getAk()));
      builder.setNk(ByteString.copyFrom(fullViewingKey.getNk()));

      BytesMessage nullifier = blockingStubFull.createShieldNullifier(builder.build());
      return ByteArray.toHexString(nullifier.getValue().toByteArray());

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;

  }


  /**
   * constructor.
   */
  public static String sendShieldCoinGetTxid(byte[] publicZenTokenOwnerAddress,
      long fromAmount, ShieldAddressInfo shieldAddressInfo,
      NoteTx noteTx, List<GrpcAPI.Note> shieldOutputList,
      byte[] publicZenTokenToAddress,
      long toAmount, String priKey, WalletGrpc.WalletBlockingStub blockingStubFull) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    ECKey temKey = null;
    try {
      BigInteger priK = new BigInteger(priKey, 16);
      temKey = ECKey.fromPrivate(priK);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    final ECKey ecKey = temKey;

    PrivateParameters.Builder builder = PrivateParameters.newBuilder();
    if (!ByteUtil.isNullOrZeroArray(publicZenTokenOwnerAddress)) {
      builder.setTransparentFromAddress(ByteString.copyFrom(publicZenTokenOwnerAddress));
      builder.setFromAmount(fromAmount);
    }
    if (!ByteUtil.isNullOrZeroArray(publicZenTokenToAddress)) {
      builder.setTransparentToAddress(ByteString.copyFrom(publicZenTokenToAddress));
      builder.setToAmount(toAmount);
    }

    if (shieldAddressInfo != null) {
      OutputPointInfo.Builder request = OutputPointInfo.newBuilder();

      //ShieldNoteInfo noteInfo = shieldWrapper.getUtxoMapNote().get(shieldInputList.get(i));
      OutputPoint.Builder outPointBuild = OutputPoint.newBuilder();
      outPointBuild.setHash(ByteString.copyFrom(noteTx.getTxid().toByteArray()));
      outPointBuild.setIndex(noteTx.getIndex());
      request.addOutPoints(outPointBuild.build());

      //ShieldNoteInfo noteInfo = shieldWrapper.getUtxoMapNote().get(shieldInputList.get(i));

      //String shieldAddress = noteInfo.getPaymentAddress();
      //ShieldAddressInfo addressInfo =
      //    shieldWrapper.getShieldAddressInfoMap().get(shieldAddress);
      SpendingKey spendingKey = new SpendingKey(shieldAddressInfo.getSk());
      try {
        ExpandedSpendingKey expandedSpendingKey = spendingKey.expandedSpendingKey();
        builder.setAsk(ByteString.copyFrom(expandedSpendingKey.getAsk()));
        builder.setNsk(ByteString.copyFrom(expandedSpendingKey.getNsk()));
        builder.setOvk(ByteString.copyFrom(expandedSpendingKey.getOvk()));
      } catch (Exception e) {
        System.out.println(e);
      }

      Note.Builder noteBuild = Note.newBuilder();
      noteBuild.setPaymentAddress(shieldAddressInfo.getAddress());
      noteBuild.setValue(noteTx.getNote().getValue());
      noteBuild.setRcm(ByteString.copyFrom(noteTx.getNote().getRcm().toByteArray()));
      noteBuild.setMemo(ByteString.copyFrom(noteTx.getNote().getMemo().toByteArray()));

      //System.out.println("address " + noteInfo.getPaymentAddress());
      //System.out.println("value " + noteInfo.getValue());
      //System.out.println("rcm " + ByteArray.toHexString(noteInfo.getR()));
      //System.out.println("trxId " + noteInfo.getTrxId());
      //System.out.println("index " + noteInfo.getIndex());
      //System.out.println("meno " + new String(noteInfo.getMemo()));

      SpendNote.Builder spendNoteBuilder = SpendNote.newBuilder();
      spendNoteBuilder.setNote(noteBuild.build());
      try {
        spendNoteBuilder.setAlpha(ByteString.copyFrom(org.tron.core.zen.note.Note.generateR()));
      } catch (Exception e) {
        System.out.println(e);
      }

      IncrementalMerkleVoucherInfo merkleVoucherInfo = blockingStubFull
          .getMerkleTreeVoucherInfo(request.build());
      spendNoteBuilder.setVoucher(merkleVoucherInfo.getVouchers(0));
      spendNoteBuilder.setPath(merkleVoucherInfo.getPaths(0));

      builder.addShieldedSpends(spendNoteBuilder.build());

    } else {
      byte[] ovk = ByteArray
          .fromHexString("030c8c2bc59fb3eb8afb047a8ea4b028743d23e7d38c6fa30908358431e2314d");
      builder.setOvk(ByteString.copyFrom(ovk));
    }

    if (shieldOutputList.size() > 0) {
      for (int i = 0; i < shieldOutputList.size(); ++i) {
        builder
            .addShieldedReceives(ReceiveNote.newBuilder().setNote(shieldOutputList.get(i)).build());
      }
    }

    TransactionExtention transactionExtention = blockingStubFull
        .createShieldedTransaction(builder.build());
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
    System.out.println(
        "Receive txid = " + ByteArray.toHexString(transactionExtention.getTxid().toByteArray()));
    Any any = transaction.getRawData().getContract(0).getParameter();

    try {
      ShieldedTransferContract shieldedTransferContract =
          any.unpack(ShieldedTransferContract.class);
      if (shieldedTransferContract.getFromAmount() > 0) {
        transaction = signTransactionForShield(ecKey, transaction);
        System.out.println(
            "trigger txid = " + ByteArray.toHexString(
                stest.tron.wallet.common.client.utils.Sha256Hash.hash(
                    DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
      } else {
        System.out.println(
            "trigger txid = " + ByteArray.toHexString(
                stest.tron.wallet.common.client.utils.Sha256Hash.hash(
                    DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
      }
    } catch (Exception e) {
      System.out.println(e);
    }
    broadcastTransaction(transaction, blockingStubFull);
    return ByteArray.toHexString(
        stest.tron.wallet.common.client.utils.Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
            transaction.getRawData().toByteArray()));
  }

  public static Protocol.Transaction signTransactionForShield(ECKey ecKey,
      Protocol.Transaction transaction) {
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
    if (ecKey == null || ecKey.getPrivKey() == null) {
      //logger.warn("Warning: Can't sign,there is no private key !!");
      return null;
    }
    logger.info("Txid in sign is " + ByteArray.toHexString(
        stest.tron.wallet.common.client.utils.Sha256Hash.hash(
            DBConfig.isECKeyCryptoEngine(), transaction.getRawData().toByteArray())));
    return TransactionUtils.sign(transaction, ecKey);
  }

  public static String parametersString(List<Object> parameters) {
    String[] inputArr = new String[parameters.size()];
    int i = 0;
    for (Object parameter : parameters) {
      if (parameter instanceof List) {
        StringBuilder sb = new StringBuilder();
        for (Object item : (List) parameter) {
          if (sb.length() != 0) {
            sb.append(",");
          }
          sb.append("\"").append(item).append("\"");
        }
        inputArr[i++] = "[" + sb.toString() + "]";
      } else {
        inputArr[i++] =
            (parameter instanceof String) ? ("\"" + parameter + "\"") : ("" + parameter);
      }
    }
    String input = StringUtils.join(inputArr, ',');
    return input;
  }

  public static GrpcAPI.Return triggerContractAndGetResponse(byte[] contractAddress, String method,
      String argsStr,
      Boolean isHex, long callValue, long feeLimit, String tokenId, long tokenValue,
      byte[] ownerAddress,
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
    if (argsStr.equalsIgnoreCase("#")) {
      logger.info("argsstr is #");
      argsStr = "";
    }

    byte[] owner = ownerAddress;
    byte[] input = Hex.decode(AbiUtil.parseMethod(method, argsStr, isHex));

    TriggerSmartContract.Builder builder = TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(owner));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(input));
    builder.setCallValue(callValue);
    builder.setTokenId(Long.parseLong(tokenId));
    builder.setCallTokenValue(tokenValue);
    TriggerSmartContract triggerContract = builder.build();

    TransactionExtention transactionExtention = blockingStubFull.triggerContract(triggerContract);
    if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
      System.out.println("RPC create call trx failed!");
      System.out.println("Code = " + transactionExtention.getResult().getCode());
      System.out
          .println("Message = " + transactionExtention.getResult().getMessage().toStringUtf8());
      return null;
    }
    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0
        && transactionExtention.getConstantResult(0) != null
        && transactionExtention.getResult() != null) {
      byte[] result = transactionExtention.getConstantResult(0).toByteArray();
      System.out.println("message:" + transaction.getRet(0).getRet());
      System.out.println(":" + ByteArray
          .toStr(transactionExtention.getResult().getMessage().toByteArray()));
      System.out.println("Result:" + Hex.toHexString(result));
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
    transaction = transactionExtention.getTransaction();
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      System.out.println("Transaction is empty");
      return null;
    }
    transaction = signTransaction(ecKey, transaction);
    System.out.println(
        "trigger txid = " + ByteArray.toHexString(
            stest.tron.wallet.common.client.utils.Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
                transaction.getRawData()
                    .toByteArray())));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);
    return response;
  }

  /**
   * constructor.
   */
  public static long getFreezeBalanceNetCount(byte[] accountAddress, String ecKey, Long targetNet,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    //Precision change as the entire network freezes
    AccountResourceMessage resourceInfo = getAccountResource(accountAddress,
        blockingStubFull);

    Account info = queryAccount(accountAddress, blockingStubFull);

    Account getAccount = queryAccount(ecKey, blockingStubFull);

    long balance = info.getBalance();
    long totalNetLimit = resourceInfo.getTotalNetLimit();
    long totalNetWeight = resourceInfo.getTotalNetWeight();
    long netUsed = resourceInfo.getNetUsed();
    long netLimit = resourceInfo.getNetLimit();

    if (netUsed > netLimit) {
      targetNet = netUsed - netLimit + targetNet;
    }

    if (totalNetWeight == 0) {
      return 1000_000L;
    }

    // totalNetLimit / (totalNetWeight + needBalance) = needNet / needBalance
    final BigInteger totalNetWeightBi = BigInteger.valueOf(totalNetWeight);
    long needBalance = totalNetWeightBi.multiply(BigInteger.valueOf(1_000_000))
        .multiply(BigInteger.valueOf(targetNet))
        .divide(BigInteger.valueOf(totalNetLimit - targetNet)).longValue();

    logger.info("getFreezeBalanceNetCount, needBalance: " + needBalance);

    if (needBalance < 1000000L) {
      needBalance = 1000000L;
      logger.info(
          "getFreezeBalanceNetCount, needBalance less than 1 TRX, modify to: " + needBalance);
    }
    return needBalance;
  }


  /**
   * constructor.
   */

  public static Optional<Exchange> getExchange(String id, WalletSolidityGrpc
      .WalletSolidityBlockingStub blockingStubSolidity) {
    BytesMessage request = BytesMessage.newBuilder().setValue(ByteString.copyFrom(
        ByteArray.fromLong(Long.parseLong(id))))
        .build();
    Exchange exchange = blockingStubSolidity.getExchangeById(request);
    return Optional.ofNullable(exchange);
  }

  public static Optional<Exchange> getExchange(String id, WalletGrpc.WalletBlockingStub
      blockingStubFull) {
    BytesMessage request = BytesMessage.newBuilder().setValue(ByteString.copyFrom(
        ByteArray.fromLong(Long.parseLong(id))))
        .build();
    Exchange exchange = blockingStubFull.getExchangeById(request);
    return Optional.ofNullable(exchange);
  }

  public static String sendcoinGetTransactionHex(byte[] to, long amount, byte[] owner,
      String priKey,
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

    Integer times = 0;
    TransferContract.Builder builder = TransferContract.newBuilder();
    ByteString bsTo = ByteString.copyFrom(to);
    ByteString bsOwner = ByteString.copyFrom(owner);
    builder.setToAddress(bsTo);
    builder.setOwnerAddress(bsOwner);
    builder.setAmount(amount);

    TransferContract contract = builder.build();
    Protocol.Transaction transaction = blockingStubFull.createTransaction(contract);
    if (transaction == null || transaction.getRawData().getContractCount() == 0) {
      logger.info("transaction ==null");
      return null;

    }
    transaction = signTransaction(ecKey, transaction);
    logger.info("HEX transaction is : " + "transaction hex string is " + ByteArray
        .toHexString(transaction.toByteArray()));
    return ByteArray.toHexString(transaction.toByteArray());
  }

  public static GrpcAPI.Return deployContractAndGetResponse(String contractName,
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
        "txid = " + ByteArray.toHexString(
            stest.tron.wallet.common.client.utils.Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(),
                transaction.getRawData().toByteArray())));
    byte[] contractAddress = generateContractAddress(transaction, owner);
    System.out.println(
        "Your smart contract address will be: " + WalletClient.encode58Check(contractAddress));
    GrpcAPI.Return response = broadcastTransaction(transaction, blockingStubFull);

    return response;
  }

  public static Optional<TransactionInfoList> getTransactionInfoByBlockNum(long blockNum,
      WalletGrpc.WalletBlockingStub blockingStubFull) {
    NumberMessage.Builder builder = NumberMessage.newBuilder();
    builder.setNum(blockNum);
    TransactionInfoList transactionInfoList;
    transactionInfoList = blockingStubFull.getTransactionInfoByBlockNum(builder.build());
    return Optional.ofNullable(transactionInfoList);
  }

  public static Optional<TransactionInfoList> getTransactionInfoByBlockNumFromSolidity(
      long blockNum, WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity) {
    NumberMessage.Builder builder = NumberMessage.newBuilder();
    builder.setNum(blockNum);
    TransactionInfoList transactionInfoList;
    transactionInfoList = blockingStubSolidity.getTransactionInfoByBlockNum(builder.build());
    return Optional.ofNullable(transactionInfoList);
  }

}