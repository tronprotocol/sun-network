package org.tron.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import lombok.extern.slf4j.Slf4j;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;

@Slf4j
public class EventStore {

  private static final JniDBFactory factory = new JniDBFactory();

  String dataBaseName;
  DB database;
  private String parentName;
  boolean alive;
  private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

  private static EventStore instance = new EventStore();

  public static EventStore getInstance() {
    return instance;
  }

  private EventStore() {
    this.dataBaseName = "event";
    this.parentName = "database";
  }

  public void initDB() {
    resetDbLock.writeLock().lock();
    try {
      logger.debug("~> LevelDbDataSourceImpl.initDB(): " + dataBaseName);

      if (isAlive()) {
        return;
      }

      if (dataBaseName == null) {
        throw new NullPointerException("no name set to the dbStore");
      }

      Options dbOptions = newDefaultLevelDbOptions();

      try {
        openDatabase(dbOptions);
        alive = true;
      } catch (IOException ioe) {
        throw new RuntimeException("Can't initialize database", ioe);
      }
    } finally {
      resetDbLock.writeLock().unlock();
    }
  }

  private void openDatabase(Options dbOptions) throws IOException {
    final Path dbPath = getDbPath();
    if (!Files.isSymbolicLink(dbPath.getParent())) {
      Files.createDirectories(dbPath.getParent());
    }
    try {
      database = factory.open(dbPath.toFile(), dbOptions);
    } catch (IOException e) {
      if (e.getMessage().contains("Corruption:")) {
        factory.repair(dbPath.toFile(), dbOptions);
        database = factory.open(dbPath.toFile(), dbOptions);
      } else {
        throw e;
      }
    }
  }

  public boolean close() {
    try {
      database.close();
      alive = false;
      return true;
    } catch (IOException e) {
      logger.error(e.getMessage());
      return false;
    }

  }

  public boolean isAlive() {
    return alive;
  }

  private static org.iq80.leveldb.Options newDefaultLevelDbOptions() {
    org.iq80.leveldb.Options dbOptions = new org.iq80.leveldb.Options();
    dbOptions.createIfMissing(true);
    dbOptions.paranoidChecks(true);
    dbOptions.verifyChecksums(true);
    dbOptions.compressionType(CompressionType.SNAPPY);
    dbOptions.blockSize(4 * 1024);
    dbOptions.writeBufferSize(10 * 1024 * 1024);
    dbOptions.cacheSize(10 * 1024 * 1024L);
    dbOptions.maxOpenFiles(100);
    return dbOptions;
  }

  public Path getDbPath() {
    return Paths.get(parentName, dataBaseName);
  }

  public byte[] getData(byte[] key) {
    return database.get(key);
  }

  public void put(byte[] key, byte[] value) {
    database.put(key, value);
  }
}
