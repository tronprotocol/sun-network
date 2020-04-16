package org.tron.client;

import static org.tron.common.utils.WalletUtil.sleep;

import com.google.protobuf.ByteString;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.common.config.Args;
import org.tron.common.config.SystemSetting;
import org.tron.common.crypto.ECKey;
import org.tron.common.crypto.ECKey.ECDSASignature;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxExpiredException;
import org.tron.common.exception.TxFailException;
import org.tron.common.exception.TxRollbackException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.TransactionUtils;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionInfo.code;

@Slf4j(topic = "walletClient")
class WalletClient {

  private RpcClient rpcCli;
  private SolidityRpcClient solidityRpcCli;
  private ECKey ecKey;
  private byte[] address;
  private boolean isMainChain;

  WalletClient(String fullNode, String solidityNode, byte[] privateKey, boolean isMainChain) {
    rpcCli = new RpcClient(fullNode);
    if (StringUtils.isNotEmpty(solidityNode)) {
      solidityRpcCli = new SolidityRpcClient(solidityNode);
    }
    ecKey = ECKey.fromPrivate(privateKey);
    address = ecKey.getAddress();
    this.isMainChain = isMainChain;
  }

  byte[] triggerConstantContractAndReturn(byte[] contractAddress, String method,
      List<Object> params, long callValue, long tokenId, long tokenValue)
      throws RpcConnectException {

    if (logger.isInfoEnabled()) {
      logger.info(
          "trigger constant, contract address: {}, method: {}, params: {}, call value: {}, token id: {}, token value: {}",
          WalletUtil.encode58Check(contractAddress), method, params.toString(), callValue, tokenId,
          tokenValue);
    }

    byte[] data = AbiUtil.parseMethod(method, params);
    // TODO retry
    org.tron.api.GrpcAPI.TransactionExtention transactionExtention = this
        .triggerConstantContract(contractAddress, data, callValue, tokenValue, tokenId);

    Transaction transaction = transactionExtention.getTransaction();
    if (transaction.getRetCount() != 0 && transactionExtention.getConstantResult(0) != null
        && transactionExtention.getResult() != null) {
      return transactionExtention.getConstantResult(0).toByteArray();
    }
    throw new RpcConnectException("no result");
  }

  Transaction triggerContractTransaction(byte[] contractAddress, String method,
      List<Object> params,
      long callValue, long tokenId, long tokenValue) throws RpcConnectException {
    if (logger.isInfoEnabled()) {
      logger.info(
          "trigger not constant, contract address: {}, method: {}, params: {}, call value: {}, token id: {}, token value: {}",
          WalletUtil.encode58Check(contractAddress), method, params.toString(), callValue, tokenId,
          tokenValue);
    }

    byte[] data = AbiUtil.parseMethod(method, params);
    return triggerContractTransaction(contractAddress, data, SystemSetting.FEE_LIMIT,
        callValue, tokenValue, tokenId);

  }

  private org.tron.api.GrpcAPI.TransactionExtention triggerConstantContract(byte[] contractAddress,
      byte[] data, long callValue, long tokenValue, Long tokenId) throws RpcConnectException {
    byte[] owner = address;
    Contract.TriggerSmartContract triggerContract = buildTriggerContract(owner, contractAddress,
        callValue, data, tokenValue, tokenId);
    org.tron.api.GrpcAPI.TransactionExtention transactionExtention = null;
    for (int i = SystemSetting.CLIENT_MAX_RETRY; i > 0; i--) {
      transactionExtention = rpcCli.triggerContract(triggerContract);
      if (transactionExtention != null && transactionExtention.getResult().getResult()) {
        break;
      }
      WalletUtil.sleep(SystemSetting.CLIENT_RETRY_INTERVAL);
    }
    if (transactionExtention == null) {
      logger.error("rpc fail, return null");
      throw new RpcConnectException("rpc fail, return null: ");
    }

    Return ret = transactionExtention.getResult();
    if (!ret.getResult()) {
      logger.error("rpc fail, code: {}, message: {}", ret.getCode(),
          ret.getMessage().toStringUtf8());
      throw new RpcConnectException(
          "rpc fail, code: " + ret.getCode());
    }
    return transactionExtention;
  }

  private Transaction triggerContractTransaction(byte[] contractAddress, byte[] data, long feeLimit,
      long callValue,
      long tokenValue, Long tokenId) throws RpcConnectException {
    GrpcAPI.TransactionExtention transactionExtention = getTransactionExtension(contractAddress,
        data, feeLimit, callValue, tokenValue, tokenId);

    return getTransaction(transactionExtention);
  }

  private GrpcAPI.TransactionExtention getTransactionExtension(byte[] contractAddress, byte[] data,
      long feeLimit, long callValue, long tokenValue, Long tokenId) throws RpcConnectException {
    byte[] owner = address;
    Contract.TriggerSmartContract triggerContract = buildTriggerContract(owner, contractAddress,
        callValue, data, tokenValue, tokenId);

    GrpcAPI.TransactionExtention transactionExtension = null;
    for (int i = SystemSetting.CLIENT_MAX_RETRY; i > 0; i--) {
      transactionExtension = rpcCli.triggerContract(triggerContract);
      if (transactionExtension != null && transactionExtension.getResult().getResult()) {
        break;
      }
      sleep(SystemSetting.CLIENT_RETRY_INTERVAL);
    }
    if (transactionExtension == null || !transactionExtension.getResult().getResult()) {
      logger.error("rpc fail, code: {}, message: {}", transactionExtension.getResult().getCode(),
          transactionExtension.getResult().getMessage().toStringUtf8());
      throw new RpcConnectException(
          "rpc fail, code: " + transactionExtension.getResult().getCode());
    }

    GrpcAPI.TransactionExtention.Builder txBuilder = GrpcAPI.TransactionExtention
        .newBuilder();
    Transaction.Builder transBuilder = Transaction.newBuilder();
    Transaction.raw.Builder rawBuilder = transactionExtension.getTransaction().getRawData()
        .toBuilder();
    rawBuilder.setFeeLimit(feeLimit);
    transBuilder.setRawData(rawBuilder);
    for (int i = 0; i < transactionExtension.getTransaction().getSignatureCount(); i++) {
      ByteString s = transactionExtension.getTransaction().getSignature(i);
      transBuilder.setSignature(i, s);
    }
    for (int i = 0; i < transactionExtension.getTransaction().getRetCount(); i++) {
      Result r = transactionExtension.getTransaction().getRet(i);
      transBuilder.setRet(i, r);
    }
    txBuilder.setTransaction(transBuilder);
    txBuilder.setResult(transactionExtension.getResult());
    txBuilder.setTxid(transactionExtension.getTxid());
    transactionExtension = txBuilder.build();
    return transactionExtension;
  }

  static Contract.TriggerSmartContract buildTriggerContract(byte[] address,
      byte[] contractAddress,
      long callValue, byte[] data, long tokenValue, Long tokenId) {
    Contract.TriggerSmartContract.Builder builder = Contract.TriggerSmartContract.newBuilder();
    builder.setOwnerAddress(ByteString.copyFrom(address));
    builder.setContractAddress(ByteString.copyFrom(contractAddress));
    builder.setData(ByteString.copyFrom(data));
    builder.setCallValue(callValue);
    builder.setTokenId(tokenId);
    builder.setCallTokenValue(tokenValue);
    return builder.build();
  }

  AssetIssueContract getAssetIssueById(String assetId) {
    AssetIssueContract assetIssueById = null;
    for (int i = SystemSetting.CLIENT_MAX_RETRY; i > 0; i--) {
      assetIssueById = rpcCli.getAssetIssueById(assetId);
      if (assetIssueById != null) {
        break;
      }
      sleep(SystemSetting.CLIENT_RETRY_INTERVAL);
    }

    return assetIssueById;
  }

  SmartContract getContract(byte[] contractAddress) {
    SmartContract smartContract = null;
    for (int i = SystemSetting.CLIENT_MAX_RETRY; i > 0; i--) {
      smartContract = rpcCli.getContract(contractAddress);
      if (smartContract != null) {
        break;
      }
      sleep(SystemSetting.CLIENT_RETRY_INTERVAL);
    }

    return smartContract;
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
    return TransactionUtils.sign(transaction, this.ecKey, getCurrentChainId(), isMainChain);
  }

  byte[] signDigest(byte[] digest) {
    ECDSASignature signature = this.ecKey.sign(digest);
    return signature.toByteArray();
  }

  private byte[] getCurrentChainId() {
    if (isMainChain) {
      return new byte[0];
    }
    List<byte[]> chainIdList = new ArrayList();
    chainIdList.add(Args.getInstance().getChainId());
    return ByteArray.fromBytes21List(chainIdList);
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

  byte[] checkTxInfo(String txId) throws TxRollbackException, TxFailException {
    for (int i = SystemSetting.CLIENT_MAX_RETRY; i > 0; i--) {
      Optional<TransactionInfo> transactionInfo;
      if (solidityRpcCli != null) {
        transactionInfo = solidityRpcCli.getTransactionInfoById(txId);
      } else {
        transactionInfo = rpcCli.getTransactionInfoById(txId);
      }
      TransactionInfo info = transactionInfo.get();
      if (info.getBlockTimeStamp() == 0L) {
        logger.info("will retry {} time(s)", i + 1);
        sleep(SystemSetting.CLIENT_RETRY_INTERVAL);
      } else {
        if (info.getResult().equals(code.SUCESS)) {
          return info.getContractResult(0).toByteArray();
        } else {
          throw new TxFailException(info.getResMessage().toStringUtf8());
        }
      }
    }
    throw new TxRollbackException(txId);
  }

  public byte[] getAddress() {
    return address;
  }

  public String getAddressStr() {
    return WalletUtil.encode58CheckForTron(getAddress());
  }
}