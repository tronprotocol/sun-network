package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MultiSignForMappingEvent;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForMappingActuator extends Actuator {

  // "event MultiSignForWithdrawToken(address from, address mainChainAddress, uint256 valueOrTokenId, uint256 _type, bytes32 userSign, bytes32 dataHash, bytes32 txId);"

  private String mainChainAddress;
  private String sideChainAddress;

  public MultiSignForMappingActuator(String from, String mainChainAddress,
      String valueOrTokenId, String type, String userSign, String dataHash, String txId) {
    this.from = from;
    this.mainChainAddress = mainChainAddress;
    this.valueOrTokenId = valueOrTokenId;
    this.type = type;
    this.userSign = userSign;
    this.dataHash = dataHash;
    this.txId = txId;
  }

  public MultiSignForMappingActuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.MULTISIGN_FOR_MAPPING_EVENT;
    this.event = eventMsg.getParameter().unpack(MultiSignForMappingEvent.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger.info(
        "MultiSignForWithdrawTokenActuator, from: {}, mainChainAddress: {}, valueOrTokenId: {}, _type: {}, userSign: {}, dataHash: {}, txId: {}",
        this.from, this.mainChainAddress, this.valueOrTokenId, this.type, this.userSign,
        this.dataHash, this.txId);
    Transaction tx = MainChainGatewayApi
        .multiSignForWithdrawTokenTransaction(this.from, this.mainChainAddress, this.valueOrTokenId,
            this.type,
            this.userSign, this.dataHash, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
