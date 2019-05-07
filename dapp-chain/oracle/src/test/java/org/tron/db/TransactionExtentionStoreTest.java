package org.tron.db;


import org.iq80.leveldb.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.tron.common.utils.ByteArray;

public class TransactionExtentionStoreTest {

  private static TransactionExtentionStore store;

  @Before
  public void init() {
    store = TransactionExtentionStore.getInstance();
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
    TransactionExtentionStore store = TransactionExtentionStore.getInstance();
    store.close();
    store.initDB();
    store.putData(key, value);
    Assert.assertArrayEquals(store.getData(key), value);
  }

  @After
  public void close() {
    store.close();
    FileUtils.deleteDirectoryContents(store.getDbPath().toFile());
  }
}