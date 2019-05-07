package org.tron.client;

import static org.tron.client.SideChainGatewayApi.GatewayApi.GATEWAY_API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.Args;
import org.tron.common.exception.ContractException;
import org.tron.common.exception.RpcException;
import org.tron.common.exception.TxNotFoundException;
import org.tron.common.utils.AbiUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.TransactionId;

@Slf4j
public class SideChainGatewayApi {

  public static TransactionId mintTrx(String to, String value) throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRX(address,uint256)";
    List params = Arrays.asList(to, value);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId mintToken10(String to, String tokenId, String value, String name,
      String symbol, int decimals)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC10(address,uint256,uint256,string,string,uint8)";
    List params = Arrays.asList(to, tokenId, value, name, symbol, decimals);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId mintToken20(String to, String mainAddress, String value)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC20(address,address,uint256)";
    List params = Arrays.asList(to, mainAddress, value);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId mintToken721(String to, String mainAddress, String value)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "depositTRC721(address,address,uint256)";
    List params = Arrays.asList(to, mainAddress, value);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  // Singleton
  enum GatewayApi {
    GATEWAY_API;

    private WalletClient instance;

    GatewayApi() {
      instance = new WalletClient(Args.getInstance().getSidechainFullNode(),
          Args.getInstance().getOraclePrivateKey());
    }

    public WalletClient getInstance() {
      return instance;
    }

  }

  public static String getMainToSideContractMap(String address) throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "mainToSideContractMap(address)";
    List params = Arrays.asList(address);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static String getSunTokenAddress() throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "sunTokenAddress()";
    List<Object> params = new ArrayList<>();
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static String getMainToSideTRC10Map(String tokenId) throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "mainToSideTRC10Map(uint256)";
    List params = Arrays.asList(tokenId);
    byte[] ret = MainChainGatewayApi.GatewayApi.GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }


  public static byte[] checkTxInfo(TransactionId txId)
      throws ContractException, TxNotFoundException {
    return GATEWAY_API.instance.checkTxInfo(txId.getTransactionId());
  }

  public static boolean broadcast(Transaction transaction) {
    return GATEWAY_API.instance.broadcast(transaction);
  }
}
