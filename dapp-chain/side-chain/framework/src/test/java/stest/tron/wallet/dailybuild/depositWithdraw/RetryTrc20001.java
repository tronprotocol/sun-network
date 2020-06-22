package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.protos.Protocol.TransactionInfo.code.FAILED;

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
public class RetryTrc20001 {


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
  private final String testOracle = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracle);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  private final byte[] gateWaySideOwnerAddress = PublicMethed
      .getFinalAddress(gateWatOwnerSideAddressKey);
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
  String nonce = null;
  String nonceMap = null;
  String nonceWithdraw = null;
  byte[] trc20Contract = null;
  byte[] sideContractAddress = null;
  String methodStr1 = null;
  String parame2 = null;
  String methodStrSide = null;
  String parameSide1 = null;
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "RetryTrc20")
  public void test1RetryTrc20001() {

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

    String contractName = "trc20Contract";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC20");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC20");
    String parame = "\"" + Base58.encode58Check(depositAddress) + "\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, testKeyFordeposit, depositAddress
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    nonceMap = ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonceMap);
    String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            depositAddress, testKeyFordeposit, blockingSideStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
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

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance:" + mainTrc20Balance);
    Assert.assertTrue(100000000000000000L == mainTrc20Balance);

    String depositTrc20txid = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infodepositTrc20 = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);
    Assert.assertEquals(0, infodepositTrc20.get().getResultValue());
    nonce = ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid : " + sideChainTxid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    int afterDepositSideChain = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(1000, afterDepositSideChain);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance2:" + mainTrc20Balance2);
    Assert.assertTrue(mainTrc20Balance - 1000 == mainTrc20Balance2);

    String withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc20.get().getResultValue());
    nonceWithdraw = ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);

    byte[] input4 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    int afterWithdrawsidechain = ByteArray
        .toInt(infoById3.get().getContractResult(0).toByteArray());
    Assert.assertEquals(afterDepositSideChain - 100, afterWithdrawsidechain);

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long afterWithdrawmainBalance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return4.getConstantResult(0).toByteArray())));
    logger.info("afterWithdrawmainBalance:" + afterWithdrawmainBalance);
    Assert.assertTrue(mainTrc20Balance - 900 == afterWithdrawmainBalance);

    //retry deposit trc10 with no retryfee
    String retryDepositTxid = PublicMethed.retryDeposit(mainChainAddress,
        nonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);

    TransactionExtention return5 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);

    Long afterretry = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return5.getConstantResult(0).toByteArray())));
    Assert.assertEquals(afterWithdrawmainBalance, afterretry);

    //retry mapping trc10 with no retryfee
    String retryMaptxid = PublicMethed.retryMapping(mainChainAddress,
        nonceMap, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid);
    Optional<TransactionInfo> infoByIdretryMaptxid = PublicMethed
        .getTransactionInfoById(retryMaptxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid.get().getResultValue() == 0);

    //retry withdraw trc20 with no retryfee

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideChainAddress,
        nonceWithdraw, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);

    //setRetryFee

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame2 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame2, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "retryFee()";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input5, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return6.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(parame2));
    logger.info("retryFee:" + retryFee1);
    //bonus
    byte[] input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    //retry deposit trx, =setRetryFee
    Account accountMainBeforeRetry = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeRetryBalance = accountMainBeforeRetry.getBalance();
    logger.info("accountMainBeforeRetryBalance:" + accountMainBeforeRetryBalance);

    String retryDepositTxid1 = PublicMethed.retryDepositForRetryFee(mainChainAddress,
        nonce, setRetryFee,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid1);
    Optional<TransactionInfo> infoByIdretryDeposit1 = PublicMethed
        .getTransactionInfoById(retryDepositTxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit1.get().getResultValue() == 0);
    fee1 = infoByIdretryDeposit1.get().getFee();
    TransactionExtention return7 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);

    Long afterretry1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return7.getConstantResult(0).toByteArray())));
    Assert.assertEquals(afterWithdrawmainBalance, afterretry1);

    Account accountMainAfterRetry = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance = accountMainAfterRetry.getBalance();
    logger.info("accountMainAfterRetryBalance:" + accountMainAfterRetryBalance);
    Assert.assertEquals(accountMainBeforeRetryBalance - setRetryFee - fee1,
        accountMainAfterRetryBalance);
    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter:" + bonusAfter);
    Assert.assertEquals(bonusBefore + setRetryFee, bonusAfter);

    //retry deposit trx, >=setRetryFee

    retryDepositTxid1 = PublicMethed.retryDepositForRetryFee(mainChainAddress,
        nonce, setRetryFee + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid1);
    infoByIdretryDeposit1 = PublicMethed
        .getTransactionInfoById(retryDepositTxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit1.get().getResultValue() == 0);
    fee1 = infoByIdretryDeposit1.get().getFee();
    return7 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);

    afterretry1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return7.getConstantResult(0).toByteArray())));
    Assert.assertEquals(afterWithdrawmainBalance, afterretry1);

    Account accountMainAfterRetry1 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance1 = accountMainAfterRetry1.getBalance();
    logger.info("accountMainAfterRetryBalance1:" + accountMainAfterRetryBalance1);
    Assert.assertEquals(accountMainAfterRetryBalance - setRetryFee - fee1,
        accountMainAfterRetryBalance1);
    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter:" + bonusAfter1);
    Assert.assertEquals(bonusAfter + setRetryFee, bonusAfter1);

    //retry deposit trx, <setRetryFee

    retryDepositTxid1 = PublicMethed.retryDepositForRetryFee(mainChainAddress,
        nonce, setRetryFee - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid1);
    infoByIdretryDeposit1 = PublicMethed
        .getTransactionInfoById(retryDepositTxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit1.get().getResultValue() != 0);
    fee1 = infoByIdretryDeposit1.get().getFee();

    return7 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);

    afterretry1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return7.getConstantResult(0).toByteArray())));
    Assert.assertEquals(afterWithdrawmainBalance, afterretry1);

    Account accountMainAfterRetry2 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance2 = accountMainAfterRetry2.getBalance();
    logger.info("accountMainAfterRetryBalance2:" + accountMainAfterRetryBalance2);
    Assert.assertEquals(accountMainAfterRetryBalance1 - fee1, accountMainAfterRetryBalance2);
    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter2:" + bonusAfter2);
    Assert.assertEquals(bonusAfter2, bonusAfter1);

    //retry mapping trc10 with < setRetryFee
    Account accountMainAfterRetry3 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance3 = accountMainAfterRetry3.getBalance();
    logger.info("accountMainAfterRetryBalance3:" + accountMainAfterRetryBalance3);

    String retryMaptxid1 = PublicMethed.retryMappingForRetryFee(mainChainAddress,
        nonceMap, setRetryFee - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid1);
    Optional<TransactionInfo> infoByIdretryMaptxid1 = PublicMethed
        .getTransactionInfoById(retryMaptxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid1.get().getResultValue() == 1);
    fee1 = infoByIdretryMaptxid1.get().getFee();

    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter3:" + bonusAfter3);
    Assert.assertEquals(bonusAfter2, bonusAfter3);
    Account accountMainAfterRetry4 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance4 = accountMainAfterRetry4.getBalance();
    logger.info("accountMainAfterRetryBalance4:" + accountMainAfterRetryBalance4);
    Assert.assertEquals(accountMainAfterRetryBalance3 - fee1, accountMainAfterRetryBalance4);

    //retry mapping trc10 with = setRetryFee
    Account accountMainAfterRetry5 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance5 = accountMainAfterRetry5.getBalance();
    logger.info("accountMainAfterRetryBalance5:" + accountMainAfterRetryBalance5);

    retryMaptxid1 = PublicMethed.retryMappingForRetryFee(mainChainAddress,
        nonceMap, setRetryFee,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid1);
    infoByIdretryMaptxid1 = PublicMethed
        .getTransactionInfoById(retryMaptxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid1.get().getResultValue() == 0);
    fee1 = infoByIdretryMaptxid1.get().getFee();

    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter4 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter4:" + bonusAfter4);
    Assert.assertEquals(bonusAfter3 + setRetryFee, bonusAfter4);
    Account accountMainAfterRetry6 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance6 = accountMainAfterRetry6.getBalance();
    logger.info("accountMainAfterRetryBalance6:" + accountMainAfterRetryBalance6);
    Assert.assertEquals(accountMainAfterRetryBalance5 - fee1 - setRetryFee,
        accountMainAfterRetryBalance6);

    //retry mapping trc10 with > setRetryFee
    Account accountMainAfterRetry7 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance7 = accountMainAfterRetry7.getBalance();
    logger.info("accountMainAfterRetryBalance7:" + accountMainAfterRetryBalance7);

    retryMaptxid1 = PublicMethed.retryMappingForRetryFee(mainChainAddress,
        nonceMap, setRetryFee + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid1);
    infoByIdretryMaptxid1 = PublicMethed
        .getTransactionInfoById(retryMaptxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid1.get().getResultValue() == 0);
    fee1 = infoByIdretryMaptxid1.get().getFee();

    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter5 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter5:" + bonusAfter5);
    Assert.assertEquals(bonusAfter4 + setRetryFee, bonusAfter5);
    Account accountMainAfterRetry8 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterRetryBalance8 = accountMainAfterRetry8.getBalance();
    logger.info("accountMainAfterRetryBalance8:" + accountMainAfterRetryBalance8);
    Assert.assertEquals(accountMainAfterRetryBalance7 - fee1 - setRetryFee,
        accountMainAfterRetryBalance8);

    //retry withdraw trc20 with no retryfee

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideChainAddress),
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

    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey,
            blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);
    //<setRetryFeeSide
    Account accountSideBeforeWithdraw0 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance0 = accountSideBeforeWithdraw0.getBalance();
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideChainAddress,
        nonceWithdraw, setRetryFeeSide - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 1);
    long infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter, bonusSideAfter1);
    Account accountSideBeforeWithdraw1 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance1 = accountSideBeforeWithdraw1.getBalance();
    Assert.assertEquals(accountSideBeforeWithdrawBalance0 - infoByIdretryWithdrawFee,
        accountSideBeforeWithdrawBalance1);
    //=setRetryFeeSide

    Account accountSideBeforeWithdraw2 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance2 = accountSideBeforeWithdraw2.getBalance();
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideChainAddress,
        nonceWithdraw, setRetryFeeSide,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();
    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress),
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

    //>setRetryFeeSide
    Account accountSideBeforeWithdraw4 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeWithdrawBalance4 = accountSideBeforeWithdraw4.getBalance();
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideChainAddress,
        nonceWithdraw, setRetryFeeSide + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
    infoByIdretryWithdrawFee = infoByIdretryWithdraw.get().getFee();

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress),
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

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with nonce exception ")
  public void test2RetryTrc20002() {
    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame2 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame2, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideChainAddress),
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
    String retryDepositTxid1 = PublicMethed.retryDeposit(mainChainAddress,
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

    //Deposit noce value is 0
    String smallNonce = "0000000000000000000000000000000000000000000000000000000000000000";
    logger.info("smallNonce:" + smallNonce);
    String retryDepositTxid3 = PublicMethed.retryDeposit(mainChainAddress,
        smallNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid3:" + retryDepositTxid3);
    Optional<TransactionInfo> infoByIdretryDepositTxid3 = PublicMethed
        .getTransactionInfoById(retryDepositTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid3.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid3.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid3.get().getResMessage().toStringUtf8());

    //Mapping noce value is 0
    String retryMaptxid3 = PublicMethed.retryMapping(mainChainAddress,
        smallNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid3:" + retryMaptxid3);
    Optional<TransactionInfo> infoByIdretryMaptxid3 = PublicMethed
        .getTransactionInfoById(retryMaptxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid3.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryMaptxid3.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMaptxid3.get().getResMessage().toStringUtf8());

    //retry withdraw trc20 noce value is 0
    String retryWithdrawTxid3 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, smallNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid3:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdretryWithdraw3 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid3, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdretryWithdraw3.get().getResultValue());

    //Deposit noce value is 1*10**20+1*10**6（nonexistent）
    String maxNonce = "0000000000000000000000000000000000000000000000056bc75e2d636b8d80";
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid4 = PublicMethed.retryDeposit(mainChainAddress,
        maxNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid4:" + retryDepositTxid4);
    Optional<TransactionInfo> infoByIdretryDepositTxid4 = PublicMethed
        .getTransactionInfoById(retryDepositTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid4.get().getResMessage().toStringUtf8());

    //Mapping noce value is 1*10**20+1*10**6（nonexistent）
    String retryMaptxid4 = PublicMethed.retryMapping(mainChainAddress,
        maxNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid3:" + retryMaptxid4);
    Optional<TransactionInfo> infoByIdretryMaptxid4 = PublicMethed
        .getTransactionInfoById(retryMaptxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryMaptxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMaptxid4.get().getResMessage().toStringUtf8());

    //retry withdraw trc20 noce value is 1*10**20+1*10**6（nonexistent）
    String retryWithdrawTxid4 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, maxNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid4:" + retryWithdrawTxid4);
    Optional<TransactionInfo> infoByIdretryWithdraw4 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid4, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryWithdraw4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdraw4.get().getResMessage().toStringUtf8());

    //Deposit noce value is 1*10**20
    String initialNonce = "0000000000000000000000000000000000000000000000056bc75e2d63100000";
    logger.info("initialNonce:" + initialNonce);
    String retryDepositTxid5 = PublicMethed.retryDeposit(mainChainAddress,
        initialNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid4:" + retryDepositTxid5);
    Optional<TransactionInfo> infoByIdretryDepositTxid5 = PublicMethed
        .getTransactionInfoById(retryDepositTxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid5.get().getResultValue() == 0);

    // retry mapping noce value is 1*10**20
    String retryMaptxid5 = PublicMethed.retryMapping(mainChainAddress,
        initialNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid3:" + retryMaptxid5);
    Optional<TransactionInfo> infoByIdretryMaptxid5 = PublicMethed
        .getTransactionInfoById(retryMaptxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid5.get().getResultValue() == 0);

    // retry withdraw trc20 noce value is 1*10**20
    String retryWithdrawTxid5 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, initialNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid5:" + retryWithdrawTxid5);
    Optional<TransactionInfo> infoByIdretryWithdraw5 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid5, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw5.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryWithdraw5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdraw5.get().getResMessage().toStringUtf8());

    // retry deposit noce value is -1
    String minusNonce = PublicMethed.numToHex64(-1L);
    logger.info("minusNonce:" + minusNonce);
    String retryDepositTxid6 = PublicMethed.retryDeposit(mainChainAddress,
        minusNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid6:" + retryDepositTxid6);
    Optional<TransactionInfo> infoByIdretryDepositTxid6 = PublicMethed
        .getTransactionInfoById(retryDepositTxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid6.get().getResMessage().toStringUtf8());

    //Mapping noce value is -1
    String retryMaptxid6 = PublicMethed.retryMapping(mainChainAddress,
        minusNonce, maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid6:" + retryMaptxid6);
    Optional<TransactionInfo> infoByIdretryMaptxid6 = PublicMethed
        .getTransactionInfoById(retryMaptxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryMaptxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
       infoByIdretryMaptxid6.get().getResMessage().toStringUtf8());

    //retry withdraw trc20 noce value is -1
    String retryWithdrawTxid6 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, minusNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid6:" + retryWithdrawTxid6);
    Optional<TransactionInfo> infoByIdretryWithdraw6 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid6, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryWithdraw6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdraw6.get().getResMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc20 with mainOralce value is 0")
  public void test3RetryTrc20003() {
    String parame = "\"" + Base58.encode58Check(depositAddress) + "\"";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return1.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance:" + mainTrc20Balance);

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid : " + sideChainTxid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    int beforerDepositSideChain = ByteArray
        .toInt(infoById.get().getContractResult(0).toByteArray());
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress, 100000000,
        0, 0, testOracle, blockingStubFull));
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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String depositTrc20txid = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infodepositTrc20 = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);
    Assert.assertEquals(0, infodepositTrc20.get().getResultValue());
    nonce = ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);

    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    int afterDepositSideChain = ByteArray.toInt(infoById1.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Assert.assertEquals(beforerDepositSideChain + 1000, afterDepositSideChain);

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance2:" + mainTrc20Balance2);
    Assert.assertTrue(mainTrc20Balance - 1000 == mainTrc20Balance2);

    String withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertTrue(infoByIdwithdrawTrc20.get().getResultValue() == 0);
    nonceWithdraw = ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);

    String sideChainTxid2 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid2 : " + sideChainTxid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid2, blockingSideStubFull);
    int afterDepositSideChain2 = ByteArray
        .toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain - 100, afterDepositSideChain2);
    logger.info("afterDepositSideChain2:" + afterDepositSideChain2);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance3 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance3:" + mainTrc20Balance3);
    Assert.assertEquals(mainTrc20Balance2, mainTrc20Balance3);

    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress, oracleMainBeforeSendBalance - 200000, depositAddress2,
            testKeyFordeposit2,
            blockingStubFull));
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideChainAddress,
        nonceWithdraw, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw1 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw1.get().getResultValue() == 0);

    String sideChainTxid3 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid3 : " + sideChainTxid3);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid3, blockingSideStubFull);
    int afterDepositSideChain3 = ByteArray
        .toInt(infoById3.get().getContractResult(0).toByteArray());
    logger.info("afterDepositSideChain3:" + afterDepositSideChain3);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain2, afterDepositSideChain3);

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance4 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return4.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance4:" + mainTrc20Balance4);
    Assert.assertTrue(mainTrc20Balance3 + 100 == mainTrc20Balance4);
  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc20 with sideOralce value is 0")
  public void test4RetryTrc20004() {
    String parame = "\"" + Base58.encode58Check(depositAddress) + "\"";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return1.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance:" + mainTrc20Balance);

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid : " + sideChainTxid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    int beforerDepositSideChain = ByteArray
        .toInt(infoById.get().getContractResult(0).toByteArray());
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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String depositTrc20txid = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infodepositTrc20 = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);

    // check Deposit Msg when deposit failed
    depositNonce = ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray());
    String[] Msg = {
        WalletClient.encode58Check(depositAddress), "" + "0",
        "2", WalletClient.encode58Check(trc20Contract), "0", "0", "1000"
    };
    Assert.assertTrue(PublicMethed.checkDepositMsg(depositNonce, mainChainAddress, depositAddress,
        testKeyFordeposit, blockingStubFull, Msg));

    Assert.assertEquals(0, infodepositTrc20.get().getResultValue());
    nonce = ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);

    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    int afterDepositSideChain = ByteArray.toInt(infoById1.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Assert.assertEquals(beforerDepositSideChain, afterDepositSideChain);

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance2:" + mainTrc20Balance2);
    Assert.assertEquals(mainTrc20Balance - 1000, mainTrc20Balance2.longValue());

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame2 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame2, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "retryFee()";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input5, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return6.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(parame2));
    logger.info("retryFee:" + retryFee1);
    //bonus
    byte[] input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //retry deposit trc10 with <setRetryFee
    String retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainChainAddress,
        nonce, setRetryFee - 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);

    //retry deposit trc10 with >setRetryFee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainChainAddress,
        nonce, setRetryFee + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    String sideChainTxid2 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid2 : " + sideChainTxid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid2, blockingSideStubFull);
    int afterDepositSideChain2 = ByteArray
        .toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain + 1000, afterDepositSideChain2);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance3 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance3:" + mainTrc20Balance3);
    Assert.assertEquals(mainTrc20Balance2, mainTrc20Balance3);

    Account oracleSideBeforeWithdrawSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleSideBeforeWithdrawSendBalance = oracleSideBeforeWithdrawSend.getBalance();

    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + setRetryFee, bonusBefore1);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeWithdrawSendBalance,
            testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));

    String withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);

    // check Withdraw Msg when withdraw failed
    withdrawNonce = ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray());
    String[] MsgWithdraw = {
        WalletClient.encode58Check(depositAddress),
        WalletClient.encode58Check(trc20Contract), "0", "100", "2", "0"
    };
    Assert.assertTrue(PublicMethed.checkWithdrawMsg(withdrawNonce, sideChainAddress, depositAddress,
        testKeyFordeposit, blockingSideStubFull, MsgWithdraw));

    Assert.assertTrue(infoByIdwithdrawTrc20.get().getResultValue() == 0);
    nonceWithdraw = ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);

    String sideChainTxid3 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid3 : " + sideChainTxid3);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid3, blockingSideStubFull);
    int afterDepositSideChain3 = ByteArray
        .toInt(infoById3.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById3.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain2 - 100, afterDepositSideChain3);
    logger.info("afterDepositSideChain3:" + afterDepositSideChain3);

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance4 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return4.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance4:" + mainTrc20Balance4);
    Assert.assertEquals(mainTrc20Balance3, mainTrc20Balance4);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeWithdrawSendBalance - 1000000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    String txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideChainAddress),
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

    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);

    //<setRetryFeeSide
    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideChainAddress,
        nonceWithdraw, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw1 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw1.get().getResultValue() == 1);

    //>setRetryFeeSide
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideChainAddress,
        nonceWithdraw, setRetryFeeSide + 1,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw1 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw1.get().getResultValue() == 0);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    long bonusSideAfter3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter3:" + bonusSideAfter3);
    Assert.assertEquals(bonusSideAfter + setRetryFeeSide, bonusSideAfter3);

    String sideChainTxid4 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("sideChainTxid4 : " + sideChainTxid4);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(sideChainTxid4, blockingSideStubFull);
    int afterDepositSideChain4 = ByteArray
        .toInt(infoById4.get().getContractResult(0).toByteArray());
    logger.info("afterDepositSideChain4:" + afterDepositSideChain4);
    Assert.assertEquals(0, infoById4.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain3, afterDepositSideChain4);

    TransactionExtention return5 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance5 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return5.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance5:" + mainTrc20Balance5);
    Assert.assertTrue(mainTrc20Balance4 + 100 == mainTrc20Balance5);
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

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame2 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame2, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideChainAddress),
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
