package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRC10Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.EventMsg.TaskEnum;
import org.tron.service.capsule.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC10Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Actuator extends DepositActuator {

  private static final String NONCE_TAG = "deposit_";
  private DepositTRC10Event event;
  @Getter
  private EventType type = EventType.DEPOSIT_TRC10_EVENT;
  @Getter
  private TaskEnum taskEnum = TaskEnum.SIDE_CHAIN;

  public DepositTRC10Actuator(String from, String tokenId, String value, String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString tokenIdBS = ByteString.copyFrom(ByteArray.fromString(tokenId));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = DepositTRC10Event.newBuilder().setFrom(fromBS).setValue(valueBS)
        .setTokenId(tokenIdBS)
        .setNonce(nonceBS).build();
  }

  public DepositTRC10Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(DepositTRC10Event.class);
  }

  @Override
  public CreateRet createTransactionExtensionCapsule() {

    if (Objects.nonNull(this.transactionExtensionCapsule)) {
      return CreateRet.SUCCESS;
    }
    try {
      String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
      String tokenIdStr = event.getTokenId().toStringUtf8();
      String valueStr = event.getValue().toStringUtf8();
      String nonceStr = event.getNonce().toStringUtf8();

      logger.info("DepositTRC10Actuator, from: {}, value: {}, tokenId: {}, nonce: {}",
          fromStr, valueStr, tokenIdStr, nonceStr);

      AssetIssueContract assetIssue = MainChainGatewayApi
          .getAssetIssueById(tokenIdStr);
      if (logger.isInfoEnabled()) {
        logger.info(
            "DepositTRC10Actuator, assetIssue name: {}, assetIssue symbol: {}, assetIssue precision: {}",
            assetIssue.getName().toStringUtf8(), assetIssue.getName().toStringUtf8(),
            assetIssue.getPrecision());
      }
      Transaction tx = SideChainGatewayApi
          .mintToken10Transaction(fromStr, tokenIdStr, valueStr,
              assetIssue.getName().toStringUtf8(),
              assetIssue.getName().toStringUtf8(), assetIssue.getPrecision(), nonceStr);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(NONCE_TAG + nonceStr, tx,
          0);
      return CreateRet.SUCCESS;
    } catch (Exception e) {
      logger.error("when create transaction extension capsule", e);
      return CreateRet.FAIL;
    }
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType())
        .setTaskEnum(getTaskEnum()).build();
  }

  @Override
  public Actuator getNextActuator() {
    return new MultiSignForWithdrawTRC10Actuator(Args.getInstance().getMainchainGatewayStr(),
        event.getTokenId().toStringUtf8(), event.getValue().toStringUtf8(),
        WalletUtil.bigIntegerStrAdd(SystemSetting.OPERATION_BASE_VALUE,  event.getNonce().toStringUtf8()));
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
