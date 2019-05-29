package org.tron.service.eventactuator.sidechain;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.protos.Sidechain.WithdrawTRC721Event;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC721Actuator extends Actuator {

  // "event WithdrawTRC721(address from, uint256 tokenId, address mainChainAddress, bytes memory userSign);"

  private String from;
  private String mainChainAddress;
  private String tokenId;
  private String userSign;

  public WithdrawTRC721Actuator(String from, String tokenId, String mainChainAddress, String txData,
      String txId) {
    this.from = from;
    this.tokenId = tokenId;
    this.mainChainAddress = mainChainAddress;
    this.userSign = txData;
    this.txId = txId;
  }

  public WithdrawTRC721Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.WITHDRAW_TRC721_EVENT;
    this.event = eventMsg.getParameter().unpack(WithdrawTRC721Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger
        .info(
            "WithdrawTRC721Actuator, from: {}, tokenId: {}, mainChainAddress: {}, userSign: {}, txId: {}",
            this.from, this.tokenId, this.mainChainAddress, this.userSign, this.txId);
    Transaction tx = SideChainGatewayApi
        .withdrawTRC721Transaction(this.from, this.mainChainAddress, this.tokenId, this.userSign,
            this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);

    return this.transactionExtensionCapsule;
  }

}
