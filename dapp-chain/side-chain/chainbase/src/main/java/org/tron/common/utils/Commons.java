package org.tron.common.utils;

import static org.tron.common.utils.DecodeUtil.addressPreFixByte;
import static org.tron.common.utils.Hash.sha3omit12;
import static org.tron.core.Constant.ADD_PRE_FIX_BYTE_MAINNET;
import static org.tron.core.Constant.SUN_TOKEN_ID;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.math.ec.ECPoint;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.ExchangeCapsule;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.core.store.AccountStore;
import org.tron.core.store.AssetIssueStore;
import org.tron.core.store.AssetIssueV2Store;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.core.store.ExchangeStore;
import org.tron.core.store.ExchangeV2Store;

@Slf4j(topic = "Commons")
public class Commons {

  public static final int ADDRESS_SIZE = 42;
  public static final int ASSET_ISSUE_COUNT_LIMIT_MAX = 1000;

  public static byte[] clone(byte[] value) {
    byte[] clone = new byte[value.length];
    System.arraycopy(value, 0, clone, 0, value.length);
    return clone;
  }

  private static byte[] decode58Check(String input) {
    byte[] decodeCheck = Base58.decode(input);
    if (decodeCheck.length <= 4) {
      return null;
    }
    byte[] decodeData = new byte[decodeCheck.length - 4];
    System.arraycopy(decodeCheck, 0, decodeData, 0, decodeData.length);
    byte[] hash0 = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), decodeData);
    byte[] hash1 = Sha256Hash.hash(DBConfig.isECKeyCryptoEngine(), hash0);
    if (hash1[0] == decodeCheck[decodeData.length] &&
        hash1[1] == decodeCheck[decodeData.length + 1] &&
        hash1[2] == decodeCheck[decodeData.length + 2] &&
        hash1[3] == decodeCheck[decodeData.length + 3]) {
      return decodeData;
    }
    return null;
  }

  public static boolean addressValid(byte[] address) {
    if (ArrayUtils.isEmpty(address)) {
      logger.warn("Warning: Address is empty !!");
      return false;
    }
    if (address.length != ADDRESS_SIZE / 2) {
      logger.warn(
          "Warning: Address length need " + ADDRESS_SIZE + " but " + address.length
              + " !!");
      return false;
    }

    if (address[0] != addressPreFixByte) {
      logger.warn("Warning: Address need prefix with " + addressPreFixByte + " but "
          + address[0] + " !!");
      return false;
    }
    //Other rule;
    return true;
  }

  public static byte[] decodeFromBase58Check(String addressBase58) {
    if (StringUtils.isEmpty(addressBase58)) {
      logger.warn("Warning: Address is empty !!");
      return null;
    }
    byte[] address = decode58Check(addressBase58);
    if (address == null) {
      return null;
    }

    if (!addressValid(address)) {
      return null;
    }

    return address;
  }

  public static void adjustBalance(AccountStore accountStore, byte[] accountAddress, long amount)
      throws BalanceInsufficientException {
    AccountCapsule account = accountStore.getUnchecked(accountAddress);
    adjustBalance(accountStore, account, amount);
  }

  public static void adjustBalance(AccountStore accountStore, byte[] accountAddress, long amount, int chargingType)
          throws BalanceInsufficientException {
    AccountCapsule account = accountStore.getUnchecked(accountAddress);

    if (chargingType == 0) {
      adjustBalance(accountStore, account, amount);
    } else {
      adjustSunTokenBalance(accountStore, account, amount);
    }
  }

  public static void adjustBalance(AccountStore accountStore, AccountCapsule accountCapsule, long amount, int chargingType)
          throws BalanceInsufficientException {
    if (chargingType == 0) {
      adjustBalance(accountStore, accountCapsule, amount);
    } else {
      adjustSunTokenBalance(accountStore, accountCapsule, amount);
    }
  }

  /**
   * judge balance.
   */
  public void adjustBalance(AccountCapsule account, AccountStore accountStore, long amount)
          throws BalanceInsufficientException {

    long balance = account.getBalance();
    if (amount == 0) {
      return;
    }

    if (amount < 0 && balance < -amount) {
      throw new BalanceInsufficientException(
              StringUtil.createReadableString(account.createDbKey()) + " insufficient balance");
    }
    account.setBalance(Math.addExact(balance, amount));
    accountStore.put(account.getAddress().toByteArray(), account);
  }

  private static void adjustSunTokenBalance(AccountStore accountStore, AccountCapsule account, long amount) throws BalanceInsufficientException {

    long sunTokenBalance = account.getAssetMapV2().getOrDefault(SUN_TOKEN_ID, 0L);
    if (amount == 0) {
      return;
    }

    if (amount < 0 && sunTokenBalance < -amount) {
      throw new BalanceInsufficientException(
              createReadableString(account.createDbKey()) + " insufficient sun token balance");
    }
    account.addAssetAmountV2(SUN_TOKEN_ID.getBytes(), amount);
    accountStore.put(account.getAddress().toByteArray(), account);
  }

  public static String createReadableString(byte[] bytes) {
    return ByteArray.toHexString(bytes);
  }

  /**
   * judge balance.
   */
  public static void adjustBalance(AccountStore accountStore, AccountCapsule account, long amount)
      throws BalanceInsufficientException {

    long balance = account.getBalance();
    if (amount == 0) {
      return;
    }

    if (amount < 0 && balance < -amount) {
      throw new BalanceInsufficientException(
          createReadableString(account.createDbKey()) + " insufficient balance");
    }
    account.setBalance(Math.addExact(balance, amount));
    accountStore.put(account.getAddress().toByteArray(), account);
  }

  public static ExchangeStore getExchangeStoreFinal(DynamicPropertiesStore dynamicPropertiesStore,
      ExchangeStore exchangeStore,
      ExchangeV2Store exchangeV2Store) {
      return exchangeV2Store;
  }

  public static void putExchangeCapsule(ExchangeCapsule exchangeCapsule,
      DynamicPropertiesStore dynamicPropertiesStore, ExchangeStore exchangeStore,
      ExchangeV2Store exchangeV2Store, AssetIssueStore assetIssueStore) {
      exchangeV2Store.put(exchangeCapsule.createDbKey(), exchangeCapsule);
  }

  public static AssetIssueStore getAssetIssueStoreFinal(
      DynamicPropertiesStore dynamicPropertiesStore,
      AssetIssueStore assetIssueStore, AssetIssueV2Store assetIssueV2Store) {
      return assetIssueV2Store;
  }

  public static void adjustAssetBalanceV2(AccountCapsule account, String AssetID, long amount,
      AccountStore accountStore, AssetIssueStore assetIssueStore,
      DynamicPropertiesStore dynamicPropertiesStore)
      throws BalanceInsufficientException {
    if (amount < 0) {
      if (!account.reduceAssetAmountV2(AssetID.getBytes(), -amount)) {
        throw new BalanceInsufficientException("reduceAssetAmount failed !");
      }
    } else if (amount > 0 &&
        !account.addAssetAmountV2(AssetID.getBytes(), amount)) {
      throw new BalanceInsufficientException("addAssetAmount failed !");
    }
    accountStore.put(account.getAddress().toByteArray(), account);
  }

  public static void adjustTotalShieldedPoolValue(long valueBalance,
      DynamicPropertiesStore dynamicPropertiesStore) throws BalanceInsufficientException {
    long totalShieldedPoolValue = Math
        .subtractExact(dynamicPropertiesStore.getTotalShieldedPoolValue(), valueBalance);
    if (totalShieldedPoolValue < 0) {
      throw new BalanceInsufficientException("Total shielded pool value can not below 0");
    }
    dynamicPropertiesStore.saveTotalShieldedPoolValue(totalShieldedPoolValue);
  }

  public static void adjustAssetBalanceV2(byte[] accountAddress, String AssetID, long amount
      , AccountStore accountStore, AssetIssueStore assetIssueStore,
      DynamicPropertiesStore dynamicPropertiesStore)
      throws BalanceInsufficientException {
    AccountCapsule account = accountStore.getUnchecked(accountAddress);
    adjustAssetBalanceV2(account, AssetID, amount, accountStore, assetIssueStore,
        dynamicPropertiesStore);
  }

  public static long adjustFund(DynamicPropertiesStore dynamicPropertiesStore, long num) {
    long fund = dynamicPropertiesStore.getFund();
    if (num == 0) {
      return 0;
    }

    if (num < 0 && fund < -num) {//if |num| > fund, return all of fund
      dynamicPropertiesStore.saveFund(0);
      return fund * (-1);
    }

    dynamicPropertiesStore.saveFund(fund + num);
    return num;
  }

  public static boolean isGatewayAddress(DynamicPropertiesStore dynamicPropertiesStore, byte[] ownerAddress) {
    List<byte[]> gatewayList = dynamicPropertiesStore.getSideChainGateWayList();

    for (byte[] gateway: gatewayList) {
      if (ByteUtil.equals(gateway, ownerAddress)) {
        return true;
      }
    }
    return false;
  }
}
