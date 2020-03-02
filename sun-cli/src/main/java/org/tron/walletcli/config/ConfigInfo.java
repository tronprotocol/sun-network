package org.tron.walletcli.config;

import com.typesafe.config.Config;
import org.tron.core.config.Configuration;


public class ConfigInfo {


  public static Config config = Configuration.getByPath("config.conf");

  public static final Long interval = config.getLong("sidechain.interval");

  public static final String privateKeyAddressFile = config
      .getString("sidechain.private.key.address.file");

  public static final String privateKey = config.getString("sidechain.basic.private.key");

  public static final Long basicFreezeBalance = config.getLong("sidechain.basic.freeze.balance");

  public static final Long accountFreezeBalance = config
      .getLong("sidechain.account.freeze.balance");

  public static final String contractAddress = config.getString("sidechain.contract.address");

  public static final String contractDeposit = config.getString("sidechain.contract.deposit.func");

  public static final Long contractDepositValue = config
      .getLong("sidechain.contract.deposit.value");

  public static final String contractWithdraw = config
      .getString("sidechain.contract.withdraw.func");

  public static final Long accountNum = config.getLong("sidechain.account.num");
  public static final Long accountAddNum = config.getLong("hour.account.add.num");


}
