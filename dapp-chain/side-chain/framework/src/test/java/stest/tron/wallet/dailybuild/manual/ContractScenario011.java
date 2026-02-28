package stest.tron.wallet.dailybuild.manual;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.springframework.util.StringUtils;
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
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractScenario011 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  String kittyCoreAddressAndCut = "";
  byte[] kittyCoreContractAddress = null;
  byte[] saleClockAuctionContractAddress = null;
  byte[] siringClockAuctionContractAddress = null;
  byte[] geneScienceInterfaceContractAddress = null;
  Integer consumeUserResourcePercent = 50;
  String txid = "";
  Optional<TransactionInfo> infoById = null;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] deployAddress = ecKey1.getAddress();
  String deployKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] triggerAddress = ecKey2.getAddress();
  String triggerKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull1 = null;
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
    PublicMethedForDailybuild.printAddress(deployKey);
    PublicMethedForDailybuild.printAddress(triggerKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(deployAddress, 50000000000L, fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(triggerAddress, 50000000000L, fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "Deploy Erc721 contract \"Kitty Core\"")
  public void deployErc721KittyCore() {
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(deployAddress, 100000000L,
        0, 1, deployKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalance(deployAddress, 100000000L, 0,
        deployKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalance(triggerAddress, 100000000L, 0,
        triggerKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(deployAddress,
            blockingStubFull);
    Long cpuLimit = accountResource.getEnergyLimit();
    Long cpuUsage = accountResource.getEnergyUsed();
    Account account = PublicMethedForDailybuild.queryAccount(deployAddress, blockingStubFull);
    logger.info("before balance is " + Long.toString(account.getBalance()));
    logger.info("before cpu limit is " + Long.toString(cpuLimit));
    logger.info("before cpu usage is " + Long.toString(cpuUsage));
    String contractName = "KittyCore";
    String filePath = "./src/test/resources/soliditycode/contractScenario011.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    logger.info("Kitty Core");
    kittyCoreContractAddress = PublicMethedForDailybuild.deployContract(contractName, abi, code, "",
        maxFeeLimit, 0L, consumeUserResourcePercent, null, deployKey,
        deployAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    SmartContract smartContract = PublicMethedForDailybuild.getContract(kittyCoreContractAddress,
        blockingStubFull);
    Assert.assertFalse(StringUtils.isEmpty(smartContract.getBytecode()));

    Assert.assertTrue(smartContract.getAbi() != null);
    accountResource = PublicMethedForDailybuild.getAccountResource(deployAddress, blockingStubFull);
    cpuLimit = accountResource.getEnergyLimit();
    cpuUsage = accountResource.getEnergyUsed();
    account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("after balance is " + Long.toString(account.getBalance()));
    logger.info("after cpu limit is " + Long.toString(cpuLimit));
    logger.info("after cpu usage is " + Long.toString(cpuUsage));
    logger.info(ByteArray.toHexString(kittyCoreContractAddress));
    logger.info(ByteArray.toHexString(kittyCoreContractAddress).substring(2));

    kittyCoreAddressAndCut = "000000000000000000000000" + ByteArray
        .toHexString(kittyCoreContractAddress).substring(2);
    kittyCoreAddressAndCut = kittyCoreAddressAndCut + "0000000000000000000000000000000000000000000"
        + "000000000000000000100";
  }

  @Test(enabled = true, description = "Deploy Erc721 contract \"Sale Clock Auction\"")
  public void deploySaleClockAuction() {
    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(deployAddress,
            blockingStubFull);
    Long cpuLimit = accountResource.getEnergyLimit();
    Long cpuUsage = accountResource.getEnergyUsed();
    Account account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("before balance is " + Long.toString(account.getBalance()));
    logger.info("before cpu limit is " + Long.toString(cpuLimit));
    logger.info("before cpu usage is " + Long.toString(cpuUsage));
    String contractName = "SaleClockAuction";
    String filePath = "./src/test/resources/soliditycode/contractScenario011.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    logger.info("Sale Clock Auction");
    //saleClockAuctionContractAddress;
    String data = "\"" + Base58.encode58Check(kittyCoreContractAddress) + "\"," + 100;
    String deplTxid = PublicMethedForDailybuild
        .deployContractWithConstantParame(contractName, abi, code, "constructor(address,uint256)",
            data, "", maxFeeLimit, 0L, consumeUserResourcePercent, null, deployKey, deployAddress,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> info = PublicMethedForDailybuild
        .getTransactionInfoById(deplTxid, blockingStubFull);
    Assert.assertTrue(info.get().getResultValue() == 0);

    saleClockAuctionContractAddress = info.get().getContractAddress().toByteArray();
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    SmartContract smartContract = PublicMethedForDailybuild
        .getContract(saleClockAuctionContractAddress,
            blockingStubFull);
    Assert.assertFalse(StringUtils.isEmpty(smartContract.getBytecode()));
    Assert.assertTrue(smartContract.getAbi() != null);
    accountResource = PublicMethedForDailybuild.getAccountResource(deployAddress, blockingStubFull);
    cpuLimit = accountResource.getEnergyLimit();
    cpuUsage = accountResource.getEnergyUsed();
    account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("after balance is " + Long.toString(account.getBalance()));
    logger.info("after cpu limit is " + Long.toString(cpuLimit));
    logger.info("after cpu usage is " + Long.toString(cpuUsage));

    String triggerTxid = PublicMethedForDailybuild
        .triggerContract(saleClockAuctionContractAddress, "isSaleClockAuction()", "#", false, 0,
            maxFeeLimit, deployAddress, deployKey, blockingStubFull);
    Optional<TransactionInfo> inFoByid = PublicMethedForDailybuild
        .getTransactionInfoById(triggerTxid, blockingStubFull);
    logger.info("Ttttt " + triggerTxid);
    Assert.assertTrue(inFoByid.get().getResultValue() == 0);
  }

  @Test(enabled = true, description = "Deploy Erc721 contract \"Siring Clock Auction\"")
  public void deploySiringClockAuction() {
    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(deployAddress,
            blockingStubFull);
    Long cpuLimit = accountResource.getEnergyLimit();
    Long cpuUsage = accountResource.getEnergyUsed();
    Account account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("before balance is " + Long.toString(account.getBalance()));
    logger.info("before cpu limit is " + Long.toString(cpuLimit));
    logger.info("before cpu usage is " + Long.toString(cpuUsage));
    String contractName = "SiringClockAuction";
    String filePath = "./src/test/resources/soliditycode/contractScenario011.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String data = "\"" + Base58.encode58Check(kittyCoreContractAddress) + "\"," + 100;
    String siringClockAuctionContractAddressTxid = PublicMethedForDailybuild
        .deployContractWithConstantParame(contractName, abi, code, "constructor(address,uint256)",
            data,
            "", maxFeeLimit, 0L, consumeUserResourcePercent, null, deployKey,
            deployAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> info2 = PublicMethedForDailybuild
        .getTransactionInfoById(siringClockAuctionContractAddressTxid, blockingStubFull);
    siringClockAuctionContractAddress = info2.get().getContractAddress().toByteArray();
    Assert.assertTrue(info2.get().getResultValue() == 0);
    SmartContract smartContract = PublicMethedForDailybuild
        .getContract(siringClockAuctionContractAddress,
            blockingStubFull);
    Assert.assertFalse(StringUtils.isEmpty(smartContract.getBytecode()));
    Assert.assertTrue(smartContract.getAbi() != null);
    accountResource = PublicMethedForDailybuild.getAccountResource(deployAddress, blockingStubFull);
    cpuLimit = accountResource.getEnergyLimit();
    cpuUsage = accountResource.getEnergyUsed();
    account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("after balance is " + Long.toString(account.getBalance()));
    logger.info("after cpu limit is " + Long.toString(cpuLimit));
    logger.info("after cpu usage is " + Long.toString(cpuUsage));
  }

  @Test(enabled = true, description = "Deploy Erc721 contract \"Gene Science Interface\"")
  public void deployGeneScienceInterface() {
    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(deployAddress,
            blockingStubFull);
    Long cpuLimit = accountResource.getEnergyLimit();
    Long cpuUsage = accountResource.getEnergyUsed();
    Account account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("before balance is " + Long.toString(account.getBalance()));
    logger.info("before cpu limit is " + Long.toString(cpuLimit));
    logger.info("before cpu usage is " + Long.toString(cpuUsage));
    String contractName = "GeneScienceInterface";
    String filePath = "./src/test/resources/soliditycode/contractScenario011.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    String txid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code,
            "", maxFeeLimit,
            0L, consumeUserResourcePercent, null, deployKey, deployAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> info2 = PublicMethedForDailybuild
        .getTransactionInfoById(txid, blockingStubFull);
    geneScienceInterfaceContractAddress = info2.get().getContractAddress().toByteArray();
    Assert.assertTrue(info2.get().getResultValue() == 0);

    SmartContract smartContract = PublicMethedForDailybuild
        .getContract(geneScienceInterfaceContractAddress,
            blockingStubFull);
    Assert.assertFalse(StringUtils.isEmpty(smartContract.getBytecode()));
    Assert.assertTrue(smartContract.getAbi() != null);
    accountResource = PublicMethedForDailybuild.getAccountResource(deployAddress, blockingStubFull);
    cpuLimit = accountResource.getEnergyLimit();
    cpuUsage = accountResource.getEnergyUsed();
    account = PublicMethedForDailybuild.queryAccount(deployKey, blockingStubFull);
    logger.info("after balance is " + Long.toString(account.getBalance()));
    logger.info("after cpu limit is " + Long.toString(cpuLimit));
    logger.info("after cpu usage is " + Long.toString(cpuUsage));
  }

  @Test(enabled = true, description = "Set three contract address for Kitty Core, "
      + "set three CXO roles")
  public void triggerToSetThreeContractAddressToKittyCore() {
    //Set SaleAuctionAddress to kitty core.
    String saleContractString = "\"" + Base58.encode58Check(saleClockAuctionContractAddress) + "\"";
    txid = PublicMethedForDailybuild
        .triggerContract(kittyCoreContractAddress, "setSaleAuctionAddress(address)",
            saleContractString, false, 0, 10000000L, deployAddress, deployKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    logger.info(txid);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);

    //Set SiringAuctionAddress to kitty core.
    String siringContractString = "\"" + Base58.encode58Check(siringClockAuctionContractAddress)
        + "\"";
    txid = PublicMethedForDailybuild
        .triggerContract(kittyCoreContractAddress, "setSiringAuctionAddress(address)",
            siringContractString, false, 0, 10000000L, deployAddress, deployKey, blockingStubFull);
    logger.info(txid);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);

    //Set gen contract to kitty core
    String genContractString = "\"" + Base58.encode58Check(geneScienceInterfaceContractAddress)
        + "\"";
    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setGeneScienceAddress(address)", genContractString,
        false, 0, 10000000L, deployAddress, deployKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    logger.info(txid);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);

    //Start the game.
    Integer result = 1;
    Integer times = 0;
    while (result == 1) {
      txid = PublicMethedForDailybuild
          .triggerContract(kittyCoreContractAddress, "unpause()", "", false, 0,
              10000000L, deployAddress, deployKey, blockingStubFull);
      PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
      infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
      result = infoById.get().getResultValue();
      if (times++ == 3) {
        break;
      }
    }

    Assert.assertTrue(result == 0);
    logger.info("start the game " + txid);

    //Create one gen0 cat.
    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "createGen0Auction(uint256)", "-1000000000000000", false,
        0, 100000000L, deployAddress, deployKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "gen0CreatedCount()", "#", false,
        0, 100000000L, deployAddress, deployKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "getKitty(uint256)", "1", false, 0, 10000000, triggerAddress,
        triggerKey, blockingStubFull);
    logger.info("getKitty " + txid);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    String newCxoAddress = "\"" + Base58.encode58Check(triggerAddress)
        + "\"";

    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setCOO(address)", newCxoAddress, false, 0, 10000000, deployAddress,
        deployKey, blockingStubFull);
    logger.info("COO " + txid);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setCFO(address)", newCxoAddress, false, 0, 10000000, deployAddress,
        deployKey, blockingStubFull);
    logger.info("CFO " + txid);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setCEO(address)", newCxoAddress, false, 0, 1000000, deployAddress,
        deployKey, blockingStubFull);
    logger.info("CEO " + txid);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
  }

  @Test(enabled = true, description = "Create Gen0 cat")
  public void triggerUseTriggerEnergyUsage() {
    ECKey ecKey3 = new ECKey(Utils.getRandom());
    byte[] triggerUseTriggerEnergyUsageAddress = ecKey3.getAddress();
    final String triggerUseTriggerEnergyUsageKey = ByteArray.toHexString(ecKey3.getPrivKeyBytes());
    Assert.assertTrue(
        PublicMethedForDailybuild.sendcoin(triggerUseTriggerEnergyUsageAddress, 100000000000L,
            fromAddress, testKey002, blockingStubFull));
    String newCxoAddress = "\"" + Base58.encode58Check(triggerUseTriggerEnergyUsageAddress)
        + "\"";
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    final String txid1;
    final String txid2;
    final String txid3;
    txid1 = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setCOO(address)", newCxoAddress, false, 0, maxFeeLimit, triggerAddress,
        triggerKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    logger.info("COO " + txid);

    txid2 = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setCFO(address)", newCxoAddress, false, 0, maxFeeLimit, triggerAddress,
        triggerKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    logger.info("CFO " + txid);

    txid3 = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "setCEO(address)", newCxoAddress, false, 0, maxFeeLimit, triggerAddress,
        triggerKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    logger.info("CEO " + txid);

    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Long beforeBalance = PublicMethedForDailybuild
        .queryAccount(triggerUseTriggerEnergyUsageKey, blockingStubFull).getBalance();
    logger.info("before balance is " + Long.toString(beforeBalance));
    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "createGen0Auction(uint256)", "0", false,
        0, 100000000L, triggerUseTriggerEnergyUsageAddress, triggerUseTriggerEnergyUsageKey,
        blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull1);
    logger.info("Q " + Long
        .toString(infoById.get().getReceipt().getEnergyFee()));
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsage() == 0);
    Assert.assertTrue(infoById.get().getReceipt().getEnergyFee() > 10000);
    //    Assert.assertTrue(infoById.get().getReceipt().getOriginEnergyUsage() > 10000);
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal()
        == infoById.get().getReceipt().getEnergyFee() / 100 + infoById.get().getReceipt()
        .getOriginEnergyUsage());

    Long fee = infoById.get().getFee();
    Long afterBalance = PublicMethedForDailybuild
        .queryAccount(triggerUseTriggerEnergyUsageKey, blockingStubFull1).getBalance();
    logger.info("after balance is " + Long.toString(afterBalance));
    logger.info("fee is " + Long.toString(fee));
    Assert.assertTrue(beforeBalance == afterBalance + fee);

    logger.info("before EnergyUsage is " + infoById.get().getReceipt().getEnergyUsage());
    logger.info("before EnergyFee is " + infoById.get().getReceipt().getEnergyFee());
    logger.info("before OriginEnergyUsage is " + infoById.get().getReceipt()
        .getOriginEnergyUsage());
    logger.info("before EnergyTotal is " + infoById.get().getReceipt().getEnergyUsageTotal());

    Assert.assertTrue(
        PublicMethedForDailybuild
            .freezeBalanceGetEnergy(triggerUseTriggerEnergyUsageAddress, 100000000L,
                0, 1, triggerUseTriggerEnergyUsageKey, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    beforeBalance = PublicMethedForDailybuild
        .queryAccount(triggerUseTriggerEnergyUsageKey, blockingStubFull)
        .getBalance();
    logger.info("before balance is " + Long.toString(beforeBalance));

    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(triggerUseTriggerEnergyUsageAddress, blockingStubFull);
    Long energyLimit = accountResource.getEnergyLimit();
    logger.info("before EnergyLimit is " + Long.toString(energyLimit));

    txid = PublicMethedForDailybuild.triggerContract(kittyCoreContractAddress,
        "createGen0Auction(uint256)", "0", false,
        0, 100000000L, triggerUseTriggerEnergyUsageAddress, triggerUseTriggerEnergyUsageKey,
        blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull1);
    logger.info("after EnergyUsage is " + infoById.get().getReceipt().getEnergyUsage());
    logger.info("after EnergyFee is " + infoById.get().getReceipt().getEnergyFee());
    logger.info("after OriginEnergyUsage is " + infoById.get().getReceipt().getOriginEnergyUsage());
    logger.info("after EnergyTotal is " + infoById.get().getReceipt().getEnergyUsageTotal());
    fee = infoById.get().getFee();
    afterBalance = PublicMethedForDailybuild
        .queryAccount(triggerUseTriggerEnergyUsageKey, blockingStubFull1)
        .getBalance();
    logger.info("after balance is " + Long.toString(afterBalance));
    logger.info("fee is " + Long.toString(fee));

    accountResource = PublicMethedForDailybuild
        .getAccountResource(triggerUseTriggerEnergyUsageAddress, blockingStubFull1);
    energyLimit = accountResource.getEnergyLimit();

    logger.info("after EnergyLimit is " + Long.toString(energyLimit));

    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsage() > 10000);
    Assert.assertTrue(infoById.get().getReceipt().getEnergyFee() == 0);

    //Assert.assertTrue(infoById.get().getReceipt().getOriginEnergyUsage() > 10000);
    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsageTotal() == infoById.get()
        .getReceipt().getEnergyUsage() + infoById.get().getReceipt().getOriginEnergyUsage());
    //    Assert.assertTrue(infoById.get().getReceipt().getEnergyUsage() == infoById.get()
    //        .getReceipt().getOriginEnergyUsage());

    Assert.assertTrue(beforeBalance == afterBalance + fee);
    PublicMethedForDailybuild.unFreezeBalance(deployAddress, deployKey, 1,
        deployAddress, blockingStubFull);
    PublicMethedForDailybuild.unFreezeBalance(triggerAddress, triggerKey, 1,
        triggerAddress, blockingStubFull);

    PublicMethedForDailybuild
        .unFreezeBalance(triggerUseTriggerEnergyUsageAddress, triggerUseTriggerEnergyUsageKey, 1,
            triggerUseTriggerEnergyUsageAddress, blockingStubFull);
    PublicMethedForDailybuild
        .freedResource(triggerUseTriggerEnergyUsageAddress, triggerUseTriggerEnergyUsageKey,
            fromAddress, blockingStubFull);

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


