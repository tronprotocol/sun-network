package org.tron.walletcli.checker;

import java.util.Map;
import org.iq80.leveldb.DBIterator;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.ByteArray;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.walletcli.db.WithdrawFailedStore;
import org.tron.walletcli.db.WithdrawStore;

public class WithdrawChecker extends ContractChecker {
  private WithdrawStore store;
  private WithdrawFailedStore failedStore;

  public WithdrawChecker() {
    this.store = WithdrawStore.getInstance();
  }

  public void checkWithdraw() {
    byte[] data = store.getData("next_nonce".getBytes());
    long nextNonce = ByteArray.toLong(data);
    while (true) {
      try {
        walletApiWrapper.switch2Side();
        TransactionResponse response = walletApiWrapper
            .callConstantContractRet(sideChainGateway, "getWithdrawMsg(uint256)",
                String.valueOf(nextNonce), false, 10000000);
        if (response.result == true) {
          store.putData(ByteArray.fromLong(nextNonce),
              ByteArray.fromHexString(response.getConstantResult()));
          walletApiWrapper.switch2Main();
          response = walletApiWrapper
              .callConstantContractRet(mainChainGateway, "withdrawDone(bytes32,uint256)",
                  String.valueOf(nextNonce), false, 10000000);
          byte[] resp = ByteArray.fromHexString(response.getConstantResult());
          boolean result = AbiUtil.unpackBoolean(resp);
          if (!result) {
            failedStore
                .putData(ByteArray.fromLong(nextNonce),
                    ByteArray.fromLong(System.currentTimeMillis()));
          }
          store.putData("next_nonce".getBytes(), ByteArray.fromLong(++nextNonce));
        } else {
          Thread.sleep(5 * 60 * 1000);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void checkFailedWithdraw(){
    DBIterator iterator = failedStore.database.iterator();
    while(iterator.hasNext()) {
      Map.Entry<byte[], byte[]> entry = iterator.next();
      byte[] targetNonce = entry.getKey();
      walletApiWrapper.switch2Side();
      TransactionResponse response = walletApiWrapper
          .callConstantContractRet(mainChainGateway, "withdrawDone(bytes32,uint256)",
              String.valueOf(targetNonce), false, 10000000);
      byte[] resp = ByteArray.fromHexString(response.getConstantResult());
      boolean result = AbiUtil.unpackBoolean(resp);
      if (!result) {
        failedStore
            .putData(targetNonce,
                ByteArray.fromLong(System.currentTimeMillis()));
      } else {
        failedStore.deleteData(targetNonce);
      }
    }
  }

}
