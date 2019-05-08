package org.tron.service.eventactuator.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC10Actuator extends Actuator {

  // "event WithdrawTRC10(address from, uint256 value, uint256 trc10, bytes memory txData);"

  private String from;
  private String value;
  private String trc10;
  private String txData;

  public WithdrawTRC10Actuator(String from, String value, String trc10, String txData) {
    this.from = from;
    this.value = value;
    this.trc10 = trc10;
    this.txData = txData;
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule() throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger
      .info("WithdrawTRC10Actuator, from: {}, value: {}, trc10: {}, txData: {}", this.from,
        this.value, this.trc10, this.txData);
    // if (this.trc10.equalsIgnoreCase("2000000")) {
    //   Transaction tx = MainChainGatewayApi.withdrawTRC20Transaction(this.from,
    //     WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress()), this.value, this.txData);
    //   this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    // } else {
    // Transaction tx = MainChainGatewayApi
    //   .withdrawTRC10Transaction(this.from, this.trc10, this.value, this.txData);
    // this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    // }
    Transaction tx = MainChainGatewayApi
      .withdrawTRC10Transaction(this.from, this.trc10, this.value, this.txData);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }
}
