package org.tron.client;

import static org.tron.client.SideChainGatewayApi.GatewayApi.GATEWAY_API;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.config.Args;
import org.tron.common.crypto.Hash;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.exception.TxRollbackException;
import org.tron.common.exception.TxFailException;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j
public class SideChainGatewayApi {

  public static Transaction mintTrxTransaction(String to, String value, String txId)
      throws RpcConnectException {

    byte[] toBytes = WalletUtil.decodeFromBase58Check(to);
    byte[] mainChainAddressBytes = Args.getInstance().getMainchainGateway();
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    long type = 1;
    byte[] typeBytes = new DataWord(type).getData();
    // FIXME: right ? hex string in log ?
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(toBytes, 1, toBytes.length),
        Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length), valueBytes,
        typeBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();

    String method = "multiSignForDepositToken(address,address,uint256,uint256,bytes32,bytes)";
    List params = Arrays
        .asList(to, WalletUtil.encode58Check(mainChainAddressBytes), value, type, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken10Transaction(String to, String tokenId, String value,
      String name, String symbol, int decimals, String txId)
      throws RpcConnectException {
    byte[] toBytes = WalletUtil.decodeFromBase58Check(to);
    byte[] tokenIdBytes = new DataWord((new BigInteger(tokenId, 10)).toByteArray()).getData();
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    byte[] decimalsBytes = new DataWord(decimals).getData();
    byte[] nameBytes = new DataWord(name.getBytes()).getData();
    byte[] symbolBytes = new DataWord(symbol.getBytes()).getData();
    // FIXME: right ? hex string in log ?
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(toBytes, 1, toBytes.length), tokenIdBytes, valueBytes,
            decimalsBytes, nameBytes, symbolBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDepositTRC10(address,uint256,uint256,bytes32,bytes32,uint8,bytes32,bytes)";
    List params = Arrays
        .asList(to, tokenId, value, Hex.toHexString(nameBytes), Hex.toHexString(symbolBytes),
            decimals, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken20Transaction(String to, String mainAddress, String value,
      String txId) throws RpcConnectException {
    byte[] toBytes = WalletUtil.decodeFromBase58Check(to);
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainAddress);
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    long type = 2;
    byte[] typeBytes = new DataWord(type).getData();
    // FIXME: right ? hex string in log ?
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(toBytes, 1, toBytes.length),
        Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length), valueBytes,
        typeBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();

    String method = "multiSignForDepositToken(address,address,uint256,uint256,bytes32,bytes)";
    List params = Arrays.asList(to, mainAddress, value, type, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken721Transaction(String to, String mainAddress, String tokenId,
      String txId) throws RpcConnectException {
    byte[] toBytes = WalletUtil.decodeFromBase58Check(to);
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainAddress);
    byte[] tokenIdBytes = new DataWord((new BigInteger(tokenId, 10)).toByteArray()).getData();
    long type = 3;
    byte[] typeBytes = new DataWord(type).getData();
    // FIXME: right ? hex string in log ?
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(toBytes, 1, toBytes.length),
        Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length), tokenIdBytes,
        typeBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();

    String method = "multiSignForDepositToken(address,address,uint256,uint256,bytes32,bytes)";
    List params = Arrays.asList(to, mainAddress, tokenId, type, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction withdrawTRXTransaction(String from, String value, String userSign,
      String txId) throws RpcConnectException {

    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    // FIXME: right ? hx string in log ?
    byte[] userSignBytes = new DataWord(userSign).getData();
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), valueBytes, userSignBytes,
            txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRX(address,uint256,bytes,bytes32,bytes)";
    List params = Arrays.asList(from, value, userSign, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction withdrawTRC10Transaction(String from, String trc10, String value,
      String userSign, String txId) throws RpcConnectException {

    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] trc10Bytes = new DataWord((new BigInteger(trc10, 10)).toByteArray()).getData();
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    // FIXME: right ? hx string in log ?
    byte[] userSignBytes = new DataWord(userSign).getData();
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), trc10Bytes, valueBytes,
            userSignBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRC10(address,uint256,uint256,bytes,bytes32,bytes)";
    List params = Arrays.asList(from, trc10, value, userSign, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction withdrawTRC20Transaction(String from, String mainChainAddress,
      String value, String userSign, String txId) throws RpcConnectException {

    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainChainAddress);
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    long type = 2;
    byte[] typeBytes = new DataWord(type).getData();
    // FIXME: right ? hx string in log ?
    byte[] userSignBytes = new DataWord(userSign).getData();
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length),
        Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length), valueBytes,
        typeBytes, userSignBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawToken(address,address,uint256,uint256,bytes,bytes32,bytes)";
    List params = Arrays.asList(from, mainChainAddress, value, type, userSign, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction withdrawTRC721Transaction(String from, String mainChainAddress,
      String tokenId, String userSign, String txId) throws RpcConnectException {
    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainChainAddress);
    byte[] tokenIdBytes = new DataWord((new BigInteger(tokenId, 10)).toByteArray()).getData();
    long type = 3;
    byte[] typeBytes = new DataWord(type).getData();
    // FIXME: right ? hx string in log ?
    byte[] userSignBytes = new DataWord(userSign).getData();
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length),
        Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length), tokenIdBytes,
        typeBytes, userSignBytes, txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawToken(address,address,uint256,uint256,bytes,bytes32,bytes)";
    List params = Arrays.asList(from, mainChainAddress, tokenId, type, userSign, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mappingTransaction(String mainChainAddress, String sideChainAddress, String txId) throws RpcConnectException {
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainChainAddress);
    byte[] sideChainAddressBytes = WalletUtil.decodeFromBase58Check(sideChainAddress);
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length),
        Arrays.copyOfRange(sideChainAddressBytes, 1, sideChainAddressBytes.length), txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDeployAndMapping(address,address,bytes32,bytes)";
    List params = Arrays.asList(mainChainAddress, sideChainAddress, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  // Singleton
  enum GatewayApi {
    GATEWAY_API;

    private WalletClient instance;
    private WalletClient solidityInstance;

    GatewayApi() {
      instance = new WalletClient(Args.getInstance().getSidechainFullNode(),
          Args.getInstance().getOraclePrivateKey(), false);
      solidityInstance = new WalletClient(Args.getInstance().getSidechainSolidity(),
          Args.getInstance().getOraclePrivateKey(), false);
    }

    public WalletClient getInstance() {
      return instance;
    }

    public WalletClient getSolidityInstance() {
      return solidityInstance;
    }
  }

  public static List<String> getWithdrawOracleSigns(String txId, String dataHash)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "withdrawSigns(bytes32,bytes32)";
    List params = Arrays.asList(txId, dataHash);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackOracleSigns(ret);
  }

  public static List<String> getMappingOracleSigns(String txId, String dataHash)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "mappingSigns(bytes32,bytes32)";
    List params = Arrays.asList(txId, dataHash);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackOracleSigns(ret);
  }


  public static String getSunTokenAddress() throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "sunTokenAddress()";
    List<Object> params = new ArrayList<>();
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static byte[] checkTxInfo(TransactionExtensionCapsule txId)
      throws TxFailException, TxRollbackException {
    return GATEWAY_API.getSolidityInstance().checkTxInfo(txId.getTransactionId());
  }

  public static boolean broadcast(Transaction transaction)
      throws RpcConnectException, TxValidateException {
    return GATEWAY_API.getInstance().broadcast(transaction);
  }
}
