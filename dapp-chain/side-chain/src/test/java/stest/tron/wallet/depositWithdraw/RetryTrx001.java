package stest.tron.wallet.depositWithdraw;

import static org.tron.protos.Protocol.TransactionInfo.code.FAILED;
import static org.tron.protos.Protocol.TransactionInfo.code.SUCESS;

import com.google.protobuf.ByteString;
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
import org.tron.api.WalletSolidityGrpc;
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
public class RetryTrx001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  final String gateWatOwnerSideAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  private final byte[] gateWaySideOwnerAddress = PublicMethed
      .getFinalAddress(gateWatOwnerSideAddressKey);
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
  ECKey ecKey = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey.getPrivKeyBytes());
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress1 = ecKey1.getAddress();
  String testKeyFordeposit1 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
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
  String nonce = null;
  String nonceWithdraw = null;
  String methodStr1 = null;
  String parame1 = null;
  String methodStrSide = null;
  String parameSide1 = null;
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String depositNonce;
  private String withdrawNonce;

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
    PublicMethed.printAddress(testKeyFordeposit);
    PublicMethed.printAddress(testKeyFordeposit1);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
    parame1 = "0";
    methodStr1 = "setRetryFee(uint256)";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx")
  public void test1RetryTrx001() {

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 2000000000L);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("transferTokenContractAddress:" + mainGateWayAddress);
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1500000000;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid:" + txid);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);

    nonce = Hex.toHexString(infoById.get().getLogList()
            .get(infoById.get().getLogCount() - 1).getData().toByteArray())
            .substring(192);
    logger.info("nonce:" + nonce);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
    Assert.assertEquals(1500000000, accountSideAfterBalance);

    logger.info("sideGatewayAddress:" + sideGatewayAddress);
    long withdrawValue = 1;
    String txid1 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid1:" + txid1);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    nonceWithdraw = Hex.toHexString(infoById1.get().getLogList()
            .get(infoById1.get().getLogCount() - 1).getData().toByteArray())
            .substring(128);
    logger.info("nonceWithdraw:" + nonceWithdraw);

    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterBalance - fee1 - withdrawValue,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterBalance + withdrawValue);

    //retry deposit trx with no retryfee
    String retryDepositTxid = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance = accountMainAfterRetry.getBalance();
    logger.info("accountMainAfterRetryBalance:" + accountMainAfterRetryBalance);

    Assert.assertEquals(accountMainAfterWithdrawBalance - infoByIdretryDepositFee,
        accountMainAfterRetryBalance);

    Account accountSideAfterRetry = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterRetryBalance = accountSideAfterRetry.getBalance();
    logger.info("accountSideAfterRetryBalance:" + accountSideAfterRetryBalance);

    Assert.assertEquals(accountSideAfterWithdrawBalance,
        accountSideAfterRetryBalance);
    //retry  Withdraw trx with no retryfee

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    long infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    //setRetryFee
    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame1 = String.valueOf(setRetryFee);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "retryFee()";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input2, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(parame1));
    logger.info("retryFee:" + retryFee1);
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    //retry deposit trx, =setRetryFee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry1 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance1 = accountMainAfterRetry1.getBalance();
    logger.info("accountMainAfterRetryBalance1:" + accountMainAfterRetryBalance1);

    Assert.assertEquals(accountMainAfterRetryBalance - infoByIdretryDepositFee - setRetryFee,
        accountMainAfterRetryBalance1);

    Account accountSideAfteRetry1 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterRetryBalance1 = accountSideAfteRetry1.getBalance();
    logger.info("accountSideAfterRetryBalance1:" + accountSideAfterRetryBalance1);

    Assert.assertEquals(accountSideAfterRetryBalance,
        accountSideAfterRetryBalance1);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + setRetryFee, bonusBefore1);

    //retry deposit trx, <setRetryFee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);
    infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance2 = accountMainAfterRetry2.getBalance();
    logger.info("accountMainAfterRetryBalance2:" + accountMainAfterRetryBalance2);

    Assert.assertEquals(accountMainAfterRetryBalance1 - infoByIdretryDepositFee,
        accountMainAfterRetryBalance2);

    Account accountSideAfteRetry2 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterRetryBalance2 = accountSideAfteRetry2.getBalance();
    logger.info("accountSideAfterRetryBalance2:" + accountSideAfterRetryBalance2);

    Assert.assertEquals(accountSideAfterRetryBalance1,
        accountSideAfterRetryBalance2);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Assert.assertEquals(bonusBefore1, bonusBefore2);

    //retry deposit trx, >setRetryFee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry3 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance3 = accountMainAfterRetry3.getBalance();
    logger.info("accountMainAfterRetryBalance3:" + accountMainAfterRetryBalance3);

    Assert.assertEquals(accountMainAfterRetryBalance2 - infoByIdretryDepositFee - setRetryFee,
        accountMainAfterRetryBalance3);

    Account accountSideAfteRetry3 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterRetryBalance3 = accountSideAfteRetry3.getBalance();
    logger.info("accountSideAfterRetryBalance3:" + accountSideAfterRetryBalance3);

    Assert.assertEquals(accountSideAfterRetryBalance2,
        accountSideAfterRetryBalance3);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore3:" + bonusBefore3);
    Assert.assertEquals(bonusBefore2 + setRetryFee, bonusBefore3);

    //retryWithdraw setretryfee
    Account accountMainAfterWithdraw1 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance1 = accountMainAfterWithdraw1.getBalance();

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStrSide2 = "retryFee()";
    byte[] inputSide2 = Hex.decode(AbiUtil.parseMethod(methodStrSide2, "", false));

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

//retryWithdraw <setFee
    Account accountSideBeforeWithdraw0 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance0 = accountSideBeforeWithdraw0.getBalance();
    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideBefore);

    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFee - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 1);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);
    Assert.assertEquals(bonusSideAfter, bonusSideBefore);

    Account accountSideAfterWithdraw0 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance0 = accountSideAfterWithdraw0.getBalance();
    Assert.assertEquals(accountSideBeforeWithdrawBalance0 - infoByIdretryWithdrawFee,
        accountSideAfterWithdrawBalance0);

    //retryWithdraw=setFee
    Account accountSideBeforeWithdraw1 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance1 = accountSideBeforeWithdraw1.getBalance();
    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore1:" + bonusSideBefore1);

    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFee,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter1, bonusSideBefore1 + setRetryFee);

    Account accountSideBeforeWithdraw2 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance2 = accountSideBeforeWithdraw2.getBalance();
    Assert.assertEquals(accountSideBeforeWithdrawBalance1 - setRetryFee - infoByIdretryWithdrawFee,
        accountSideBeforeWithdrawBalance2);

    //retryWithdraw>setFee
    Account accountSideAfterWithdraw1 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance1 = accountSideAfterWithdraw1.getBalance();
    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore2:" + bonusSideBefore2);

    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFee + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore2:" + bonusSideAfter2);
    Assert.assertEquals(bonusSideAfter2, bonusSideBefore2 + setRetryFee);

    Account accountSideAfterWithdraw2 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance2 = accountSideAfterWithdraw2.getBalance();
    Assert.assertEquals(accountSideAfterWithdrawBalance1 - infoByIdretryWithdrawFee - setRetryFee,
        accountSideAfterWithdrawBalance2);
    Account accountMainAfterWithdraw3 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance3 = accountMainAfterWithdraw3.getBalance();
    Assert.assertEquals(accountMainAfterWithdrawBalance1,
        accountMainAfterWithdrawBalance3);

  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with nonce exception ")
  public void test2RetryTrx002() {
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));

    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    //other account Retry Deposit
    Account accountMainBeforeRetry = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeRetryBalance = accountMainBeforeRetry.getBalance();
    logger.info("accountMainBeforeRetryBalance:" + accountMainBeforeRetryBalance);
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] depositAddress2 = ecKey2.getAddress();
    String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    String retryDepositTxid1 = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce,
        maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid1);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance = accountMainAfterRetry.getBalance();
    logger.info("accountMainAfterRetryBalance:" + accountMainAfterRetryBalance);
    Assert.assertEquals(accountMainBeforeRetryBalance,
        accountMainAfterRetryBalance);

    //other account Retry Withdraw
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1300000000;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdWithdrawDeposit = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingStubFull);
    Assert.assertTrue(infoByIdWithdrawDeposit.get().getResultValue() == 0);

    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountMainAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterRetryBalance);

    //Deposit noce value is 1*10**20
    String initialNonce = "0000000000000000000000000000000000000000000000056bc75e2d63100000";
    logger.info("initialNonce:" + initialNonce);
    String retryDepositTxid2 = PublicMethed.retryDeposit(mainGateWayAddress,
        initialNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid2:" + retryDepositTxid2);
    Optional<TransactionInfo> infoByIdretryDepositTxid2 = PublicMethed
        .getTransactionInfoById(retryDepositTxid2, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid2.get().getResultValue() == 0);

    //Withdraw noce value is 1*10**20
    String retryWithdrawTxid2 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        initialNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid2:" + retryWithdrawTxid2);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid2 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid2, blockingSideStubFull);
    Assert.assertEquals(1, infoByIdrretryWithdrawTxid2.get().getResultValue());
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid2.get().getResMessage().toStringUtf8());

    //Deposit noce value is 0
    String smallNonce = "0000000000000000000000000000000000000000000000000000000000000000";
    logger.info("smallNonce:" + smallNonce);
    String retryDepositTxid3 = PublicMethed.retryDeposit(mainGateWayAddress,
        smallNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid3:" + retryDepositTxid3);
    Optional<TransactionInfo> infoByIdretryDepositTxid3 = PublicMethed
        .getTransactionInfoById(retryDepositTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid3.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid3.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid3.get().getResMessage().toStringUtf8());

    //Withdraw noce value is 0
    String retryWithdrawTxid3 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        smallNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid3:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid3 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid3, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdrretryWithdrawTxid3.get().getResultValue());

    //Deposit noce value is 1*10**20+1*10**6（nonexistent）
    String maxNonce = "0000000000000000000000000000000000000000000000056bc75e2d636b8d80";
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid4 = PublicMethed.retryDeposit(mainGateWayAddress,
        maxNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid4:" + retryDepositTxid4);
    Optional<TransactionInfo> infoByIdretryDepositTxid4 = PublicMethed
        .getTransactionInfoById(retryDepositTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid4.get().getResMessage().toStringUtf8());

    //Withdraw noce value is 1*10**20+1*10**6（nonexistent）
    String retryWithdrawTxid4 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        maxNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid2:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid4 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid4, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid4.get().getResMessage().toStringUtf8());

    //Deposit noce value is -1
    String minusNonce = PublicMethed.numToHex64(-1L);
    logger.info("minusNonce:" + minusNonce);
    String retryDepositTxid6 = PublicMethed.retryDeposit(mainGateWayAddress,
        minusNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid4:" + retryDepositTxid6);
    Optional<TransactionInfo> infoByIdretryDepositTxid6 = PublicMethed
        .getTransactionInfoById(retryDepositTxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid6.get().getResMessage().toStringUtf8());

    //Withdraw noce value is -1
    String retryWithdrawTxid6 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        minusNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid6:" + retryWithdrawTxid6);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid6 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid6, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid6.get().getResMessage().toStringUtf8());
  }


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with mainOralce value is 0")
  public void test3RetryTrx003() {

    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress1, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 2000000000L);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("mainGateWayAddress:" + mainGateWayAddress);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1500000000;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress1, testKeyFordeposit1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid:" + txid);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);

    nonce = Hex.toHexString(infoById.get().getLogList()
            .get(infoById.get().getLogCount() - 1).getData().toByteArray())
            .substring(192);
    logger.info("nonce:" + nonce);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress1), accountSideAfterAddress);
    Assert.assertEquals(1500000000, accountSideAfterBalance);

    logger.info("sideGatewayAddress:" + sideGatewayAddress);

    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress, 10000000,
        0, 0, testOracle, blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress2, 10000000,
        0, 0, testOracle2, blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress3, 10000000,
        0, 0, testOracle3, blockingStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress4, 10000000,
        0, 0, testOracle4, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account oracleMainBeforeSend = PublicMethed.queryAccount(testOracleAddress, blockingStubFull);
    long oracleMainBeforeSendBalance = oracleMainBeforeSend.getBalance();

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
    long oracleMainBeforeSendBalance2 = oracleMainBeforeSend2.getBalance();
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
    long oracleMainBeforeSendBalance3 = oracleMainBeforeSend3.getBalance();
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
    long oracleMainBeforeSendBalance4 = oracleMainBeforeSend4.getBalance();
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

    long withdrawValue = 1;
    String txid1 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull,
            blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid1:" + txid1);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    nonceWithdraw = Hex.toHexString(infoById1.get().getLogList()
            .get(infoById1.get().getLogCount() - 1).getData().toByteArray())
            .substring(128);
    logger.info("nonceWithdraw:" + nonceWithdraw);

    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress1), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterBalance - fee1 - 1,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterBalance);

    //retry  Withdraw trx

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
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Account oracleMainAfterSend = PublicMethed.queryAccount(testOracleAddress, blockingStubFull);
    long oracleMainAfterSendBalance = oracleMainAfterSend.getBalance();
    logger.info("oracleMainAfterSendBalance:" + oracleMainAfterSendBalance);

    methodStrSide = "setRetryFee(uint256)";
    setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);

    inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStrSide2 = "retryFee()";
    byte[] inputSide2 = Hex.decode(AbiUtil.parseMethod(methodStrSide2, "", false));

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

    //bonus
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);

    Account accountSideBeforeRetryWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideBeforeRetryWithdrawBalance = accountSideBeforeRetryWithdraw.getBalance();

    //retryWithdraw =setRetryFeeSide
    String retryWithdrawTxid = PublicMethed
        .retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
            nonceWithdraw, setRetryFeeSide,
            maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingSideStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    long infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainAfterRetryWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterRetryWithdrawBalance = accountMainAfterRetryWithdraw.getBalance();
    Account accountSideAfterRetryWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterRetryWithdrawBalance = accountSideAfterRetryWithdraw.getBalance();
    Assert.assertEquals(
        accountSideBeforeRetryWithdrawBalance - infoByIdretryWithdrawFee - setRetryFeeSide,
        accountSideAfterRetryWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance + 1,
        accountMainAfterRetryWithdrawBalance);

    //bonus
    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter + setRetryFeeSide, bonusSideAfter1);
  }


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with sideOralce value is 0")
  public void test4RetryTrx004() {
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide1 = 0;
    String parameSiden = String.valueOf(setRetryFeeSide1);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSiden, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);

    PublicMethed.printAddress(testKeyFordeposit);
    PublicMethed.printAddress(testKeyFordeposit1);
    PublicMethed.printAddress(testKeyFordeposit2);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress1, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("mainGateWayAddress:" + mainGateWayAddress);
    PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress, 100000000,
        3, 0, testOracle, chainIdAddressKey, blockingSideStubFull);
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress2, 100000000,
        3, 0, testOracle2,chainIdAddressKey, blockingSideStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress3, 100000000,
        3, 0, testOracle3,chainIdAddressKey, blockingSideStubFull));
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress4, 100000000,
        3, 0, testOracle4,chainIdAddressKey, blockingSideStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account oracleMainBeforeSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleMainBeforeSendBalance = oracleMainBeforeSend.getBalance();

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleMainBeforeSendBalance, testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeWithdraw = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingSideStubFull);
    long oracleSideBeforeWithdrawnEnergyLimit = oracleSideBeforeWithdraw.getEnergyLimit();
    long oracleSideBeforeWithdrawEnergyUsage = oracleSideBeforeWithdraw.getEnergyUsed();
    long oracleSideBeforeWithdrawNetUsed = oracleSideBeforeWithdraw.getNetUsed();
    long oracleSideBeforeWithdrawNetLimit = oracleSideBeforeWithdraw.getNetLimit();
    Assert.assertEquals(oracleSideBeforeWithdrawnEnergyLimit, 0);
    Assert.assertEquals(oracleSideBeforeWithdrawEnergyUsage, 0);
    Assert.assertTrue(oracleSideBeforeWithdrawNetUsed < oracleSideBeforeWithdrawNetLimit);

    Account oracleSideBeforeSend2 = PublicMethed
        .queryAccount(testOracleAddress2, blockingSideStubFull);
    long oracleSideBeforeSendBalance2 = oracleSideBeforeSend2.getBalance();
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
    long oracleSideBeforeSendBalance3 = oracleSideBeforeSend3.getBalance();
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
    long oracleSideBeforeSendBalance4 = oracleSideBeforeSend4.getBalance();
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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1500000000;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress1, testKeyFordeposit1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid:" + txid);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    // check Deposit Msg when deposit failed
    depositNonce = Hex.toHexString(infoById.get().getLogList()
            .get(infoById.get().getLogCount() - 1).getData().toByteArray())
            .substring(192);
    String[] Msg = {
        WalletClient.encode58Check(depositAddress1), "" + callValue,
        "0", "0", "0", "0", "0"
    };
    Assert
        .assertTrue(PublicMethed.checkDepositMsg(depositNonce, mainGateWayAddress, depositAddress1,
            testKeyFordeposit1, blockingStubFull, Msg));

    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);

    nonce = Hex.toHexString(infoById.get().getLogList()
            .get(infoById.get().getLogCount() - 1).getData().toByteArray())
            .substring(192);
    logger.info("nonce:" + nonce);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    ByteString addressMainAfter = accountMainAfter.getAddress();
    String accountMainAfterAddress = Base58.encode58Check(addressMainAfter.toByteArray());
    logger.info("accountMainAfterAddress:" + accountMainAfterAddress);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
//    Assert.assertEquals(Base58.encode58Check(depositAddress1), accountSideAfterAddress);
    Assert.assertEquals(accountSideBeforeBalance, accountSideAfterBalance);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleMainBeforeSendBalance - 200000,
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
    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame1 = String.valueOf(setRetryFee);

    input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "retryFee()";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input2, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(parame1));
    logger.info("retryFee:" + retryFee1);
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey,
            blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //retry deposit trx <setRetryFee
    String retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee - 1,
        maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);

    //retry deposit trx =setRetryFee
    Account accountMainAfter1 = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterBalance1 = accountMainAfter1.getBalance();
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee,
        maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterRetryBalance = accountMainAfterRetry.getBalance();
    logger.info("accountMainAfterRetryBalance:" + accountMainAfterRetryBalance);

    Assert.assertEquals(accountMainAfterBalance1 - infoByIdretryDepositFee - setRetryFee,
        accountMainAfterRetryBalance);

    Account accountSideAfteRetry = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterRetryBalance = accountSideAfteRetry.getBalance();
    logger.info("accountSideAfterRetryBalance:" + accountSideAfterRetryBalance);

    Assert.assertEquals(accountSideAfterBalance + callValue,
        accountSideAfterRetryBalance);
    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey,
            blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + 2, bonusBefore1);

    Account oracleSideBeforeSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleSideBeforeSendBalance = oracleSideBeforeSend.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeSendBalance, testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));

    Account oracleSideBeforeWithdrawSend2 = PublicMethed
        .queryAccount(testOracleAddress2, blockingSideStubFull);
    long oracleSideBeforeWithdrawSendBalance2 = oracleSideBeforeWithdrawSend2.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress3, oracleSideBeforeWithdrawSendBalance2,
            testOracleAddress2,
            testOracle2, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeWithdraw2 = PublicMethed
        .getAccountResource(testOracleAddress2,
            blockingSideStubFull);
    long oracleSideBeforeWithdrawnEnergyLimit2 = oracleSideBeforeWithdraw2.getEnergyLimit();
    long oracleSideBeforeWithdrawEnergyUsage2 = oracleSideBeforeWithdraw2.getEnergyUsed();
    long oracleSideBeforeWithdrawNetUsed2 = oracleSideBeforeWithdraw2.getNetUsed();
    long oracleSideBeforeWithdrawNetLimit2 = oracleSideBeforeWithdraw2.getNetLimit();
    Assert.assertEquals(oracleSideBeforeWithdrawnEnergyLimit2, 0);
    Assert.assertEquals(oracleSideBeforeWithdrawEnergyUsage2, 0);
    Assert.assertTrue(oracleSideBeforeWithdrawNetUsed2 < oracleSideBeforeWithdrawNetLimit2);

    Account oracleSideBeforeWithdrawSend3 = PublicMethed
        .queryAccount(testOracleAddress3, blockingSideStubFull);
    long oracleSideBeforeWithdrawSendBalance3 = oracleSideBeforeWithdrawSend3.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress4, oracleSideBeforeWithdrawSendBalance3,
            testOracleAddress3,
            testOracle3, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeWithdraw3 = PublicMethed
        .getAccountResource(testOracleAddress3,
            blockingSideStubFull);
    long oracleSideBeforeWithdrawnEnergyLimit3 = oracleSideBeforeWithdraw3.getEnergyLimit();
    long oracleSideBeforeWithdrawEnergyUsage3 = oracleSideBeforeWithdraw3.getEnergyUsed();
    long oracleSideBeforeWithdrawNetUsed3 = oracleSideBeforeWithdraw3.getNetUsed();
    long oracleSideBeforeWithdrawNetLimit3 = oracleSideBeforeWithdraw3.getNetLimit();
    Assert.assertEquals(oracleSideBeforeWithdrawnEnergyLimit3, 0);
    Assert.assertEquals(oracleSideBeforeWithdrawEnergyUsage3, 0);
    Assert.assertTrue(oracleSideBeforeWithdrawNetUsed3 < oracleSideBeforeWithdrawNetLimit3);

    Account oracleSideBeforeWithdrawSend4 = PublicMethed
        .queryAccount(testOracleAddress4, blockingSideStubFull);
    long oracleSideBeforeWithdrawSendBalance4 = oracleSideBeforeWithdrawSend4.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress5, oracleSideBeforeWithdrawSendBalance4,
            testOracleAddress4,
            testOracle4, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeWithdraw4 = PublicMethed
        .getAccountResource(testOracleAddress4,
            blockingSideStubFull);
    long oracleSideBeforeWithdrawnEnergyLimit4 = oracleSideBeforeWithdraw4.getEnergyLimit();
    long oracleSideBeforeWithdrawEnergyUsage4 = oracleSideBeforeWithdraw4.getEnergyUsed();
    long oracleSideBeforeWithdrawNetUsed4 = oracleSideBeforeWithdraw4.getNetUsed();
    long oracleSideBeforeWithdrawNetLimit4 = oracleSideBeforeWithdraw4.getNetLimit();
    Assert.assertEquals(oracleSideBeforeWithdrawnEnergyLimit4, 0);
    Assert.assertEquals(oracleSideBeforeWithdrawEnergyUsage4, 0);
    Assert.assertTrue(oracleSideBeforeWithdrawNetUsed4 < oracleSideBeforeWithdrawNetLimit4);

    // withdrawTrx
    logger.info("sideGatewayAddress:" + sideGatewayAddress);

    long withdrawValue = 1;
    txid1 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull,
            blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid1:" + txid1);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    logger.info("infoById1:" + infoById1);

    // check Withdraw Msg when withdraw failed
    withdrawNonce = Hex.toHexString(infoById1.get().getLogList()
            .get(infoById1.get().getLogCount() - 1).getData().toByteArray())
            .substring(128);
    String[] MsgWithdraw = {
        WalletClient.encode58Check(depositAddress1),
        "0", "0", "" + withdrawValue, "0", "0"
    };
    Assert.assertTrue(
        PublicMethed.checkWithdrawMsg(withdrawNonce, sideGatewayAddress, depositAddress1,
            testKeyFordeposit1, blockingSideStubFull, MsgWithdraw));

    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    nonceWithdraw = Hex.toHexString(infoById1.get().getLogList()
            .get(infoById1.get().getLogCount() - 1).getData().toByteArray())
            .substring(128);
    logger.info("nonceWithdraw:" + nonceWithdraw);

    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress1), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterRetryBalance - fee1 - withdrawValue,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterRetryBalance);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeSendBalance - 100000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress2, oracleSideBeforeWithdrawSendBalance2 - 200000,
            depositAddress3,
            testKeyFordeposit3, chainIdAddressKey,
            blockingSideStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress3, oracleSideBeforeWithdrawSendBalance3 - 200000,
            depositAddress4,
            testKeyFordeposit4, chainIdAddressKey,
            blockingSideStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress4, oracleSideBeforeWithdrawSendBalance4 - 200000,
            depositAddress5,
            testKeyFordeposit5, chainIdAddressKey,
            blockingSideStubFull));

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);

    inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStrSide2 = "retryFee()";
    byte[] inputSide2 = Hex.decode(AbiUtil.parseMethod(methodStrSide2, "", false));

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

    //bonus
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response2.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter:" + bonusSideAfter);

    Account accountMainBeforeRetryWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingStubFull);
    long accountMainBeforeRetryWithdrawBalance = accountMainBeforeRetryWithdraw.getBalance();

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //retry  Withdraw trx >setRetryFeeSide

    Account oracleMainAfterSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleMainAfterSendBalance = oracleMainAfterSend.getBalance();
    logger.info("oracleMainAfterSendBalance:" + oracleMainAfterSendBalance);

    String retryWithdrawTxid = PublicMethed
        .retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
            nonceWithdraw, setRetryFeeSide + 1,
            maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingSideStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    long infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainAfterRetryWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterRetryWithdrawBalance = accountMainAfterRetryWithdraw.getBalance();
    Account accountSideAfterRetryWithdraw = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterRetryWithdrawBalance = accountSideAfterRetryWithdraw.getBalance();
    Assert
        .assertEquals(accountSideAfterWithdrawBalance - infoByIdretryWithdrawFee - setRetryFeeSide,
            accountSideAfterRetryWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance + withdrawValue,
        accountMainAfterRetryWithdrawBalance);

    //bonus
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response2.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter + setRetryFeeSide, bonusSideAfter1);

  }


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with sideOralce value is 0")
  public void test5RetryTrx005() {
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] depositAddress2 = ecKey2.getAddress();
    String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame1 = String.valueOf(setRetryFee);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 1);

    setRetryFee = Long.MAX_VALUE + 1;
    parame1 = String.valueOf(setRetryFee);
    input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals("FAILED", infoById1.get().getResult().name());
    Assert.assertEquals(1, infoById1.get().getResultValue());
    String msg = Hex.toHexString(infoById1.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135 + 136, 170 + 136)));
    Assert.assertEquals("\u0001less than 100 TRX", msg);

    setRetryFee = Long.MIN_VALUE - 1;
    parame1 = String.valueOf(setRetryFee);
    input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals("FAILED", infoById1.get().getResult().name());
    Assert.assertEquals(1, infoById1.get().getResultValue());
    msg = Hex.toHexString(infoById1.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135 + 136, 170 + 136)));
    Assert.assertEquals("\u0001less than 100 TRX", msg);

    setRetryFee = -1;
    parame1 = String.valueOf(setRetryFee);
    input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    //msg = Hex.toHexString(infoById1.get().getContractResult(0).toByteArray());
    //msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135 + 136, 170 + 136)));
    //Assert.assertEquals("\u0001less than 100 TRX", msg);

    setRetryFee = 100_000_001;
    parame1 = String.valueOf(setRetryFee);
    input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals("FAILED", infoById1.get().getResult().name());
    Assert.assertEquals(1, infoById1.get().getResultValue());
    msg = Hex.toHexString(infoById1.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135 + 136, 170 + 136)));
    Assert.assertEquals("\u0001less than 100 TRX", msg);

    setRetryFee = 99_999_999L;
    parame1 = String.valueOf(setRetryFee);
    input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertNotNull(txid1);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());

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
    Account depositAddress3MainAccount = PublicMethed
        .queryAccount(depositAddress3, blockingStubFull);
    long depositAddress3MainBalance = depositAddress3MainAccount.getBalance();
    logger.info("depositAddress3MainBalance:" + depositAddress3MainBalance);
    if (depositAddress3MainBalance > 3000000) {
      Assert.assertTrue(PublicMethed
          .sendcoin(testOracleAddress2, depositAddress3MainBalance - 1000000, depositAddress3,
              testKeyFordeposit3,
              blockingStubFull));
    }
    Account depositAddress3SideAccount = PublicMethed
        .queryAccount(depositAddress3, blockingSideStubFull);
    long depositAddress3SideBalance = depositAddress3SideAccount.getBalance();
    logger.info("depositAddress3SideBalance:" + depositAddress3SideBalance);
    if (depositAddress3SideBalance > 3000000) {
      Assert.assertTrue(PublicMethed
          .sendcoinForSidechain(testOracleAddress2, depositAddress3SideBalance - 1000000,
              depositAddress3,
              testKeyFordeposit3, chainIdAddressKey, blockingSideStubFull));
    }

    Account depositAddress4MainAccount = PublicMethed
        .queryAccount(depositAddress4, blockingStubFull);
    long depositAddress4MainBalance = depositAddress4MainAccount.getBalance();
    logger.info("depositAddress4MainBalance:" + depositAddress4MainBalance);
    if (depositAddress4MainBalance > 4000000) {
      Assert.assertTrue(PublicMethed
          .sendcoin(testOracleAddress3, depositAddress4MainBalance - 1000000, depositAddress4,
              testKeyFordeposit4,
              blockingStubFull));
    }
    Account depositAddress4SideAccount = PublicMethed
        .queryAccount(depositAddress4, blockingSideStubFull);
    long depositAddress4SideBalance = depositAddress4SideAccount.getBalance();
    logger.info("depositAddress4SideBalance:" + depositAddress4SideBalance);
    if (depositAddress4SideBalance > 4000000) {
      Assert.assertTrue(PublicMethed
          .sendcoinForSidechain(testOracleAddress3, depositAddress4SideBalance - 1000000,
              depositAddress4,
              testKeyFordeposit4, chainIdAddressKey, blockingSideStubFull));
    }

    Account depositAddress5MainAccount = PublicMethed
        .queryAccount(depositAddress5, blockingStubFull);
    long depositAddress5MainBalance = depositAddress5MainAccount.getBalance();
    logger.info("depositAddress5MainBalance:" + depositAddress5MainBalance);
    if (depositAddress5MainBalance > 5000000) {
      Assert.assertTrue(PublicMethed
          .sendcoin(testOracleAddress4, depositAddress5MainBalance - 1000000, depositAddress5,
              testKeyFordeposit5,
              blockingStubFull));
    }
    Account depositAddress5SideAccount = PublicMethed
        .queryAccount(depositAddress5, blockingSideStubFull);
    long depositAddress5SideBalance = depositAddress5SideAccount.getBalance();
    logger.info("depositAddress5SideBalance:" + depositAddress5SideBalance);
    if (depositAddress5SideBalance > 5000000) {
      Assert.assertTrue(PublicMethed
          .sendcoinForSidechain(testOracleAddress4, depositAddress5SideBalance - 1000000,
              depositAddress5,
              testKeyFordeposit5, chainIdAddressKey, blockingSideStubFull));
    }
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
