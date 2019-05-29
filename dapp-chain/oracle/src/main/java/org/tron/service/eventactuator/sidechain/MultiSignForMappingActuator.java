package org.tron.service.eventactuator.sidechain;

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
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForMappingActuator extends Actuator {

  // "event MultiSignForDeployAndMapping(address mainChainAddress, address sideChainAddress, bytes32 dataHash, bytes32 txId);"

  MultiSignForMappingEvent event;
  @Getter
  EventType type = EventType.MULTISIGN_FOR_MAPPING_EVENT;

  public MultiSignForMappingActuator(String mainChainAddress, String sideChainAddress,
      String dataHash, String transactionId) {
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString sideChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(sideChainAddress));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.event = MultiSignForMappingEvent.newBuilder().setMainchainAddress(mainChainAddressBS)
        .setSidechainAddress(sideChainAddressBS).setTransactionId(transactionIdBS)
        .setWillTaskEnum(TaskEnum.MAIN_CHAIN).build();
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
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info("MultiSignForMappingActuator,  mainChainAddress: {},"
            + " sideChainAddress: {}, transactionId: {}", mainChainAddressStr,
        sideChainAddressStr, transactionIdStr);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTokenTransaction(mainChainAddressStr, sideChainAddressStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
