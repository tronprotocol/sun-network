package org.tron.walletcli.db;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WithdrawStore extends MonitorStore {

  private static WithdrawStore instance = new WithdrawStore();

  public static WithdrawStore getInstance() {
    return instance;
  }

  private WithdrawStore() {
    this.dataBaseName = "withdraw";
    this.parentName = "database";
    initDB();
  }

}
