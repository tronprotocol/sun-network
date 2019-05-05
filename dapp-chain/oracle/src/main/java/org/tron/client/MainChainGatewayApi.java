package org.tron.client;

import static org.tron.client.MainChainGatewayApi.GatewayApi.GATEWAY_API;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.Args;
import org.tron.common.exception.ContractException;
import org.tron.common.exception.RpcException;
import org.tron.common.exception.TxNotFoundException;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.TransactionId;
import org.tron.service.task.TaskEnum;

@Slf4j
public class MainChainGatewayApi {

  // Singleton
  enum GatewayApi {
    GATEWAY_API;
    private WalletClient instance;

    GatewayApi() {
      instance = new WalletClient(Args.getInstance().getMainchainFullNode(),
          Args.getInstance().getOraclePrivateKey());
    }

    public WalletClient getInstance() {
      return instance;
    }
  }

  public static TransactionId addTokenMapping(String mainChainAddress, String sideChainAddress)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "migrationToken(address,address)";
    List params = Arrays.asList(mainChainAddress, sideChainAddress);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId withdrawTRC10(String to, String trc10, String value, String txData)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "withdrawTRC10(address,trcToken,uint256,bytes)";
    List params = Arrays.asList(to, trc10, value, txData);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId withdrawTRC20(String to, String mainChainAddress, String value,
      String txData)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "withdrawTRC20(address,address,uint256,bytes)";
    List params = Arrays.asList(to, mainChainAddress, value, txData);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId withdrawTRC721(String to, String mainChainAddress, String value,
      String txData)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "withdrawTRC721(address,address,uint256,bytes)";
    List params = Arrays.asList(to, mainChainAddress, value, txData);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static TransactionId withdrawTRX(String to, String value, String txData)
      throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "withdrawTRX(address,uint256,bytes)";
    List params = Arrays.asList(to, value, txData);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public static AssetIssueContract getAssetIssueById(String assetId) {
    AssetIssueContract assetIssueContract = GATEWAY_API.getInstance()
        .getAssetIssueById(assetId);
    return assetIssueContract;
  }

  public static byte[] checkTxInfo(TransactionId txId)
      throws ContractException, TxNotFoundException {
    txId.setType(TaskEnum.SIDE_CHAIN);
    return GATEWAY_API.getInstance().checkTxInfo(txId.getTransactionId());
  }

  public static boolean broadcast(Transaction transaction) {
    return GATEWAY_API.instance.broadcast(transaction);
  }
}
