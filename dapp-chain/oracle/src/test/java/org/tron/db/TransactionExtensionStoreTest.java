package org.tron.db;

import org.iq80.leveldb.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.ByteArray;

public class TransactionExtensionStoreTest {

  private static TransactionExtensionStore store;

  @Before
  public void init() {
    store = TransactionExtensionStore.getInstance();
    store.initDB();
  }

  @Test
  public void getData() {

    byte[] key = ByteArray.fromString("aa");
    byte[] value = ByteArray.fromString("mmmmm");
    store.putData(key, value);
    Assert.assertArrayEquals(store.getData(key), value);
  }

  @Test
  public void putData() {
    byte[] key = ByteArray.fromString("aa");
    byte[] value = ByteArray.fromString("mmmmm");
    TransactionExtensionStore store = TransactionExtensionStore.getInstance();
    store.closeDB();
    store.initDB();
    store.putData(key, value);
    Assert.assertArrayEquals(store.getData(key), value);
  }

  @After
  public void close() {
    store.closeDB();
    FileUtils.deleteDirectoryContents(store.getDbPath().toFile());
  }
}