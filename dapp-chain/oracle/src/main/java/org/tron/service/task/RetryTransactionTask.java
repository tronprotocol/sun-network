package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.tron.common.config.Args;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.AlertUtil;
import org.tron.db.EventStore;
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

    try {
      Actuator newActuator = InitTask.getActuatorByEventMsg(EventStore.getInstance().getData(actuator.getNonceKey()));
      byte[] nonceMsgBytes = NonceStore.getInstance().getData(newActuator.getNonceKey());
      NonceMsg nonceMsg = NonceMsg.parseFrom(nonceMsgBytes);
      int retryTimes = nonceMsg.getRetryTimes() + 1;
      newActuator.setRetryTimes(retryTimes);
      logger.info("RetryTransactionTask processAndSubmit! msg = {}, retryTimes = {}", msg, retryTimes);
      if (retryTimes % SystemSetting.RETRY_TIMES_EPOCH_OFFSET >= Args.getInstance().getOracleRetryTimes()) {
        Manager.getInstance().setProcessFail(newActuator.getNonceKey(), retryTimes - 1);
        AlertUtil.sendAlert(msg);
      } else {
        Manager.getInstance().setProcessRetry(newActuator.getNonceKey(), nonceMsg);
        CreateTransactionTask.getInstance().submitCreate(newActuator, getDelay(retryTimes));
      }
    } catch (Exception e) {
      logger.error("parse pb error", e);
    }

  }

  private long getDelay(int retryTimes) {
    // retryTimes in [0, 4]
    // delay = (2 ^ retryTimes - 1) * 10 * 60 seconds
    return (Double.valueOf(Math.pow(2, retryTimes % SystemSetting.RETRY_TIMES_EPOCH_OFFSET)).longValue() - 1) * 10 * 60;
  }

}
