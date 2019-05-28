package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.DepositTRC721Event;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC721Actuator extends Actuator {

  DepositTRC721Event event;

  public DepositTRC721Actuator(String from, String tokenId, String contractAddress,
      String transactionId) {
    ByteString fromBS = ByteString.copyFrom(WalletUtil.decodeFromBase58Check(from));
    ByteString tokenIdBS = ByteString.copyFrom(ByteArray.fromHexString(tokenId));
    ByteString contractAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(contractAddress));
    ByteString transactionIdBS = ByteString.copyFrom(ByteArray.fromHexString(transactionId));
    this.type = EventType.DEPOSIT_TRC10_EVENT;
    this.event = DepositTRC721Event.newBuilder().setFrom(fromBS).setTokenId(tokenIdBS)
        .setContractAddress(contractAddressBS)
        .setTransactionId(transactionIdBS).setWillTaskEnum(TaskEnum.SIDE_CHAIN).build();
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }

    String fromStr = WalletUtil.encode58Check(event.getFrom().toByteArray());
    String tokenIdStr = ByteArray.toHexString(event.getTokenId().toByteArray());
    String contractAddressStr = WalletUtil.encode58Check(event.getContractAddress().toByteArray());
    String transactionIdStr = ByteArray.toHexString(event.getTransactionId().toByteArray());
    logger.info(
        "DepositTRC721Actuator, from: {}, tokenId: {}, contractAddress: {}, transactionId: {}",
        fromStr, tokenIdStr, contractAddressStr, transactionIdStr);
    Transaction tx = SideChainGatewayApi
        .mintToken721Transaction(fromStr, contractAddressStr, tokenIdStr, transactionIdStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(this.type).build();
  }

  @Override
  public byte[] getKey() {
    return event.getTransactionId().toByteArray();
  }
}
