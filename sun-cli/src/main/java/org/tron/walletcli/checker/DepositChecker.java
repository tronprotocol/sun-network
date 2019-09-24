package org.tron.walletcli.checker;

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
  }

  public void checkDeposit() {
    byte[] data = store.getData("next_nonce".getBytes());
    long nextNonce = ByteArray.toLong(data);
    while (true) {
      try {
        walletApiWrapper.switch2Main();
        TransactionResponse response = walletApiWrapper
            .callConstantContractRet(mainChainGateway, "getDepositMsg(uint256)",
                String.valueOf(nextNonce), false, 10000000);
        if (response.result == true) {
          store.putData(ByteArray.fromLong(nextNonce),
              ByteArray.fromHexString(response.getConstantResult()));
          walletApiWrapper.switch2Side();
          response = walletApiWrapper
              .callConstantContractRet(mainChainGateway, "depositDone(uint256)",
                  String.valueOf(nextNonce), false, 10000000);
          byte[] resp = ByteArray.fromHexString(response.getConstantResult());
          boolean result = GatewayUtils.unpackBoolean(resp);
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

}
