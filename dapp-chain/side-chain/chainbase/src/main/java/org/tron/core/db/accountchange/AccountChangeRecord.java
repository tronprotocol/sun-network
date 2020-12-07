package org.tron.core.db.accountchange;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.tron.common.utils.WalletUtil;
import org.tron.core.capsule.AccountCapsule;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AccountChangeRecord {

  private static volatile boolean recordBalance = false;

  private static final int DELETE = 2;
  private static final int CREATE = 1;
  private static final int UPDATE = 3;

  // not byte[]! because the same byte[] but not hashCode.
  private static Map<String, AccountInfo> tempAccountMap = new HashMap<>();

  public void startRecord(boolean record) {
    this.recordBalance = record;
  }

  public void clear() {
    this.recordBalance = false;
    tempAccountMap.clear();
  }

  public Map<String, AccountInfo> getTempAccountMap() {
    return Maps.newHashMap(tempAccountMap);
  }

  public void recordChangedAccount(byte[] key, AccountCapsule oldAccount, AccountCapsule newAccount) {
    if (!recordBalance || newAccount == null) {
      return;
    }

    AccountInfo accountInfo = null;
    try {
      if (oldAccount == null) {
        accountInfo = AccountInfo.of(newAccount);
        accountInfo.getActions().add(CREATE);
      }
      else {
        accountInfo = AccountInfo.of(oldAccount, newAccount);

        if (accountInfo == null) {
          return;
        }

        accountInfo.getActions().add(UPDATE);
      }
    }
    catch (Exception ex) {
      logger.error("", ex);
      return;
    }

    // if null, means balance not change, return.
    if (accountInfo == null) {
      return;
    }

    merge(key, accountInfo);
  }

  private static final String errorMsg = " delete account but balance is not 0";

  public void delete(byte[] key, AccountCapsule oldAccount) {
    if (!recordBalance) {
      return;
    }

    // check balance == 0; 否则报错
    Assert.isTrue(oldAccount.getBalance() == 0, errorMsg);
    Assert.isTrue(oldAccount.getFrozenBalance() == 0, errorMsg);
    Assert.isTrue(oldAccount.getEnergyFrozenBalance() == 0, errorMsg);
    Assert.isTrue(oldAccount.getDelegatedFrozenBalanceForEnergy() == 0, errorMsg);
    Assert.isTrue(oldAccount.getDelegatedFrozenBalanceForBandwidth() == 0, errorMsg);
    Assert.isTrue(oldAccount.getFrozenSupplyBalance() == 0, errorMsg);
    Assert.isTrue(oldAccount.getAcquiredDelegatedFrozenBalanceForEnergy() == 0, errorMsg);
    Assert.isTrue(oldAccount.getAcquiredDelegatedFrozenBalanceForBandwidth() == 0, errorMsg);

    AccountInfo accountInfo = AccountInfo.of(oldAccount);
    accountInfo.getActions().add(DELETE);
    merge(key, accountInfo);
  }

  private void merge(byte[] key, AccountInfo accountInfo) {
    final String keyString = Hex.toHexString(key);
    final AccountInfo inMapInfo = tempAccountMap.get(keyString);

    if (inMapInfo == null) {
      tempAccountMap.put(keyString, accountInfo);
      return;
    }

    try {
      mergeInfo(inMapInfo, accountInfo);
    }
    catch (Exception ex) {
      logger.error("", ex);
    }
  }

  private void mergeInfo(AccountInfo inMapInfo, AccountInfo addInfo) {
    //  merge：update balance， add incrementBalance.
    AccountInfo.setBalance(inMapInfo, addInfo);
    inMapInfo.getActions().addAll(addInfo.getActions());

    inMapInfo.setIncrementBalance(inMapInfo.getIncrementBalance() + addInfo.getIncrementBalance());
    inMapInfo.setIncrementFrozenBalance(inMapInfo.getIncrementFrozenBalance() + addInfo.getIncrementFrozenBalance());
    inMapInfo.setIncrementEnergyFrozenBalance(inMapInfo.getIncrementEnergyFrozenBalance() + addInfo.getIncrementEnergyFrozenBalance());
    inMapInfo.setIncrementDelegatedFrozenBalanceForEnergy(inMapInfo.getIncrementDelegatedFrozenBalanceForEnergy() + addInfo.getIncrementDelegatedFrozenBalanceForEnergy());
    inMapInfo.setIncrementDelegatedFrozenBalanceForBandwidth(inMapInfo.getIncrementDelegatedFrozenBalanceForBandwidth() + addInfo.getIncrementDelegatedFrozenBalanceForBandwidth());
    inMapInfo.setIncrementFrozenSupplyBalance(inMapInfo.getFrozenSupplyBalance() + addInfo.getFrozenSupplyBalance());
    inMapInfo.setIncrementAcquiredDelegatedFrozenBalanceForEnergy(inMapInfo.getAcquiredDelegatedFrozenBalanceForEnergy() + addInfo.getAcquiredDelegatedFrozenBalanceForEnergy());
    inMapInfo.setIncrementAcquiredDelegatedFrozenBalanceForBandwidth(inMapInfo.getAcquiredDelegatedFrozenBalanceForBandwidth() + addInfo.getAcquiredDelegatedFrozenBalanceForBandwidth());

    Map<String, Trc10Info> trc10Map = inMapInfo.getTrc10Map();
    Map<String, Trc10Info> addMap = addInfo.getTrc10Map();

    if (trc10Map == null) {
      trc10Map = new HashMap<>();
      inMapInfo.setTrc10Map(trc10Map);
    }

    if (addMap == null) {
      return;
    }

    trc10Map.forEach((tokenId, info) -> {
      final Trc10Info addTrc10Info = addMap.get(tokenId);
      if (addTrc10Info == null) {
        // is null 表示10币没有变动， 所以不需要merge
//        info.setIncrementBalance(-info.getBalance());
//        info.setBalance(0);
        return;
      }

      info.setBalance(addTrc10Info.getBalance());
      info.setIncrementBalance(info.getIncrementBalance() + addTrc10Info.getIncrementBalance());
      addMap.remove(tokenId);
    });

    if (addMap != null && addMap.size() > 0) {
      trc10Map.putAll(addMap);
    }
  }


  @Data
  public static class AccountInfo {
    private String accountAddress;
    // 2=delete; 1=add; 0=update;
    private List<Integer> actions = new LinkedList<>();

    private long balance;
    private long frozenBalance;
    private long energyFrozenBalance;
    private long delegatedFrozenBalanceForEnergy;
    private long delegatedFrozenBalanceForBandwidth;
    private long frozenSupplyBalance;
    private long acquiredDelegatedFrozenBalanceForEnergy;
    private long acquiredDelegatedFrozenBalanceForBandwidth;

    private long incrementBalance;
    private long incrementFrozenBalance;
    private long incrementEnergyFrozenBalance;
    private long incrementDelegatedFrozenBalanceForEnergy;
    private long incrementDelegatedFrozenBalanceForBandwidth;
    private long incrementFrozenSupplyBalance;
    private long incrementAcquiredDelegatedFrozenBalanceForEnergy;
    private long incrementAcquiredDelegatedFrozenBalanceForBandwidth;

    private Map<String, Trc10Info> trc10Map;

    public static AccountInfo of(AccountCapsule account) {
      AccountInfo info = new AccountInfo();
//      final String address = WalletUtil.encode58Check(account.getAddress().toByteArray());
      final String address = WalletUtil.encode58Check(account.getAddress().toByteArray());
      info.setAccountAddress(address);

      setBalance(info, account);

      info.setIncrementBalance(account.getBalance());
      info.setIncrementFrozenBalance(account.getFrozenBalance());
      info.setIncrementEnergyFrozenBalance(account.getEnergyFrozenBalance());
      info.setIncrementDelegatedFrozenBalanceForEnergy(account.getDelegatedFrozenBalanceForEnergy());
      info.setIncrementDelegatedFrozenBalanceForBandwidth(account.getDelegatedFrozenBalanceForBandwidth());
      info.setIncrementFrozenSupplyBalance(account.getFrozenSupplyBalance());
      info.setIncrementAcquiredDelegatedFrozenBalanceForEnergy(account.getAcquiredDelegatedFrozenBalanceForEnergy());
      info.setIncrementAcquiredDelegatedFrozenBalanceForBandwidth(account.getAcquiredDelegatedFrozenBalanceForBandwidth());

      info.setTrc10Map(Trc10Info.of(account.getAssetMapV2(), true));
      return info;
    }

    // 检查余额是否有变动，没有变动 return null.
    public static AccountInfo of(AccountCapsule oldAccount, AccountCapsule newAccount) {
      AccountInfo info = new AccountInfo();
//      final String address = WalletUtil.encode58Check(newAccount.getAddress().toByteArray());
      final String address = WalletUtil.encode58Check(newAccount.getAddress().toByteArray());
      info.setAccountAddress(address);

      setBalance(info, newAccount);

      info.setIncrementBalance(newAccount.getBalance() - oldAccount.getBalance());
      info.setIncrementFrozenBalance(newAccount.getFrozenBalance() - oldAccount.getFrozenBalance());
      info.setIncrementEnergyFrozenBalance(newAccount.getEnergyFrozenBalance() - oldAccount.getEnergyFrozenBalance());
      info.setIncrementDelegatedFrozenBalanceForEnergy(newAccount.getDelegatedFrozenBalanceForEnergy() - oldAccount.getDelegatedFrozenBalanceForEnergy());
      info.setIncrementDelegatedFrozenBalanceForBandwidth(newAccount.getDelegatedFrozenBalanceForBandwidth() - oldAccount.getDelegatedFrozenBalanceForBandwidth());
      info.setIncrementFrozenSupplyBalance(newAccount.getFrozenSupplyBalance() - oldAccount.getFrozenSupplyBalance());
      info.setIncrementAcquiredDelegatedFrozenBalanceForEnergy(newAccount.getAcquiredDelegatedFrozenBalanceForEnergy() - oldAccount.getAcquiredDelegatedFrozenBalanceForEnergy());
      info.setIncrementAcquiredDelegatedFrozenBalanceForBandwidth(newAccount.getAcquiredDelegatedFrozenBalanceForBandwidth() - oldAccount.getAcquiredDelegatedFrozenBalanceForBandwidth());

      info.setTrc10Map(Trc10Info.of(oldAccount.getAssetMapV2(), newAccount.getAssetMapV2()));

      // 检查余额是否有变动，没有变动 return null.
      if (info.getIncrementBalance() == 0
          && info.getIncrementFrozenBalance() == 0
          && info.getIncrementEnergyFrozenBalance() == 0
          && info.getIncrementDelegatedFrozenBalanceForEnergy() == 0
          && info.getIncrementDelegatedFrozenBalanceForBandwidth() == 0
          && info.getIncrementFrozenSupplyBalance() == 0
          && info.getIncrementAcquiredDelegatedFrozenBalanceForEnergy() == 0
          && info.getIncrementAcquiredDelegatedFrozenBalanceForBandwidth() == 0
          && info.getTrc10Map() == null) {
        return null;
      }

      return info;
    }

    public static void setBalance(AccountInfo info, AccountCapsule account) {
      info.setBalance(account.getBalance());
      info.setFrozenBalance(account.getFrozenBalance());
      info.setEnergyFrozenBalance(account.getEnergyFrozenBalance());
      info.setDelegatedFrozenBalanceForEnergy(account.getDelegatedFrozenBalanceForEnergy());
      info.setDelegatedFrozenBalanceForBandwidth(account.getDelegatedFrozenBalanceForBandwidth());
      info.setFrozenSupplyBalance(account.getFrozenSupplyBalance());
      info.setAcquiredDelegatedFrozenBalanceForEnergy(account.getAcquiredDelegatedFrozenBalanceForEnergy());
      info.setAcquiredDelegatedFrozenBalanceForBandwidth(account.getAcquiredDelegatedFrozenBalanceForBandwidth());
    }

    public static void setBalance(AccountInfo info, AccountInfo account) {
      info.setBalance(account.getBalance());
      info.setFrozenBalance(account.getFrozenBalance());
      info.setEnergyFrozenBalance(account.getEnergyFrozenBalance());
      info.setDelegatedFrozenBalanceForEnergy(account.getDelegatedFrozenBalanceForEnergy());
      info.setDelegatedFrozenBalanceForBandwidth(account.getDelegatedFrozenBalanceForBandwidth());
      info.setFrozenSupplyBalance(account.getFrozenSupplyBalance());
      info.setAcquiredDelegatedFrozenBalanceForEnergy(account.getAcquiredDelegatedFrozenBalanceForEnergy());
      info.setAcquiredDelegatedFrozenBalanceForBandwidth(account.getAcquiredDelegatedFrozenBalanceForBandwidth());
    }
  }


  @Data
  public static class Trc10Info {
    private String tokenId;
    private long balance;
    private long incrementBalance;

    public static Map<String, Trc10Info> of(Map<String, Long> assetMapV2, boolean isNew) {
      Map<String, Trc10Info> trc10Map = new HashMap<>();
      if (CollectionUtils.isEmpty(assetMapV2)) {
        return trc10Map;
      }

      assetMapV2.forEach((key, val) -> {
        Trc10Info trc10Info = null;
        if (isNew) {
          trc10Info = of(key, val);
        }
        else {
          trc10Info = of(key, val, 0);
        }

        if (trc10Info != null) {
          trc10Map.put(key, trc10Info);
        }
      });

      return trc10Map;
    }

    // if not change return null;
    public static Map<String, Trc10Info> of(Map<String, Long> oldAssetMapV2, Map<String, Long> newAssetMapV2) {
      final boolean oldEmpty = CollectionUtils.isEmpty(oldAssetMapV2);
      final boolean newEmpty = CollectionUtils.isEmpty(newAssetMapV2);

      // trc10 没有修改的， return null
      if (oldEmpty && newEmpty) {
        return null;
      }

      if (oldEmpty) {
        return of(newAssetMapV2, true);
      }

      if (newEmpty) {
        return of(oldAssetMapV2, false);
      }

      Map<String, Trc10Info> trc10Map = new HashMap<>();
      newAssetMapV2.forEach((key, val) -> {
        Long oldVal = oldAssetMapV2.get(key);

        if (oldVal == null) {
          oldVal = 0L;
        }

        final Trc10Info trc10Info = of(key, oldVal, val);

        if (trc10Info != null) {
          trc10Map.put(key, trc10Info);
        }
      });

      oldAssetMapV2.forEach((key, oldVal) -> {
        if (newAssetMapV2.containsKey(key)) {
          return;
        }

        final Trc10Info trc10Info = of(key, oldVal, 0);

        if (trc10Info != null) {
          trc10Map.put(key, trc10Info);
        }
      });

      // trc10 没有修改的， return null
      if (CollectionUtils.isEmpty(trc10Map)) {
        return null;
      }

      return trc10Map;
    }

    private static Trc10Info of(String tokenId, long val) {
      Trc10Info info = new Trc10Info();
      info.setTokenId(tokenId);
      info.setBalance(val);
      info.setIncrementBalance(val);
      return info;
    }

    private static Trc10Info of(String tokenId, long oldVal, long val) {
      Trc10Info info = new Trc10Info();
      info.setTokenId(tokenId);
      info.setBalance(val);
      info.setIncrementBalance(val - oldVal);

      // not change
      if (info.getIncrementBalance() == 0) {
        return null;
      }
      return info;
    }
  }

}
