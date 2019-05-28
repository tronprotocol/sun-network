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
import org.tron.protos.Sidechain.WithdrawTRC10Event;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC10Actuator extends Actuator {

  // "event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes memory userSign);"

  private String from;
  private String trc10;
  private String value;
  private String userSign;

  public WithdrawTRC10Actuator(String from, String value, String trc10, String txData,
      String txId) {
    this.from = from;
    this.value = value;
    this.trc10 = trc10;
    this.userSign = txData;
    this.txId = txId;
  }

  public WithdrawTRC10Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.type = EventType.WITHDRAW_TRC10_EVENT;
    this.event = eventMsg.getParameter().unpack(WithdrawTRC10Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger
        .info("WithdrawTRC10Actuator, from: {}, value: {}, trc10: {}, userSign: {}, txId: {}",
            this.from,
            this.value, this.trc10, this.userSign, this.txId);
    // if (this.trc10.equalsIgnoreCase("2000000")) {
    //   Transaction tx = MainChainGatewayApi.withdrawTRC20Transaction(this.from,
    //     WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress()), this.value, this.userSign);
    //   this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    // } else {
    // Transaction tx = MainChainGatewayApi
    //   .withdrawTRC10Transaction(this.from, this.trc10, this.value, this.userSign);
    // this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    // }
    Transaction tx = SideChainGatewayApi
        .withdrawTRC10Transaction(this.from, this.trc10, this.value, this.userSign, this.txId);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }
}
