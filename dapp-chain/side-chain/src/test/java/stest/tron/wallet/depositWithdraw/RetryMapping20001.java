package stest.tron.wallet.depositWithdraw;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class RetryMapping20001 {


  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String gateWatOwnerSideAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String testOracle = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracle);
  private final String testOracle2 = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key2");
  private final byte[] testOracleAddress2 = PublicMethed.getFinalAddress(testOracle2);
  private final String testOracle3 = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key3");
  private final byte[] testOracleAddress3 = PublicMethed.getFinalAddress(testOracle3);
  private final String testOracle4 = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key4");
  private final byte[] testOracleAddress4 = PublicMethed.getFinalAddress(testOracle4);
  private final byte[] gateWaySideOwnerAddress = PublicMethed
      .getFinalAddress(gateWatOwnerSideAddressKey);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);

  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] depositAddress2 = ecKey2.getAddress();
  String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  ECKey ecKey3 = new ECKey(Utils.getRandom());
  byte[] depositAddress3 = ecKey3.getAddress();
  String testKeyFordeposit3 = ByteArray.toHexString(ecKey3.getPrivKeyBytes());
  ECKey ecKey4 = new ECKey(Utils.getRandom());
  byte[] depositAddress4 = ecKey4.getAddress();
  String testKeyFordeposit4 = ByteArray.toHexString(ecKey4.getPrivKeyBytes());
  ECKey ecKey5 = new ECKey(Utils.getRandom());
  byte[] depositAddress5 = ecKey5.getAddress();
  String testKeyFordeposit5 = ByteArray.toHexString(ecKey5.getPrivKeyBytes());
  String methodStrSide = null;
  String parameSide1 = null;
  String nonce = null;
  String nonceMap = null;
  String nonceWithdraw = null;
  byte[] trc20Contract = null;
  byte[] sideContractAddress = null;
  long oracleMainBeforeSendBalance = 0;
  long oracleMainBeforeSendBalance2 = 0;
  long oracleMainBeforeSendBalance3 = 0;
  long oracleMainBeforeSendBalance4 = 0;
  long oracleSideBeforeSendBalance = 0;
  long oracleSideBeforeSendBalance2 = 0;
  long oracleSideBeforeSendBalance3 = 0;
  long oracleSideBeforeSendBalance4 = 0;
  String parame1 = null;
  String methodStr2 = null;
  String contractName = "trc20Contract";
  String code = Configuration.getByPath("testng.conf")
      .getString("code.code_ContractTRC20");
  String abi = Configuration.getByPath("testng.conf")
      .getString("abi.abi_ContractTRC20");
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
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
    PublicMethed.printAddress(testKeyFordeposit2);
    PublicMethed.printAddress(testKeyFordeposit3);
    PublicMethed.printAddress(testKeyFordeposit4);
    PublicMethed.printAddress(testKeyFordeposit5);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
    methodStr2 = "setRetryFee(uint256)";
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideChainAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
  }

  @Test(enabled = true, description = "RetryMapping with mainOralce value is 0")
  public void test1RetryMapping20001() {

    PublicMethed.printAddress(testKeyFordeposit);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 11000_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
    Assert.assertEquals(callValue, accountSideBeforeBalance);

    String parame = "\"" + Base58.encode58Check(depositAddress) + "\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, testKeyFordeposit, depositAddress,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress, 10000000,
        0, 0, testOracle, blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress2, 10000000,
        0, 0, testOracle2, blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress3, 10000000,
        0, 0, testOracle3, blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress4, 10000000,
        0, 0, testOracle4, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account oracleMainBeforeSend = PublicMethed.queryAccount(testOracleAddress, blockingStubFull);
    oracleMainBeforeSendBalance = oracleMainBeforeSend.getBalance();

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, oracleMainBeforeSendBalance, testOracleAddress, testOracle,
            blockingStubFull));
    AccountResourceMessage oracleMainBeforeWithdraw = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingStubFull);
    long oracleMainBeforeWithdrawnEnergyLimit = oracleMainBeforeWithdraw.getEnergyLimit();
    long oracleMainBeforeWithdrawEnergyUsage = oracleMainBeforeWithdraw.getEnergyUsed();
    long oracleMainBeforeWithdrawNetUsed = oracleMainBeforeWithdraw.getNetUsed();
    long oracleMainBeforeWithdrawNetLimit = oracleMainBeforeWithdraw.getNetLimit();
    Assert.assertEquals(oracleMainBeforeWithdrawnEnergyLimit, 0);
    Assert.assertEquals(oracleMainBeforeWithdrawEnergyUsage, 0);
    Assert.assertTrue(oracleMainBeforeWithdrawNetUsed < oracleMainBeforeWithdrawNetLimit);

    Account oracleMainBeforeSend2 = PublicMethed.queryAccount(testOracleAddress2, blockingStubFull);
    oracleMainBeforeSendBalance2 = oracleMainBeforeSend2.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress3, oracleMainBeforeSendBalance2, testOracleAddress2, testOracle2,
            blockingStubFull));
    AccountResourceMessage oracleMainBeforeWithdraw2 = PublicMethed
        .getAccountResource(testOracleAddress2,
            blockingStubFull);
    long oracleMainBeforeWithdrawnEnergyLimit2 = oracleMainBeforeWithdraw2.getEnergyLimit();
    long oracleMainBeforeWithdrawEnergyUsage2 = oracleMainBeforeWithdraw2.getEnergyUsed();
    long oracleMainBeforeWithdrawNetUsed2 = oracleMainBeforeWithdraw2.getNetUsed();
    long oracleMainBeforeWithdrawNetLimit2 = oracleMainBeforeWithdraw2.getNetLimit();
    Assert.assertEquals(oracleMainBeforeWithdrawnEnergyLimit2, 0);
    Assert.assertEquals(oracleMainBeforeWithdrawEnergyUsage2, 0);
    Assert.assertTrue(oracleMainBeforeWithdrawNetUsed2 < oracleMainBeforeWithdrawNetLimit2);

    Account oracleMainBeforeSend3 = PublicMethed.queryAccount(testOracleAddress3, blockingStubFull);
    oracleMainBeforeSendBalance3 = oracleMainBeforeSend3.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress4, oracleMainBeforeSendBalance3, testOracleAddress3, testOracle3,
            blockingStubFull));
    AccountResourceMessage oracleMainBeforeWithdraw3 = PublicMethed
        .getAccountResource(testOracleAddress3,
            blockingStubFull);
    long oracleMainBeforeWithdrawnEnergyLimit3 = oracleMainBeforeWithdraw3.getEnergyLimit();
    long oracleMainBeforeWithdrawEnergyUsage3 = oracleMainBeforeWithdraw3.getEnergyUsed();
    long oracleMainBeforeWithdrawNetUsed3 = oracleMainBeforeWithdraw3.getNetUsed();
    long oracleMainBeforeWithdrawNetLimit3 = oracleMainBeforeWithdraw3.getNetLimit();
    Assert.assertEquals(oracleMainBeforeWithdrawnEnergyLimit3, 0);
    Assert.assertEquals(oracleMainBeforeWithdrawEnergyUsage3, 0);
    Assert.assertTrue(oracleMainBeforeWithdrawNetUsed3 < oracleMainBeforeWithdrawNetLimit3);

    Account oracleMainBeforeSend4 = PublicMethed.queryAccount(testOracleAddress4, blockingStubFull);
    oracleMainBeforeSendBalance4 = oracleMainBeforeSend4.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress5, oracleMainBeforeSendBalance4, testOracleAddress4, testOracle4,
            blockingStubFull));
    AccountResourceMessage oracleMainBeforeWithdraw4 = PublicMethed
        .getAccountResource(testOracleAddress4,
            blockingStubFull);
    long oracleMainBeforeWithdrawnEnergyLimit4 = oracleMainBeforeWithdraw4.getEnergyLimit();
    long oracleMainBeforeWithdrawEnergyUsage4 = oracleMainBeforeWithdraw4.getEnergyUsed();
    long oracleMainBeforeWithdrawNetUsed4 = oracleMainBeforeWithdraw4.getNetUsed();
    long oracleMainBeforeWithdrawNetLimit4 = oracleMainBeforeWithdraw4.getNetLimit();
    Assert.assertEquals(oracleMainBeforeWithdrawnEnergyLimit4, 0);
    Assert.assertEquals(oracleMainBeforeWithdrawEnergyUsage4, 0);
    Assert.assertTrue(oracleMainBeforeWithdrawNetUsed4 < oracleMainBeforeWithdrawNetLimit4);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    nonceMap = Hex.toHexString(infoById1.get().getLogList()
            .get(infoById1.get().getLogCount() - 1).getData().toByteArray())
            .substring(192);
    logger.info("nonceMap:" + nonceMap);
    String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            depositAddress, testKeyFordeposit, blockingSideStubFull);

    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertNotNull(sideContractAddress);
    Assert.assertNotEquals(addressFinal, "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");

    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress, oracleMainBeforeSendBalance - 200000, depositAddress2,
            testKeyFordeposit2,
            blockingStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress2, oracleMainBeforeSendBalance2 - 200000, depositAddress3,
            testKeyFordeposit3,
            blockingStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress3, oracleMainBeforeSendBalance3 - 200000, depositAddress4,
            testKeyFordeposit4,
            blockingStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress4, oracleMainBeforeSendBalance4 - 200000, depositAddress5,
            testKeyFordeposit5,
            blockingStubFull));

  }


  @Test(enabled = true, description = "RetryMapping with sideOralce value is 0")
  public void test2RetryMapping20002() {

    String parame = "\"" + Base58.encode58Check(depositAddress) + "\"";
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress, 100000000,
        3, 0, testOracle, chainIdAddressKey, blockingSideStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress2, 10000000,
        3, 0, testOracle2,chainIdAddressKey, blockingSideStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress3, 10000000,
        3, 0, testOracle3,chainIdAddressKey, blockingSideStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress4, 10000000,
        3, 0, testOracle4,chainIdAddressKey, blockingSideStubFull));
    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, testKeyFordeposit, depositAddress,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    Account oracleSideBeforeSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    oracleSideBeforeSendBalance = oracleSideBeforeSend.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeSendBalance, testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeDeposit = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingSideStubFull);
    long oracleSideBeforeDepositEnergyLimit = oracleSideBeforeDeposit.getEnergyLimit();
    long oracleSideBeforeDepositUsage = oracleSideBeforeDeposit.getEnergyUsed();
    long oracleSideBeforeDepositNetUsed = oracleSideBeforeDeposit.getNetUsed();
    long oracleSideBeforeDepositNetLimit = oracleSideBeforeDeposit.getNetLimit();
    Assert.assertEquals(oracleSideBeforeDepositEnergyLimit, 0);
    Assert.assertEquals(oracleSideBeforeDepositUsage, 0);
    Assert.assertTrue(oracleSideBeforeDepositNetUsed < oracleSideBeforeDepositNetLimit);

    Account oracleSideBeforeSend2 = PublicMethed
        .queryAccount(testOracleAddress2, blockingSideStubFull);
    oracleSideBeforeSendBalance2 = oracleSideBeforeSend2.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress3, oracleSideBeforeSendBalance2, testOracleAddress2,
            testOracle2, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeDeposit2 = PublicMethed
        .getAccountResource(testOracleAddress2,
            blockingSideStubFull);
    long oracleSideBeforeDepositEnergyLimit2 = oracleSideBeforeDeposit2.getEnergyLimit();
    long oracleSideBeforeDepositUsage2 = oracleSideBeforeDeposit2.getEnergyUsed();
    long oracleSideBeforeDepositNetUsed2 = oracleSideBeforeDeposit2.getNetUsed();
    long oracleSideBeforeDepositNetLimit2 = oracleSideBeforeDeposit2.getNetLimit();
    Assert.assertEquals(oracleSideBeforeDepositEnergyLimit2, 0);
    Assert.assertEquals(oracleSideBeforeDepositUsage2, 0);
    Assert.assertTrue(oracleSideBeforeDepositNetUsed2 < oracleSideBeforeDepositNetLimit2);

    Account oracleSideBeforeSend3 = PublicMethed
        .queryAccount(testOracleAddress3, blockingSideStubFull);
    oracleSideBeforeSendBalance3 = oracleSideBeforeSend3.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress4, oracleSideBeforeSendBalance3, testOracleAddress3,
            testOracle3, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeDeposit3 = PublicMethed
        .getAccountResource(testOracleAddress3,
            blockingSideStubFull);
    long oracleSideBeforeDepositEnergyLimit3 = oracleSideBeforeDeposit3.getEnergyLimit();
    long oracleSideBeforeDepositUsage3 = oracleSideBeforeDeposit3.getEnergyUsed();
    long oracleSideBeforeDepositNetUsed3 = oracleSideBeforeDeposit3.getNetUsed();
    long oracleSideBeforeDepositNetLimit3 = oracleSideBeforeDeposit3.getNetLimit();
    Assert.assertEquals(oracleSideBeforeDepositEnergyLimit3, 0);
    Assert.assertEquals(oracleSideBeforeDepositUsage3, 0);
    Assert.assertTrue(oracleSideBeforeDepositNetUsed3 < oracleSideBeforeDepositNetLimit3);

    Account oracleSideBeforeSend4 = PublicMethed
        .queryAccount(testOracleAddress4, blockingSideStubFull);
    oracleSideBeforeSendBalance4 = oracleSideBeforeSend4.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress5, oracleSideBeforeSendBalance4, testOracleAddress4,
            testOracle4, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeDeposit4 = PublicMethed
        .getAccountResource(testOracleAddress4,
            blockingSideStubFull);
    long oracleSideBeforeDepositEnergyLimit4 = oracleSideBeforeDeposit4.getEnergyLimit();
    long oracleSideBeforeDepositUsage4 = oracleSideBeforeDeposit4.getEnergyUsed();
    long oracleSideBeforeDepositNetUsed4 = oracleSideBeforeDeposit4.getNetUsed();
    long oracleSideBeforeDepositNetLimit4 = oracleSideBeforeDeposit4.getNetLimit();
    Assert.assertEquals(oracleSideBeforeDepositEnergyLimit4, 0);
    Assert.assertEquals(oracleSideBeforeDepositUsage4, 0);
    Assert.assertTrue(oracleSideBeforeDepositNetUsed4 < oracleSideBeforeDepositNetLimit4);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    nonceMap = Hex.toHexString(infoById1.get().getLogList()
            .get(infoById1.get().getLogCount() - 1).getData().toByteArray())
            .substring(192);
    logger.info("nonceMap:" + nonceMap);

    // check Deposit Msg when deposit failed
    String mappingNonce = nonceMap;
    String[] Msg = {
        WalletClient.encode58Check(trc20Contract), "2", "0"
    };
    Assert.assertTrue(PublicMethed.checkMappingMsg(mappingNonce, mainChainAddress, depositAddress,
        testKeyFordeposit, blockingStubFull, Msg));

    String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            depositAddress, testKeyFordeposit, blockingSideStubFull);

    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(sideContractAddress);
    Assert.assertEquals(addressFinal, "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress2, oracleSideBeforeSendBalance2 - 200000,
            depositAddress3,
            testKeyFordeposit3, chainIdAddressKey,
            blockingSideStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress3, oracleSideBeforeSendBalance3 - 200000,
            depositAddress4,
            testKeyFordeposit4, chainIdAddressKey,
            blockingSideStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress4, oracleSideBeforeSendBalance4 - 200000,
            depositAddress5,
            testKeyFordeposit5, chainIdAddressKey,
            blockingSideStubFull));

    methodStr2 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame1 = String.valueOf(setRetryFee);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStrSide2 = "retryFee()";
    byte[] inputSide2 = Hex.decode(AbiUtil.parseMethod(methodStrSide2, "", false));

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //retry mapping <setRetryFeeSide
    String retryMaptxid = PublicMethed.retryMapping(mainChainAddress,
        nonceMap,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid:" + retryMaptxid);
    Optional<TransactionInfo> infoByIdretryMaptxid = PublicMethed
        .getTransactionInfoById(retryMaptxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid.get().getResultValue() == 1);

    //retry mapping trc10 >setRetryFeeSide

    Account accountBeforeMap = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeMapBalance = accountBeforeMap.getBalance();
    retryMaptxid = PublicMethed.retryMappingForRetryFee(mainChainAddress,
        nonceMap, setRetryFee + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("retryDepositTxid:" + retryMaptxid);
    infoByIdretryMaptxid = PublicMethed
        .getTransactionInfoById(retryMaptxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid.get().getResultValue() == 0);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info(Hex.toHexString(return3.getConstantResult(0).toByteArray()));
    String ContractRestule2 = Hex.toHexString(return3.getConstantResult(0).toByteArray());
    long fee2 = infoByIdretryMaptxid.get().getFee();

    String tmpAddress2 = ContractRestule2.substring(24);
    logger.info(tmpAddress2);
    String addressHex2 = "41" + tmpAddress2;
    logger.info("address_hex: " + addressHex2);
    String addressFinal2 = Base58.encode58Check(ByteArray.fromHexString(addressHex2));
    logger.info("address_final: " + addressFinal2);

    sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal2);
    Assert.assertNotNull(sideContractAddress);
    Assert.assertNotEquals(addressFinal2, "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter + setRetryFee, bonusSideAfter1);

    Account accountAfterMap = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountAfterMapBalance = accountAfterMap.getBalance();

    Assert.assertEquals(accountBeforeMapBalance - fee2 - setRetryFee, accountAfterMapBalance);

  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {

    Account depositAddress2MainAccount = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull);
    long depositAddress2MainBalance = depositAddress2MainAccount.getBalance();
    logger.info("depositAddress2MainBalance:" + depositAddress2MainBalance);
    if (depositAddress2MainBalance > 2000000) {
      Assert.assertTrue(PublicMethed
          .sendcoin(testOracleAddress, depositAddress2MainBalance - 1000000, depositAddress2,
              testKeyFordeposit2,
              blockingStubFull));
    }
    Account depositAddress2SideAccount = PublicMethed
        .queryAccount(depositAddress2, blockingSideStubFull);
    long depositAddress2SideBalance = depositAddress2SideAccount.getBalance();
    logger.info("depositAddress2SideBalance:" + depositAddress2SideBalance);
    if (depositAddress2SideBalance > 2000000) {
      Assert.assertTrue(PublicMethed
          .sendcoinForSidechain(testOracleAddress, depositAddress2SideBalance - 1000000,
              depositAddress2,
              testKeyFordeposit2, chainIdAddressKey, blockingSideStubFull));
    }

    PublicMethed
        .sendcoin(testOracleAddress2, oracleMainBeforeSendBalance2 - 200000, depositAddress3,
            testKeyFordeposit3,
            blockingStubFull);
    PublicMethed
        .sendcoinForSidechain(testOracleAddress2, oracleSideBeforeSendBalance2 - 200000,
            depositAddress3,
            testKeyFordeposit3, chainIdAddressKey,
            blockingSideStubFull);

    PublicMethed
        .sendcoin(testOracleAddress3, oracleMainBeforeSendBalance3 - 200000, depositAddress4,
            testKeyFordeposit4,
            blockingStubFull);
    PublicMethed
        .sendcoinForSidechain(testOracleAddress3, oracleSideBeforeSendBalance3 - 200000,
            depositAddress4,
            testKeyFordeposit4, chainIdAddressKey,
            blockingSideStubFull);

    PublicMethed
        .sendcoin(testOracleAddress4, oracleMainBeforeSendBalance4 - 200000, depositAddress5,
            testKeyFordeposit5,
            blockingStubFull);
    PublicMethed
        .sendcoinForSidechain(testOracleAddress4, oracleSideBeforeSendBalance4 - 200000,
            depositAddress5,
            testKeyFordeposit5, chainIdAddressKey,
            blockingSideStubFull);
    methodStr2 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame1 = String.valueOf(setRetryFee);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
