package org.tron.service.eventactuator.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.utils.ByteArray;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public abstract class MultSignForWIthdrawActuator extends Actuator {

  @Override
  public BroadcastRet broadcastTransactionExtensionCapsule() {

    String nonceStr = ByteArray.toStr(getNonce());
    try {
      boolean done = MainChainGatewayApi.getWithdrawStatus(nonceStr);
      if (done) {
        return BroadcastRet.DONE;
      } else {
        return super.broadcastTransactionExtensionCapsule();
      }
    } catch (Exception e) {
      logger.error("get withdraw status trx err txId is {}",
          transactionExtensionCapsule.getTransactionId(), e);
      return BroadcastRet.FAIL;
    }
  }

  public CheckTxRet checkTxInfo() {
    String transactionId = transactionExtensionCapsule.getTransactionId();
    try {
      byte[] txInfo = MainChainGatewayApi.checkTxInfo(transactionId);
      // success
      return CheckTxRet.SUCCESS;
    } catch (Exception e) {
      // fail
      logger.error("capsule err txId is {}", transactionId, e);
      return CheckTxRet.FAIL;
    }
  }
}
