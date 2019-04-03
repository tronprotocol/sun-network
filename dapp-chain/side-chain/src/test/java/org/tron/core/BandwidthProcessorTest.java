package org.tron.core;

import com.google.protobuf.ByteString;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tron.common.application.TronApplicationContext;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.FileUtil;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.BandwidthProcessor;
import org.tron.core.db.Manager;
import org.tron.core.db.TransactionTrace;
import org.tron.core.exception.AccountResourceInsufficientException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.TooBigTransactionResultException;
import org.tron.protos.Contract;
import org.tron.protos.Protocol.AccountType;

@Slf4j
public class BandwidthProcessorTest {

  private static Manager dbManager;
  private static final String dbPath = "output_bandwidth_test";
  private static TronApplicationContext context;
  private static final String ASSET_NAME;
  private static final String ASSET_NAME_V2;
  private static final String OWNER_ADDRESS;
  private static final String ASSET_ADDRESS;
  private static final String ASSET_ADDRESS_V2;
  private static final String TO_ADDRESS;
  private static final long TOTAL_SUPPLY = 10000000000000L;
  private static final int TRX_NUM = 2;
  private static final int NUM = 2147483647;
  private static final int VOTE_SCORE = 2;
  private static final String DESCRIPTION = "TRX";
  private static final String URL = "https://tron.network";
  private static long START_TIME;
  private static long END_TIME;


  static {
    Args.setParam(new String[]{"--output-directory", dbPath}, Constant.TEST_CONF);
    context = new TronApplicationContext(DefaultConfig.class);
    ASSET_NAME = "1";
    ASSET_NAME_V2 = "2";
    OWNER_ADDRESS = Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a1abc";
    TO_ADDRESS = Wallet.getAddressPreFixString() + "abd4b9367799eaa3197fecb144eb71de1e049abc";
    ASSET_ADDRESS = Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a3456";
    ASSET_ADDRESS_V2 = Wallet.getAddressPreFixString() + "548794500882809695a8a687866e76d4271a7890";
    START_TIME = DateTime.now().minusDays(1).getMillis();
    END_TIME = DateTime.now().getMillis();
  }

  /**
   * Init data.
   */
  @BeforeClass
  public static void init() {
    dbManager = context.getBean(Manager.class);
  }

  /**
   * Release resources.
   */
  @AfterClass
  public static void destroy() {
    Args.clearParam();
    context.destroy();
    if (FileUtil.deleteDir(new File(dbPath))) {
      logger.info("Release resources successful.");
    } else {
      logger.info("Release resources failure.");
    }
  }

  /**
   * create temp Capsule test need.
   */
  @Before
  public void createCapsule() {

    AccountCapsule ownerCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8("owner"),
            ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)),
            AccountType.Normal,
            0L);

    AccountCapsule toAccountCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8("toAccount"),
            ByteString.copyFrom(ByteArray.fromHexString(TO_ADDRESS)),
            AccountType.Normal,
            0L);

    AccountCapsule assetCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8("asset"),
            ByteString.copyFrom(ByteArray.fromHexString(ASSET_ADDRESS)),
            AccountType.Normal,
            dbManager.getDynamicPropertiesStore().getAssetIssueFee());

    AccountCapsule assetCapsule2 =
        new AccountCapsule(
            ByteString.copyFromUtf8("asset2"),
            ByteString.copyFrom(ByteArray.fromHexString(ASSET_ADDRESS_V2)),
            AccountType.Normal,
            dbManager.getDynamicPropertiesStore().getAssetIssueFee());

    dbManager.getAccountStore().reset();
    dbManager.getAccountStore().put(ownerCapsule.getAddress().toByteArray(), ownerCapsule);
    dbManager.getAccountStore().put(toAccountCapsule.getAddress().toByteArray(), toAccountCapsule);
    dbManager.getAccountStore().put(assetCapsule.getAddress().toByteArray(), assetCapsule);
    dbManager.getAccountStore().put(assetCapsule2.getAddress().toByteArray(), assetCapsule2);

  }

  /**
   * sameTokenName close, consume success contract.getType() = TransferContract toAddressAccount
   * isn't exist.
   */
  @Test
  public void sameTokenNameCloseTransferToAccountNotExist() {
//    dbManager.getDynamicPropertiesStore().saveAllowSameTokenName(0);
    dbManager.getDynamicPropertiesStore().saveTotalNetWeight(10_000_000L);

    AccountCapsule ownerCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8("owner"),
            ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)),
            AccountType.Normal,
            dbManager.getDynamicPropertiesStore().getAssetIssueFee());
    ownerCapsule.setBalance(10_000_000L);
    long expireTime = DateTime.now().getMillis() + 6 * 86_400_000;
    ownerCapsule.setFrozenForBandwidth(2_000_000L, expireTime);
    dbManager.getAccountStore().put(ownerCapsule.getAddress().toByteArray(), ownerCapsule);

    AccountCapsule toAddressCapsule =
        new AccountCapsule(
            ByteString.copyFromUtf8("owner"),
            ByteString.copyFrom(ByteArray.fromHexString(TO_ADDRESS)),
            AccountType.Normal,
            dbManager.getDynamicPropertiesStore().getAssetIssueFee());
    toAddressCapsule.setBalance(10_000_000L);
    long expireTime2 = DateTime.now().getMillis() + 6 * 86_400_000;
    toAddressCapsule.setFrozenForBandwidth(2_000_000L, expireTime2);
    dbManager.getAccountStore().delete(toAddressCapsule.getAddress().toByteArray());

    Contract.TransferContract contract = Contract.TransferContract.newBuilder()
        .setOwnerAddress(ByteString.copyFrom(ByteArray.fromHexString(OWNER_ADDRESS)))
        .setToAddress(ByteString.copyFrom(ByteArray.fromHexString(TO_ADDRESS)))
        .setAmount(100L)
        .build();

    TransactionCapsule trx = new TransactionCapsule(contract, dbManager.getAccountStore());
    TransactionTrace trace = new TransactionTrace(trx, dbManager);

    long byteSize = trx.getInstance().toBuilder().clearRet().build().getSerializedSize() +
        Constant.MAX_RESULT_SIZE_IN_TX;

    BandwidthProcessor processor = new BandwidthProcessor(dbManager);

    try {
      processor.consume(trx, trace);

      Assert.assertEquals(trace.getReceipt().getNetEnergyCost(), 0);
      Assert.assertEquals(trace.getReceipt().getNetUsage(), byteSize);
      AccountCapsule fromAccount =
          dbManager.getAccountStore().get(ByteArray.fromHexString(OWNER_ADDRESS));
      Assert.assertNotNull(fromAccount);
      Assert.assertEquals(fromAccount.getNetUsage(), byteSize);
    } catch (ContractValidateException e) {
      Assert.assertFalse(e instanceof ContractValidateException);
    } catch (TooBigTransactionResultException e) {
      Assert.assertFalse(e instanceof TooBigTransactionResultException);
    } catch (AccountResourceInsufficientException e) {
      Assert.assertFalse(e instanceof AccountResourceInsufficientException);
    } finally {
      dbManager.getAccountStore().delete(ByteArray.fromHexString(OWNER_ADDRESS));
      dbManager.getAccountStore().delete(ByteArray.fromHexString(TO_ADDRESS));
    }
  }
}
