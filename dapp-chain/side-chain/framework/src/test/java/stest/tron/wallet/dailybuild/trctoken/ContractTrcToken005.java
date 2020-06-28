package stest.tron.wallet.dailybuild.trctoken;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
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
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractTrcToken005 {

  private static final long now = System.currentTimeMillis();
  private static final long TotalSupply = 1000L;
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static ByteString assetAccountId = null;
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private byte[] transferTokenContractAddress = null;
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

    PublicMethedForDailybuild.printAddress(dev001Key);
    PublicMethedForDailybuild.printAddress(user001Key);
    assetAccountId = ByteString.copyFromUtf8(tokenId);
    Assert.assertTrue(
        PublicMethedForDailybuild.transferAsset(dev001Address, assetAccountId.toByteArray(),
            10000000L, tokenOnwerAddress, tokenOwnerKey, blockingStubFull));
  }


  @Test(enabled = true, description = "TriggerContract with exception condition")
  public void deployTransferTokenContract() {
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(dev001Address, 1100_000_000L, fromAddress,
        testKey002, blockingStubFull));
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(user001Address, 1_000_000L, fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(fromAddress,
        PublicMethedForDailybuild.getFreezeBalanceCount(dev001Address, dev001Key, 70000L,
            blockingStubFull), 0, 1,
        ByteString.copyFrom(dev001Address), testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
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
        .getAssetIssueValue(dev001Address, assetAccountId, blockingStubFull);

    logger.info("before energyLimit is " + Long.toString(energyLimit));
    logger.info("before energyUsage is " + Long.toString(energyUsage));
    logger.info("before balanceBefore is " + Long.toString(balanceBefore));
    logger.info("before AssetId: " + assetAccountId.toStringUtf8()
        + ", devAssetCountBefore: " + devAssetCountBefore);

    String filePath = "./src/test/resources/soliditycode/contractTrcToken005.sol";
    String contractName = "tokenTest";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    String tokenId = assetAccountId.toStringUtf8();
    long tokenValue = 200;
    long callValue = 0;

    String transferTokenTxid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "",
            maxFeeLimit, callValue, 0, 10000,
            tokenId, tokenValue, null, dev001Key,
            dev001Address, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(transferTokenTxid, blockingStubFull);
    logger.info("Deploy energytotal is " + infoById.get().getReceipt().getEnergyUsageTotal());

    if (transferTokenTxid == null || infoById.get().getResultValue() != 0) {
      Assert.fail("deploy transaction failed with message: " + infoById.get().getResMessage());
    }

    transferTokenContractAddress = infoById.get().getContractAddress().toByteArray();
    SmartContract smartContract = PublicMethedForDailybuild
        .getContract(transferTokenContractAddress,
            blockingStubFull);
    Assert.assertNotNull(smartContract.getAbi());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    accountResource = PublicMethedForDailybuild.getAccountResource(dev001Address, blockingStubFull);
    energyLimit = accountResource.getEnergyLimit();
    energyUsage = accountResource.getEnergyUsed();
    long balanceAfter = PublicMethedForDailybuild.queryAccount(dev001Key, blockingStubFull)
        .getBalance();
    Long devAssetCountAfter = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId, blockingStubFull);

    logger.info("after energyLimit is " + Long.toString(energyLimit));
    logger.info("after energyUsage is " + Long.toString(energyUsage));
    logger.info("after balanceAfter is " + Long.toString(balanceAfter));
    logger.info("after AssetId: " + assetAccountId.toStringUtf8()
        + ", devAssetCountAfter: " + devAssetCountAfter);

    Assert.assertFalse(PublicMethedForDailybuild.transferAsset(transferTokenContractAddress,
        assetAccountId.toByteArray(), 100L, dev001Address, dev001Key, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Long contractAssetCount = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId, blockingStubFull);
    logger.info("Contract has AssetId: " + assetAccountId.toStringUtf8() + ", Count: "
        + contractAssetCount);

    Assert.assertEquals(Long.valueOf(200), Long.valueOf(devAssetCountBefore - devAssetCountAfter));
    Assert.assertEquals(Long.valueOf(200), contractAssetCount);

    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(fromAddress,
        PublicMethedForDailybuild.getFreezeBalanceCount(user001Address, user001Key, 50000L,
            blockingStubFull), 0, 1,
        ByteString.copyFrom(user001Address), testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    accountResource = PublicMethedForDailybuild.getAccountResource(dev001Address,
        blockingStubFull);
    long devEnergyLimitBefore = accountResource.getEnergyLimit();
    long devEnergyUsageBefore = accountResource.getEnergyUsed();
    long devBalanceBefore = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull)
        .getBalance();

    logger.info("before trigger, devEnergyLimitBefore is " + Long.toString(devEnergyLimitBefore));
    logger.info("before trigger, devEnergyUsageBefore is " + Long.toString(devEnergyUsageBefore));
    logger.info("before trigger, devBalanceBefore is " + Long.toString(devBalanceBefore));

    accountResource = PublicMethedForDailybuild
        .getAccountResource(user001Address, blockingStubFull);
    long userEnergyLimitBefore = accountResource.getEnergyLimit();
    long userEnergyUsageBefore = accountResource.getEnergyUsed();
    long userBalanceBefore = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("before trigger, userEnergyLimitBefore is " + Long.toString(userEnergyLimitBefore));
    logger.info("before trigger, userEnergyUsageBefore is " + Long.toString(userEnergyUsageBefore));
    logger.info("before trigger, userBalanceBefore is " + Long.toString(userBalanceBefore));

    Long transferAssetBefore = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress, assetAccountId,
            blockingStubFull);
    logger.info("before trigger, transferTokenContractAddress has AssetId "
        + assetAccountId.toStringUtf8() + ", Count is " + transferAssetBefore);

    Long userAssetId = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, ByteString.copyFromUtf8(tokenId),
            blockingStubFull);
    logger.info("before userAssetId has AssetId "
        + tokenId + ", Count is " + userAssetId);

    // not such tokenId
    tokenId = Long.toString(Long.valueOf(assetAccountId.toStringUtf8()) + 100000);
    tokenValue = 10;
    callValue = 5;

    GrpcAPI.Return response = PublicMethedForDailybuild
        .triggerContractAndGetResponse(transferTokenContractAddress,
            "msgTokenValueAndTokenIdTest()", "#", false, callValue,
            1000000000L, tokenId, tokenValue, user001Address, user001Key,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : No asset !",
        response.getMessage().toStringUtf8());

    // not have this tokenId
    tokenId = assetAccountId.toStringUtf8();
    tokenValue = 10;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : Owner no asset!",
        response.getMessage().toStringUtf8());

    // tokenId is Long.MAX_VALUE
    tokenId = Long.toString(Long.MAX_VALUE);
    tokenValue = 10;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : No asset !",
        response.getMessage().toStringUtf8());

    Assert.assertTrue(PublicMethedForDailybuild.transferAsset(user001Address,
        assetAccountId.toByteArray(), 10L, dev001Address, dev001Key, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // tokenValue is not enough
    tokenId = assetAccountId.toStringUtf8();
    tokenValue = 100;
    callValue = 5;

    response = PublicMethedForDailybuild
        .triggerContractAndGetResponse(transferTokenContractAddress,
            "msgTokenValueAndTokenIdTest()", "#", false, callValue,
            1000000000L, tokenId, tokenValue, user001Address, user001Key,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : assetBalance is not sufficient.",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild
        .sendcoin(transferTokenContractAddress, 5000000, fromAddress,
            testKey002, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // tokenvalue is less than 0
    tokenId = assetAccountId.toStringUtf8();
    tokenValue = -1;
    callValue = 5;

    response = PublicMethedForDailybuild
        .triggerContractAndGetResponse(transferTokenContractAddress,
            "msgTokenValueAndTokenIdTest()", "#", false, callValue,
            1000000000L, tokenId, tokenValue, user001Address, user001Key,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenValue must be >= 0",
        response.getMessage().toStringUtf8());

    tokenId = assetAccountId.toStringUtf8();
    tokenValue = Long.MIN_VALUE;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenValue must be >= 0",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild
        .sendcoin(transferTokenContractAddress, 5000000, fromAddress, testKey002, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // tokenId is 100_0000
    tokenId = Long.toString(100_0000);
    tokenValue = 10;
    callValue = 5;

    response = PublicMethedForDailybuild
        .triggerContractAndGetResponse(transferTokenContractAddress,
            "msgTokenValueAndTokenIdTest()", "#", false, callValue,
            1000000000L, tokenId, tokenValue, user001Address, user001Key,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    // tokenId is long.min
    tokenId = Long.toString(Long.MIN_VALUE);
    tokenValue = 10;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    // tokenId is 0
    tokenId = Long.toString(0);
    tokenValue = 10;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals(
        "contract validate error : invalid arguments with tokenValue = 10, tokenId = 0",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild
        .sendcoin(transferTokenContractAddress, 5000000, fromAddress, testKey002, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    tokenId = Long.toString(Long.MIN_VALUE);
    tokenValue = 0;
    callValue = 5;

    response = PublicMethedForDailybuild
        .triggerContractAndGetResponse(transferTokenContractAddress,
            "msgTokenValueAndTokenIdTest()", "#", false, callValue,
            1000000000L, tokenId, tokenValue, user001Address, user001Key,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    tokenId = Long.toString(-1);
    tokenValue = 0;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    tokenId = Long.toString(100_0000L);
    tokenValue = 0;
    callValue = 5;

    response = PublicMethedForDailybuild.triggerContractAndGetResponse(transferTokenContractAddress,
        "msgTokenValueAndTokenIdTest()", "#", false, callValue,
        1000000000L, tokenId, tokenValue, user001Address, user001Key,
        blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response.getMessage().toStringUtf8());

    accountResource = PublicMethedForDailybuild.getAccountResource(dev001Address, blockingStubFull);
    long devEnergyLimitAfter = accountResource.getEnergyLimit();
    long devEnergyUsageAfter = accountResource.getEnergyUsed();
    long devBalanceAfter = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull)
        .getBalance();

    logger.info("after trigger, devEnergyLimitAfter is " + Long.toString(devEnergyLimitAfter));
    logger.info("after trigger, devEnergyUsageAfter is " + Long.toString(devEnergyUsageAfter));
    logger.info("after trigger, devBalanceAfter is " + Long.toString(devBalanceAfter));

    accountResource = PublicMethedForDailybuild
        .getAccountResource(user001Address, blockingStubFull);
    long userEnergyLimitAfter = accountResource.getEnergyLimit();
    long userEnergyUsageAfter = accountResource.getEnergyUsed();
    long userBalanceAfter = PublicMethedForDailybuild.queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("after trigger, userEnergyLimitAfter is " + Long.toString(userEnergyLimitAfter));
    logger.info("after trigger, userEnergyUsageAfter is " + Long.toString(userEnergyUsageAfter));
    logger.info("after trigger, userBalanceAfter is " + Long.toString(userBalanceAfter));

    PublicMethedForDailybuild.unFreezeBalance(fromAddress, testKey002, 1,
        dev001Address, blockingStubFull);
    PublicMethedForDailybuild.unFreezeBalance(fromAddress, testKey002, 0,
        dev001Address, blockingStubFull);
    PublicMethedForDailybuild.unFreezeBalance(fromAddress, testKey002, 1,
        user001Address, blockingStubFull);
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


