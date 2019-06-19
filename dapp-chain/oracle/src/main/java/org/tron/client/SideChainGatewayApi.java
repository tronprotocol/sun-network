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
import org.tron.common.exception.TxExpiredException;
import org.tron.common.exception.TxFailException;
import org.tron.common.exception.TxRollbackException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;

@Slf4j
public class SideChainGatewayApi {

  public static Transaction mintTrxTransaction(String to, String value, String nonce)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();

    String method = "multiSignForDepositTRX(address,uint256,uint256)";
    List params = Arrays.asList(to, value, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken10Transaction(String to, String tokenId, String value,
      String name, String symbol, int decimals, String nonce)
      throws RpcConnectException {

    byte[] nameBytes = new DataWord(name.getBytes()).getData();
    byte[] symbolBytes = new DataWord(symbol.getBytes()).getData();

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDepositTRC10(address,trcToken,uint256,bytes32,bytes32,uint8,uint256)";
    List params = Arrays
        .asList(to, tokenId, value, Hex.toHexString(nameBytes), Hex.toHexString(symbolBytes),
            decimals, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken20Transaction(String to, String mainAddress, String value,
      String nonce) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();

    String method = "multiSignForDepositTRC20(address,address,uint256,uint256)";
    List params = Arrays.asList(to, mainAddress, value, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken721Transaction(String to, String mainAddress, String uid,
      String nonce) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();

    String method = "multiSignForDepositTRC721(address,address,uint256,uint256)";
    List params = Arrays.asList(to, mainAddress, uid, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction withdrawTRXTransaction(String from, String value, String userSign,
      String txId) throws RpcConnectException {

    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    // FIXME: right ? hx string in log ?
    byte[] userSignBytes = ByteArray.fromHexString(userSign);
    byte[] txIdBytes = ByteArray.fromHexString(txId);
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
      String userSign, String nonce) throws RpcConnectException {

    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] trc10Bytes = new DataWord((new BigInteger(trc10, 10)).toByteArray()).getData();
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    // FIXME: right ? hx string in log ?
    byte[] userSignBytes = ByteArray.fromHexString(userSign);
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), trc10Bytes, valueBytes,
            userSignBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRC10(address,uint256,uint256,bytes,bytes32,uint256)";
    List params = Arrays.asList(from, trc10, value, userSign, ownSign, nonce);
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
    byte[] userSignBytes = ByteArray.fromHexString(userSign);
    byte[] txIdBytes = ByteArray.fromHexString(txId);
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
    byte[] userSignBytes = ByteArray.fromHexString(userSign);
    byte[] txIdBytes = ByteArray.fromHexString(txId);
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

  public static Transaction mappingTransaction(String mainChainAddress, String sideChainAddress,
      String txId) throws RpcConnectException {
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainChainAddress);
    byte[] sideChainAddressBytes = WalletUtil.decodeFromBase58Check(sideChainAddress);
    byte[] txIdBytes = new DataWord(txId).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length),
            Arrays.copyOfRange(sideChainAddressBytes, 1, sideChainAddressBytes.length), txIdBytes);
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDeployAndMapping(address,address,bytes32,bytes)";
    List params = Arrays.asList(mainChainAddress, sideChainAddress, txId, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction multiSignForMappingTRC20(String contractAddressStr, String trcName,
      String trcSymbol, long trcDecimals, String nonce) throws RpcConnectException {

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDeployDAppTRC20AndMapping(address,string,string,uint8,uint256)";
    List params = Arrays
        .asList(contractAddressStr, trcName, trcSymbol, trcDecimals, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction multiSignForMappingTRC721(String contractAddressStr, String trcName,
      String trcSymbol, String nonce) throws RpcConnectException {

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDeployDAppTRC721AndMapping(address,string,string,uint256)";
    List params = Arrays
        .asList(contractAddressStr, trcName, trcSymbol, nonce);
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
    String method = "getWithdrawSigns(bytes32,bytes32)";
    List params = Arrays.asList(txId, dataHash);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackOracleSigns(ret);
  }

  public static List<String> getMappingOracleSigns(String txId, String dataHash)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "getMappingSigns(bytes32,bytes32)";
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

  public static byte[] checkTxInfo(String txId)
      throws TxFailException, TxRollbackException {
    return GATEWAY_API.getSolidityInstance().checkTxInfo(txId);
  }

  public static boolean broadcast(Transaction transaction)
      throws RpcConnectException, TxValidateException, TxExpiredException {
    return GATEWAY_API.getInstance().broadcast(transaction);
  }
}
