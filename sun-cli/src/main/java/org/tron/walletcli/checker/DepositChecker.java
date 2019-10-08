package org.tron.walletcli.checker;

import java.nio.ByteBuffer;
import java.util.Set;
import org.tron.common.utils.ByteArray;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.walletcli.db.DepositFailedStore;
import org.tron.walletcli.db.DepositStore;
import org.tron.walletcli.utils.GatewayUtils;

public class DepositChecker extends ContractChecker {

  private DepositStore store;
  private DepositFailedStore failedStore;

  public DepositChecker() {
    this.store = DepositStore.getInstance();
    this.failedStore = DepositFailedStore.getInstance();
  }

  public void checkDeposit() {
    while (true) {
      try {
        // loop nonce
        byte[] data = store.getData("next_nonce".getBytes());
        long nextNonce = ByteArray.toLong(data);
        walletApiWrapper.switch2Main();
        TransactionResponse response = walletApiWrapper
            .callConstantContractRet(mainChainGateway, "getDepositMsg(uint256)",
                String.valueOf(nextNonce), false, 10000000);
        if (response.result) {
          store.putData(ByteArray.fromLong(nextNonce),
              ByteArray.fromHexString(response.getConstantResult()));
          walletApiWrapper.switch2Side();
          response = walletApiWrapper
              .callConstantContractRet(sideChainGateway, "depositDone(uint256)",
                  String.valueOf(nextNonce), false, 10000000);
          if (!response.result) {
            failedStore
                .putData(ByteArray.fromLong(nextNonce),
                    ByteArray.fromLong(System.currentTimeMillis()));
          } else {
            byte[] resp = ByteArray.fromHexString(response.getConstantResult());
            boolean result = GatewayUtils.unpackBoolean(resp);
            if (!result) {
              failedStore
                  .putData(ByteArray.fromLong(nextNonce),
                      ByteArray.fromLong(System.currentTimeMillis()));
            }
          }
          store.putData("next_nonce".getBytes(), ByteArray.fromLong(++nextNonce));
          logger.info("next deposit nonce is {}", nextNonce);
          Thread.sleep(1 * 1000);
        } else {
          logger.info("check deposit sleep 5 minutes. next is {}.", nextNonce);
          Thread.sleep(5 * 60 * 1000);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void checkFailedDeposit() {
    Set<ByteBuffer> allFailed = failedStore.allKeys();
    logger.info("check fail deposit size is {}.", allFailed.size());
    allFailed.forEach(nonceBuffer -> {
      byte[] targetNonce = nonceBuffer.array();
      walletApiWrapper.switch2Side();
      TransactionResponse response = walletApiWrapper
          .callConstantContractRet(sideChainGateway, "depositDone(uint256)",
              String.valueOf(ByteArray.toLong(targetNonce)), false, 10000000);
      byte[] resp = ByteArray.fromHexString(response.getConstantResult());
      boolean result = GatewayUtils.unpackBoolean(resp);
      if (!result) {
        failedStore
            .putData(targetNonce,
                ByteArray.fromLong(System.currentTimeMillis()));
      } else {
        failedStore.deleteData(targetNonce);
      }
      try {
        Thread.sleep(5 * 100);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
  }

  public void println() {
    StringBuilder str = new StringBuilder();
    // nonce
    String currentNonce = Long.toString(ByteArray.toLong(store.getData("next_nonce".getBytes())));
    str.append("Current Deposit count: " + currentNonce);

    walletApiWrapper.switch2Main();

    // Gate way balance
    long mainChainGatewayBalance = walletApiWrapper.getAccount(mainChainGateway).getData().getBalance();
    logger.info("MainChain Gate way balance is {}", mainChainGatewayBalance);
    str.append("\nMainChain Gateway balance is " + mainChainGatewayBalance);

    // bonus
    TransactionResponse response = walletApiWrapper.callConstantContractRet(mainChainGateway, "bonus()",
        null, false, 10000000);
    long bonus = ByteArray.toLong(ByteArray.fromHexString(response.constantResult));
    logger.info("bonus is {}", bonus);
    str.append("\nBonus is " + bonus);

    // deposit success check
    Set<ByteBuffer> allFailed = failedStore.allKeys();
    logger.info("print fail deposit size is {}.", allFailed.size());
    if (allFailed.isEmpty()) {
      sendAlert(str.append("\nAll deposit succeed.").toString());
      return;
    }
    str.append("\nFailed deposit nonces are ");
    allFailed.forEach(nonceBuffer -> {
      byte[] targetNonce = nonceBuffer.array();
      str.append(ByteArray.toLong(targetNonce)).append(" ");
    });
    str.append(".");
    System.out.println(str);

    sendAlert(str.toString());
  }
}
