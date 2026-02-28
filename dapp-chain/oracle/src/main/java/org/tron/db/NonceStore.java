package org.tron.db;

import lombok.extern.slf4j.Slf4j;
import org.tron.common.config.SystemSetting;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Sidechain.NonceMsg;
import org.tron.protos.Sidechain.NonceMsg.NonceStatus;

@Slf4j(topic = "nonceStore")
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
      byte[] nonceByteInStore = database.get(key);
      if (nonceByteInStore == null) {
        database.put(key, nonceMsg.toByteArray());
        logger.info("putDataIfIdle firstly nonce = {}, status = {}, retryTimes = {}",
            ByteArray.toStr(key), nonceMsg.getStatus(), nonceMsg.getRetryTimes());
        return true;
      } else {
        NonceMsg nonceMsgInStore = NonceMsg.parseFrom(nonceByteInStore);
        if (nonceMsgInStore.getStatus() == NonceStatus.FAIL) {
          if (nonceMsg.getRetryTimes() == nonceMsgInStore.getRetryTimes() + 1 || nonceMsg.getRetryTimes() / SystemSetting.RETRY_TIMES_EPOCH_OFFSET
              == nonceMsgInStore.getRetryTimes() / SystemSetting.RETRY_TIMES_EPOCH_OFFSET + 1) {
            database.put(key, nonceMsg.toByteArray());
            logger.info("putDataIfIdle retry nonce = {}, status = {}, retryTimes = {}; InStore status = {}, retryTimes = {}",
                ByteArray.toStr(key), nonceMsg.getStatus(), nonceMsg.getRetryTimes(),
                nonceMsgInStore.getStatus(), nonceMsgInStore.getRetryTimes());
            return true;
          } else {
            logger.info("putDataIfIdle fail! nonce = {}, nonceMsg retry = {}, nonceInStore retry = {}",
                ByteArray.toStr(key), nonceMsg.getRetryTimes(), nonceMsgInStore.getRetryTimes());
          }
        }
      }
      return false;
    } catch (Exception e) {
      logger.info(e.getMessage(), e);
      return false;
    } finally {
      resetDbLock.readLock().unlock();
    }
  }
}
