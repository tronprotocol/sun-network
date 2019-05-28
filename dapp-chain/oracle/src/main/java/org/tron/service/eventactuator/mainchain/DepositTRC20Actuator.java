package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRC20Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC20Actuator extends Actuator {

  private String from;
  private String amount;
  private String contractAddress;

  public DepositTRC20Actuator(String from, String amount, String contractAddress, String txId) {
    this.from = from;
    this.amount = amount;
    this.contractAddress = contractAddress;
    this.txId = txId;
  }

  public DepositTRC20Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.DEPOSIT_TRC20_EVENT;
    this.event = eventMsg.getParameter().unpack(DepositTRC20Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    logger.info("DepositTRC20Actuator, from: {}, amount: {}, contractAddress: {}, txId: {}",
        this.from, this.amount, this.contractAddress, this.txId);

    Transaction tx = SideChainGatewayApi
        .mintToken20Transaction(this.from, this.contractAddress, this.amount, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }

}
