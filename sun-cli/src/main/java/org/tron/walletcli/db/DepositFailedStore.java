package org.tron.walletcli.db;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DepositFailedStore extends MonitorStore {

  private static DepositFailedStore instance = new DepositFailedStore();

  public static DepositFailedStore getInstance() {
    return instance;
  }

  private DepositFailedStore() {
    this.dataBaseName = "deposit";
    this.parentName = "database";
    initDB();
  }

}
