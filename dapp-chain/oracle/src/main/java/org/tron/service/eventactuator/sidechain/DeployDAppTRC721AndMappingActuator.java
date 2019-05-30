package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DeployDAppTRC721AndMappingEvent;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class DeployDAppTRC721AndMappingActuator extends Actuator {

  // "event DeployDAppTRC721AndMapping(address developer, address mainChainAddress, address sideChainAddress);"

  private DeployDAppTRC721AndMappingEvent event;
  @Getter
  private EventType type = EventType.DEPLOY_DAPPTRC721_AND_MAPPING_EVENT;

  public DeployDAppTRC721AndMappingActuator(String developer, String mainChainAddress,
      String sideChainAddress, String transactionId) {
    ByteString developerBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(developer));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString sideChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(sideChainAddress));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.event = DeployDAppTRC721AndMappingEvent.newBuilder().setDeveloper(developerBS)
        .setMainchainAddress(mainChainAddressBS).setSidechainAddress(sideChainAddressBS)
        .setTransactionId(transactionIdBS).build();
  }

  public DeployDAppTRC721AndMappingActuator(EventMsg eventMsg)
      throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(DeployDAppTRC721AndMappingEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }

    String developerStr = WalletUtil.encode58Check(event.getDeveloper().toByteArray());
    String mainChainAddressStr = WalletUtil
        .encode58Check(event.getMainchainAddress().toByteArray());
    String sideChainAddressStr = WalletUtil
        .encode58Check(event.getSidechainAddress().toByteArray());
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());

    logger.info("DeployDAppTRC721AndMappingActuator, developer: {}, mainChainAddress: {},"
            + " sideChainAddress: {}, transactionId: {}", developerStr, mainChainAddressStr,
        sideChainAddressStr, transactionIdStr);

    Transaction tx = SideChainGatewayApi
        .mappingTransaction(mainChainAddressStr, sideChainAddressStr, transactionIdStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);

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