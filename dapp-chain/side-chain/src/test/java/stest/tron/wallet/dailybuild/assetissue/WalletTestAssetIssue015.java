package stest.tron.wallet.dailybuild.assetissue;

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
import org.tron.api.GrpcAPI.AccountNetMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class WalletTestAssetIssue015 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final byte[] toAddress = PublicMethedForDailybuild.getFinalAddress(testKey003);

  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");
  private static ByteString assetAccountId = null;
  private static final long now = System.currentTimeMillis();
  private static String name = "AssetIssue015_" + Long.toString(now);
  private static final long totalSupply = now;
  private static final long sendAmount = 10000000000L;
  private static final long netCostMeasure = 200L;

  Long freeAssetNetLimit = 30000L;
  Long publicFreeAssetNetLimit = 30000L;
  String description = "for case assetissue015";
  String url = "https://stest.assetissue015.url";

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);


  //get account
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] asset015Address = ecKey1.getAddress();
  String testKeyForAssetIssue015 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());


  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] transferAssetAddress = ecKey2.getAddress();
  String transferAssetCreateKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

  ECKey ecKey3 = new ECKey(Utils.getRandom());
  byte[] newAddress = ecKey3.getAddress();
  String testKeyForNewAddress = ByteArray.toHexString(ecKey3.getPrivKeyBytes());

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass(enabled = true)
  public void beforeClass() {
    logger.info(testKeyForAssetIssue015);
    logger.info(transferAssetCreateKey);
    logger.info(testKeyForNewAddress);

    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(asset015Address, sendAmount, fromAddress, testKey002, blockingStubFull));
    assetAccountId = ByteString.copyFromUtf8(tokenId);
    org.junit.Assert
        .assertTrue(PublicMethedForDailybuild.transferAsset(asset015Address, assetAccountId.toByteArray(),
            10000000L, tokenOnwerAddress, tokenOwnerKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true, description = "Use transfer net when token owner has not enough bandwidth")
  public void atestWhenCreatorHasNoEnoughBandwidthUseTransferNet() {


    //Transfer asset to an account.
    Assert.assertTrue(PublicMethedForDailybuild
        .transferAsset(transferAssetAddress, assetAccountId.toByteArray(), 10000000L,
            asset015Address, testKeyForAssetIssue015, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    //Before use transfer net, query the net used from creator and transfer.
    AccountNetMessage assetCreatorNet = PublicMethedForDailybuild
        .getAccountNet(asset015Address, blockingStubFull);
    AccountNetMessage assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    Long creatorBeforeFreeNetUsed = assetCreatorNet.getFreeNetUsed();
    Long transferBeforeFreeNetUsed = assetTransferNet.getFreeNetUsed();
    logger.info(Long.toString(creatorBeforeFreeNetUsed));
    logger.info(Long.toString(transferBeforeFreeNetUsed));

    //Transfer send some asset issue to default account, to test if this
    // transaction use the transaction free net.
    Assert.assertTrue(PublicMethedForDailybuild.transferAsset(toAddress, assetAccountId.toByteArray(), 1L,
        transferAssetAddress, transferAssetCreateKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    assetCreatorNet = PublicMethedForDailybuild
        .getAccountNet(asset015Address, blockingStubFull);
    assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    Long creatorAfterFreeNetUsed = assetCreatorNet.getFreeNetUsed();
    Long transferAfterFreeNetUsed = assetTransferNet.getFreeNetUsed();
    logger.info(Long.toString(creatorAfterFreeNetUsed));
    logger.info(Long.toString(transferAfterFreeNetUsed));

    Assert.assertTrue(creatorAfterFreeNetUsed - creatorBeforeFreeNetUsed < netCostMeasure);
    Assert.assertTrue(transferAfterFreeNetUsed - transferBeforeFreeNetUsed > netCostMeasure);
  }

  @Test(enabled = true, description = "Use balance when transfer has not enough net")
  public void btestWhenTransferHasNoEnoughBandwidthUseBalance() {
    Integer i = 0;
    AccountNetMessage assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    while (assetTransferNet.getNetUsed() < 4700 && i++ < 200) {
      PublicMethedForDailybuild.transferAsset(toAddress, assetAccountId.toByteArray(), 1L,
          transferAssetAddress, transferAssetCreateKey, blockingStubFull);
      assetTransferNet = PublicMethedForDailybuild
          .getAccountNet(transferAssetAddress, blockingStubFull);
    }

    logger.info(Long.toString(assetTransferNet.getFreeNetUsed()));
    Assert.assertTrue(assetTransferNet.getFreeNetUsed() >= 4700);

    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(transferAssetAddress,
        20000000, fromAddress, testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account transferAccount = PublicMethedForDailybuild.queryAccount(transferAssetCreateKey, blockingStubFull);
    Long beforeBalance = transferAccount.getBalance();
    logger.info(Long.toString(beforeBalance));

    Assert.assertTrue(PublicMethedForDailybuild.transferAsset(toAddress, assetAccountId.toByteArray(), 1L,
        transferAssetAddress, transferAssetCreateKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    transferAccount = PublicMethedForDailybuild.queryAccount(transferAssetCreateKey, blockingStubFull);
    Long afterBalance = transferAccount.getBalance();
    logger.info(Long.toString(afterBalance));

    Assert.assertTrue(beforeBalance - afterBalance > 2000);
  }

  @Test(enabled = true, description = "Transfer asset use bandwidth when freeze balance")
  public void ctestWhenFreezeBalanceUseNet() {
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalance(transferAssetAddress, 5000000,
        3, transferAssetCreateKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    AccountNetMessage assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    Account transferAccount = PublicMethedForDailybuild.queryAccount(transferAssetCreateKey, blockingStubFull);

    final Long transferNetUsedBefore = assetTransferNet.getNetUsed();
    final Long transferBalanceBefore = transferAccount.getBalance();
    logger.info("before  " + Long.toString(transferBalanceBefore));

    Assert.assertTrue(PublicMethedForDailybuild.transferAsset(toAddress, assetAccountId.toByteArray(), 1L,
        transferAssetAddress, transferAssetCreateKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    transferAccount = PublicMethedForDailybuild.queryAccount(transferAssetCreateKey, blockingStubFull);
    final Long transferNetUsedAfter = assetTransferNet.getNetUsed();
    final Long transferBalanceAfter = transferAccount.getBalance();
    logger.info("after " + Long.toString(transferBalanceAfter));

    Assert.assertTrue(transferBalanceAfter - transferBalanceBefore == 0);
    Assert.assertTrue(transferNetUsedAfter - transferNetUsedBefore > 200);


  }

  /**
   * constructor.
   */

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


