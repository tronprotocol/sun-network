package org.tron.db;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j(topic = "DB")
@Component
public class EventStore extends OracleStore {

//  private static EventStore instance = new EventStore();
//
//  public static EventStore getInstance() {
//    return instance;
//  }

  private EventStore() {
    this.dataBaseName = "event";
    this.parentName = "database";
    initDB();
  }

}
