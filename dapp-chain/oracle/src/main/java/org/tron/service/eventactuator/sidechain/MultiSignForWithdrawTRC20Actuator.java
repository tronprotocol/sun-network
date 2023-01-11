package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.SignUtils;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.EventMsg.TaskEnum;
import org.tron.protos.Sidechain.MultiSignForWithdrawTRC20Event;
import org.tron.service.capsule.TransactionExtensionCapsule;
import org.tron.service.eventactuator.SignListParam;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTRC20Actuator extends MultiSignForWithdrawActuator {

  private static final String PREFIX = "withdraw_2_";
  private MultiSignForWithdrawTRC20Event event;
  @Getter
  private EventType type = EventType.MULTISIGN_FOR_WITHDRAW_TRC20_EVENT;
  @Getter
  private TaskEnum taskEnum = TaskEnum.MAIN_CHAIN;

  public MultiSignForWithdrawTRC20Actuator(String from, String mainChainAddress, String value,
      String nonce) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(value));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = MultiSignForWithdrawTRC20Event.newBuilder().setFrom(fromBS)
        .setMainchainAddress(mainChainAddressBS).setValue(valueBS).setNonce(nonceBS).build();
  }

  public MultiSignForWithdrawTRC20Actuator(EventMsg eventMsg)
      throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MultiSignForWithdrawTRC20Event.class);
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
      String valueStr = event.getValue().toStringUtf8();
      String nonceStr = event.getNonce().toStringUtf8();
      logger.info(
          "MultiSignForWithdrawTRC20Actuator, from: {}, mainChainAddress: {}, value: {}, nonce: {}",
          fromStr, mainChainAddressStr, valueStr, nonceStr);
      SignListParam signParam;
      long delay = 0;
      if (new BigInteger(nonceStr, 10).compareTo(new BigInteger(SystemSetting.OPERATION_BASE_VALUE))
          < 0) {
        signParam = SideChainGatewayApi.getWithdrawOracleSigns(nonceStr);
        delay = getDelay(fromStr, mainChainAddressStr, valueStr, nonceStr,
            signParam.getOracleSigns());
      } else {
        signParam = SideChainGatewayApi
            .getWithdrawTRCTokenSignParam(fromStr, mainChainAddressStr, valueStr, nonceStr);
      }
      Transaction tx = MainChainGatewayApi
          .multiSignForWithdrawTRC20Transaction(fromStr, mainChainAddressStr, valueStr, nonceStr,
              signParam);

      this.transactionExtensionCapsule = new TransactionExtensionCapsule(PREFIX + nonceStr, tx,
          delay);
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

  @Override
  public String getWithdrawDataHash() {
    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String mainChainAddressStr = WalletUtil
        .encode58Check(event.getMainchainAddress().toByteArray());
    String valueStr = event.getValue().toStringUtf8();
    String nonceStr = event.getNonce().toStringUtf8();
    return ByteArray
        .toHexString(SideChainGatewayApi
            .getWithdrawTRCTokenDataHash(fromStr, mainChainAddressStr, valueStr, nonceStr));
  }
}