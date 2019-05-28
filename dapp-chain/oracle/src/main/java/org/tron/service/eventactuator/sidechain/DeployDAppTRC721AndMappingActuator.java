package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
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

  private String developer;
  private String mainChainAddress;
  private String sideChainAddress;

  public DeployDAppTRC721AndMappingActuator(String developer, String mainChainAddress,
      String sideChainAddress, String txId) {
    this.developer = developer;
    this.mainChainAddress = mainChainAddress;
    this.sideChainAddress = sideChainAddress;
    this.txId = txId;
  }

  public DeployDAppTRC721AndMappingActuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.DEPLOY_DAPPTRC721_AND_MAPPING_EVENT;
    this.event = eventMsg.getParameter().unpack(DeployDAppTRC721AndMappingEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }
    logger.info(
        "DeployDAppTRC721AndMappingActuator, developer: {}, mainChainAddress: {}, sideChainAddress: {}",
        this.developer, this.mainChainAddress, this.sideChainAddress);
    Transaction tx = MainChainGatewayApi
        .addTokenMappingTransaction(this.mainChainAddress, this.sideChainAddress);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
