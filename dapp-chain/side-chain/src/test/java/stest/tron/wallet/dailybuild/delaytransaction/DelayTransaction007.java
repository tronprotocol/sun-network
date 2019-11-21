package stest.tron.wallet.dailybuild.delaytransaction;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.SmartContract;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class DelayTransaction007 {

  private static final long now = System.currentTimeMillis();
  private static final long totalSupply = now;
  private static final String name = "Asset008_" + Long.toString(now);
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final byte[] toAddress = PublicMethedForDailybuild.getFinalAddress(testKey003);
  String description = "just-test";
  String url = "https://github.com/tronprotocol/wallet-cli/";
  Long delaySecond = 10L;
  ByteString assetId;
  SmartContract smartContract;
  ECKey ecKey = new ECKey(Utils.getRandom());
  byte[] doCreateAccountAddress = ecKey.getAddress();
  String doCreateAccountKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] newAccountAddress = ecKey1.getAddress();
  String newAccountKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);
  private Long delayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.delayTransactionFee");
  private Long cancleDelayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.cancleDelayTransactionFee");
  private byte[] contractAddress = null;

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass(enabled = false)
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = false, description = "Delay account create contract")
  public void test1DelayAccountCreate() {
    //get account
    ecKey = new ECKey(Utils.getRandom());
    doCreateAccountAddress = ecKey.getAddress();
    doCreateAccountKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(doCreateAccountKey);

    ecKey1 = new ECKey(Utils.getRandom());
    newAccountAddress = ecKey1.getAddress();
    newAccountKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(newAccountKey);

    Assert.assertTrue(
        PublicMethedForDailybuild.sendcoin(doCreateAccountAddress, 1000000L, fromAddress,
            testKey002, blockingStubFull));

    Long beforeCreateAccountBalance = PublicMethedForDailybuild.queryAccount(doCreateAccountKey,
        blockingStubFull).getBalance();
    final String txid = PublicMethedForDailybuild.createAccountDelayGetTxid(doCreateAccountAddress,
        newAccountAddress, delaySecond, doCreateAccountKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild.queryAccount(newAccountAddress, blockingStubFull)
        .getCreateTime() == 0);
    Long balanceInDelay = PublicMethedForDailybuild
        .queryAccount(doCreateAccountKey, blockingStubFull)
        .getBalance();
    Assert.assertTrue(beforeCreateAccountBalance - balanceInDelay == delayTransactionFee);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    logger.info("create time is " + PublicMethedForDailybuild
        .queryAccount(newAccountAddress, blockingStubFull)
        .getCreateTime());
    Assert.assertTrue(PublicMethedForDailybuild.queryAccount(newAccountAddress, blockingStubFull)
        .getCreateTime() > 0);
    Long afterCreateAccountBalance = PublicMethedForDailybuild
        .queryAccount(doCreateAccountKey, blockingStubFull)
        .getBalance();
    Long netFee = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull).get()
        .getReceipt()
        .getNetFee();
    Long fee = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull).get()
        .getFee();
    Assert.assertTrue(fee - netFee == delayTransactionFee);
    Assert.assertTrue(beforeCreateAccountBalance - afterCreateAccountBalance
        == delayTransactionFee + 100000L);

  }

  @Test(enabled = false, description = "Cancel delay account create contract")
  public void test2CancelDelayUpdateSetting() {
    ecKey1 = new ECKey(Utils.getRandom());
    newAccountAddress = ecKey1.getAddress();
    newAccountKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(newAccountKey);

    final Long beforeCreateBalance = PublicMethedForDailybuild
        .queryAccount(doCreateAccountKey, blockingStubFull)
        .getBalance();
    final String txid = PublicMethedForDailybuild.createAccountDelayGetTxid(doCreateAccountAddress,
        newAccountAddress, delaySecond, doCreateAccountKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Assert.assertFalse(
        PublicMethedForDailybuild.cancelDeferredTransactionById(txid, fromAddress, testKey002,
            blockingStubFull));
    final String cancelTxid = PublicMethedForDailybuild.cancelDeferredTransactionByIdGetTxid(txid,
        doCreateAccountAddress, doCreateAccountKey, blockingStubFull);
    Assert.assertFalse(
        PublicMethedForDailybuild.cancelDeferredTransactionById(txid, doCreateAccountAddress,
            doCreateAccountKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    final Long afterCreateBalance = PublicMethedForDailybuild
        .queryAccount(doCreateAccountKey, blockingStubFull)
        .getBalance();
    final Long netFee = PublicMethedForDailybuild
        .getTransactionInfoById(cancelTxid, blockingStubFull).get()
        .getReceipt().getNetFee();
    final Long fee = PublicMethedForDailybuild.getTransactionInfoById(cancelTxid, blockingStubFull)
        .get()
        .getFee();
    logger.info("net fee : " + PublicMethedForDailybuild
        .getTransactionInfoById(cancelTxid, blockingStubFull)
        .get().getReceipt().getNetFee());
    logger.info(
        "Fee : " + PublicMethedForDailybuild.getTransactionInfoById(cancelTxid, blockingStubFull)
            .get().getFee());

    Assert.assertTrue(fee - netFee == cancleDelayTransactionFee + delayTransactionFee);
    Assert.assertTrue(beforeCreateBalance - afterCreateBalance
        == cancleDelayTransactionFee + delayTransactionFee);

  }


  /**
   * constructor.
   */

  @AfterClass(enabled = false)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


