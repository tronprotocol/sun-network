package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRC721Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.EventMsg.TaskEnum;
import org.tron.service.capsule.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.sidechain.MultiSignForWithdrawTRC721Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC721Actuator extends DepositActuator {

  private static final String NONCE_TAG = "deposit_";

  private DepositTRC721Event event;
  @Getter
  private EventType type = EventType.DEPOSIT_TRC721_EVENT;
  @Getter
  private TaskEnum taskEnum = TaskEnum.SIDE_CHAIN;

  public DepositTRC721Actuator(String from, String contractAddress, String uid,
      String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString contractAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(contractAddress));
    ByteString uidBS = ByteString.copyFrom(ByteArray.fromString(uid));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = DepositTRC721Event.newBuilder().setFrom(fromBS).setUId(uidBS)
        .setContractAddress(contractAddressBS).setNonce(nonceBS).build();
  }

  public DepositTRC721Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(DepositTRC721Event.class);
  }

  @Override
  public CreateRet createTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return CreateRet.SUCCESS;
    }
    try {
      String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
      String contractAddressStr = WalletUtil
          .encode58Check(event.getContractAddress().toByteArray());
      String uIdStr = event.getUId().toStringUtf8();
      String nonceStr = event.getNonce().toStringUtf8();

      logger.info(
          "DepositTRC721Actuator, from: {}, tokenId: {}, contractAddress: {}, nonce: {}",
          fromStr, uIdStr, contractAddressStr, nonceStr);
      Transaction tx = SideChainGatewayApi
          .mintToken721Transaction(fromStr, contractAddressStr, uIdStr, nonceStr);
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
    return new MultiSignForWithdrawTRC721Actuator(Args.getInstance().getMainchainGatewayStr(),
        WalletUtil.encode58Check(event.getContractAddress().toByteArray()),
        event.getUId().toStringUtf8(),
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
