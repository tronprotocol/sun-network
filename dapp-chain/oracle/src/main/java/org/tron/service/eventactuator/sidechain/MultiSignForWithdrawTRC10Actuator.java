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
import org.tron.protos.Sidechain.MultiSignForWithdrawTRC10Event;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTRC10Actuator extends Actuator {

  // "event MultiSignForWithdrawTRC10(address from, trcToken tokenId, uint256 value, uint256 nonce);"

  private static final String PREFIX = "withdraw_2_";
  private MultiSignForWithdrawTRC10Event event;
  @Getter
  private EventType type = EventType.MULTISIGN_FOR_WITHDRAW_TRC10_EVENT;

  public MultiSignForWithdrawTRC10Actuator(String from, String tokenId, String value,
      String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString tokenIdBS = ByteString.copyFrom(ByteArray.fromString(tokenId));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = MultiSignForWithdrawTRC10Event.newBuilder().setFrom(fromBS).setTokenId(tokenIdBS)
        .setValue(valueBS).setNonce(nonceBS).build();
  }

  public MultiSignForWithdrawTRC10Actuator(EventMsg eventMsg)
      throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MultiSignForWithdrawTRC10Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String tokenIdStr = event.getTokenId().toStringUtf8();
    String valueStr = event.getValue().toStringUtf8();
    String nonceStr = event.getNonce().toStringUtf8();

    logger.info("MultiSignForWithdrawTRC10Actuator, from: {}, tokenId: {}, value: {}, nonce: {}",
        fromStr, tokenIdStr, valueStr, nonceStr);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTRC10Transaction(fromStr, tokenIdStr, valueStr, nonceStr);
    if (tx == null) {
      return null;
    }
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN,
        PREFIX + nonceStr, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType()).build();
  }

  @Override
  public byte[] getNonceKey() {
    return ByteArray.fromString(PREFIX + event.getNonce().toStringUtf8());
  }

  @Override
  public byte[] getNonce() {
    return event.getNonce().toByteArray();
  }

}