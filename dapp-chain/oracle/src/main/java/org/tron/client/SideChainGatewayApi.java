package org.tron.client;

import static org.tron.client.SideChainGatewayApi.GatewayApi.GATEWAY_API;

import com.beust.jcommander.internal.Lists;
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
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.eventactuator.SignListParam;

@Slf4j(topic = "sideApi")
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

  public static Transaction withdrawTRC10Transaction(String from, String tokenId, String value,
      String nonce) throws RpcConnectException {

    String ownSign = getWithdrawTRC10Sign(from, tokenId, value, nonce);

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRC10(uint256,bytes)";
    List params = Arrays.asList(nonce, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }


  public static String getWithdrawTRC10Sign(String from, String tokenId, String value,
      String nonce) {
    return Hex.toHexString(GATEWAY_API.getInstance()
        .signDigest(getWithdrawTRC10DataHash(from, tokenId, value, nonce)));
  }

  public static SignListParam getWithdrawTRC10SignParam(String from, String tokenId, String value,
      String nonce) {
    return new SignListParam(
        Lists.newArrayList(getWithdrawTRC10Sign(from, tokenId, value, nonce)),
        Lists.newArrayList(GATEWAY_API.getInstance().getAddressStr()));
  }

  public static byte[] getWithdrawTRC10DataHash(String from, String tokenId, String value,
      String nonce) {
    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] tokenIdBytes = new DataWord((new BigInteger(tokenId, 10)).toByteArray()).getData();
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    byte[] nonceBytes = new DataWord((new BigInteger(nonce, 10)).toByteArray()).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), tokenIdBytes, valueBytes,
            nonceBytes);
    return Hash.sha3(data);
  }

  public static Transaction withdrawTRC20Transaction(String from, String mainChainAddress,
      String value, String nonce) throws RpcConnectException {

    String ownSign = getWithdrawTRCTokenSign(from, mainChainAddress, value, nonce);

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRC20(uint256,bytes)";
    List params = Arrays.asList(nonce, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static String getWithdrawTRCTokenSign(String from, String mainChainAddress, String value,
      String nonce) {
    return Hex.toHexString(GATEWAY_API.getInstance()
        .signDigest(getWithdrawTRCTokenDataHash(from, mainChainAddress, value, nonce)));
  }

  public static SignListParam getWithdrawTRCTokenSignParam(String from, String mainChainAddress,
      String value, String nonce) {
    return new SignListParam(
        Lists.newArrayList(getWithdrawTRCTokenSign(from, mainChainAddress, value, nonce)),
        Lists.newArrayList(GATEWAY_API.getInstance().getAddressStr()));
  }

  public static byte[] getWithdrawTRCTokenDataHash(String from, String mainChainAddress,
      String value,
      String nonce) {
    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] mainChainAddressBytes = WalletUtil.decodeFromBase58Check(mainChainAddress);
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    byte[] nonceBytes = new DataWord((new BigInteger(nonce, 10)).toByteArray()).getData();
    byte[] data = ByteUtil.merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length),
        Arrays.copyOfRange(mainChainAddressBytes, 1, mainChainAddressBytes.length), valueBytes,
        nonceBytes);
    return Hash.sha3(data);
  }

  public static Transaction withdrawTRC721Transaction(String from, String mainChainAddress,
      String uId, String nonce) throws RpcConnectException {
    String ownSign = getWithdrawTRCTokenSign(from, mainChainAddress, uId, nonce);

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRC721(uint256,bytes)";
    List params = Arrays.asList(nonce, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction withdrawTRXTransaction(String from, String value, String nonce)
      throws RpcConnectException {
    String ownSign = getWithdrawTRXSign(from, value, nonce);

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForWithdrawTRX(uint256,bytes)";
    List params = Arrays.asList(nonce, ownSign);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static String getWithdrawTRXSign(String from, String value, String nonce) {
    return Hex.toHexString(
        GATEWAY_API.getInstance().signDigest(getWithdrawTRXDataHash(from, value, nonce)));
  }

  public static SignListParam getWithdrawTRXSignParam(String from, String value, String nonce) {
    return new SignListParam(Lists.newArrayList(getWithdrawTRXSign(from, value, nonce)),
        Lists.newArrayList(GATEWAY_API.getInstance().getAddressStr()));
  }

  public static byte[] getWithdrawTRXDataHash(String from, String value, String nonce) {
    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    byte[] nonceBytes = new DataWord((new BigInteger(nonce, 10)).toByteArray()).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), valueBytes, nonceBytes);
    return Hash.sha3(data);
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
      String trcSymbol, long trcDecimals, String contractOwner, String nonce)
      throws RpcConnectException {

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDeployDAppTRC20AndMapping(address,string,string,uint8,address,uint256)";
    List params = Arrays
        .asList(contractAddressStr, trcName, trcSymbol, trcDecimals, contractOwner, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction multiSignForMappingTRC721(String contractAddressStr, String trcName,
      String trcSymbol, String contractOwner, String nonce) throws RpcConnectException {

    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "multiSignForDeployDAppTRC721AndMapping(address,string,string,address,uint256)";
    List params = Arrays
        .asList(contractAddressStr, trcName, trcSymbol, contractOwner, nonce);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  // Singleton
  enum GatewayApi {
    GATEWAY_API;

    private WalletClient instance;

    GatewayApi() {
      instance = new WalletClient(Args.getInstance().getSidechainFullNode(),
          Args.getInstance().getSidechainSolidity(), Args.getInstance().getOraclePrivateKey(),
          false);
    }

    public WalletClient getInstance() {
      return instance;
    }
  }

  public static SignListParam getWithdrawOracleSigns(String nonce) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "getWithdrawSigns(uint256)";
    List params = Arrays.asList(nonce);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackSignListParam(ret);
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
    return GATEWAY_API.getInstance().checkTxInfo(txId);
  }

  public static boolean broadcast(Transaction transaction)
      throws RpcConnectException, TxValidateException, TxExpiredException {
    return GATEWAY_API.getInstance().broadcast(transaction);
  }
}
