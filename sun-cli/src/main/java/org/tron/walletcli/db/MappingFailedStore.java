package org.tron.walletcli.db;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MappingFailedStore extends MonitorStore {

  private static MappingFailedStore instance = new MappingFailedStore();

  public static MappingFailedStore getInstance() {
    return instance;
  }

  private MappingFailedStore() {
    this.dataBaseName = "mappingFailed";
    this.parentName = "database";
    initDB();
  }

}
