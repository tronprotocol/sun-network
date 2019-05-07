package org.tron.service.eventactuator.sidechain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public class WithdrawTRC20Actuator extends Actuator {

  // "event WithdrawTRC20(address from, uint256 value, address mainChainAddress, bytes memory txData);"

  private String from;
  private String value;
  private String mainChainAddress;
  private String txData;

  public WithdrawTRC20Actuator(String from, String value, String mainChainAddress, String txData) {
    this.from = from;
    this.value = value;
    this.mainChainAddress = mainChainAddress;
    this.txData = txData;
  }

  @Override
  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    try {
      Transaction tx = MainChainGatewayApi
          .withdrawTRC20Transaction(this.from, this.mainChainAddress, this.value, this.txData);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.MAIN_CHAIN, tx);
    } catch (Exception e) {
      logger.error(
          "WithdrawTRC20Actuator fail, from: {}, value: {}, mainChainAddress: {}, txData: {}",
          this.from, this.value, this.mainChainAddress, this.txData);
    }
    return this.transactionExtensionCapsule;
  }

}
