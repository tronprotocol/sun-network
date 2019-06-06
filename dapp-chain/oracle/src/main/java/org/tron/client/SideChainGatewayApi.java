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
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j
public class SideChainGatewayApi {

  public static TransactionExtensionCapsule mintTrx(String to, String value)
      throws RpcConnectException, TxValidateException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRX(address,uint256)";
    List params = Arrays.asList(to, value);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintTrxTransaction(String to, String value) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRX(address,uint256)";
    List params = Arrays.asList(to, value);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionExtensionCapsule mintToken10(String to, String tokenId, String value,
      String name,
      String symbol, int decimals)
      throws RpcConnectException, TxValidateException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC10(address,uint256,uint256,string,string,uint8)";
    List params = Arrays.asList(to, tokenId, value, name, symbol, decimals);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken10Transaction(String to, String tokenId, String value,
      String name,
      String symbol, int decimals)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC10(address,uint256,uint256,bytes32,bytes32,uint8)";
    List params = Arrays
        .asList(to, tokenId, value, Hex.toHexString(new DataWord(name.getBytes()).getData()),
            Hex.toHexString(new DataWord(symbol.getBytes()).getData()), decimals);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionExtensionCapsule mintToken20(String to, String mainAddress, String value)
      throws RpcConnectException, TxValidateException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC20(address,address,uint256)";
    List params = Arrays.asList(to, mainAddress, value);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken20Transaction(String to, String mainAddress, String value)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC20(address,address,uint256)";
    List params = Arrays.asList(to, mainAddress, value);
    return GATEWAY_API.getInstance()
        .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionExtensionCapsule mintToken721(String to, String mainAddress,
      String value)
      throws RpcConnectException, TxValidateException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC721(address,address,uint256)";
    List params = Arrays.asList(to, mainAddress, value);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static Transaction mintToken721Transaction(String to, String mainAddress, String value)
      throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC721(address,address,uint256)";
    List params = Arrays.asList(to, mainAddress, value);
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

  public static String getMainToSideContractMap(String address) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "mainToSideContractMap(address)";
    List params = Arrays.asList(address);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static String getSunTokenAddress() throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "sunTokenAddress()";
    List<Object> params = new ArrayList<>();
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static String getMainToSideTRC10Map(String tokenId) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "mainToSideTRC10Map(uint256)";
    List params = Arrays.asList(tokenId);
    byte[] ret = MainChainGatewayApi.GatewayApi.GATEWAY_API.getInstance()
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
