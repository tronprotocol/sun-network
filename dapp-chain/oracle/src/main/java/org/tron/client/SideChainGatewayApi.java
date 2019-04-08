package org.tron.client;

import static org.tron.client.SideChainGatewayApi.GatewayApi.GATEWAY_API;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.Args;
import org.tron.common.exception.ContractException;
import org.tron.common.exception.RpcException;
import org.tron.common.exception.TxNotFoundException;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.WalletUtil;

@Slf4j
public class SideChainGatewayApi {

  public static String mintTrx(String to, long value) throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "mint(address,uint256)";
    List params = Arrays.asList(to, value);
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

  public static String deployDAppTRC20AndMapping(String txId, String name, String symbol,
      int decimals)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "deployDAppTRC20AndMapping(bytes,string,string,uint8)";
    List params = Arrays.asList(txId, name, symbol, decimals);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static String getMainToSideContractMap(String address) throws RpcException {
    byte[] contractAddress = Args.getInstance().getSidechainGateway();
    String method = "mainToSideContractMap(address)";
    List params = Arrays.asList(address);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static String getMainToSideTRC10Map(long tokenId) throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "mainToSideTRC10Map(uint256)";
    List params = Arrays.asList(tokenId);
    byte[] ret = MainChainGatewayApi.GatewayApi.GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public static String mintToken(String contractAddress, String to, long value)
      throws RpcException {
    byte[] contract = WalletUtil.decodeFromBase58Check(contractAddress);
    String method = "mint(address,uint256)";
    List params = Arrays.asList(to, value);
    return GATEWAY_API.getInstance().triggerContract(contract, method, params, 0, 0, 0);
  }


  public static byte[] getTxInfo(String trxId) throws ContractException, TxNotFoundException {
    return GATEWAY_API.instance.getTxInfo(trxId);
  }

  public static void checkTxInfo(String trxId) {

  }

  public static void main(String[] args) {
  }
}
