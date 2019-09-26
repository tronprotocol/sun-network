package org.tron.walletcli.checker;

import java.nio.ByteBuffer;
import java.util.Set;
import org.tron.common.utils.ByteArray;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.walletcli.db.MappingFailedStore;
import org.tron.walletcli.db.MappingStore;
import org.tron.walletcli.utils.GatewayUtils;

public class MappingChecker extends ContractChecker {

  private MappingStore store;
  private MappingFailedStore failedStore;

  public MappingChecker() {
    this.store = MappingStore.getInstance();
    this.failedStore = MappingFailedStore.getInstance();
  }

  public void checkMapping() {
    while (true) {
      try {
        byte[] data = store.getData("next_nonce".getBytes());
        long nextNonce = ByteArray.toLong(data);
        walletApiWrapper.switch2Main();
        TransactionResponse response = walletApiWrapper
            .callConstantContractRet(mainChainGateway, "getMappingMsg(uint256)",
                String.valueOf(nextNonce), false, 10000000);
        if (response.result) {
          store.putData(ByteArray.fromLong(nextNonce),
              ByteArray.fromHexString(response.getConstantResult()));
          walletApiWrapper.switch2Side();
          response = walletApiWrapper
              .callConstantContractRet(sideChainGateway, "mappingDone(uint256)",
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
          logger.info("next mapping nonce is {}", nextNonce);
          Thread.sleep(1 * 1000);

        } else {
          logger.info("check mapping sleep 5 minutes. next is {}.", nextNonce);
          Thread.sleep(5 * 60 * 1000);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void checkFailedMapping() {

    Set<ByteBuffer> allFailed = failedStore.allKeys();
    logger.info("check fail mapping size is {}.", allFailed.size());
    allFailed.forEach(nonceBuffer -> {
      byte[] targetNonce = nonceBuffer.array();
      walletApiWrapper.switch2Side();
      TransactionResponse response = walletApiWrapper
          .callConstantContractRet(mainChainGateway, "mappingDone(uint256)",
              String.valueOf(targetNonce), false, 10000000);
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
        Thread.sleep(5 * 1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    });
  }


  public void println() {
    Set<ByteBuffer> allFailed = failedStore.allKeys();
    logger.info("print fail mapping size is {}.", allFailed.size());
    StringBuilder str = new StringBuilder("Failed mapping nonces are ");
    allFailed.forEach(nonceBuffer -> {
      byte[] targetNonce = nonceBuffer.array();
      str.append(ByteArray.toLong(targetNonce)).append(" ");
    });
    str.append(".");
    System.out.println(str);
    sendAlert(str.toString());
  }
}
