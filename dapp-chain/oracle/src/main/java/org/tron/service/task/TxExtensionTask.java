package org.tron.service.task;

import lombok.extern.slf4j.Slf4j;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.exception.TxValidateException;
import org.tron.service.check.CheckTransaction;
import org.tron.service.check.TransactionExtensionCapsule;

@Slf4j(topic = "task")
public class TxExtensionTask implements Runnable {

  private TransactionExtensionCapsule txExtensionCapsule;

  public TxExtensionTask(TransactionExtensionCapsule txExtensionCapsule) {
    this.txExtensionCapsule = txExtensionCapsule;
  }

  @Override
  public void run() {
    try {
      CheckTransaction.getInstance().broadcastTransaction(this.txExtensionCapsule);
      CheckTransaction.getInstance().submitCheck(this.txExtensionCapsule);
    } catch (RpcConnectException e) {
      // TODO: 1.2 连接的fullnode或soliditynode服务停止、网络超时等引起的RPC接口connect异常
      // TODO: 重试5次，仍然失败则告警、退出oracle进程、排查问题、重启fullnode和oracle(不会丢数据)

      logger.error("RpcConnectException: {}", e.getMessage());
      e.printStackTrace();
    } catch (TxValidateException e) {
      // TODO: 4.1 交易验证失败，没有上链
      // TODO: 	告警、退出oracle进程、排查问题、重启oracle进程
      logger.error("TxValidateException: {}", e.getMessage());
      e.printStackTrace();
    }
  }
}
