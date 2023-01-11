package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.EventMsg.TaskEnum;
import org.tron.protos.Sidechain.WithdrawTRC721Event;
import org.tron.service.capsule.TransactionExtensionCapsule;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC721Actuator extends WithdrawActuator {

  private static final String PREFIX = "withdraw_1_";
  private WithdrawTRC721Event event;
  @Getter
  private EventType type = EventType.WITHDRAW_TRC721_EVENT;
  @Getter
  private TaskEnum taskEnum = TaskEnum.SIDE_CHAIN;

  public WithdrawTRC721Actuator(String from, String mainChainAddress, String uId, String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString uIdBS = ByteString.copyFrom(ByteArray.fromString(uId));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = WithdrawTRC721Event.newBuilder().setFrom(fromBS)
        .setMainchainAddress(mainChainAddressBS).setUId(uIdBS).setNonce(nonceBS).build();
  }

  public WithdrawTRC721Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(WithdrawTRC721Event.class);
  }

  @Override
  public CreateRet createTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return CreateRet.SUCCESS;
    }
    try {
      String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
      String mainChainAddressStr = WalletUtil
          .encode58Check(event.getMainchainAddress().toByteArray());
      String uIdStr = event.getUId().toStringUtf8();
      String nonceStr = event.getNonce().toStringUtf8();

      logger
          .info("WithdrawTRC721Actuator, from: {}, mainChainAddress: {}, uId: {}, nonce: {}",
              fromStr,
              mainChainAddressStr, uIdStr, nonceStr);
      Transaction tx = SideChainGatewayApi
          .withdrawTRC721Transaction(fromStr, mainChainAddressStr, uIdStr, nonceStr);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(PREFIX + nonceStr, tx, 0);
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
  public byte[] getNonceKey() {
    return ByteArray.fromString(PREFIX + event.getNonce().toStringUtf8());
  }

  @Override
  public byte[] getNonce() {
    return event.getNonce().toByteArray();
  }

}