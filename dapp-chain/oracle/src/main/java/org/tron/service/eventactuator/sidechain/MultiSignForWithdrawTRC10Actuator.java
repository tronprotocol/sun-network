package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.DataWord;
import org.tron.common.utils.SignUtils;
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

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

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
  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    try {
      String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
      String tokenIdStr = event.getTokenId().toStringUtf8();
      String valueStr = event.getValue().toStringUtf8();
      String nonceStr = event.getNonce().toStringUtf8();
      List<String> oracleSigns = SideChainGatewayApi.getWithdrawOracleSigns(nonceStr);

      loggerOracle
          .info("MultiSignForWithdrawTRC10Actuator, from: {}, tokenId: {}, value: {}, nonce: {}",
              fromStr, tokenIdStr, valueStr, nonceStr);
      Transaction tx = MainChainGatewayApi
          .multiSignForWithdrawTRC10Transaction(fromStr, tokenIdStr, valueStr, nonceStr,
              oracleSigns);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN,
          PREFIX + nonceStr, tx, getDelay(fromStr,
          tokenIdStr, valueStr, nonceStr, oracleSigns));
      return this.transactionExtensionCapsule;
    } catch (Exception e) {
      // FIXME: exception level is right ?
      logger.error("when create transaction extension capsule", e);
      return null;
    }
  }

  private long getDelay(String from, String tokenId, String value, String nonce,
      List<String> oracleSigns) {

    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
    byte[] tokenIdBytes = new DataWord((new BigInteger(tokenId, 10)).toByteArray()).getData();
    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
    byte[] nonceBytes = new DataWord((new BigInteger(nonce, 10)).toByteArray()).getData();
    byte[] data = ByteUtil
        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), tokenIdBytes, valueBytes,
            nonceBytes);
    String ownSign = MainChainGatewayApi.sign(data);
    return SignUtils.getDelay(ownSign, oracleSigns);
  }

  @Override
  public boolean broadcastTransactionExtensionCapsule() {

    String nonceStr = event.getNonce().toStringUtf8();
    try {
      boolean done = MainChainGatewayApi.getWithdrawStatus(nonceStr);
      if (!done) {
        super.broadcastTransactionExtensionCapsule();
      }
    } catch (Exception e) {
      // FIXME: exception level is right ?
      logger.error("when broadcast transaction extension capsule", e);
    }

//    catch (RpcConnectException e) {
//      AlertUtil.sendAlert(
//          String.format("tx: %s, rpc connect fail", txExtensionCapsule.getTransactionId()));
//      logger.error(e.getMessage(), e);
//    } catch (TxValidateException e) {
//      AlertUtil.sendAlert(String.format("tx: %s, validation fail, will not exist on chain",
//          txExtensionCapsule.getTransactionId()));
//      logger.error(e.getMessage(), e);
//    } catch (TxExpiredException e) {
//      AlertUtil.sendAlert(String.format("tx: %s, expired", txExtensionCapsule.getTransactionId()));
//      logger.error(e.getMessage(), e);
//    }

//    byte[] fromBytes = WalletUtil.decodeFromBase58Check(from);
//    byte[] tokenIdBytes = new DataWord((new BigInteger(tokenId, 10)).toByteArray()).getData();
//    byte[] valueBytes = new DataWord((new BigInteger(value, 10)).toByteArray()).getData();
//    byte[] nonceBytes = new DataWord((new BigInteger(nonce, 10)).toByteArray()).getData();
//    byte[] data = ByteUtil
//        .merge(Arrays.copyOfRange(fromBytes, 1, fromBytes.length), tokenIdBytes, valueBytes,
//            nonceBytes);
//    String ownSign = Hex.toHexString(GATEWAY_API.getInstance().signDigest(Hash.sha3(data)));
//
//    sleeping(ownSign, oracleSigns);
//    boolean done = getWithdrawStatus(nonce);
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