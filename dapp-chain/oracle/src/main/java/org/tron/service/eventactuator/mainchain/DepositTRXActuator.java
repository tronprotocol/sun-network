package org.tron.service.eventactuator.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRXActuator extends Actuator {

  private String from;
  private String amount;

  public DepositTRXActuator(String from, String amount) {
    this.from = from;
    this.amount = amount;
  }

  @Override
  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }
    try {
      logger.info("from:{},amount:{}", this.from, this.amount);
      Transaction tx = SideChainGatewayApi.mintTrxTransaction(this.from, this.amount);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    } catch (Exception e) {
      logger.error("from:{},amount:{}", this.from, this.amount);
      e.printStackTrace();
    }
    return this.transactionExtensionCapsule;
  }

}
