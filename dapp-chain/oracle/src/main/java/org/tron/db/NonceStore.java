package org.tron.db;

import lombok.extern.slf4j.Slf4j;
import org.iq80.leveldb.DBException;
import org.tron.common.config.SystemSetting;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;

@Slf4j
public class NonceStore extends OracleStore {

  private static NonceStore instance = new NonceStore();

  public static NonceStore getInstance() {
    return instance;
  }

  private NonceStore() {
    this.dataBaseName = "nonce";
    this.parentName = "database";
    initDB();
  }

  public boolean putDataIfIdle(byte[] key, NonceMsg nonceMsg) {
    // if new nonce or fail
    resetDbLock.readLock().lock();
    try {
      NonceMsg nonceMsgInStore = NonceMsg.parseFrom(database.get(key));
      if (nonceMsgInStore == null) {
        database.put(key, nonceMsg.toByteArray());
        return true;
      } else if (nonceMsgInStore.getStatus() == NonceStatus.FAIL) {
        if (nonceMsg.getRetryTimes() == nonceMsgInStore.getRetryTimes() + 1 || nonceMsg.getRetryTimes() / SystemSetting.RETRY_TIMES_OFFSET
            == nonceMsgInStore.getRetryTimes() / SystemSetting.RETRY_TIMES_OFFSET + 1) {
          database.put(key, nonceMsg.toByteArray());
          return true;
        }
      }
      return false;
    } catch (Exception e) {
      logger.debug(e.getMessage(), e);
      return false;
    } finally {
      resetDbLock.readLock().unlock();
    }
  }
}
