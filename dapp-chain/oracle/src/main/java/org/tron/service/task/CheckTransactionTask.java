package org.tron.service.task;

import java.math.BigInteger;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.MessageCode;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
import org.tron.db.Manager;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.eventactuator.Actuator.CheckTxRet;

@Slf4j(topic = "checkTxTask")
public class CheckTransactionTask {

  private static CheckTransactionTask instance = new CheckTransactionTask();

  public static CheckTransactionTask getInstance() {
    return instance;
  }

  private CheckTransactionTask() {
  }

  private final ScheduledExecutorService checkPool = Executors
      .newScheduledThreadPool(SystemSetting.CHECK_POOL_SIZE);

  public void submitCheck(Actuator eventActuator, long delay) {
    if (logger.isInfoEnabled()) {
      logger.info("check tx submit txId is {} , delay is {} ",
          eventActuator.getTransactionExtensionCapsule().getTransactionId(), delay);
    }
    checkPool.schedule(() -> instance.checkTransaction(eventActuator), delay, TimeUnit.SECONDS);
  }

  private void checkTransaction(Actuator eventActuator) {
    try {
      CheckTxRet checkTxRet = eventActuator.checkTxInfo();
      String transactionId = eventActuator.getTransactionExtensionCapsule().getTransactionId();
      String chain = eventActuator.getTaskEnum().name();
      if (checkTxRet == CheckTxRet.SUCCESS) {
        Manager.getInstance().setProcessSuccess(eventActuator.getNonceKey());
        if (logger.isInfoEnabled()) {
          String msg = MessageCode.CHECK_TRANSACTION_SUCCESS
              .getMsg(chain, transactionId);
          logger.info(msg);
        }
        //deposit G1 will withdraw to G2
        if (isTypeOfDeposit(eventActuator)
            && new BigInteger(eventActuator.getNonce())
            .compareTo(new BigInteger(SystemSetting.VERSION_BASE_VALUE)) < 0) {
          if (Objects.nonNull(eventActuator.getNextActuator())) {
            CreateTransactionTask.getInstance().submitCreate(eventActuator.getNextActuator(), 0L);
          } else {
            logger.info("G1 deposit nonce is {} next G1 to G2 withdraw actuator is null",
                eventActuator.getNonce());
          }
        }
      } else {
        String msg = MessageCode.CHECK_TRANSACTION_FAIL
            .getMsg(chain, transactionId);
        RetryTransactionTask.getInstance().processAndSubmit(eventActuator, msg);
      }
    } catch (Exception e) {
      logger.error("checkTransaction catch error! nouce = {}",
          ByteArray.toStr(eventActuator.getNonceKey()), e);
      Manager.getInstance()
          .setProcessFail(eventActuator.getNonceKey(), eventActuator.getRetryTimes());
    }
  }

  private boolean isTypeOfDeposit(Actuator eventActuator) {
    switch (eventActuator.getType()) {
      case DEPOSIT_TRC10_EVENT:
      case DEPOSIT_TRC20_EVENT:
      case DEPOSIT_TRC721_EVENT:
      case DEPOSIT_TRX_EVENT:
        return true;
    }
    return false;
  }
}
