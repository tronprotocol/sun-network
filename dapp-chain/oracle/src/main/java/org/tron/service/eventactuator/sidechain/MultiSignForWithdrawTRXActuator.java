package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MultiSignForWithdrawTRXEvent;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTRXActuator extends Actuator {

  // "event MultiSignForWithdrawTRX(address from, uint256 value, bytes32 userSign, bytes32 dataHash, bytes32 txId);"

  private MultiSignForWithdrawTRXEvent event;
  @Getter
  private EventType type = EventType.MULTISIGN_FOR_WITHDRAW_TRX_EVENT;


  public MultiSignForWithdrawTRXActuator(String from, String value, String userSign,
      String dataHash, String transactionId, String originalTransactionId) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString userSignBS = ByteString.copyFrom(ByteArray.fromHexString(userSign));
    ByteString dataHashBS = ByteString.copyFrom(ByteArray.fromHexString(dataHash));
    ByteString originalTransactionIdBS = ByteString
        .copyFrom(ByteArray.fromHexString(originalTransactionId));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.event = MultiSignForWithdrawTRXEvent.newBuilder().setFrom(fromBS).setValue(valueBS)
        .setUserSign(userSignBS).setDataHash(dataHashBS).setTransactionId(transactionIdBS).build();
  }

  public MultiSignForWithdrawTRXActuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MultiSignForWithdrawTRXEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String valueStr = event.getValue().toStringUtf8();
    String userSignStr = ByteArray.toHexString(event.getUserSign().toByteArray());
    String dataHashStr = ByteArray.toHexString(event.getDataHash().toByteArray());
    String originalTransactionIdStr = ByteArray
        .toHexString(event.getOriginalTransactionId().toByteArray());
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info(
        "MultiSignForWithdrawTRXActuator, from: {}, value: {}, userSign: {}, dataHash: {}, originalTransactionId: {}, transactionId: {}",
        fromStr, valueStr, userSignStr, dataHashStr, originalTransactionIdStr, transactionIdStr);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTRXTransaction(fromStr, valueStr, userSignStr, dataHashStr,
            originalTransactionIdStr);
    if (tx == null) {
      return null;
    }
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN,
        transactionIdStr, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(this.type).build();
  }

  @Override
  public byte[] getKey() {
    return event.getTransactionId().toByteArray();
  }
}
