package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.protos.Sidechain.WithdrawTRC10Event;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC10Actuator extends Actuator {

  // "event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes memory userSign);"

  private WithdrawTRC10Event event;
  @Getter
  private EventType type = EventType.WITHDRAW_TRC10_EVENT;

  public WithdrawTRC10Actuator(String nonce, String from, String value, String trc10,
      String userSign) {
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString trc10BS = ByteString.copyFrom(ByteArray.fromString(trc10));
    ByteString userSignBS = ByteString.copyFrom(ByteArray.fromHexString(userSign));
    this.event = WithdrawTRC10Event.newBuilder().setNonce(nonceBS).setFrom(fromBS).setValue(valueBS)
        .setTrc10(trc10BS)
        .setUserSign(userSignBS).build();
  }

  public WithdrawTRC10Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(WithdrawTRC10Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    String nonceStr = event.getNonce().toStringUtf8();
    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String valueStr = event.getValue().toStringUtf8();
    String trc10Str = event.getTrc10().toStringUtf8();
    String userSignStr = ByteArray.toHexString(event.getUserSign().toByteArray());

    logger.info("WithdrawTRC10Actuator, nonce: {}, from: {}, value: {}, trc10: {}, userSign: {}",
        nonceStr, fromStr, valueStr, trc10Str, userSignStr);

    Transaction tx = SideChainGatewayApi
        .withdrawTRC10Transaction(fromStr, trc10Str, valueStr, userSignStr, nonceStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN,
        nonceStr, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType()).build();
  }

  @Override
  public byte[] getKey() {
    return event.getNonce().toByteArray();
  }

  @Override
  public byte[] getNonce() {
    return event.getNonce().toByteArray();
  }

}

