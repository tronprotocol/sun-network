package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRXEvent;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRXActuator extends Actuator {

  private String from;
  private String amount;

  public DepositTRXActuator(String from, String amount, String txId) {
    this.from = from;
    this.amount = amount;
    this.txId = txId;
  }

  public DepositTRXActuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.DEPOSIT_TRX_EVENT;
    this.event = eventMsg.getParameter().unpack(DepositTRXEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }
    logger.info("DepositTRXActuator, from: {}, amount: {}, txId: {}", this.from, this.amount,
        this.txId);
    Transaction tx = SideChainGatewayApi.mintTrxTransaction(this.from, this.amount, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }

}
