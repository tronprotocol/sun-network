package org.tron.client;

import static org.tron.common.utils.WalletUtil.sleep;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.ByteString;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.common.config.SystemSetting;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.Hash;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxExpiredException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Sha256Hash;
import org.tron.common.utils.TransactionUtils;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Contract.AccountCreateContract;
import org.tron.protos.Contract.CreateSmartContract;
import org.tron.protos.Contract.TriggerSmartContract;
import org.tron.protos.Protocol.AccountType;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.raw.Builder;

@Slf4j(topic = "walletClient")
public class WalletClient {

  private RpcClient rpcCli;
  private ECKey ecKey;
  @Getter
  private byte[] address;
  private boolean isMainChain;
  private byte[] chainId;

  public WalletClient(String target, byte[] priateKey) {
    rpcCli = new RpcClient(target);
    ecKey = ECKey.fromPrivate(priateKey);
    address = ecKey.getAddress();
    this.isMainChain = true;
  }

  public WalletClient(String target, byte[] priateKey, String chainId) {
    rpcCli = new RpcClient(target);
    ecKey = ECKey.fromPrivate(priateKey);
    address = ecKey.getAddress();
    this.isMainChain = false;
    this.chainId = ByteArray.fromHexString(chainId);
  }

  public void triggerContract(String contractAddressStr, String method, String paramter) {
    try {
      byte[] input = AbiUtil.parseMethod(method, paramter, false);
      byte[] contractAddress = WalletUtil.decodeFromBase58Check(contractAddressStr);

      TriggerSmartContract triggerSmartContract = TriggerSmartContract.newBuilder()
          .setOwnerAddress(ByteString.copyFrom(address)).setData(ByteString.copyFrom(input))
          .setContractAddress(ByteString.copyFrom(contractAddress)).build();
      TransactionExtention transactionExtention = rpcCli
          .triggerContract(triggerSmartContract);
      Builder raw = transactionExtention.getTransaction().getRawData().toBuilder()
          .setFeeLimit(1000000000);
      Transaction.Builder transaction = transactionExtention.getTransaction().toBuilder()
          .setRawData(raw);
      TransactionExtention extention = transactionExtention.toBuilder().setTransaction(transaction)
          .build();
      Transaction transactionRet = getTransaction(extention);
      System.out.println("txid is " + ByteArray
          .toHexString(Sha256Hash.hash(transactionRet.getRawData().toByteArray())));
      broadcast(transactionRet);
    } catch (RpcConnectException e) {
      e.printStackTrace();
    } catch (TxValidateException e) {
      e.printStackTrace();
    } catch (TxExpiredException e) {
      e.printStackTrace();
    }

  }

  public void triggerSideContract(String contractAddressStr, String method, String paramter) {
    try {
      byte[] input = AbiUtil.parseMethod(method, paramter, false);
      byte[] contractAddress = WalletUtil.decodeFromBase58Check(contractAddressStr);

      TriggerSmartContract triggerSmartContract = TriggerSmartContract.newBuilder()
          .setOwnerAddress(ByteString.copyFrom(address)).setData(ByteString.copyFrom(input))
          .setContractAddress(ByteString.copyFrom(contractAddress)).build();
      TransactionExtention transactionExtention = rpcCli
          .triggerContract(triggerSmartContract);
      Builder raw = transactionExtention.getTransaction().getRawData().toBuilder()
          .setFeeLimit(1000000000);
      Transaction.Builder transaction = transactionExtention.getTransaction().toBuilder()
          .setRawData(raw);
      TransactionExtention extention = transactionExtention.toBuilder().setTransaction(transaction)
          .build();
      Transaction transactionRet = getSideTransaction(extention);
      System.out.println("txid is " + ByteArray
          .toHexString(Sha256Hash.hash(transactionRet.getRawData().toByteArray())));
      broadcast(transactionRet);
    } catch (RpcConnectException e) {
      e.printStackTrace();
    } catch (TxValidateException e) {
      e.printStackTrace();
    } catch (TxExpiredException e) {
      e.printStackTrace();
    }

  }

  public void createAccount(String newAddress) {
    try {
      byte[] accountCreateAddress = WalletUtil.decodeFromBase58Check(newAddress);

      AccountCreateContract accountCreateContract = AccountCreateContract.newBuilder()
          .setOwnerAddress(ByteString.copyFrom(address)).setType(AccountType.Normal)
          .setAccountAddress(ByteString.copyFrom(accountCreateAddress)).build();
      TransactionExtention transactionExtention = rpcCli
          .createAccount(accountCreateContract);
      Transaction transactionRet = getSideTransaction(transactionExtention);
      System.out.println("txid is " + ByteArray
          .toHexString(Sha256Hash.hash(transactionRet.getRawData().toByteArray())));
      broadcast(transactionRet);
    } catch (RpcConnectException e) {
      e.printStackTrace();
    } catch (TxValidateException e) {
      e.printStackTrace();
    } catch (TxExpiredException e) {
      e.printStackTrace();
    }

  }

  public byte[] triggerConstantContract(String contractAddressStr, String method,
      String paramter) {

    byte[] input = AbiUtil.parseMethod(method, paramter, false);
    byte[] contractAddress = WalletUtil.decodeFromBase58Check(contractAddressStr);

    TriggerSmartContract triggerSmartContract = TriggerSmartContract.newBuilder()
        .setOwnerAddress(ByteString.copyFrom(address)).setData(ByteString.copyFrom(input))
        .setContractAddress(ByteString.copyFrom(contractAddress)).build();
    TransactionExtention extention = rpcCli
        .triggerContract(triggerSmartContract);
    return extention.getConstantResult(0).toByteArray();
  }

  public byte[] deployContract(String contractName, String abi, String code) {
    try {
      SmartContract.ABI contractABI = jsonStr2ABI(abi);
      if (contractABI == null) {
        logger.error("abi is null");
        return new byte[0];
      }
      SmartContract.Builder builder = SmartContract.newBuilder();
      builder.setName(contractName);
      builder.setOriginAddress(ByteString.copyFrom(address));
      builder.setAbi(contractABI);
      builder.setConsumeUserResourcePercent(100)
          .setOriginEnergyLimit(1000000);
      builder.setBytecode(ByteString.copyFrom(Hex.decode(code)));
      CreateSmartContract createSmartContract = CreateSmartContract.newBuilder().setOwnerAddress(
          ByteString.copyFrom(address)).setNewContract(builder).build();
      TransactionExtention transactionExtention = rpcCli.deployContract(createSmartContract);

      Builder raw = transactionExtention.getTransaction().getRawData().toBuilder()
          .setFeeLimit(1000000000);
      Transaction.Builder transaction = transactionExtention.getTransaction().toBuilder()
          .setRawData(raw);
      TransactionExtention extention = transactionExtention.toBuilder().setTransaction(transaction)
          .build();
      Transaction transactionRet = getTransaction(extention);
      broadcast(transactionRet);
      return Sha256Hash.hash(transactionRet.getRawData().toByteArray());
    } catch (RpcConnectException e) {
      e.printStackTrace();
    } catch (TxValidateException e) {
      e.printStackTrace();
    } catch (TxExpiredException e) {
      e.printStackTrace();
    }

    return null;
  }

  public byte[] deploySideContract(String contractName, String abi, String code) {
    try {
      SmartContract.ABI contractABI = jsonStr2ABI(abi);
      if (contractABI == null) {
        logger.error("abi is null");
        return new byte[0];
      }
      SmartContract.Builder builder = SmartContract.newBuilder();
      builder.setName(contractName);
      builder.setOriginAddress(ByteString.copyFrom(address));
      builder.setAbi(contractABI);
      builder.setConsumeUserResourcePercent(100)
          .setOriginEnergyLimit(1000000);
      builder.setBytecode(ByteString.copyFrom(Hex.decode(code)));
      CreateSmartContract createSmartContract = CreateSmartContract.newBuilder().setOwnerAddress(
          ByteString.copyFrom(address)).setNewContract(builder).build();
      TransactionExtention transactionExtention = rpcCli.deployContract(createSmartContract);

      Builder raw = transactionExtention.getTransaction().getRawData().toBuilder()
          .setFeeLimit(1000000000);
      Transaction.Builder transaction = transactionExtention.getTransaction().toBuilder()
          .setRawData(raw);
      TransactionExtention extention = transactionExtention.toBuilder().setTransaction(transaction)
          .build();
      Transaction transactionRet = getSideTransaction(extention);
      broadcast(transactionRet);
      return Sha256Hash.hash(transactionRet.getRawData().toByteArray());
    } catch (RpcConnectException e) {
      e.printStackTrace();
    } catch (TxValidateException e) {
      e.printStackTrace();
    } catch (TxExpiredException e) {
      e.printStackTrace();
    }

    return null;
  }

  private Transaction getTransaction(
      org.tron.api.GrpcAPI.TransactionExtention transactionExtention)
      throws RpcConnectException {
    if (transactionExtention == null) {
      throw new RpcConnectException("transactionExtensionCapsule is null");
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      logger
          .error("rpc fail, code: {}, message: {}", ret.getCode(), ret.getMessage().toStringUtf8());
      throw new RpcConnectException("rpc fail, code: " + ret.getCode());
    }
    Transaction transaction = transactionExtention.getTransaction();

    if (transaction.getRawData().getTimestamp() == 0) {
      transaction = TransactionUtils.setTimestamp(transaction);
    }
    return TransactionUtils
        .sign(transaction, this.ecKey, new byte[]{}, isMainChain);
  }

  private Transaction getSideTransaction(
      org.tron.api.GrpcAPI.TransactionExtention transactionExtention)
      throws RpcConnectException {
    if (transactionExtention == null) {
      throw new RpcConnectException("transactionExtensionCapsule is null");
    }
    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      logger
          .error("rpc fail, code: {}, message: {}", ret.getCode(), ret.getMessage().toStringUtf8());
      throw new RpcConnectException("rpc fail, code: " + ret.getCode());
    }
    Transaction transaction = transactionExtention.getTransaction();

    if (transaction.getRawData().getTimestamp() == 0) {
      transaction = TransactionUtils.setTimestamp(transaction);
    }
    return TransactionUtils.sign(transaction, this.ecKey, this.chainId, isMainChain);
  }

  boolean broadcast(Transaction transaction)
      throws RpcConnectException, TxValidateException, TxExpiredException {
    for (int i = SystemSetting.CLIENT_MAX_RETRY; i > 0; i--) {
      Optional<Return> broadcastResponse = rpcCli.broadcastTransaction(transaction);
      Return response = broadcastResponse.get();
      if (response.getResult()) {
        // true is success
        return true;
      } else {
        // false is fail
        if (response.getCode().equals(response_code.SERVER_BUSY)) {
          // when SERVER_BUSY, retry
          logger.info("will retry {} time(s)", i + 1);
          sleep(SystemSetting.CLIENT_RETRY_INTERVAL);
        } else if (response.getCode().equals(response_code.DUP_TRANSACTION_ERROR)) {
          logger.info("this tx has be broadcasted");
          return true;
        } else if (response.getCode().equals(response_code.TRANSACTION_EXPIRATION_ERROR)) {
          logger.info("transaction expired");
          throw new TxExpiredException("tx error, " + response.getMessage().toStringUtf8());
        } else {
          logger.error("tx error, fail, code: {}, message {}", response.getCode(),
              response.getMessage().toStringUtf8());
          // fail, not retry
          throw new TxValidateException("tx error, " + response.getMessage().toStringUtf8());
        }
      }
    }
    logger.error("broadcast transaction, exceed max retry, fail");
    throw new RpcConnectException("broadcast transaction, exceed max retry, fail");
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

  private static SmartContract.ABI.Entry.EntryType getEntryType(String type) {
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

  private static SmartContract.ABI.Entry.StateMutabilityType getStateMutability(
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

  public byte[] generateContractAddress(Transaction trx) {

    // get owner address
    // this address should be as same as the onweraddress in trx, DONNOT modify it
    byte[] ownerAddress = address;

    // get tx hash
    byte[] txRawDataHash = Sha256Hash.of(trx.getRawData().toByteArray()).getBytes();

    // combine
    byte[] combined = new byte[txRawDataHash.length + ownerAddress.length];
    System.arraycopy(txRawDataHash, 0, combined, 0, txRawDataHash.length);
    System.arraycopy(ownerAddress, 0, combined, txRawDataHash.length, ownerAddress.length);

    return Hash.sha3omit12(combined);

  }
}