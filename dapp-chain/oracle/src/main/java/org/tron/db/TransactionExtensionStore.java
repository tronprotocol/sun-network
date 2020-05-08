package org.tron.db;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TransactionExtensionStore extends OracleStore {


  private static TransactionExtensionStore instance = new TransactionExtensionStore();

  public static TransactionExtensionStore getInstance() {
    return instance;
  }

  private TransactionExtensionStore() {
    this.dataBaseName = "transaction";
    this.parentName = "database";
    initDB();
  }

}
