package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRC721Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC721Actuator extends Actuator {

  private String from;
  private String uid;
  private String contractAddress;

  public DepositTRC721Actuator(String from, String uid, String contractAddress, String txId) {
    this.from = from;
    this.uid = uid;
    this.contractAddress = contractAddress;
    this.txId = txId;
  }

  public DepositTRC721Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.DEPOSIT_TRC721_EVENT;
    this.event = eventMsg.getParameter().unpack(DepositTRC721Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }

    logger
        .info("DepositTRC721Actuator, from: {}, uid: {}, contractAddress: {}, txId: {}", this.from,
            this.uid, this.contractAddress, this.txId);
    Transaction tx = SideChainGatewayApi
        .mintToken721Transaction(this.from, this.contractAddress, this.uid, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }

}
