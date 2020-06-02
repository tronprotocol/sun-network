package org.tron.core.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tron.core.capsule.StorageRowCapsule;
import org.tron.core.db.TronStoreWithRevoking;
import org.tron.core.db.accountstate.AccountStateCallBackUtils;

import java.util.Objects;

@Slf4j(topic = "DB")
@Component
public class StorageRowStore extends TronStoreWithRevoking<StorageRowCapsule> {

  @Autowired
  private StorageRowStore(@Value("storage-row") String dbName) {
    super(dbName);
  }

  @Autowired
  private AccountStateCallBackUtils accountStateCallBackUtils;


  @Override
  public StorageRowCapsule get(byte[] key) {
    StorageRowCapsule row = getUnchecked(key);
    row.setRowKey(key);
    return row;
  }

  @Override
  public void put(byte[] key, StorageRowCapsule item) {
    super.put(key, item);
    if (Objects.isNull(key) || Objects.isNull(item)) {
      return;
    }
    accountStateCallBackUtils.storageCallBack(key, item.getData());
  }

  @Override
  public void delete(byte[] key) {
    super.delete(key);
    if (Objects.isNull(key)) {
      return;
    }
    accountStateCallBackUtils.storageCallBack(key, new byte[]{});
  }
}
