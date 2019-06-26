package org.tron.service.eventactuator.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.logger.LoggerOracle;
import org.tron.common.utils.ByteArray;
import org.tron.service.eventactuator.Actuator;

@Slf4j
public abstract class MultSignForWIthdrawActuator extends Actuator {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

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
      loggerOracle.error("get withdraw status trx err txId is {}",
          transactionExtensionCapsule.getTransactionId(), e);
      return BroadcastRet.FAIL;
    }
  }
}
