package stest.tron.wallet.dailybuild.trctoken;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;


@Slf4j
public class ContractTrcToken003 {

  private static final long now = System.currentTimeMillis();
  private static final long TotalSupply = 1000L;
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static ByteString assetAccountDev = null;
  private static ByteString assetAccountUser = null;
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");
  private final String tokenOwnerKey2 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey2");
  private final byte[] tokenOnwerAddress2 = PublicMethedForDailybuild
      .getFinalAddress(tokenOwnerKey2);
  private final String tokenId2 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId2");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");
  private ECKey ecKey1 = new ECKey(Utils.getRandom());
  private byte[] dev001Address = ecKey1.getAddress();
  private String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private ECKey ecKey2 = new ECKey(Utils.getRandom());
  private byte[] user001Address = ecKey2.getAddress();
  private String user001Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

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

    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    assetAccountDev = ByteString.copyFromUtf8(tokenId);
    Assert.assertTrue(
        PublicMethedForDailybuild.transferAsset(dev001Address, assetAccountDev.toByteArray(),
            1000, tokenOnwerAddress, tokenOwnerKey, blockingStubFull));

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    PublicMethedForDailybuild.printAddress(tokenOwnerKey2);
    assetAccountUser = ByteString.copyFromUtf8(tokenId2);
    Assert.assertTrue(
        PublicMethedForDailybuild.transferAsset(user001Address, assetAccountUser.toByteArray(),
            1000, tokenOnwerAddress2, tokenOwnerKey2, blockingStubFull));

  }

  @Test(enabled = true, description = "DeployContract with exception condition")
  public void deployTransferTokenContract() {
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(dev001Address, 1100_000_000L, fromAddress,
        testKey002, blockingStubFull));
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(user001Address, 1100_000_000L, fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(fromAddress,
        PublicMethedForDailybuild
            .getFreezeBalanceCount(dev001Address, dev001Key, 50000L, blockingStubFull),
        0, 1, ByteString.copyFrom(dev001Address), testKey002, blockingStubFull));
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(fromAddress, 10_000_000L,
        0, 0, ByteString.copyFrom(dev001Address), testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    //before deploy, check account resource
    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    long energyLimit = accountResource.getEnergyLimit();
    long energyUsage = accountResource.getEnergyUsed();
    long balanceBefore = PublicMethedForDailybuild.queryAccount(dev001Key, blockingStubFull)
        .getBalance();
    Long devAssetCountBefore = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountDev, blockingStubFull);
    Long userAssetCountBefore = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountUser,
            blockingStubFull);

    logger.info("before energyLimit is " + Long.toString(energyLimit));
    logger.info("before energyUsage is " + Long.toString(energyUsage));
    logger.info("before balanceBefore is " + Long.toString(balanceBefore));
    logger.info("before dev has AssetId: " + assetAccountDev.toStringUtf8()
        + ", devAssetCountBefore: " + devAssetCountBefore);
    logger.info("before dev has AssetId: " + assetAccountUser.toStringUtf8()
        + ", userAssetCountBefore: " + userAssetCountBefore);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    String filePath = "./src/test/resources/soliditycode/contractTrcToken003.sol";
    String contractName = "tokenTest";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    // the tokenId is not exist
    String fakeTokenId = Long.toString(Long.valueOf(assetAccountDev.toStringUtf8()) + 100);
    Long fakeTokenValue = 100L;

    GrpcAPI.Return response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, fakeTokenValue, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : No asset !",
        response.getMessage().toStringUtf8());

    // deployer didn't have any such token
    fakeTokenId = assetAccountUser.toStringUtf8();
    fakeTokenValue = 100L;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, fakeTokenValue, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : assetBalance must greater than 0.",
        response.getMessage().toStringUtf8());

    // deployer didn't have any Long.MAX_VALUE
    fakeTokenId = Long.toString(Long.MAX_VALUE);
    fakeTokenValue = 100L;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, fakeTokenValue, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : No asset !",
        response.getMessage().toStringUtf8());

    // the tokenValue is not enough
    fakeTokenId = assetAccountDev.toStringUtf8();
    fakeTokenValue = devAssetCountBefore + 100;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, fakeTokenValue, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : assetBalance is not sufficient.",
        response.getMessage().toStringUtf8());

    // tokenid is -1
    fakeTokenId = Long.toString(-1);
    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, 100, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // tokenid is 100_0000L
    fakeTokenId = Long.toString(100_0000L);
    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, 100, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    // tokenid is Long.MIN_VALUE
    fakeTokenId = Long.toString(Long.MIN_VALUE);
    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, 100, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // tokenid is 0
    fakeTokenId = Long.toString(0);

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            fakeTokenId, 100, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals(
        "contract validate error : invalid arguments with tokenValue = 100, tokenId = 0",
        response.getMessage().toStringUtf8());

    // tokenvalue is less than 0
    fakeTokenValue = -1L;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            assetAccountDev.toStringUtf8(), fakeTokenValue, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenValue must >= 0",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // tokenvalue is long.min
    fakeTokenValue = Long.MIN_VALUE;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            assetAccountDev.toStringUtf8(), fakeTokenValue, null, dev001Key,
            dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenValue must >= 0",
        response.getMessage().toStringUtf8());

    String tokenId = Long.toString(-1);
    long tokenValue = 0;
    long callValue = 10;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "", maxFeeLimit,
            callValue, 0, 10000, tokenId, tokenValue,
            null, dev001Key, dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    tokenId = Long.toString(Long.MIN_VALUE);
    tokenValue = 0;
    callValue = 10;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "", maxFeeLimit,
            callValue, 0, 10000, tokenId, tokenValue,
            null, dev001Key, dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    tokenId = Long.toString(1000000);
    tokenValue = 0;
    callValue = 10;

    response = PublicMethedForDailybuild
        .deployContractAndGetResponse(contractName, abi, code, "", maxFeeLimit,
            callValue, 0, 10000, tokenId, tokenValue,
            null, dev001Key, dev001Address, blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    accountResource = PublicMethedForDailybuild.getAccountResource(dev001Address, blockingStubFull);
    energyLimit = accountResource.getEnergyLimit();
    energyUsage = accountResource.getEnergyUsed();
    long balanceAfter = PublicMethedForDailybuild.queryAccount(dev001Key, blockingStubFull)
        .getBalance();
    Long devAssetCountAfter = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountDev, blockingStubFull);
    Long userAssetCountAfter = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountUser,
            blockingStubFull);

    logger.info("after energyLimit is " + Long.toString(energyLimit));
    logger.info("after energyUsage is " + Long.toString(energyUsage));
    logger.info("after balanceAfter is " + Long.toString(balanceAfter));
    logger.info("after dev has AssetId: " + assetAccountDev.toStringUtf8()
        + ", devAssetCountAfter: " + devAssetCountAfter);
    logger.info("after user has AssetId: " + assetAccountDev.toStringUtf8()
        + ", userAssetCountAfter: " + userAssetCountAfter);

    Assert.assertEquals(devAssetCountBefore, devAssetCountAfter);
    Assert.assertEquals(userAssetCountBefore, userAssetCountAfter);

    PublicMethedForDailybuild.unFreezeBalance(fromAddress, testKey002, 1,
        dev001Address, blockingStubFull);
    PublicMethedForDailybuild.unFreezeBalance(fromAddress, testKey002, 0,
        dev001Address, blockingStubFull);
  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


