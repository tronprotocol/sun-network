package org.tron.service.eventactuator.mainchain;

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
import org.tron.protos.Sidechain.DepositTRC20Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC20Actuator extends Actuator {

  private static final String NONCE_TAG = "deposit_";

  private DepositTRC20Event event;
  @Getter
  private EventType type = EventType.DEPOSIT_TRC20_EVENT;

  public DepositTRC20Actuator(String from, String contractAddress, String value,
      String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString contractAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(contractAddress));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = DepositTRC20Event.newBuilder().setFrom(fromBS).setValue(valueBS)
        .setContractAddress(contractAddressBS).setNonce(nonceBS).build();
  }

  public DepositTRC20Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(DepositTRC20Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String valueStr = event.getValue().toStringUtf8();
    String contractAddressStr = WalletUtil.encode58Check(event.getContractAddress().toByteArray());
    String nonceStr = event.getNonce().toStringUtf8();

    logger.info("DepositTRC20Actuator, from: {}, value: {}, contractAddress: {}, nonce: {}",
        fromStr, valueStr, contractAddressStr, nonceStr);

    Transaction tx = SideChainGatewayApi
        .mintToken20Transaction(fromStr, contractAddressStr, valueStr, nonceStr);
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