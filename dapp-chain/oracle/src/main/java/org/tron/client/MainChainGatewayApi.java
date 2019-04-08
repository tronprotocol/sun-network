package org.tron.client;

import static org.tron.client.MainChainGatewayApi.GatewayApi.GATEWAY_API;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.Args;
import org.tron.common.exception.ContractException;
import org.tron.common.exception.RpcException;
import org.tron.common.exception.TxNotFoundException;
import org.tron.common.utils.AbiUtil;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.Protocol.TransactionInfo.code;

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

  public static String deployDAppTRC20AndMapping(String txId, String name, String symbol,
    int decimals)
    throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "deployDAppTRC20AndMapping(bytes,string,string,uint8)";
    List params = Arrays.asList(txId, name, symbol, decimals);
    return GATEWAY_API.getInstance().triggerContract(contractAddress, method, params, 0, 0, 0);
  }

  public String getMainToSideContractMap(String address) throws RpcException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "mainToSideContractMap(address)";
    List params = Arrays.asList(address);
    byte[] ret = GATEWAY_API.getInstance()
      .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackAddress(ret);
  }

  public byte[] getTxInfo(String txId) throws ContractException, TxNotFoundException {
    int maxRetry = 3;
    for (int i = 0; i < maxRetry; i++) {
      Optional<TransactionInfo> transactionInfo = GATEWAY_API.getInstance()
        .getTransactionInfoById(txId);
      TransactionInfo info = transactionInfo.get();
      if (info.getBlockTimeStamp() == 0L) {
        logger.info("will retry {} time(s)", i + 1);
        try {
          Thread.sleep(3_000);
        } catch (InterruptedException e) {
          logger.error(e.getMessage(), e);
        }
      } else {
        if (info.getResult().equals(code.SUCESS)) {
          return info.getContractResult(0).toByteArray();
        } else {
          throw new ContractException(info.getResMessage().toStringUtf8());
        }
      }
    }
    throw new TxNotFoundException(txId);
  }

  public static void main(String[] args) {
  }
}
