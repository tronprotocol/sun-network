package org.tron.walletcli.db;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DepositStore extends MonitorStore {

  private static DepositStore instance = new DepositStore();

  public static DepositStore getInstance() {
    return instance;
  }

  private DepositStore() {
    this.dataBaseName = "deposit";
    this.parentName = "database";
    initDB();
  }

}
