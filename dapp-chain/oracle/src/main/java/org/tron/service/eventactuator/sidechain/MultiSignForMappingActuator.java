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
import org.tron.protos.Sidechain.MultiSignForMappingEvent;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForMappingActuator extends Actuator {

  // "event MultiSignForDeployAndMapping(address mainChainAddress, address sideChainAddress, bytes32 dataHash, bytes32 txId);"

  private MultiSignForMappingEvent event;
  @Getter
  private EventType type = EventType.MULTISIGN_FOR_MAPPING_EVENT;

  public MultiSignForMappingActuator(String mainChainAddress, String sideChainAddress,
      String dataHash, String originalTransactionId, String transactionId) {
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString sideChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(sideChainAddress));
    ByteString dataHashBS = ByteString
        .copyFrom(ByteArray.fromHexString(dataHash));
    ByteString originalTransactionIdBS = ByteString
        .copyFrom(ByteArray.fromHexString(originalTransactionId));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.event = MultiSignForMappingEvent.newBuilder().setMainchainAddress(mainChainAddressBS)
        .setSidechainAddress(sideChainAddressBS).setDataHash(dataHashBS)
        .setOriginalTransactionId(originalTransactionIdBS)
        .setTransactionId(transactionIdBS).build();
  }

  public MultiSignForMappingActuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MultiSignForMappingEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }

    String mainChainAddressStr = WalletUtil
        .encode58Check(event.getMainchainAddress().toByteArray());
    String sideChainAddressStr = WalletUtil
        .encode58Check(event.getSidechainAddress().toByteArray());
    String dataHashStr = ByteArray.toHexString(event.getDataHash().toByteArray());
    String originalTransactionIdStr = ByteArray
        .toHexString(event.getOriginalTransactionId().toByteArray());
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info("MultiSignForMappingActuator,  mainChainAddress: {},"
            + " sideChainAddress: {}, originalTransactionId: {}, transactionId: {}",
        mainChainAddressStr,
        sideChainAddressStr, originalTransactionIdStr, transactionIdStr);
    Transaction tx = MainChainGatewayApi
        .addTokenMappingTransaction(mainChainAddressStr, sideChainAddressStr, dataHashStr,
            originalTransactionIdStr);
    if (tx == null) {
      return null;
    }
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