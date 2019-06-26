package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.SignUtils;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MultiSignForWithdrawTRC721Event;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTRC721Actuator extends Actuator {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  private static final String PREFIX = "withdraw_2_";
  private MultiSignForWithdrawTRC721Event event;
  @Getter
  private EventType type = EventType.MULTISIGN_FOR_WITHDRAW_TRC721_EVENT;

  public MultiSignForWithdrawTRC721Actuator(String from, String mainChainAddress, String uId,
      String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString uIdBS = ByteString.copyFrom(ByteArray.fromString(uId));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = MultiSignForWithdrawTRC721Event.newBuilder().setFrom(fromBS)
        .setMainchainAddress(mainChainAddressBS).setUId(uIdBS).setNonce(nonceBS).build();
  }

  public MultiSignForWithdrawTRC721Actuator(EventMsg eventMsg)
      throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MultiSignForWithdrawTRC721Event.class);
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
      List<String> oracleSigns = SideChainGatewayApi.getWithdrawOracleSigns(nonceStr);

      loggerOracle.info(
          "MultiSignForWithdrawTRC721Actuator, from: {}, mainChainAddress: {}, uId: {}, nonce: {}",
          fromStr, mainChainAddressStr, uIdStr, nonceStr);
      Transaction tx = MainChainGatewayApi
          .multiSignForWithdrawTRC721Transaction(fromStr, mainChainAddressStr, uIdStr, nonceStr,
              oracleSigns);
      if (tx == null) {
        return null;
      }
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN,
          PREFIX + nonceStr, tx,
          getDelay(fromStr, mainChainAddressStr, uIdStr, nonceStr, oracleSigns));
      return CreateRet.SUCCESS;
    } catch (Exception e) {
      logger.error("when create transaction extension capsule", e);
      return CreateRet.FAIL;
    }
  }

  private long getDelay(String fromStr, String mainChainAddressStr, String valueStr,
      String nonceStr, List<String> oracleSigns) {
    String ownSign = SideChainGatewayApi
        .getWithdrawTRCTokenSign(fromStr, mainChainAddressStr, valueStr, nonceStr);
    return SignUtils.getDelay(ownSign, oracleSigns);
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