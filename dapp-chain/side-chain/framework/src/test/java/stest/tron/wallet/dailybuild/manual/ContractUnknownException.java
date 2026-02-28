package stest.tron.wallet.dailybuild.manual;

import static org.hamcrest.core.StringContains.containsString;

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
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractUnknownException {


  private final String testNetAccountKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] testNetAccountAddress = PublicMethedForDailybuild
      .getFinalAddress(testNetAccountKey);
  byte[] contractAddress = null;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] grammarAddress = ecKey1.getAddress();
  String testKeyForGrammarAddress = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] grammarAddress2 = ecKey2.getAddress();
  String testKeyForGrammarAddress2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  ECKey ecKey3 = new ECKey(Utils.getRandom());
  byte[] grammarAddress3 = ecKey3.getAddress();
  String testKeyForGrammarAddress3 = ByteArray.toHexString(ecKey3.getPrivKeyBytes());
  ECKey ecKey4 = new ECKey(Utils.getRandom());
  byte[] grammarAddress4 = ecKey4.getAddress();
  String testKeyForGrammarAddress4 = ByteArray.toHexString(ecKey4.getPrivKeyBytes());
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);

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
    PublicMethedForDailybuild.printAddress(testKeyForGrammarAddress);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);

    logger.info(
        Long.toString(PublicMethedForDailybuild.queryAccount(testNetAccountKey, blockingStubFull)
            .getBalance()));
  }

  @Test(enabled = true, description = "trigger selfdestruct method")
  public void testGrammar001() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress, 1000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(grammarAddress, 204800000,
        0, 1, testKeyForGrammarAddress, blockingStubFull));
    Account info;
    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(grammarAddress,
            blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(grammarAddress, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    long beforeenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeenergyLimit:" + beforeenergyLimit);
    String filePath = "src/test/resources/soliditycode/contractUnknownException.sol";
    String contractName = "testA";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String txid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit,
            20L, 100, null, testKeyForGrammarAddress,
            grammarAddress, blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    final String s = infoById.get().getResMessage().toStringUtf8();
    long fee = infoById.get().getFee();
    long energyUsage = infoById.get().getReceipt().getEnergyUsage();
    long energyFee = infoById.get().getReceipt().getEnergyFee();
    Account infoafter = PublicMethedForDailybuild.queryAccount(grammarAddress, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
        .getAccountResource(grammarAddress,
            blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterNetUsed = resourceInfo.getNetUsed();
    Long afterFreeNetUsed = resourceInfo.getFreeNetUsed();
    long aftereenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterenergyLimit:" + aftereenergyLimit);
    Assert.assertThat(s, containsString("REVERT opcode executed"));
    PublicMethedForDailybuild
        .unFreezeBalance(grammarAddress, testKeyForGrammarAddress, 1, grammarAddress,
            blockingStubFull);
    PublicMethedForDailybuild
        .freedResource(grammarAddress, testKeyForGrammarAddress, testNetAccountAddress,
            blockingStubFull);
  }

  @Test(enabled = true, description = "trigger revert method")
  public void testGrammar002() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress2, 100000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(grammarAddress2, 10000000L,
        0, 1, testKeyForGrammarAddress2, blockingStubFull));
    Account info;
    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(grammarAddress2,
            blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(grammarAddress2, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    long beforeenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeenergyLimit:" + beforeenergyLimit);
    String filePath = "src/test/resources/soliditycode/contractUnknownException.sol";
    String contractName = "testB";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String txid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit,
            20L, 100, null, testKeyForGrammarAddress2,
            grammarAddress2, blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    final long fee = infoById.get().getFee();
    final long energyUsage = infoById.get().getReceipt().getEnergyUsage();
    final long energyFee = infoById.get().getReceipt().getEnergyFee();

    final String s = infoById.get().getResMessage().toStringUtf8();

    Account infoafter = PublicMethedForDailybuild.queryAccount(grammarAddress2, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
        .getAccountResource(grammarAddress2,
            blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterNetUsed = resourceInfo.getNetUsed();
    Long afterFreeNetUsed = resourceInfo.getFreeNetUsed();
    long aftereenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterenergyLimit:" + aftereenergyLimit);
    Assert.assertThat(s, containsString("REVERT opcode executed"));
    Assert.assertFalse(energyFee == 1000000000);

    Assert.assertTrue(beforeBalance - fee == afterBalance);
    PublicMethedForDailybuild
        .unFreezeBalance(grammarAddress2, testKeyForGrammarAddress2, 1, grammarAddress2,
            blockingStubFull);
    PublicMethedForDailybuild
        .freedResource(grammarAddress2, testKeyForGrammarAddress2, testNetAccountAddress,
            blockingStubFull);

  }

  @Test(enabled = true, description = "trigger assert method")
  public void testGrammar003() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress3, 100000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(grammarAddress3, 1000000000L,
        0, 1, testKeyForGrammarAddress3, blockingStubFull));
    Account info;
    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(grammarAddress3,
            blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(grammarAddress3, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    long beforeenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeenergyLimit:" + beforeenergyLimit);
    String filePath = "src/test/resources/soliditycode/contractUnknownException.sol";
    String contractName = "testC";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String txid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit,
            20L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    final long fee = infoById.get().getFee();
    final long energyUsage = infoById.get().getReceipt().getEnergyUsage();
    final long energyFee = infoById.get().getReceipt().getEnergyFee();
    String s = infoById.get().getResMessage().toStringUtf8();
    Account infoafter = PublicMethedForDailybuild.queryAccount(grammarAddress3, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
        .getAccountResource(grammarAddress3,
            blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterNetUsed = resourceInfo.getNetUsed();
    Long afterFreeNetUsed = resourceInfo.getFreeNetUsed();
    long aftereenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterenergyLimit:" + aftereenergyLimit);
    logger.info("s:" + s);
    Assert.assertThat(s, containsString("Not enough energy for"));
    Assert.assertTrue(beforeBalance - fee == afterBalance);
    PublicMethedForDailybuild
        .unFreezeBalance(grammarAddress3, testKeyForGrammarAddress3, 1, grammarAddress3,
            blockingStubFull);
    PublicMethedForDailybuild
        .freedResource(grammarAddress3, testKeyForGrammarAddress3, testNetAccountAddress,
            blockingStubFull);

  }


  @Test(enabled = true, description = "trigger require method")
  public void testGrammar004() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress4, 100000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(grammarAddress4, 100000000L,
        0, 1, testKeyForGrammarAddress4, blockingStubFull));
    Account info;
    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
        .getAccountResource(grammarAddress4,
            blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(grammarAddress4, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    long beforeenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeenergyLimit:" + beforeenergyLimit);
    String filePath = "src/test/resources/soliditycode/contractUnknownException.sol";
    String contractName = "testD";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String txid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit,
            20L, 100, null, testKeyForGrammarAddress4,
            grammarAddress4, blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    final String s = infoById.get().getResMessage().toStringUtf8();
    final long fee = infoById.get().getFee();
    long energyUsage = infoById.get().getReceipt().getEnergyUsage();
    final long energyFee = infoById.get().getReceipt().getEnergyFee();

    Account infoafter = PublicMethedForDailybuild.queryAccount(grammarAddress4, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
        .getAccountResource(grammarAddress4,
            blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterNetUsed = resourceInfo.getNetUsed();
    Long afterFreeNetUsed = resourceInfo.getFreeNetUsed();
    long aftereenergyLimit = resourceInfo.getEnergyLimit();

    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterenergyLimit:" + aftereenergyLimit);
    Assert.assertThat(s, containsString("REVERT opcode executed"));
    Assert.assertTrue(beforeBalance - fee == afterBalance);
    Assert.assertFalse(energyFee == 1000000000);
    PublicMethedForDailybuild
        .unFreezeBalance(grammarAddress4, testKeyForGrammarAddress4, 1, grammarAddress4,
            blockingStubFull);
    PublicMethedForDailybuild
        .freedResource(grammarAddress4, testKeyForGrammarAddress4, testNetAccountAddress,
            blockingStubFull);
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
