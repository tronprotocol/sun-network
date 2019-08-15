package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.tron.common.config.Args;
import org.tron.common.utils.AlertUtil;
import org.tron.db.Manager;
import org.tron.db.NonceStore;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "retryTransactionTask")
public class RetryTransactionTask {

  private static RetryTransactionTask instance = new RetryTransactionTask();

  public static RetryTransactionTask getInstance() {
    return instance;
  }

  @Transactional
  public void processAndSubmit(Actuator actuator, String msg) {

    logger.info("RetryTransactionTask processAndSubmit! msg = {}", msg);
    try {
      byte[] nonceMsgBytes = NonceStore.getInstance().getData(actuator.getNonceKey());
      NonceMsg nonceMsg = NonceMsg.parseFrom(nonceMsgBytes);
      int retryTimes = nonceMsg.getRetryTimes() + 1;
      if (retryTimes >= Args.getInstance().getOracleRetryTimes()) {
        Manager.getInstance().setProcessFail(actuator.getNonceKey());
        AlertUtil.sendAlert(msg);
      } else {
        Manager.getInstance().setProcessRetry(actuator.getNonceKey(), retryTimes);
        CreateTransactionTask.getInstance().submitCreate(actuator, getDelay(retryTimes));
      }
    } catch (Exception e) {
      logger.error("parse pb error", e);
    }

  }

  private long getDelay(int retryTimes) {
    // retryTimes in [0, 4]
    // delay = (2 ^ retryTimes - 1) * 10 * 60 seconds
    return (Double.valueOf(Math.pow(2, retryTimes)).longValue() - 1) * 10 * 60;
  }

}
