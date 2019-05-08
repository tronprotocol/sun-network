package org.tron.service.eventactuator.mainchain;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class DepositTRC10Actuator extends Actuator {

  private String from;
  private String amount;
  private String tokenId;

  public DepositTRC10Actuator(String from, String amount, String tokenId) {
    this.from = from;
    this.amount = amount;
    this.tokenId = tokenId;
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule() throws RpcConnectException {
    if (Objects.nonNull(this.transactionExtensionCapsule)) {
      return this.transactionExtensionCapsule;
    }
    logger.info("DepositTRC10Actuator, from: {}, amount: {}, tokenId: {}", this.from, this.amount,
      this.tokenId);
    // TODO: throw RpcConnectException
    AssetIssueContract assetIssue = MainChainGatewayApi.getAssetIssueById(this.tokenId);
    Transaction tx = SideChainGatewayApi
      .mintToken10Transaction(this.from, this.tokenId, this.amount,
        assetIssue.getName().toStringUtf8(), assetIssue.getName().toStringUtf8(),
        assetIssue.getPrecision());
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN, tx);
    return this.transactionExtensionCapsule;
  }
}
