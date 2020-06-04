package org.tron.core.db.accountstate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.tron.core.capsule.AccountCapsule;

@Slf4j(topic = "AccountState")
public class AccountStateCallBackUtils {

  protected volatile boolean execute = false;
  protected volatile boolean allowGenerateRoot = false;
  protected List<TrieEntry> trieEntryList = new ArrayList<>();
  protected List<TrieEntry> storageList = new ArrayList<>();

  public void accountCallBack(byte[] key, AccountCapsule item) {
    if (!exe()) {
      return;
    }
    if (item == null) {
      return;
    }
    logger.info("forTest150 [account trieEntry] key is {}, value is {}", Arrays.toString(key), item.getInstance());
    trieEntryList
        .add(TrieEntry.build(key, new AccountStateEntity(item.getInstance()).toByteArrays()));
  }

  public void storageCallBack(byte[] key, byte[] value) {
    if (!exe()) {
      return;
    }
    if (value == null) {
      return;
    }
    logger.info("forTest150 [storage trieEntry] key is {}, value is {}", Arrays.toString(key), Arrays.toString(value));
    storageList
            .add(TrieEntry.build(key, value));
  }

  protected boolean exe() {
    if (!execute || !allowGenerateRoot) {
      //Agreement same block high to generate account state root
      execute = false;
      return false;
    }
    return true;
  }

  public static class TrieEntry {

    private byte[] key;
    private byte[] data;

    public static TrieEntry build(byte[] key, byte[] data) {
      TrieEntry trieEntry = new TrieEntry();
      return trieEntry.setKey(key).setData(data);
    }

    public byte[] getKey() {
      return key;
    }

    public TrieEntry setKey(byte[] key) {
      this.key = key;
      return this;
    }

    public byte[] getData() {
      return data;
    }

    public TrieEntry setData(byte[] data) {
      this.data = data;
      return this;
    }
  }

}
