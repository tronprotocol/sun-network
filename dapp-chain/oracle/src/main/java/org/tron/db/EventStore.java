package org.tron.db;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.service.eventactuator.Actuator;
import org.tron.service.task.InitTask;

@Slf4j
public class EventStore extends OracleStore {

  private static EventStore instance = new EventStore();

  public static EventStore getInstance() {
    return instance;
  }

  private EventStore() {
    this.dataBaseName = "event";
    this.parentName = "database";
    initDB();
  }

  public byte[] getNonce(byte[] key) {
    byte[] data = getData(key);
    if (data == null) {
      return null;
    }
    try {
      Actuator actuator = InitTask.getActuatorByEventMsg(data);
      return actuator.getNonce();
    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return null;
  }

}
