package org.tron.service.eventactuator.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC721Actuator extends Actuator {

  private String from;
  private String uid;
  private String contractAddress;

  public DepositTRC721Actuator(String from, String uid, String contractAddress) {
    this.from = from;
    this.uid = uid;
    this.contractAddress = contractAddress;
  }

  @Override
  public TransactionExtensionCapsule getTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }
    try {
      Transaction tx = SideChainGatewayApi
          .mintToken721Transaction(this.from, this.contractAddress, this.uid);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    } catch (Exception e) {
      logger
          .error("from:{},uid:{},contractAddress{}", this.from, this.uid, this.contractAddress);
      e.printStackTrace();
    }
    return this.transactionExtensionCapsule;
  }

}
