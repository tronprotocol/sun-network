package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRXEvent;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRXActuator extends Actuator {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  private static final String NONCE_TAG = "deposit_";

  private DepositTRXEvent event;
  @Getter
  private EventType type = EventType.DEPOSIT_TRX_EVENT;

  public DepositTRXActuator(String from, String value, String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = DepositTRXEvent.newBuilder().setFrom(fromBS).setValue(valueBS)
        .setNonce(nonceBS).build();
  }

  public DepositTRXActuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(DepositTRXEvent.class);
  }

  @Override
  public TransactionExtensionCapsule getTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }
    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String valueStr = event.getValue().toStringUtf8();
    String nonceStr = event.getNonce().toStringUtf8();

    loggerOracle.info("DepositTRXActuator, from: {}, value: {}, nonce: {}", fromStr, valueStr,
        nonceStr);

    Transaction tx = SideChainGatewayApi.mintTrxTransaction(fromStr, valueStr, nonceStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN,
        NONCE_TAG + nonceStr, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType()).build();
  }

  @Override
  public byte[] getNonceKey() {
    return ByteArray.fromString(NONCE_TAG + event.getNonce().toStringUtf8());
  }

  @Override
  public byte[] getNonce() {
    return event.getNonce().toByteArray();
  }

}
