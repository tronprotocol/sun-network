package org.tron.client;

import static org.tron.client.MainChainGatewayApi.GatewayApi.GATEWAY_API;

import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.config.Args;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxFailException;
import org.tron.common.exception.TxRollbackException;
import org.tron.common.exception.TxValidateException;
import org.tron.common.utils.AbiUtil;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j
public class MainChainGatewayApi {

  // Singleton
  enum GatewayApi {
    GATEWAY_API;
    private WalletClient instance;
    private WalletClient solidityInstance;

    GatewayApi() {
      instance = new WalletClient(Args.getInstance().getMainchainFullNode(),
          Args.getInstance().getOraclePrivateKey(), true);
      solidityInstance = new WalletClient(Args.getInstance().getMainchainSolidity(),
          Args.getInstance().getOraclePrivateKey(), true);
    }

    public WalletClient getInstance() {
      return instance;
    }

    public WalletClient getSolidityInstance() {
      return solidityInstance;
    }

  }

  public static Transaction addTokenMappingTransaction(String mainChainAddress,
      String sideChainAddress, String dataHash, String txId) throws RpcConnectException {
    List<String> oracleSigns = SideChainGatewayApi.getMappingOracleSigns(txId, dataHash);
    sleeping(dataHash, oracleSigns);
    boolean done = getMappingStatus(mainChainAddress);
    if (done) {
      return null;
    } else {
      byte[] contractAddress = Args.getInstance().getMainchainGateway();
      String method = "migrationToken(address,address,bytes32,bytes[])";
      List params = Arrays.asList(mainChainAddress, sideChainAddress, txId, oracleSigns);
      return GATEWAY_API.getInstance()
          .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
    }
  }

  public static boolean getMappingStatus(String mainChainAddress) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "allows(address)";
    List params = Arrays.asList(mainChainAddress);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackStatus(ret);
  }

  public static boolean getWithdrawStatus(String txId) throws RpcConnectException {
    byte[] contractAddress = Args.getInstance().getMainchainGateway();
    String method = "withdrawDone(bytes32)";
    List params = Arrays.asList(txId);
    byte[] ret = GATEWAY_API.getInstance()
        .triggerConstantContractAndReturn(contractAddress, method, params, 0, 0, 0);
    return AbiUtil.unpackStatus(ret);
  }

  public static void sleeping(String dataHash, List<String> oracleSigns) {
    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hex.decode(dataHash)));
    int sleepCnt = 0;

    for (String signs : oracleSigns) {
      if (signs.equalsIgnoreCase(ownSign)) {
        break;
      }
      sleepCnt++;
    }

    try {
      Thread.sleep(sleepCnt * 60_000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static Transaction multiSignForWithdrawTRXTransaction(String from, String value,
      String userSign, String dataHash, String txId) throws RpcConnectException {
    List<String> oracleSigns = SideChainGatewayApi.getWithdrawOracleSigns(txId, dataHash);
    sleeping(dataHash, oracleSigns);
    boolean done = getWithdrawStatus(txId);
    if (done) {
      return null;
    } else {
      byte[] contractAddress = Args.getInstance().getMainchainGateway();
      String method = "withdrawTRX(address,uint256,bytes,bytes32,bytes[])";
      List params = Arrays.asList(from, value, userSign, txId, oracleSigns);
      return GATEWAY_API.getInstance()
          .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
    }
  }

  public static Transaction multiSignForWithdrawTRC10Transaction(String from, String trc10,
      String value, String userSign, String dataHash, String txId) throws RpcConnectException {
    List<String> oracleSigns = SideChainGatewayApi.getWithdrawOracleSigns(txId, dataHash);
    sleeping(dataHash, oracleSigns);
    boolean done = getWithdrawStatus(txId);
    if (done) {
      return null;
    } else {
      byte[] contractAddress = Args.getInstance().getMainchainGateway();
      String method = "withdrawTRC10(address,trcToken,uint256,bytes,bytes32,bytes[])";
      List params = Arrays.asList(from, trc10, value, userSign, txId, oracleSigns);
      return GATEWAY_API.getInstance()
          .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
    }
  }

  public static Transaction multiSignForWithdrawTokenTransaction(String from,
      String mainChainAddress, String valueOrTokenId, String type, String userSign, String dataHash,
      String txId) throws RpcConnectException {
    List<String> oracleSigns = SideChainGatewayApi.getWithdrawOracleSigns(txId, dataHash);
    sleeping(dataHash, oracleSigns);
    boolean done = getWithdrawStatus(txId);
    if (done) {
      return null;
    } else {
      String method;
      List params;
      if (type.equalsIgnoreCase("2")) {
        method = "withdrawTRC20(address,address,uint256,bytes,bytes32,bytes[])";
      } else {
        method = "withdrawTRC721(address,address,uint256,bytes,bytes32,bytes[])";
      }
      params = Arrays.asList(from, mainChainAddress, valueOrTokenId, userSign, txId, oracleSigns);
      byte[] contractAddress = Args.getInstance().getMainchainGateway();
      return GATEWAY_API.getInstance()
          .triggerContractTransaction(contractAddress, method, params, 0, 0, 0);
    }
  }

  public static AssetIssueContract getAssetIssueById(String assetId) {
    return GATEWAY_API.getInstance().getAssetIssueById(assetId);
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
