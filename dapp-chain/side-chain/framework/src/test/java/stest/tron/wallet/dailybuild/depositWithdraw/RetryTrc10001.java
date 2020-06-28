package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.protos.Protocol.TransactionInfo.code.FAILED;

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
public class RetryTrc10001 {


  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
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
  private final String testKeyFordeposit2 = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key5");
  private final byte[] depositAddress2 = PublicMethed.getFinalAddress(testKeyFordeposit2);
  private final String testOracle = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracle);
  private final byte[] gateWaySideOwnerAddress = PublicMethed
      .getFinalAddress(gateWatOwnerSideAddressKey);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);


  private final String tokenFundtionKey = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key6");
  private final byte[] tokenFundtionAddress = PublicMethed.getFinalAddress(tokenFundtionKey);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  ByteString assetAccountId;
  String nonce = null;
  String nonceWithdraw = null;
  String parame1 = null;
  String methodStr2 = null;
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 31000_000_000L, tokenFundtionAddress, tokenFundtionKey,
            blockingStubFull));
    assetAccountId = PublicMethed
        .queryAccount(tokenFundtionAddress, blockingStubFull).getAssetIssuedID();

    Assert.assertTrue(
        PublicMethed
            .transferAsset(depositAddress, assetAccountId.toByteArray(), 1000000,
                tokenFundtionAddress, tokenFundtionKey, blockingStubFull));

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

  }

  @Test(enabled = true, description = "Withdraw Trc10 normal and Withdraw Trc10 with account exception.")
  public void test1RetryTrc10001() {

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBalance = accountMainBefore.getBalance();
    logger.info("The token ID: " + assetAccountId.toStringUtf8());
    Long depositMainTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Long depositSideTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);
    logger.info("depositMainTokenBefore:" + depositMainTokenBefore);
    logger.info("depositSideAddressTokenBefore:" + depositSideTokenBefore);
    Assert.assertTrue(depositSideTokenBefore == 0);

    String methodStr = "depositTRC10(uint64,uint64)";

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 10;
    String inputParam = inputTokenID + "," + inputTokenValue;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById;
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    nonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountMainAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBalance - fee);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
    Assert.assertEquals(0, accountSideAfterBalance);
    Long depositSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(inputTokenValue == depositSideTokenAfter);
    Long depositMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenBefore - inputTokenValue == depositMainTokenAfter);
    logger.info("depositMainTokenAfter:" + depositMainTokenAfter);
    logger.info("depositSideTokenAfter:" + depositSideTokenAfter);
    Assert.assertTrue(depositSideTokenAfter == inputTokenValue);

    String methodStr1 = "depositTRX()";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, "", false));

    long callValue1 = 1500000000;
    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue1,
            input1,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    //withdrawTrc10
    String withdrawToken = Long.toString(inputTokenValue - 5);
    String txid2 = PublicMethed
        .withdrawTrc10(inputTokenID, withdrawToken, chainIdAddress,
            sideGatewayAddress,
            0,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    long fee2 = infoById2.get().getFee();
    nonceWithdraw = ByteArray.toHexString(infoById2.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);
    logger.info("fee2:" + fee2);
    Account accountWithdrawSideAfter = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountWithdrawSideAfterBalance = accountWithdrawSideAfter.getBalance();
    ByteString addressWithdrawSideAfter = accountWithdrawSideAfter.getAddress();
    String addressWithdrawSideAfterAddress = Base58
        .encode58Check(addressWithdrawSideAfter.toByteArray());
    logger.info("addressWithdrawSideAfterAddress:" + addressWithdrawSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressWithdrawSideAfterAddress);
    Assert.assertEquals(callValue1, accountWithdrawSideAfterBalance);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    long withdrawSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("withdrawSideTokenAfter:" + withdrawSideTokenAfter);
    logger.info("withdrawMainTokenAfter:" + withdrawMainTokenAfter);
    Assert.assertTrue(5 == withdrawSideTokenAfter);
    Assert.assertTrue(depositMainTokenAfter + inputTokenValue - 5 == withdrawMainTokenAfter);

    //retry deposit trc10 with no retryfee
    String retryDepositTxid = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long depositSideTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long depositMainTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(withdrawMainTokenAfter, depositMainTokenAfterRetry);
    Assert.assertEquals(withdrawSideTokenAfter, depositSideTokenAfterRetry);

    //retry  Withdraw  trc10 with no retryfee

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    long infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    long withdrawSideTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(depositMainTokenAfterRetry, withdrawMainTokenAfterRetry);
    Assert.assertEquals(depositSideTokenAfterRetry, withdrawSideTokenAfterRetry);

    methodStr2 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame1 = String.valueOf(setRetryFee);

    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input2,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr3 = "retryFee()";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input3, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(parame1));
    logger.info("retryFee:" + retryFee1);
    //bonus
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    Account accountBeforeRetry = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeRetryBalance = accountBeforeRetry.getBalance();
    logger.info("accountBeforeRetryBalance:" + accountBeforeRetryBalance);
    //retry deposit trc10 with = retryfee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();

    long depositSideTokenAfterRetry1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long depositMainTokenAfterRetry1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(depositMainTokenAfterRetry, depositMainTokenAfterRetry1);
    Assert.assertEquals(depositSideTokenAfterRetry, depositSideTokenAfterRetry1);

    Account accountAfterRetry = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountAfterRetryBalance = accountAfterRetry.getBalance();
    logger.info("accountAfterRetryBalance:" + accountAfterRetryBalance);
    Assert.assertEquals(accountBeforeRetryBalance - infoByIdretryDepositFee - setRetryFee,
        accountAfterRetryBalance);

    //bonus
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + setRetryFee, bonusBefore1);

    //retry deposit trc10 with > retryfee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee + 1, maxFeeLimit, depositAddress, testKeyFordeposit,
        blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long infoByIdretryDepositFee1 = infoByIdretryDeposit.get().getFee();

    long depositSideTokenAfterRetry2 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long depositMainTokenAfterRetry2 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(depositMainTokenAfterRetry2, depositMainTokenAfterRetry1);
    Assert.assertEquals(depositSideTokenAfterRetry2, depositSideTokenAfterRetry1);

    Account accountAfterRetry1 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountAfterRetryBalance1 = accountAfterRetry1.getBalance();
    logger.info("accountAfterRetryBalance1:" + accountAfterRetryBalance1);
    Assert.assertEquals(accountAfterRetryBalance - infoByIdretryDepositFee1 - setRetryFee,
        accountAfterRetryBalance1);

    //bonus
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Assert.assertEquals(bonusBefore1 + setRetryFee, bonusBefore2);

    //retry deposit trc10 with <retryfee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee - 1, maxFeeLimit, depositAddress, testKeyFordeposit,
        blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);
    infoByIdretryDepositFee1 = infoByIdretryDeposit.get().getFee();

    long depositSideTokenAfterRetry3 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long depositMainTokenAfterRetry3 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(depositMainTokenAfterRetry2, depositMainTokenAfterRetry3);
    Assert.assertEquals(depositSideTokenAfterRetry2, depositSideTokenAfterRetry3);

    Account accountAfterRetry2 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountAfterRetryBalance2 = accountAfterRetry2.getBalance();
    logger.info("accountAfterRetryBalance2:" + accountAfterRetryBalance2);
    Assert.assertEquals(accountAfterRetryBalance1 - infoByIdretryDepositFee1,
        accountAfterRetryBalance2);

    //bonus
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore3:" + bonusBefore3);
    Assert.assertEquals(bonusBefore2, bonusBefore3);

//set side retryfee
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

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);
//retryWithdraw <setFee
    Account accountSideBeforeWithdraw0 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance0 = accountSideBeforeWithdraw0.getBalance();
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFeeSide - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 1);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    long withdrawSideTokenAfterRetry1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfterRetry1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(withdrawMainTokenAfterRetry1, withdrawMainTokenAfterRetry);
    Assert.assertEquals(withdrawSideTokenAfterRetry1, withdrawSideTokenAfterRetry);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter1, bonusSideAfter);
    Account accountSideBeforeWithdraw1 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance1 = accountSideBeforeWithdraw1.getBalance();
    Assert.assertEquals(accountSideBeforeWithdrawBalance0 - infoByIdretryWithdrawFee,
        accountSideBeforeWithdrawBalance1);

    //retryWithdraw =setFee
    Account accountSideBeforeWithdraw2 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance2 = accountSideBeforeWithdraw2.getBalance();
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFeeSide,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    long withdrawSideTokenAfterRetry2 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfterRetry2 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(withdrawMainTokenAfterRetry1, withdrawMainTokenAfterRetry2);
    Assert.assertEquals(withdrawSideTokenAfterRetry1, withdrawSideTokenAfterRetry2);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter2:" + bonusSideAfter2);
    Assert.assertEquals(bonusSideAfter1 + setRetryFeeSide, bonusSideAfter2);
    Account accountSideBeforeWithdraw3 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance3 = accountSideBeforeWithdraw3.getBalance();
    Assert.assertEquals(
        accountSideBeforeWithdrawBalance2 - infoByIdretryWithdrawFee - setRetryFeeSide,
        accountSideBeforeWithdrawBalance3);

    //retryWithdraw >setFee
    Account accountSideBeforeWithdraw4 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance4 = accountSideBeforeWithdraw4.getBalance();
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFeeSide + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    logger.info("infoByIdretryWithdrawFee:" + infoByIdretryWithdrawFee);

    long withdrawSideTokenAfterRetry3 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfterRetry3 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(withdrawMainTokenAfterRetry3, withdrawMainTokenAfterRetry2);
    Assert.assertEquals(withdrawSideTokenAfterRetry3, withdrawSideTokenAfterRetry2);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter3:" + bonusSideAfter3);
    Assert.assertEquals(bonusSideAfter2 + setRetryFeeSide, bonusSideAfter3);

    Account accountSideBeforeWithdraw5 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance5 = accountSideBeforeWithdraw5.getBalance();
    Assert.assertEquals(
        accountSideBeforeWithdrawBalance4 - infoByIdretryWithdrawFee - setRetryFeeSide,
        accountSideBeforeWithdrawBalance5);


  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc10 with nonce exception ")
  public void test2RetryTrc10002() {
    methodStr2 = "setRetryFee(uint256)";
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));
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
        nonce, maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingStubFull);
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
        nonceWithdraw,maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdWithdrawDeposit = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingStubFull);
    Assert.assertTrue(infoByIdWithdrawDeposit.get().getResultValue() == 0);

    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountMainAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterRetryBalance);
    //Deposit noce value
    String bigNonce = "100000";
    String retryDepositTxid2 = PublicMethed.retryDeposit(mainGateWayAddress,
        bigNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid2:" + retryDepositTxid2);
    Optional<TransactionInfo> infoByIdretryDepositTxid2 = PublicMethed
        .getTransactionInfoById(retryDepositTxid2, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid2.get().getResMessage().toStringUtf8());

    //Withdraw noce value
    String retryWithdrawTxid2 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        bigNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid2:" + retryWithdrawTxid2);
    Optional<TransactionInfo> infoByIdretryWithdrawTxid2 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid2, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdrawTxid2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryWithdrawTxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdrawTxid2.get().getResMessage().toStringUtf8());

    //Deposit noce value is 1*10**20
    String smallNonce = "0000000000000000000000000000000000000000000000056bc75e2d63100000";
    logger.info("smallNonce:" + smallNonce);
    String retryDepositTxid3 = PublicMethed.retryDeposit(mainGateWayAddress,
        smallNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid3:" + retryDepositTxid3);
    Optional<TransactionInfo> infoByIdretryDepositTxid3 = PublicMethed
        .getTransactionInfoById(retryDepositTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid3.get().getResultValue() == 0);

    //Withdraw noce value is 1*10**20
    String retryWithdrawTxid3 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        smallNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid2:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid3 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid3.get().getResultValue() == 0);

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
  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc10 with mainOralce value is 0")
  public void test3RetryTrc10003() {
    methodStr2 = "setRetryFee(uint256)";
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));
    Long depositMainTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Long depositSideTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress, 100000000,
        0, 0, testOracle, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Account oracleMainBeforeSend = PublicMethed.queryAccount(testOracleAddress, blockingStubFull);
    long oracleMainBeforeSendBalance = oracleMainBeforeSend.getBalance();

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, oracleMainBeforeSendBalance, testOracleAddress, testOracle,
            blockingStubFull));
    AccountResourceMessage oracleMainBeforeDeposit = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingStubFull);
    long oracleMainBeforeDepositEnergyLimit = oracleMainBeforeDeposit.getEnergyLimit();
    long oracleMainBeforeDepositEnergyUsage = oracleMainBeforeDeposit.getEnergyUsed();
    long oracleMainBeforeDepositNetUsed = oracleMainBeforeDeposit.getNetUsed();
    long oracleMainBeforeDepositNetLimit = oracleMainBeforeDeposit.getNetLimit();
    Assert.assertEquals(oracleMainBeforeDepositEnergyLimit, 0);
    Assert.assertEquals(oracleMainBeforeDepositEnergyUsage, 0);
    Assert.assertTrue(oracleMainBeforeDepositNetUsed < oracleMainBeforeDepositNetLimit);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10(uint64,uint64)";

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 10;
    String inputParam = inputTokenID + "," + inputTokenValue;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById;
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    nonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);

    Long depositSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(depositSideTokenBefore + inputTokenValue == depositSideTokenAfter);
    Long depositMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenBefore - inputTokenValue == depositMainTokenAfter);
    logger.info("depositMainTokenAfter:" + depositMainTokenAfter);
    logger.info("depositSideTokenAfter:" + depositSideTokenAfter);

    //withdrawTrc10
    String withdrawToken = Long.toString(3);
    String txid2 = PublicMethed
        .withdrawTrc10(inputTokenID, withdrawToken, chainIdAddress,
            sideGatewayAddress,
            0,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    long fee2 = infoById2.get().getFee();
    nonceWithdraw = ByteArray.toHexString(infoById2.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);

    Long depositSideTokenAfterWithdraw = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(depositSideTokenAfter - 3 == depositSideTokenAfterWithdraw);
    Long depositMainTokenAfterWithdraw = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertEquals(depositMainTokenAfter, depositMainTokenAfterWithdraw);
    logger.info("depositSideTokenAfterWithdraw:" + depositSideTokenAfterWithdraw);
    logger.info("depositMainTokenAfterWithdraw:" + depositMainTokenAfterWithdraw);

    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress, oracleMainBeforeSendBalance - 200000, depositAddress2,
            testKeyFordeposit2,
            blockingStubFull));

    methodStrSide = "setRetryFee(uint256)";
    setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);
    inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter3:" + bonusSideAfter3);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //retry  Withdraw  trc10 <setRetryFeeSide

    String retryWithdrawTxid = PublicMethed
        .retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
            nonceWithdraw, setRetryFeeSide - 1,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 1);
    Account accountSideBeforeWithdraw4 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance4 = accountSideBeforeWithdraw4.getBalance();

    //retry  Withdraw  trc10 >setRetryFeeSide

    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFeeSide + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    long fee1 = infoByIdretryWithdraw.get().getFee();

    Long depositSideTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(depositSideTokenAfterRetry == depositSideTokenAfterWithdraw);
    Long depositMainTokenAfterWithdrawRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenAfterWithdraw + 3 == depositMainTokenAfterWithdrawRetry);
    logger.info("depositSideTokenAfterRetry:" + depositSideTokenAfterRetry);
    logger.info("depositMainTokenAfterWithdrawRetry:" + depositMainTokenAfterWithdrawRetry);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter4 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter4:" + bonusSideAfter4);
    Assert.assertEquals(bonusSideAfter3 + setRetryFeeSide, bonusSideAfter4);

    Account accountSideBeforeWithdraw5 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance5 = accountSideBeforeWithdraw5.getBalance();
    Assert.assertEquals(accountSideBeforeWithdrawBalance4 - fee1 - setRetryFeeSide,
        accountSideBeforeWithdrawBalance5);
  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc10 with sideOralce value is 0")
  public void test4RetryTrc10004() {

    methodStr2 = "setRetryFee(uint256)";
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

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

    Long depositMainTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Long depositSideTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress, 100000000,
        3, 0, testOracle, chainIdAddressKey, blockingSideStubFull));

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Account oracleSideBeforeSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleSideBeforeSendBalance = oracleSideBeforeSend.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeSendBalance, testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleMainBeforeDeposit = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingSideStubFull);
    long oracleMainBeforeDepositEnergyLimit = oracleMainBeforeDeposit.getEnergyLimit();
    long oracleMainBeforeDepositEnergyUsage = oracleMainBeforeDeposit.getEnergyUsed();
    long oracleMainBeforeDepositNetUsed = oracleMainBeforeDeposit.getNetUsed();
    long oracleMainBeforeDepositNetLimit = oracleMainBeforeDeposit.getNetLimit();
    Assert.assertEquals(oracleMainBeforeDepositEnergyLimit, 0);
    Assert.assertEquals(oracleMainBeforeDepositEnergyUsage, 0);
    Assert.assertTrue(oracleMainBeforeDepositNetUsed < oracleMainBeforeDepositNetLimit);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String methodStr = "depositTRC10(uint64,uint64)";

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 10;
    String inputParam = inputTokenID + "," + inputTokenValue;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById;
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);

    // check Deposit Msg when deposit failed
    depositNonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("depositNonce:" + depositNonce);
    String[] Msg = {
        WalletClient.encode58Check(depositAddress), "" + inputTokenValue,
        "1", "0", inputTokenID, "0", "0"
    };
    Assert.assertTrue(PublicMethed.checkDepositMsg(depositNonce, mainGateWayAddress, depositAddress,
        testKeyFordeposit, blockingStubFull, Msg));

    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);

    Long depositSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(depositSideTokenBefore == depositSideTokenAfter);
    Long depositMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenBefore - inputTokenValue == depositMainTokenAfter);
    logger.info("depositMainTokenAfter:" + depositMainTokenAfter);
    logger.info("depositSideTokenAfter:" + depositSideTokenAfter);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));

    methodStr2 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame1 = String.valueOf(setRetryFee);

    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input2,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr3 = "retryFee()";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input3, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(parame1));
    logger.info("retryFee:" + retryFee1);
    //bonus
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    logger.info("sideOracle balance :" + PublicMethed.queryAccount(testOracleAddress,
        blockingSideStubFull).getBalance());
    logger.info("mainOracle balance :" + PublicMethed.queryAccount(testOracleAddress,
        blockingStubFull).getBalance());
    try {
      Thread.sleep(30000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Account account = PublicMethed.queryAccount(depositAddress,blockingStubFull);
    Long beforeRetryDepostiBalance = account.getBalance();

    //retry deposit trc10 with <setRetryFee

    String retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        depositNonce, setRetryFee - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);


    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);

    Account AfterRetryDeposit = PublicMethed.queryAccount(depositAddress,blockingStubFull);

    Assert.assertEquals(AfterRetryDeposit.getBalance(),
        beforeRetryDepostiBalance - infoByIdretryDeposit.get().getFee());

    //retry deposit trc10 with =setRetryFee
    Account accountBeforeRetry = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeRetryBalance = accountBeforeRetry.getBalance();
    logger.info("accountBeforeRetryBalance:" + accountBeforeRetryBalance);
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    fee1 = infoByIdretryDeposit.get().getFee();

    long depositSideTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long depositMainTokenAfterRetry = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    Assert.assertEquals(depositMainTokenAfter.longValue(), depositMainTokenAfterRetry);
    Assert.assertEquals(depositSideTokenAfter, inputTokenValue, depositSideTokenAfterRetry);
    //bonus
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input4,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + setRetryFee, bonusBefore1);
    Account accountAfterRetry = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountAfterRetryBalance = accountAfterRetry.getBalance();
    logger.info("accountAfterRetryBalance:" + accountAfterRetryBalance);
    Assert.assertEquals(accountBeforeRetryBalance - setRetryFee - fee1, accountAfterRetryBalance);

    Account oracleSideBeforeWithdrawSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleSideBeforeWithdrawSendBalance = oracleSideBeforeWithdrawSend.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeWithdrawSendBalance,
            testOracleAddress,
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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    //withdrawTrc10
    String withdrawToken = Long.toString(3);
    String txid2 = PublicMethed
        .withdrawTrc10(inputTokenID, withdrawToken, chainIdAddress,
            sideGatewayAddress,
            0,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);

    // check Withdraw Msg when withdraw failed
    withdrawNonce = ByteArray.toHexString(infoById2.get().getContractResult(0).toByteArray());
    logger.info("withdrawNonce:" + withdrawNonce);
    String[] MsgWithdraw = {
        WalletClient.encode58Check(depositAddress),
        "0", inputTokenID, withdrawToken, "1", "0"
    };
    Assert
        .assertTrue(PublicMethed.checkWithdrawMsg(withdrawNonce, sideGatewayAddress, depositAddress,
            testKeyFordeposit, blockingSideStubFull, MsgWithdraw));

    Assert.assertTrue(infoById2.get().getResultValue() == 0);

    Long depositSideTokenAfterWithdraw = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertEquals(depositSideTokenAfterRetry - 3, depositSideTokenAfterWithdraw.longValue());
    Long depositMainTokenAfterWithdraw = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertEquals(depositMainTokenAfterRetry, depositMainTokenAfterWithdraw.longValue());
    logger.info("depositSideTokenAfterWithdraw:" + depositSideTokenAfterWithdraw);
    logger.info("depositMainTokenAfterWithdraw:" + depositMainTokenAfterWithdraw);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeWithdrawSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));

    //set side retryfee
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

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter:" + bonusSideAfter);

    try {
      Thread.sleep(30000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    Account accountWithdraw = PublicMethed.queryAccount(depositAddress,blockingSideStubFull);
    Long beforeRetryWithdrawBalance = accountWithdraw.getBalance();
    //retry  Withdraw  trc10 <setRetryFee

    String retryWithdrawTxid = PublicMethed
        .retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
            withdrawNonce, setRetryFeeSide - 1,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 1);

    Account AfterRetryWithdraw = PublicMethed.queryAccount(depositAddress,blockingSideStubFull);

    Assert.assertEquals(AfterRetryWithdraw.getBalance(),
        beforeRetryWithdrawBalance - infoByIdretryWithdraw.get().getFee());

    //retry  Withdraw  trc10 >setRetryFee

    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        withdrawNonce, setRetryFeeSide + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);

    Long depositSideTokenAfterRetryWithdraw = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertEquals(depositSideTokenAfterWithdraw, depositSideTokenAfterRetryWithdraw);
    Long depositMainTokenAfterRetryWithdraw = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertEquals(depositMainTokenAfterWithdraw + 3,
        depositMainTokenAfterRetryWithdraw.longValue());
    logger.info("depositMainTokenAfterRetryWithdraw:" + depositMainTokenAfterRetryWithdraw);
    logger.info("depositSideTokenAfterRetryWithdraw:" + depositSideTokenAfterRetryWithdraw);

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

    methodStr2 = "setRetryFee(uint256)";
    parame1 = "0";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame1, false));

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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
