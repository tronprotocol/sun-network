package org.tron.service.eventactuator.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRXActuator extends Actuator {

  // "event WithdrawTRX(address from, uint256 value, bytes memory txData);"

  private String from;
  private String value;
  private String userSign;

  public WithdrawTRXActuator(String from, String value, String userSign, String txId) {
    this.from = from;
    this.value = value;
    this.userSign = userSign;
    this.txId = txId;
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger.info("WithdrawTRXActuator, from: {}, value: {}, userSign: {}, txId: {}", this.from,
        this.value,
        this.userSign, this.txId);
    Transaction tx = SideChainGatewayApi
        .withdrawTRXTransaction(this.from, this.value, this.userSign, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
