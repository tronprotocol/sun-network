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
public class ContractTrcToken039 {

  private static final long TotalSupply = 10000000L;
  private static final long now = System.currentTimeMillis();
  private static ByteString assetAccountId = null;
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
          .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
          .getString("tokenFoundationAccount.slideTokenId");
  private final String testKey002 = Configuration.getByPath("testng.conf")
          .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  String description = Configuration.getByPath("testng.conf")
          .getString("defaultParameter.assetDescription");
  String url = Configuration.getByPath("testng.conf")
          .getString("defaultParameter.assetUrl");
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] dev001Address = ecKey1.getAddress();
  String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] user001Address = ecKey2.getAddress();
  String user001Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  byte[] proxyTestAddress;
  byte[] atestAddress;
  byte[] btestAddress;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
          .getStringList("fullnode.ip.list").get(0);
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


  @Test(enabled = true, description = "Deploy Proxy contract")
  public void deploy01TransferTokenContract() {
    Assert
            .assertTrue(PublicMethedForDailybuild.sendcoin(dev001Address, 4048000000L, fromAddress,
                    testKey002, blockingStubFull));
    logger.info(
            "dev001Address:" + Base58.encode58Check(dev001Address));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
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
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // deploy transferTokenContract
    int originEnergyLimit = 50000;

    String filePath = "src/test/resources/soliditycode/contractTrcToken039.sol";
    String contractName = "Proxy";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    proxyTestAddress = PublicMethedForDailybuild
            .deployContract(contractName, abi, code, "", maxFeeLimit,
                    1000L, 0, originEnergyLimit, assetAccountId.toStringUtf8(),
                    1000, null, dev001Key, dev001Address,
                    blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    String contractName1 = "A";
    HashMap retMap1 = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();
    atestAddress = PublicMethedForDailybuild
            .deployContract(contractName1, abi1, code1, "", maxFeeLimit,
                    0L, 0, originEnergyLimit, "0",
                    0, null, dev001Key, dev001Address,
                    blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    String contractName2 = "B";
    HashMap retMap2 = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName2);
    String code2 = retMap2.get("byteCode").toString();
    String abi2 = retMap2.get("abI").toString();
    btestAddress = PublicMethedForDailybuild
            .deployContract(contractName2, abi2, code2, "", maxFeeLimit,
                    0L, 0, originEnergyLimit, "0",
                    0, null, dev001Key, dev001Address,
                    blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // devAddress transfer token to userAddress

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true, description = "Trigger Proxy contract use AddressA")
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
            .getAssetIssueValue(proxyTestAddress, assetAccountId,
                    blockingStubFull);
    Long beforeAssetIssueBAddress = PublicMethedForDailybuild
            .getAssetIssueValue(btestAddress, assetAccountId,
                    blockingStubFull);
    Long beforeAssetIssueAAddress = PublicMethedForDailybuild
            .getAssetIssueValue(atestAddress, assetAccountId,
                    blockingStubFull);
    Long beforeBalanceContractAddress = PublicMethedForDailybuild.queryAccount(proxyTestAddress,
            blockingStubFull).getBalance();
    Long beforeUserBalance = PublicMethedForDailybuild
            .queryAccount(user001Address, blockingStubFull)
            .getBalance();
    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeAssetIssueContractAddress:" + beforeAssetIssueContractAddress);
    logger.info("beforeAssetIssueBAddress:" + beforeAssetIssueBAddress);

    logger.info("beforeAssetIssueDevAddress:" + beforeAssetIssueDevAddress);
    logger.info("beforeAssetIssueUserAddress:" + beforeAssetIssueUserAddress);
    logger.info("beforeBalanceContractAddress:" + beforeBalanceContractAddress);
    logger.info("beforeUserBalance:" + beforeUserBalance);
    String param =
            "\"" + Base58.encode58Check(atestAddress) + "\"";
    String param1 =
            "\"" + "1" + "\",\"" + Base58.encode58Check(user001Address) + "\",\"" + assetAccountId
                    .toStringUtf8()
                    + "\"";

    String triggerTxid = PublicMethedForDailybuild.triggerContract(proxyTestAddress,
            "upgradeTo(address)",
            param, false, 0, 1000000000L, "0",
            0, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    final String triggerTxid1 = PublicMethedForDailybuild.triggerContract(proxyTestAddress,
            "trans(uint256,address,trcToken)",
            param1, false, 0, 1000000000L, assetAccountId
                    .toStringUtf8(),
            1, dev001Address, dev001Key,
            blockingStubFull);
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
            .getAssetIssueValue(proxyTestAddress, assetAccountId,
                    blockingStubFull);
    Long afterAssetIssueBAddress = PublicMethedForDailybuild
            .getAssetIssueValue(btestAddress, assetAccountId,
                    blockingStubFull);
    Long afterAssetIssueAAddress = PublicMethedForDailybuild
            .getAssetIssueValue(atestAddress, assetAccountId,
                    blockingStubFull);
    Long afterAssetIssueUserAddress = PublicMethedForDailybuild
            .getAssetIssueValue(user001Address, assetAccountId, blockingStubFull);
    Long afterBalanceContractAddress = PublicMethedForDailybuild.queryAccount(proxyTestAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance = PublicMethedForDailybuild.queryAccount(user001Address, blockingStubFull)
            .getBalance();

    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterAssetIssueCount:" + afterAssetIssueDevAddress);
    logger.info("afterAssetIssueDevAddress:" + afterAssetIssueContractAddress);
    logger.info("afterAssetIssueBAddress:" + afterAssetIssueBAddress);
    logger.info("afterAssetIssueUserAddress:" + afterAssetIssueUserAddress);
    logger.info("afterBalanceContractAddress:" + afterBalanceContractAddress);
    logger.info("afterUserBalance:" + afterUserBalance);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
            .getTransactionInfoById(triggerTxid1, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    Assert.assertTrue(afterAssetIssueUserAddress == beforeAssetIssueUserAddress);
    Assert.assertTrue(afterBalanceContractAddress == beforeBalanceContractAddress - 1);
    Assert.assertTrue(afterAssetIssueContractAddress == beforeAssetIssueContractAddress + 1);
    Assert.assertTrue(afterAssetIssueDevAddress == beforeAssetIssueDevAddress - 1);
    Assert.assertTrue(afterUserBalance == beforeUserBalance + 1);
    Assert.assertTrue(afterAssetIssueUserAddress == afterAssetIssueUserAddress);
    Assert.assertTrue(afterAssetIssueBAddress == beforeAssetIssueBAddress);
  }

  @Test(enabled = true, description = "Trigger Proxy contract use AddressB")
  public void deploy03TransferTokenContract() {
    Account info1;
    AccountResourceMessage resourceInfo1 = PublicMethedForDailybuild
            .getAccountResource(dev001Address,
                    blockingStubFull);
    info1 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    Long beforeBalance1 = info1.getBalance();
    Long beforeEnergyUsed1 = resourceInfo1.getEnergyUsed();
    Long beforeNetUsed1 = resourceInfo1.getNetUsed();
    Long beforeFreeNetUsed1 = resourceInfo1.getFreeNetUsed();
    Long beforeAssetIssueDevAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(dev001Address, assetAccountId, blockingStubFull);
    Long beforeAssetIssueUserAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(user001Address, assetAccountId,
                    blockingStubFull);

    Long beforeAssetIssueContractAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(proxyTestAddress, assetAccountId,
                    blockingStubFull);
    Long beforeAssetIssueBAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(btestAddress, assetAccountId,
                    blockingStubFull);

    Long beforeBalanceContractAddress1 = PublicMethedForDailybuild.queryAccount(proxyTestAddress,
            blockingStubFull).getBalance();
    Long beforeUserBalance1 = PublicMethedForDailybuild
            .queryAccount(user001Address, blockingStubFull)
            .getBalance();
    logger.info("beforeBalance1:" + beforeBalance1);
    logger.info("beforeEnergyUsed1:" + beforeEnergyUsed1);
    logger.info("beforeNetUsed1:" + beforeNetUsed1);
    logger.info("beforeFreeNetUsed1:" + beforeFreeNetUsed1);
    logger.info("beforeAssetIssueContractAddress1:" + beforeAssetIssueContractAddress1);
    logger.info("beforeAssetIssueBAddress1:" + beforeAssetIssueBAddress1);

    logger.info("beforeAssetIssueDevAddress1:" + beforeAssetIssueDevAddress1);
    logger.info("beforeAssetIssueUserAddress1:" + beforeAssetIssueUserAddress1);
    logger.info("beforeBalanceContractAddress1:" + beforeBalanceContractAddress1);
    logger.info("beforeUserBalance1:" + beforeUserBalance1);
    String param3 =
            "\"" + Base58.encode58Check(btestAddress) + "\"";
    String param2 =
            "\"" + "1" + "\",\"" + Base58.encode58Check(user001Address) + "\",\"" + assetAccountId
                    .toStringUtf8()
                    + "\"";

    String triggerTxid2 = PublicMethedForDailybuild.triggerContract(proxyTestAddress,
            "upgradeTo(address)",
            param3, false, 0, 1000000000L, assetAccountId
                    .toStringUtf8(),
            1, dev001Address, dev001Key,
            blockingStubFull);
    String triggerTxid3 = PublicMethedForDailybuild.triggerContract(proxyTestAddress,
            "trans(uint256,address,trcToken)",
            param2, false, 0, 1000000000L, assetAccountId
                    .toStringUtf8(),
            1, dev001Address, dev001Key,
            blockingStubFull);
    Account infoafter1 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter1 = PublicMethedForDailybuild
            .getAccountResource(dev001Address,
                    blockingStubFull);
    Long afterBalance1 = infoafter1.getBalance();
    Long afterEnergyUsed1 = resourceInfoafter1.getEnergyUsed();
    Long afterAssetIssueDevAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(dev001Address, assetAccountId, blockingStubFull);
    Long afterNetUsed1 = resourceInfoafter1.getNetUsed();
    Long afterFreeNetUsed1 = resourceInfoafter1.getFreeNetUsed();
    Long afterAssetIssueContractAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(proxyTestAddress, assetAccountId,
                    blockingStubFull);
    Long afterAssetIssueBAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(btestAddress, assetAccountId,
                    blockingStubFull);

    Long afterAssetIssueUserAddress1 = PublicMethedForDailybuild
            .getAssetIssueValue(user001Address, assetAccountId,
                    blockingStubFull);
    Long afterBalanceContractAddress1 = PublicMethedForDailybuild.queryAccount(proxyTestAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance1 = PublicMethedForDailybuild
            .queryAccount(user001Address, blockingStubFull)
            .getBalance();

    logger.info("afterBalance1:" + afterBalance1);
    logger.info("afterEnergyUsed1:" + afterEnergyUsed1);
    logger.info("afterNetUsed1:" + afterNetUsed1);
    logger.info("afterFreeNetUsed1:" + afterFreeNetUsed1);
    logger.info("afterAssetIssueCount1:" + afterAssetIssueDevAddress1);
    logger.info("afterAssetIssueDevAddress1:" + afterAssetIssueContractAddress1);
    logger.info("afterAssetIssueBAddress1:" + afterAssetIssueBAddress1);
    logger.info("afterAssetIssueUserAddress1:" + afterAssetIssueUserAddress1);
    logger.info("afterBalanceContractAddress1:" + afterBalanceContractAddress1);
    logger.info("afterUserBalance1:" + afterUserBalance1);

    Optional<TransactionInfo> infoById2 = PublicMethedForDailybuild
            .getTransactionInfoById(triggerTxid3, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    Assert.assertTrue(afterAssetIssueUserAddress1 == beforeAssetIssueUserAddress1);
    Assert.assertTrue(afterBalanceContractAddress1 == beforeBalanceContractAddress1 - 1);
    Assert.assertTrue(afterAssetIssueContractAddress1 == beforeAssetIssueContractAddress1 + 1);
    Assert.assertTrue(afterAssetIssueDevAddress1 == beforeAssetIssueDevAddress1 - 1);
    Assert.assertTrue(afterUserBalance1 == beforeUserBalance1 + 1);
    Assert.assertTrue(afterAssetIssueUserAddress1 == afterAssetIssueUserAddress1);
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


