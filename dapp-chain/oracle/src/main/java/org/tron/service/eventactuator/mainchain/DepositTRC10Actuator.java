package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.Objects;
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

  DepositTRC10Event event;

  public DepositTRC10Actuator(String from, String value, String trc10, String transactionId) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString trc10BS = ByteString.copyFrom(ByteArray.fromString(trc10));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.type = EventType.DEPOSIT_TRC10_EVENT;
    this.event = DepositTRC10Event.newBuilder().setFrom(fromBS).setValue(valueBS)
        .setTrc10(trc10BS)
        .setTransactionId(transactionIdBS).setWillTaskEnum(TaskEnum.SIDE_CHAIN).build();
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
        .getAssetIssueById(event.getTransactionId().toStringUtf8());
    Transaction tx = SideChainGatewayApi
        .mintToken10Transaction(fromStr, trc10Str, valueStr, assetIssue.getName().toStringUtf8(),
            assetIssue.getName().toStringUtf8(), assetIssue.getPrecision(), transactionIdStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
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
