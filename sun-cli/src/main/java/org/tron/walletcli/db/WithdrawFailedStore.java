package org.tron.walletcli.db;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WithdrawFailedStore extends MonitorStore {

  private static WithdrawFailedStore instance = new WithdrawFailedStore();

  public static WithdrawFailedStore getInstance() {
    return instance;
  }

  private WithdrawFailedStore() {
    this.dataBaseName = "withdrawFailed";
    this.parentName = "database";
    initDB();
  }

}
