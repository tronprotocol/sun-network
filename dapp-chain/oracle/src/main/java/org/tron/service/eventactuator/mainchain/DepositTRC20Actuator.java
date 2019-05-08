package org.tron.service.eventactuator.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.Args;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC20Actuator extends Actuator {

  private String from;
  private String amount;
  private String contractAddress;

  public DepositTRC20Actuator(String from, String amount, String contractAddress) {
    this.from = from;
    this.amount = amount;
    this.contractAddress = contractAddress;
  }

  @Override
  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    try {
      if (WalletUtil.encode58Check(Args.getInstance().getSunTokenAddress())
          .equalsIgnoreCase(this.contractAddress)) {
        Transaction tx = SideChainGatewayApi
            .mintToken10Transaction(this.from, "2000000", this.amount, "sun token", "ST", 6);
        this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
      } else {
        Transaction tx = SideChainGatewayApi
            .mintToken20Transaction(this.from, this.contractAddress, this.amount);
        this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
      }
    } catch (Exception e) {
      logger.error("from:{},amount:{},contractAddress:{}", this.from, this.amount,
          this.contractAddress);
      e.printStackTrace();
    }
    return this.transactionExtensionCapsule;
  }

}
