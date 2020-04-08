package org.tron.service.eventactuator.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.utils.ByteArray;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public abstract class MultiSignForWithdrawActuator extends Actuator {

  @Override
  public BroadcastRet broadcastTransactionExtensionCapsule() {

    String nonceStr = ByteArray.toStr(getNonce());
    try {
      boolean done = MainChainGatewayApi.getWithdrawStatus(getWithdrawDataHash(), nonceStr);
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

  public abstract String getWithdrawDataHash();

  public Actuator getNextActuator() {
    return null;
  }
}
