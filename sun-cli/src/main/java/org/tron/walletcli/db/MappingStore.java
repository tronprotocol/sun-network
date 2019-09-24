package org.tron.walletcli.db;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MappingStore extends MonitorStore {

  private static MappingStore instance = new MappingStore();

  public static MappingStore getInstance() {
    return instance;
  }

  private MappingStore() {
    this.dataBaseName = "mapping";
    this.parentName = "database";
    initDB();
  }

}
