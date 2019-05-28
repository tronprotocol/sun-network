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
import org.tron.protos.Sidechain.WithdrawTRC20Event;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC20Actuator extends Actuator {

  // "event WithdrawTRC20(address from, uint256 value, address mainChainAddress, bytes memory userSign);"

  private String from;
  private String mainChainAddress;
  private String value;
  private String userSign;

  public WithdrawTRC20Actuator(String from, String value, String mainChainAddress, String txData,
      String txId) {
    this.from = from;
    this.value = value;
    this.mainChainAddress = mainChainAddress;
    this.userSign = txData;
    this.txId = txId;
  }

  public WithdrawTRC20Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.WITHDRAW_TRC20_EVENT;
    this.event = eventMsg.getParameter().unpack(WithdrawTRC20Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger.info(
        "WithdrawTRC20Actuator, from: {}, value: {}, mainChainAddress: {}, userSign: {}, txId: {}",
        this.from, this.value, this.mainChainAddress, this.userSign, this.txId);
    Transaction tx = SideChainGatewayApi
        .withdrawTRC20Transaction(this.from, this.mainChainAddress, this.value, this.userSign,
            this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }

}
