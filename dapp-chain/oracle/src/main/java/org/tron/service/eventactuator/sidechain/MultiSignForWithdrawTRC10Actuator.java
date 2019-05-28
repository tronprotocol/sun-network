package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MultiSignForWithdrawTRC10Event;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTRC10Actuator extends Actuator {

  // "event MultiSignForWithdrawTRC10(address from, uint256 trc10, uint256 value, bytes32 userSign, bytes32 dataHash, bytes32 txId);"

  private String from;
  private String trc10;
  private String value;
  private String userSign;
  private String dataHash;

  public MultiSignForWithdrawTRC10Actuator(String from, String trc10, String value, String userSign,
      String dataHash, String txId) {
    this.from = from;
    this.trc10 = trc10;
    this.value = value;
    this.userSign = userSign;
    this.dataHash = dataHash;
    this.txId = txId;
  }

  public MultiSignForWithdrawTRC10Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.MULTISIGN_FOR_WITHDRAW_TRC10_EVENT;
    this.event = eventMsg.getParameter().unpack(MultiSignForWithdrawTRC10Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger.info(
        "MultiSignForWithdrawTRC10Actuator, from: {}, trc10: {}, value: {}, userSign: {}, dataHash: {}, txId: {}",
        this.from, this.trc10, this.value, this.userSign, this.dataHash, this.txId);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTRC10Transaction(this.from, this.trc10, this.value, this.userSign,
            this.dataHash, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
