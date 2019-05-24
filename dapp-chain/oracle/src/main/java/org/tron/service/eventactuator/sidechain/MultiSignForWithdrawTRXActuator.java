package org.tron.service.eventactuator.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTRXActuator extends Actuator {

  // "event MultiSignForWithdrawTRX(address from, uint256 value, bytes32 userSign, bytes32 dataHash, bytes32 txId);"

  private String from;
  private String value;
  private String userSign;
  private String dataHash;
  private String txId;

  public MultiSignForWithdrawTRXActuator(String from, String value, String userSign,
      String dataHash,
      String txId) {
    this.from = from;
    this.value = value;
    this.userSign = userSign;
    this.dataHash = dataHash;
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger.info(
        "MultiSignForWithdrawTRXActuator, from: {}, value: {}, userSign: {}, dataHash: {}, txId: {}",
        this.from, this.value, this.userSign, this.dataHash, this.txId);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTRXTransaction(this.from, this.value, this.userSign, this.dataHash,
            this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
