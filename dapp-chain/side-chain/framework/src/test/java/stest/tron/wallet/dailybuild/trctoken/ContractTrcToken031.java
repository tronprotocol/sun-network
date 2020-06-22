package stest.tron.wallet.dailybuild.trctoken;

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
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractTrcToken031 {


  private static final long now = System.currentTimeMillis();
  private static final long TotalSupply = 10000000L;
  private static ByteString assetAccountId = null;
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] dev001Address = ecKey1.getAddress();
  String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] user001Address = ecKey2.getAddress();
  String user001Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");
  byte[] transferTokenContractAddress;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

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

    assetAccountId = ByteString.copyFromUtf8(tokenId);
    Assert.assertTrue(
        PublicMethedForDailybuild.transferAsset(dev001Address, assetAccountId.toByteArray(),
            10000000L, tokenOnwerAddress, tokenOwnerKey, blockingStubFull));

  }

  @Test(enabled = true, description = "Deploy suicide contract")
  public void deploy01TransferTokenContract() {

    Assert
        .assertTrue(PublicMethedForDailybuild.sendcoin(dev001Address, 4048000000L, fromAddress,
            testKey002, blockingStubFull));
    logger.info(
        "dev001Address:" + Base58.encode58Check(dev001Address));
    Assert
        .assertTrue(PublicMethedForDailybuild.sendcoin(user001Address, 4048000000L, fromAddress,
            testKey002, blockingStubFull));
    logger.info(
        "user001Address:" + Base58.encode58Check(user001Address));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // freeze balance
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(dev001Address, 204800000,
        0, 1, dev001Key, blockingStubFull));

    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(user001Address, 2048000000,
        0, 1, user001Key, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // deploy transferTokenContract
    int originEnergyLimit = 50000;
    String filePath = "src/test/resources/soliditycode/contractTrcToken031.sol";
    String contractName = "token";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    transferTokenContractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 0, originEnergyLimit, "0",
            0, null, dev001Key, dev001Address,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertNotNull(transferTokenContractAddress);

    Assert
        .assertFalse(PublicMethedForDailybuild
            .sendcoin(transferTokenContractAddress, 1000000000L, fromAddress,
                testKey002, blockingStubFull));

    // devAddress transfer token to userAddress
    PublicMethedForDailybuild
        .transferAsset(transferTokenContractAddress, assetAccountId.toByteArray(), 100,
            dev001Address,
            dev001Key,
            blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true, description = "Trigger suicide contract,toaddress is myself")
  public void deploy02TransferTokenContract() {
    Account info;
    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    Long beforeAssetIssueDevAddress = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId, blockingStubFull);
    Long beforeAssetIssueUserAddress = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);

    Long beforeAssetIssueContractAddress = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress, assetAccountId,
            blockingStubFull);
    Long beforeBalanceContractAddress = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long beforeUserBalance = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeAssetIssueCount:" + beforeAssetIssueContractAddress);
    logger.info("beforeAssetIssueDevAddress:" + beforeAssetIssueDevAddress);
    logger.info("beforeAssetIssueUserAddress:" + beforeAssetIssueUserAddress);
    logger.info("beforeBalanceContractAddress:" + beforeBalanceContractAddress);

    // user trigger A to transfer token to B
    String param =
        "\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\"";

    final String triggerTxid = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "kill(address)",
            param, false, 0, 1000000000L, "0",
            0, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterAssetIssueDevAddress = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId, blockingStubFull);
    Long afterNetUsed = resourceInfoafter.getNetUsed();
    Long afterFreeNetUsed = resourceInfoafter.getFreeNetUsed();
    Long afterAssetIssueContractAddress = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId, blockingStubFull);
    Long afterBalanceContractAddress = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance = PublicMethedForDailybuild.queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterAssetIssueCount:" + afterAssetIssueDevAddress);
    logger.info("afterAssetIssueDevAddress:" + afterAssetIssueContractAddress);
    logger.info("afterAssetIssueUserAddress:" + afterAssetIssueUserAddress);
    logger.info("afterBalanceContractAddress:" + afterBalanceContractAddress);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid, blockingStubFull);
    Assert.assertTrue(infoById.get()
        .getResultValue() == 0);
    Assert.assertTrue(afterBalanceContractAddress == 0);
    Assert.assertTrue(afterAssetIssueContractAddress == 0);

    PublicMethedForDailybuild.unFreezeBalance(dev001Address, dev001Key, 1,
        dev001Address, blockingStubFull);
    PublicMethedForDailybuild.unFreezeBalance(user001Address, user001Key, 1,
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


