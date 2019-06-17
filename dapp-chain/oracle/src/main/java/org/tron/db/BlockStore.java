package org.tron.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
public class BlockStore extends OracleStore {

  private static BlockStore instance = new BlockStore();

  public static BlockStore getInstance() {
    return instance;
  }

  private BlockStore() {
    this.dataBaseName = "block";
    this.parentName = "database";
    initDB();
  }

}
