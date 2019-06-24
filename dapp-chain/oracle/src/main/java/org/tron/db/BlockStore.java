package org.tron.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BlockStore extends OracleStore {

  private BlockStore() {
    this.dataBaseName = "block";
    this.parentName = "database";
    initDB();
  }

}
