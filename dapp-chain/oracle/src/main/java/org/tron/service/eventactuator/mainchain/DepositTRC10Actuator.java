package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRC10Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Actuator extends Actuator {

  private DepositTRC10Event event;
  @Getter
  private EventType type = EventType.DEPOSIT_TRC10_EVENT;

  public DepositTRC10Actuator(String from, String value, String trc10, String transactionId) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString trc10BS = ByteString.copyFrom(ByteArray.fromString(trc10));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.event = DepositTRC10Event.newBuilder().setFrom(fromBS).setValue(valueBS).setTrc10(trc10BS)
        .setTransactionId(transactionIdBS).build();
  }

  public DepositTRC10Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(DepositTRC10Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(this.transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String valueStr = event.getValue().toStringUtf8();
    String trc10Str = event.getTrc10().toStringUtf8();
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info("DepositTRC10Actuator, from: {}, value: {}, trc10: {}, transactionId: {}",
        fromStr, valueStr, trc10Str, transactionIdStr);

    // TODO: throw RpcConnectException
    AssetIssueContract assetIssue = MainChainGatewayApi
        .getAssetIssueById(trc10Str);

    logger.info(
        "DepositTRC10Actuator, assetIssue name: {}, assetIssue symbol: {}, assetIssue precision: {}",
        assetIssue.getName().toStringUtf8(), assetIssue.getName().toStringUtf8(),
        assetIssue.getPrecision());
    Transaction tx = SideChainGatewayApi
        .mintToken10Transaction(fromStr, trc10Str, valueStr, assetIssue.getName().toStringUtf8(),
            assetIssue.getName().toStringUtf8(), assetIssue.getPrecision(), transactionIdStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN,
        transactionIdStr, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType()).build();
  }

  @Override
  public byte[] getKey() {
    return event.getTransactionId().toByteArray();
  }

  @Override
  public byte[] getNonce() {
    return null;
  }

}
