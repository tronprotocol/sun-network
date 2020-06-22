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
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractTrcToken027 {


  private static final long TotalSupply = 10000000L;
  private static ByteString assetAccountId = null;
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");
  byte[] btestAddress;
  byte[] ctestAddress;
  byte[] transferTokenContractAddress;
  int i1 = randomInt(6666666, 9999999);
  ByteString tokenId1 = ByteString.copyFromUtf8(String.valueOf(i1));
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
  /**
   * constructor.
   */
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

  private static int randomInt(int minInt, int maxInt) {
    return (int) Math.round(Math.random() * (maxInt - minInt) + minInt);
  }

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

  @Test(enabled = true, description = "Deploy transferToken contract")
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
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // freeze balance
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(dev001Address, 204800000,
        0, 1, dev001Key, blockingStubFull));

    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(user001Address, 2048000000,
        0, 1, user001Key, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    String tokenName = "testAI_" + randomInt(10000, 90000);

    // deploy transferTokenContract
    int originEnergyLimit = 50000;
    String filePath = "src/test/resources/soliditycode/contractTrcToken027.sol";
    String contractName = "B";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    btestAddress = PublicMethed
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 0, originEnergyLimit, assetAccountId.toStringUtf8(),
            100, null, dev001Key, dev001Address,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String contractName1 = "C";
    HashMap retMap1 = PublicMethed.getBycodeAbi(filePath, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();
    ctestAddress = PublicMethed
        .deployContract(contractName1, abi1, code1, "", maxFeeLimit,
            0L, 0, originEnergyLimit, assetAccountId.toStringUtf8(),
            100, null, dev001Key, dev001Address,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String contractName2 = "token";
    HashMap retMap2 = PublicMethed.getBycodeAbi(filePath, contractName2);

    String code2 = retMap2.get("byteCode").toString();
    String abi2 = retMap2.get("abI").toString();
    transferTokenContractAddress = PublicMethed
        .deployContract(contractName2, abi2, code2, "", maxFeeLimit,
            1000000000L, 0, originEnergyLimit, assetAccountId.toStringUtf8(),
            100, null, dev001Key, dev001Address,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Assert
        .assertFalse(PublicMethed.sendcoin(transferTokenContractAddress, 1000000000L, fromAddress,
            testKey002, blockingStubFull));
  }

  @Test(enabled = true, description = "Multistage delegatecall transferToken use right tokenID")
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
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long beforeAssetIssueUserAddress = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);

    Long beforeAssetIssueContractAddress = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long beforeAssetIssueBAddress = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long beforeAssetIssueCAddress = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
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
    logger.info("beforeAssetIssueContractAddress:" + beforeAssetIssueContractAddress);
    logger.info("beforeAssetIssueBAddress:" + beforeAssetIssueBAddress);
    logger.info("beforeAssetIssueCAddress:" + beforeAssetIssueCAddress);

    logger.info("beforeAssetIssueDevAddress:" + beforeAssetIssueDevAddress);
    logger.info("beforeAssetIssueUserAddress:" + beforeAssetIssueUserAddress);
    logger.info("beforeBalanceContractAddress:" + beforeBalanceContractAddress);
    logger.info("beforeUserBalance:" + beforeUserBalance);
    // 1.user trigger A to transfer token to B
    String param =
        "\"" + Base58.encode58Check(btestAddress) + "\",\"" + Base58.encode58Check(ctestAddress)
            + "\",\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\",1,\"" + assetAccountId
            .toStringUtf8()
            + "\"";

    final String triggerTxid = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "testIndelegateCall(address,address,address,uint256,trcToken)",
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
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed = resourceInfoafter.getNetUsed();
    Long afterFreeNetUsed = resourceInfoafter.getFreeNetUsed();
    Long afterAssetIssueContractAddress = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long afterAssetIssueBAddress = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueCAddress = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
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
    logger.info("afterAssetIssueBAddress:" + afterAssetIssueBAddress);
    logger.info("afterAssetIssueCAddress:" + afterAssetIssueCAddress);
    logger.info("afterAssetIssueUserAddress:" + afterAssetIssueUserAddress);
    logger.info("afterBalanceContractAddress:" + afterBalanceContractAddress);
    logger.info("afterUserBalance:" + afterUserBalance);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    Assert.assertTrue(afterAssetIssueUserAddress == beforeAssetIssueUserAddress);
    Assert.assertEquals(afterBalanceContractAddress, beforeBalanceContractAddress);
    Assert.assertTrue(afterAssetIssueContractAddress == beforeAssetIssueContractAddress + 1);
    Assert.assertTrue(afterAssetIssueBAddress == beforeAssetIssueBAddress);
    Assert.assertTrue(afterAssetIssueCAddress == beforeAssetIssueCAddress - 1);

  }

  @Test(enabled = true, description = "Multistage delegatecall transferToken use fake tokenID")
  public void deploy03TransferTokenContract() {
    //3. user trigger A to transfer token to B
    Account infoafter = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterAssetIssueDevAddress = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed = resourceInfoafter.getNetUsed();
    Long afterFreeNetUsed = resourceInfoafter.getFreeNetUsed();
    final Long afterAssetIssueContractAddress = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueBAddress = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueCAddress = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueUserAddress = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    final Long afterBalanceContractAddress = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance = PublicMethedForDailybuild.queryAccount(user001Address, blockingStubFull)
        .getBalance();

    String param1 =
        "\"" + Base58.encode58Check(btestAddress) + "\",\"" + Base58.encode58Check(ctestAddress)
            + "\",\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\",1,\"" + tokenId1
            .toStringUtf8()
            + "\"";

    final String triggerTxid1 = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "testIndelegateCall(address,address,address,uint256,trcToken)",
            param1, false, 0, 1000000000L, "0",
            0, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter1 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter1 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance1 = infoafter1.getBalance();
    Long afterEnergyUsed1 = resourceInfoafter1.getEnergyUsed();
    Long afterAssetIssueDevAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed1 = resourceInfoafter1.getNetUsed();
    Long afterFreeNetUsed1 = resourceInfoafter1.getFreeNetUsed();
    Long afterAssetIssueContractAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long afterAssetIssueBAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueCAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    Long afterBalanceContractAddress1 = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
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
    logger.info("afterAssetIssueCAddress1:" + afterAssetIssueCAddress1);
    logger.info("afterAssetIssueUserAddress1:" + afterAssetIssueUserAddress1);
    logger.info("afterBalanceContractAddress1:" + afterBalanceContractAddress1);
    logger.info("afterUserBalance1:" + afterUserBalance1);

    Optional<TransactionInfo> infoById1 = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    Assert.assertTrue(afterAssetIssueUserAddress == afterAssetIssueUserAddress1);
    Assert.assertEquals(afterBalanceContractAddress, afterBalanceContractAddress1);
    Assert.assertTrue(afterAssetIssueContractAddress == afterAssetIssueContractAddress1);
    Assert.assertTrue(afterAssetIssueBAddress == afterAssetIssueBAddress1);
    Assert.assertTrue(afterAssetIssueCAddress == afterAssetIssueCAddress1);
  }


  @Test(enabled = true, description = "Multistage delegatecall transferToken token value"
      + " not enough")
  public void deploy04TransferTokenContract() {
    //4. user trigger A to transfer token to B
    Account infoafter1 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter1 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance1 = infoafter1.getBalance();
    Long afterEnergyUsed1 = resourceInfoafter1.getEnergyUsed();
    Long afterAssetIssueDevAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed1 = resourceInfoafter1.getNetUsed();
    Long afterFreeNetUsed1 = resourceInfoafter1.getFreeNetUsed();
    final Long afterAssetIssueContractAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueBAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueCAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueUserAddress1 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    final Long afterBalanceContractAddress1 =
        PublicMethedForDailybuild.queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance1 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();
    String param2 =
        "\"" + Base58.encode58Check(btestAddress) + "\",\"" + Base58.encode58Check(ctestAddress)
            + "\",\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\",10000000,\"" + assetAccountId
            .toStringUtf8()
            + "\"";

    final String triggerTxid2 = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "testIndelegateCall(address,address,address,uint256,trcToken)",
            param2, false, 0, 1000000000L, "0",
            0, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter2 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter2 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance2 = infoafter2.getBalance();
    Long afterEnergyUsed2 = resourceInfoafter2.getEnergyUsed();
    Long afterAssetIssueDevAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed2 = resourceInfoafter2.getNetUsed();
    Long afterFreeNetUsed2 = resourceInfoafter2.getFreeNetUsed();
    Long afterAssetIssueContractAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long afterAssetIssueBAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueCAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    Long afterBalanceContractAddress2 = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance2 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("afterBalance2:" + afterBalance2);
    logger.info("afterEnergyUsed2:" + afterEnergyUsed2);
    logger.info("afterNetUsed2:" + afterNetUsed2);
    logger.info("afterFreeNetUsed2:" + afterFreeNetUsed2);
    logger.info("afterAssetIssueCount2:" + afterAssetIssueDevAddress2);
    logger.info("afterAssetIssueDevAddress2:" + afterAssetIssueContractAddress2);
    logger.info("afterAssetIssueBAddress2:" + afterAssetIssueBAddress2);
    logger.info("afterAssetIssueCAddress2:" + afterAssetIssueCAddress2);
    logger.info("afterAssetIssueUserAddress2:" + afterAssetIssueUserAddress2);
    logger.info("afterBalanceContractAddress2:" + afterBalanceContractAddress2);
    logger.info("afterUserBalance2:" + afterUserBalance2);

    Optional<TransactionInfo> infoById2 = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid2, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    Assert.assertTrue(afterAssetIssueUserAddress1 == afterAssetIssueUserAddress2);
    Assert.assertEquals(afterBalanceContractAddress1, afterBalanceContractAddress2);
    Assert.assertTrue(afterAssetIssueContractAddress1 == afterAssetIssueContractAddress2);
    Assert.assertTrue(afterAssetIssueBAddress1 == afterAssetIssueBAddress2);
    Assert.assertTrue(afterAssetIssueCAddress1 == afterAssetIssueCAddress2);
  }

  @Test(enabled = true, description = "Multistage delegatecall transferToken calltoken ID"
      + " not exist")
  public void deploy05TransferTokenContract() {
    Account infoafter2 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter2 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance2 = infoafter2.getBalance();
    Long afterEnergyUsed2 = resourceInfoafter2.getEnergyUsed();
    Long afterAssetIssueDevAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed2 = resourceInfoafter2.getNetUsed();
    Long afterFreeNetUsed2 = resourceInfoafter2.getFreeNetUsed();
    final Long afterAssetIssueContractAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueBAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueCAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueUserAddress2 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    final Long afterBalanceContractAddress2 = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance2 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();
    //5. user trigger A to transfer token to B
    String param3 =
        "\"" + Base58.encode58Check(btestAddress) + "\",\"" + Base58.encode58Check(ctestAddress)
            + "\",\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\",1,\"" + assetAccountId
            .toStringUtf8()
            + "\"";

    final String triggerTxid3 = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "testIndelegateCall(address,address,address,uint256,trcToken)",
            param3, false, 0, 1000000000L, tokenId1
                .toStringUtf8(),
            1, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter3 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter3 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance3 = infoafter3.getBalance();
    Long afterEnergyUsed3 = resourceInfoafter3.getEnergyUsed();
    Long afterAssetIssueDevAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed3 = resourceInfoafter3.getNetUsed();
    Long afterFreeNetUsed3 = resourceInfoafter3.getFreeNetUsed();
    Long afterAssetIssueContractAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long afterAssetIssueBAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueCAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    Long afterBalanceContractAddress3 = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance3 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("afterBalance3:" + afterBalance3);
    logger.info("afterEnergyUsed3:" + afterEnergyUsed3);
    logger.info("afterNetUsed3:" + afterNetUsed3);
    logger.info("afterFreeNetUsed3:" + afterFreeNetUsed3);
    logger.info("afterAssetIssueCount3:" + afterAssetIssueDevAddress3);
    logger.info("afterAssetIssueDevAddress3:" + afterAssetIssueContractAddress3);
    logger.info("afterAssetIssueBAddress3:" + afterAssetIssueBAddress3);
    logger.info("afterAssetIssueCAddress3:" + afterAssetIssueCAddress3);
    logger.info("afterAssetIssueUserAddress3:" + afterAssetIssueUserAddress3);
    logger.info("afterBalanceContractAddress3:" + afterBalanceContractAddress3);
    logger.info("afterUserBalance3:" + afterUserBalance3);

    Optional<TransactionInfo> infoById3 = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid3, blockingStubFull);
    Assert.assertTrue(triggerTxid3 == null);
    Assert.assertTrue(afterAssetIssueUserAddress2 == afterAssetIssueUserAddress3);
    Assert.assertEquals(afterBalanceContractAddress2, afterBalanceContractAddress3);
    Assert.assertTrue(afterAssetIssueContractAddress2 == afterAssetIssueContractAddress3);
    Assert.assertTrue(afterAssetIssueBAddress2 == afterAssetIssueBAddress3);
    Assert.assertTrue(afterAssetIssueCAddress2 == afterAssetIssueCAddress3);
  }

  @Test(enabled = true, description = "Multistage delegatecall transferToken calltoken value "
      + "not enough")
  public void deploy06TransferTokenContract() {
    Account infoafter3 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter3 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance3 = infoafter3.getBalance();
    Long afterEnergyUsed3 = resourceInfoafter3.getEnergyUsed();
    Long afterAssetIssueDevAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed3 = resourceInfoafter3.getNetUsed();
    Long afterFreeNetUsed3 = resourceInfoafter3.getFreeNetUsed();
    final Long afterAssetIssueContractAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueBAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueCAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueUserAddress3 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    final Long afterBalanceContractAddress3 =
        PublicMethedForDailybuild.queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance3 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();
    //user trigger A to transfer token to B
    String param4 =
        "\"" + Base58.encode58Check(btestAddress) + "\",\"" + Base58.encode58Check(ctestAddress)
            + "\",\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\",1,\"" + assetAccountId
            .toStringUtf8()
            + "\"";

    final String triggerTxid4 = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "testIndelegateCall(address,address,address,uint256,trcToken)",
            param4, false, 0, 1000000000L, assetAccountId
                .toStringUtf8(),
            100000000, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter4 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter4 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance4 = infoafter4.getBalance();
    Long afterEnergyUsed4 = resourceInfoafter4.getEnergyUsed();
    Long afterAssetIssueDevAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed4 = resourceInfoafter4.getNetUsed();
    Long afterFreeNetUsed4 = resourceInfoafter4.getFreeNetUsed();
    Long afterAssetIssueContractAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long afterAssetIssueBAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueCAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    Long afterBalanceContractAddress4 = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance4 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("afterBalance4:" + afterBalance4);
    logger.info("afterEnergyUsed4:" + afterEnergyUsed4);
    logger.info("afterNetUsed4:" + afterNetUsed4);
    logger.info("afterFreeNetUsed4:" + afterFreeNetUsed4);
    logger.info("afterAssetIssueCount4:" + afterAssetIssueDevAddress4);
    logger.info("afterAssetIssueDevAddress4:" + afterAssetIssueContractAddress4);
    logger.info("afterAssetIssueBAddress4:" + afterAssetIssueBAddress4);
    logger.info("afterAssetIssueCAddress4:" + afterAssetIssueCAddress4);
    logger.info("afterAssetIssueUserAddress4:" + afterAssetIssueUserAddress4);
    logger.info("afterBalanceContractAddress4:" + afterBalanceContractAddress4);
    logger.info("afterUserBalance4:" + afterUserBalance4);

    Optional<TransactionInfo> infoById4 = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid4, blockingStubFull);
    Assert.assertTrue(triggerTxid4 == null);
    Assert.assertTrue(afterAssetIssueUserAddress3 == afterAssetIssueUserAddress4);
    Assert.assertEquals(afterBalanceContractAddress3, afterBalanceContractAddress4);
    Assert.assertTrue(afterAssetIssueContractAddress3 == afterAssetIssueContractAddress4);
    Assert.assertTrue(afterAssetIssueBAddress3 == afterAssetIssueBAddress4);
    Assert.assertTrue(afterAssetIssueCAddress3 == afterAssetIssueCAddress4);
  }

  @Test(enabled = true, description = "Multistage delegatecall transferToken use right tokenID,"
      + "tokenvalue")
  public void deploy07TransferTokenContract() {
    Account infoafter4 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter4 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance4 = infoafter4.getBalance();
    Long afterEnergyUsed4 = resourceInfoafter4.getEnergyUsed();
    final Long afterAssetIssueDevAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    final Long afterNetUsed4 = resourceInfoafter4.getNetUsed();
    final Long afterFreeNetUsed4 = resourceInfoafter4.getFreeNetUsed();
    final Long afterAssetIssueContractAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueBAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueCAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    final Long afterAssetIssueUserAddress4 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    final Long afterBalanceContractAddress4 =
        PublicMethedForDailybuild.queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance4 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();
    // user trigger A to transfer token to B
    String param5 =
        "\"" + Base58.encode58Check(btestAddress) + "\",\"" + Base58.encode58Check(ctestAddress)
            + "\",\"" + Base58.encode58Check(transferTokenContractAddress)
            + "\",1,\"" + assetAccountId
            .toStringUtf8()
            + "\"";

    final String triggerTxid5 = PublicMethedForDailybuild
        .triggerContract(transferTokenContractAddress,
            "testIndelegateCall(address,address,address,uint256,trcToken)",
            param5, false, 0, 1000000000L, assetAccountId
                .toStringUtf8(),
            1, dev001Address, dev001Key,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter5 = PublicMethedForDailybuild.queryAccount(dev001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter5 = PublicMethedForDailybuild
        .getAccountResource(dev001Address,
            blockingStubFull);
    Long afterBalance5 = infoafter5.getBalance();
    Long afterEnergyUsed5 = resourceInfoafter5.getEnergyUsed();
    Long afterAssetIssueDevAddress5 = PublicMethedForDailybuild
        .getAssetIssueValue(dev001Address, assetAccountId,
            blockingStubFull);
    Long afterNetUsed5 = resourceInfoafter5.getNetUsed();
    Long afterFreeNetUsed5 = resourceInfoafter5.getFreeNetUsed();
    Long afterAssetIssueContractAddress5 = PublicMethedForDailybuild
        .getAssetIssueValue(transferTokenContractAddress,
            assetAccountId,
            blockingStubFull);
    Long afterAssetIssueBAddress5 = PublicMethedForDailybuild
        .getAssetIssueValue(btestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueCAddress5 = PublicMethedForDailybuild
        .getAssetIssueValue(ctestAddress, assetAccountId,
            blockingStubFull);
    Long afterAssetIssueUserAddress5 = PublicMethedForDailybuild
        .getAssetIssueValue(user001Address, assetAccountId,
            blockingStubFull);
    Long afterBalanceContractAddress5 = PublicMethedForDailybuild
        .queryAccount(transferTokenContractAddress,
            blockingStubFull).getBalance();
    Long afterUserBalance5 = PublicMethedForDailybuild
        .queryAccount(user001Address, blockingStubFull)
        .getBalance();

    logger.info("afterBalance5:" + afterBalance5);
    logger.info("afterEnergyUsed5:" + afterEnergyUsed5);
    logger.info("afterNetUsed5:" + afterNetUsed5);
    logger.info("afterFreeNetUsed5:" + afterFreeNetUsed5);
    logger.info("afterAssetIssueCount5:" + afterAssetIssueDevAddress5);
    logger.info("afterAssetIssueDevAddress5:" + afterAssetIssueContractAddress5);
    logger.info("afterAssetIssueBAddress5:" + afterAssetIssueBAddress5);
    logger.info("afterAssetIssueCAddress5:" + afterAssetIssueCAddress5);
    logger.info("afterAssetIssueUserAddress5:" + afterAssetIssueUserAddress5);
    logger.info("afterBalanceContractAddress5:" + afterBalanceContractAddress5);
    logger.info("afterUserBalance5:" + afterUserBalance5);

    Optional<TransactionInfo> infoById5 = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid5, blockingStubFull);
    Assert.assertTrue(infoById5.get().getResultValue() == 0);
    Assert.assertTrue(afterAssetIssueUserAddress4 == afterAssetIssueUserAddress5);
    Assert.assertEquals(afterBalanceContractAddress4, afterBalanceContractAddress5);
    Assert.assertTrue(afterAssetIssueContractAddress4 + 2 == afterAssetIssueContractAddress5);
    Assert.assertTrue(afterAssetIssueBAddress4 == afterAssetIssueBAddress5);
    Assert.assertTrue(afterAssetIssueCAddress4 - 1 == afterAssetIssueCAddress5);
    Assert.assertTrue(afterAssetIssueDevAddress4 - 1 == afterAssetIssueDevAddress5);

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



