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
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class WalletTestAssetIssue012 {

  private static final long now = System.currentTimeMillis();
  private static final long totalSupply = now;
  private static final long sendAmount = 10000000000L;
  private static final long netCostMeasure = 200L;
  private static ByteString assetAccountId = null;
  private static String name = "AssetIssue012_" + Long.toString(now);
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final byte[] toAddress = PublicMethedForDailybuild.getFinalAddress(testKey003);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenId");
  Long freeAssetNetLimit = 10000L;
  Long publicFreeAssetNetLimit = 10000L;
  String description = "for case assetissue012";
  String url = "https://stest.assetissue012.url";
  //get account
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] asset012Address = ecKey1.getAddress();
  String testKeyForAssetIssue012 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] transferAssetAddress = ecKey2.getAddress();
  String transferAssetCreateKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);

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
    logger.info(testKeyForAssetIssue012);
    logger.info(transferAssetCreateKey);

    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    //get account
    ecKey1 = new ECKey(Utils.getRandom());
    asset012Address = ecKey1.getAddress();
    testKeyForAssetIssue012 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

    ecKey2 = new ECKey(Utils.getRandom());
    transferAssetAddress = ecKey2.getAddress();
    transferAssetCreateKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

    PublicMethedForDailybuild.printAddress(testKeyForAssetIssue012);
    PublicMethedForDailybuild.printAddress(transferAssetCreateKey);

    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(asset012Address, sendAmount, fromAddress, testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild
        .freezeBalance(asset012Address, 100000000L, 3, testKeyForAssetIssue012,
            blockingStubFull));
    assetAccountId = ByteString.copyFromUtf8(tokenId);
    org.junit.Assert
        .assertTrue(
            PublicMethedForDailybuild.transferAsset(asset012Address, assetAccountId.toByteArray(),
                100000000L, tokenOnwerAddress, tokenOwnerKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = false, description = "Transfer asset use token owner net")
  public void testTransferAssetUseCreatorNet() {
    //Transfer asset to an account.
    Assert.assertTrue(PublicMethedForDailybuild.transferAsset(
        transferAssetAddress, assetAccountId.toByteArray(), 10000000L, asset012Address,
        testKeyForAssetIssue012, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    //Before transfer asset issue, query the net used from creator and transfer.
    AccountNetMessage assetCreatorNet = PublicMethedForDailybuild
        .getAccountNet(asset012Address, blockingStubFull);
    AccountNetMessage assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    Long creatorBeforeNetUsed = assetCreatorNet.getNetUsed();
    Long transferBeforeFreeNetUsed = assetTransferNet.getFreeNetUsed();
    logger.info(Long.toString(creatorBeforeNetUsed));
    logger.info(Long.toString(transferBeforeFreeNetUsed));

    //Transfer send some asset issue to default account, to test if this
    // transaction use the creator net.
    Assert.assertTrue(
        PublicMethedForDailybuild.transferAsset(toAddress, assetAccountId.toByteArray(), 1L,
            transferAssetAddress, transferAssetCreateKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    assetCreatorNet = PublicMethedForDailybuild
        .getAccountNet(asset012Address, blockingStubFull);
    assetTransferNet = PublicMethedForDailybuild
        .getAccountNet(transferAssetAddress, blockingStubFull);
    PublicMethedForDailybuild.printAddress(testKeyForAssetIssue012);
    PublicMethedForDailybuild.printAddress(transferAssetCreateKey);
    Long creatorAfterNetUsed = assetCreatorNet.getNetUsed();
    Long transferAfterFreeNetUsed = assetTransferNet.getFreeNetUsed();
    logger.info("creatorAfterNetUsed:" + creatorAfterNetUsed);
    logger.info("creatorBeforeNetUsed:" + creatorBeforeNetUsed);

    Assert.assertTrue(creatorAfterNetUsed - creatorBeforeNetUsed > netCostMeasure);
    Assert.assertTrue(transferAfterFreeNetUsed - transferBeforeFreeNetUsed < netCostMeasure);
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


