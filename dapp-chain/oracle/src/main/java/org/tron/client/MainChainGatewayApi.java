package org.tron.client;

import static org.tron.client.MainChainGatewayApi.GatewayApi.GATEWAY_API;

import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.Args;
import org.tron.common.exception.ContractException;
import org.tron.common.exception.TxNotFoundException;
import org.tron.protos.Contract.AssetIssueContract;

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

  public static AssetIssueContract getAssetIssueById(String assetId) {
    AssetIssueContract assetIssueContract = GATEWAY_API.getInstance()
        .getAssetIssueById(assetId);
    return assetIssueContract;
  }

  public static byte[] getTxInfo(String txId) throws ContractException, TxNotFoundException {
    return GATEWAY_API.getInstance().getTxInfo(txId);
  }

  public static void main(String[] args) {
  }
}
