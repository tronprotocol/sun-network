package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.AlertUtil;
import org.tron.db.Manager;
import org.tron.db.NonceStore;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;
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
      byte[] nonceMsgBytes = NonceStore.getInstance().getData(actuator.getNonceKey());
      NonceMsg nonceMsg = NonceMsg.parseFrom(nonceMsgBytes);
      int retryTimes = nonceMsg.getRetryTimes() + 1;
      if (retryTimes >= SystemSetting.ORACLE_RETRY_TIMES) {
        Manager.getInstance().setProcessStatus(actuator.getNonceKey(), NonceStatus.FAIL);
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
    // age in [0, 4]
    // delay = (2 ^ age - 1) * 10 second
    return (Double.valueOf(Math.pow(2, retryTimes)).longValue() - 1) * 10;
  }

}
