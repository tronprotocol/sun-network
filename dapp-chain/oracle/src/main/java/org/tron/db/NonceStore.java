package org.tron.db;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NonceStore extends OracleStore {

  private static NonceStore instance = new NonceStore();

  public static NonceStore getInstance() {
    return instance;
  }

  private NonceStore() {
    this.dataBaseName = "nonce";
    this.parentName = "database";
    initDB();
  }

}
