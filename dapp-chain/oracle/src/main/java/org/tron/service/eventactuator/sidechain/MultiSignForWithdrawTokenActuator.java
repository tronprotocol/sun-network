package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.ByteString;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DeployDAppTRC20AndMappingEvent;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MultiSignForWithdrawTokenEvent;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class MultiSignForWithdrawTokenActuator extends Actuator {

  // "event MultiSignForWithdrawToken(address from, address mainChainAddress, uint256 valueOrTokenId, uint256 _type, bytes32 userSign, bytes32 dataHash, bytes32 txId);"

  MultiSignForWithdrawTokenEvent event;

  public MultiSignForWithdrawTokenActuator(String from, String mainChainAddress,
      String valueOrTokenId, String type, String userSign, String dataHash, String txId) {
    ByteString developerBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(developer));
    ByteString mainChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(mainChainAddress));
    ByteString sideChainAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(sideChainAddress));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.type = EventType.DEPOSIT_TRC10_EVENT;
    this.event = DeployDAppTRC20AndMappingEvent.newBuilder().setDeveloper(developerBS)
        .setMainchainAddress(mainChainAddressBS)
        .setSidechainAddress(sideChainAddressBS)
        .setTransactionId(transactionIdBS).setWillTaskEnum(TaskEnum.MAIN_CHAIN).build();
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
