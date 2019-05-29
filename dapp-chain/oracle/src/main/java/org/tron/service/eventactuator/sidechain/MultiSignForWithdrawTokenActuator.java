package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MultiSignForWithdrawTokenEvent;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTokenActuator extends Actuator {

  // "event MultiSignForWithdrawToken(address from, address mainChainAddress, uint256 valueOrTokenId, uint256 _type, bytes32 userSign, bytes32 dataHash, bytes32 txId);"
  MultiSignForWithdrawTokenEvent event;
  @Getter
  EventType type = EventType.MULTISIGN_FOR_WITHDRAW_TOKEN_EVENT;

  public MultiSignForWithdrawTokenActuator(String from, String mainChainAddress,
      String valueOrTokenId, String type, String userSign, String dataHash, String transactionId) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString valueOrTokenIdBS = ByteString.copyFrom(ByteArray.fromString(valueOrTokenId));
    ByteString valueBS = ByteString.copyFrom(ByteArray.fromString(type));
    ByteString userSignBS = ByteString.copyFrom(ByteArray.fromHexString(userSign));
    ByteString dataHashBS = ByteString.copyFrom(ByteArray.fromHexString(dataHash));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.event = MultiSignForWithdrawTokenEvent.newBuilder().setFrom(fromBS)
        .setValueOrTokenId(valueOrTokenIdBS).setType(valueBS)
        .setMainchainAddress(mainChainAddressBS).setUserSign(userSignBS).setDataHash(dataHashBS)
        .setTransactionId(transactionIdBS).setWillTaskEnum(TaskEnum.MAIN_CHAIN).build();
  }

  public MultiSignForWithdrawTokenActuator(EventMsg eventMsg)
      throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MultiSignForWithdrawTokenEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String valueOrTokenIdStr = event.getValueOrTokenId().toStringUtf8();
    String typeStr = event.getType().toStringUtf8();
    String mainChainAddressStr = WalletUtil
        .encode58Check(event.getMainchainAddress().toByteArray());
    String userSignStr = ByteArray.toHexString(event.getUserSign().toByteArray());
    String dataHashStr = ByteArray.toHexString(event.getDataHash().toByteArray());
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info(
        "MultiSignForWithdrawTokenActuator, from: {}, mainChainAddress: {}, valueOrTokenId: {}, "
            + "_type: {}, userSign: {}, dataHash: {}, transactionId: {}", fromStr,
        mainChainAddressStr, valueOrTokenIdStr, typeStr, userSignStr, dataHashStr,
        transactionIdStr);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTokenTransaction(fromStr, mainChainAddressStr, valueOrTokenIdStr,
            typeStr,
            userSignStr, dataHashStr, transactionIdStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);

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
}