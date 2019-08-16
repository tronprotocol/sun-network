package stest.tron.wallet.depositWithdraw;

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


  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
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

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);

  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);

  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);

  private final String testOracle = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracle);


  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] depositAddress2 = ecKey2.getAddress();
  String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

  String nonce = null;
  String nonceMap = null;
  String nonceWithdraw = null;
  byte[] trc20Contract = null;
  byte[] sideContractAddress = null;

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
    Long nonceMapLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceMapLong);
    nonceMap = Long.toString(nonceMapLong);
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
    Long nonceLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceLong);
    nonce = Long.toString(nonceLong);

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid : " + sideChainTxid);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    Long nonceWithdrawLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray())));
    logger.info("nonceWithdrawLong:" + nonceWithdrawLong);
    nonceWithdraw = Long.toString(nonceWithdrawLong);

    byte[] input4 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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

    //retry deposit trc10
    String retryDepositTxid = PublicMethed.retryDeposit(mainChainAddress,
        nonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
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

    //retry mapping trc10
    String retryMaptxid = PublicMethed.retryMapping(mainChainAddress,
        nonceMap,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid);
    Optional<TransactionInfo> infoByIdretryMaptxid = PublicMethed
        .getTransactionInfoById(retryMaptxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid.get().getResultValue() == 0);

    //retry withdraw trc20

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideChainAddress,
        nonceWithdraw,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);
  }

  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with nonce exception ")
  public void test2RetryTrc20002() {
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

    //Deposit noce value
    String bigNonce = "100000";
    String retryDepositTxid2 = PublicMethed.retryDeposit(mainChainAddress,
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

    //retry mapping trc10
    String retryMaptxid2 = PublicMethed.retryMapping(mainChainAddress,
        bigNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid2:" + retryDepositTxid2);
    Optional<TransactionInfo> infoByIdretryMaptxid2 = PublicMethed
        .getTransactionInfoById(retryMaptxid2, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMaptxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMaptxid2.get().getResMessage().toStringUtf8());

    //retry withdraw trc20

    String retryWithdrawTxid2 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, bigNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid2:" + retryDepositTxid2);
    Optional<TransactionInfo> infoByIdretryWithdraw2 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid2, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryWithdraw2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdraw2.get().getResMessage().toStringUtf8());

    //Deposit noce value is 0
    String smallNonce = Long.toString(0);
    logger.info("smallNonce:" + smallNonce);
    String retryDepositTxid3 = PublicMethed.retryDeposit(mainChainAddress,
        smallNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid3:" + retryDepositTxid3);
    Optional<TransactionInfo> infoByIdretryDepositTxid3 = PublicMethed
        .getTransactionInfoById(retryDepositTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid3.get().getResultValue() == 0);

    //Mapping noce value is 0

    String retryMaptxid3 = PublicMethed.retryMapping(mainChainAddress,
        smallNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid3:" + retryMaptxid3);
    Optional<TransactionInfo> infoByIdretryMaptxid3 = PublicMethed
        .getTransactionInfoById(retryMaptxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid3.get().getResultValue() == 0);

    //retry withdraw trc20 noce value is 0

    String retryWithdrawTxid3 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, smallNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid3:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdretryWithdraw3 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid3, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw3.get().getResultValue() == 0);

    //Deposit noce value is Long.max_value+1
    String maxNonce = Long.toString(Long.MAX_VALUE + 1);
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid4 = PublicMethed.retryDeposit(mainChainAddress,
        maxNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid4:" + retryDepositTxid4);
    Optional<TransactionInfo> infoByIdretryDepositTxid4 = PublicMethed
        .getTransactionInfoById(retryDepositTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid4.get().getResMessage().toStringUtf8());

    //Mapping noce value is Long.max_value+1

    String retryMaptxid4 = PublicMethed.retryMapping(mainChainAddress,
        maxNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid3:" + retryMaptxid4);
    Optional<TransactionInfo> infoByIdretryMaptxid4 = PublicMethed
        .getTransactionInfoById(retryMaptxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid4.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMaptxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMaptxid4.get().getResMessage().toStringUtf8());

    //retry withdraw trc20 noce value is Long.max_value+1

    String retryWithdrawTxid4 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, maxNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid4:" + retryWithdrawTxid4);
    Optional<TransactionInfo> infoByIdretryWithdraw4 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid4, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw4.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryWithdraw4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdraw4.get().getResMessage().toStringUtf8());

    //Deposit noce value is Long.min_value-1
    String minNonce = Long.toString(Long.MIN_VALUE - 1);
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid5 = PublicMethed.retryDeposit(mainChainAddress,
        minNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid4:" + retryDepositTxid5);
    Optional<TransactionInfo> infoByIdretryDepositTxid5 = PublicMethed
        .getTransactionInfoById(retryDepositTxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid5.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid5.get().getResMessage().toStringUtf8());

    //Deposit noce value is Long.min_value-1

    String retryMaptxid5 = PublicMethed.retryMapping(mainChainAddress,
        minNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid3:" + retryMaptxid5);
    Optional<TransactionInfo> infoByIdretryMaptxid5 = PublicMethed
        .getTransactionInfoById(retryMaptxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid5.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMaptxid5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMaptxid5.get().getResMessage().toStringUtf8());

    //retry withdraw trc20 noce value is Long.min_value-1

    String retryWithdrawTxid5 = PublicMethed.retryWithdraw(chainIdAddress,
        sideChainAddress, bigNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid5:" + retryWithdrawTxid5);
    Optional<TransactionInfo> infoByIdretryWithdraw5 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid5, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw5.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryWithdraw5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdraw5.get().getResMessage().toStringUtf8());

    //Deposit noce value is -1
    String minusNonce = Long.toString(-1);
    logger.info("minusNonce:" + minusNonce);
    String retryDepositTxid6 = PublicMethed.retryDeposit(mainChainAddress,
        minusNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid4:" + retryDepositTxid6);
    Optional<TransactionInfo> infoByIdretryDepositTxid6 = PublicMethed
        .getTransactionInfoById(retryDepositTxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid6.get().getResMessage().toStringUtf8());

    //Mapping noce value is -1

    String retryMaptxid6 = PublicMethed.retryMapping(mainChainAddress,
        minusNonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMaptxid6:" + retryMaptxid6);
    Optional<TransactionInfo> infoByIdretryMaptxid6 = PublicMethed
        .getTransactionInfoById(retryMaptxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid6.get().getResultValue() != 0);
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
    Assert.assertTrue(infoByIdretryWithdraw6.get().getResultValue() != 0);
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
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    int beforerDepositSideChain = ByteArray
        .toInt(infoById.get().getContractResult(0).toByteArray());

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
    Long nonceLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceLong);
    nonce = Long.toString(nonceLong);

    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    Long nonceWithdrawLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray())));
    logger.info("nonceWithdrawLong:" + nonceWithdrawLong);
    nonceWithdraw = Long.toString(nonceWithdrawLong);

    String sideChainTxid2 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid2 : " + sideChainTxid2);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid2, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
        nonceWithdraw,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
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
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid3, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    int beforerDepositSideChain = ByteArray
        .toInt(infoById.get().getContractResult(0).toByteArray());

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
    Optional<TransactionInfo> infodepositTrc20 = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);
    Assert.assertEquals(0, infodepositTrc20.get().getResultValue());
    Long nonceLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infodepositTrc20.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceLong);
    nonce = Long.toString(nonceLong);

    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    int afterDepositSideChain = ByteArray.toInt(infoById1.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Assert.assertEquals(beforerDepositSideChain, afterDepositSideChain);

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance2:" + mainTrc20Balance2);
    Assert.assertTrue(mainTrc20Balance - 1000 == mainTrc20Balance2);

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //retry deposit trc10
    String retryDepositTxid = PublicMethed.retryDeposit(mainChainAddress,
        nonce,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
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
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid2, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    Assert.assertTrue(infoByIdwithdrawTrc20.get().getResultValue() == 0);
    Long nonceWithdrawLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray())));
    logger.info("nonceWithdrawLong:" + nonceWithdrawLong);
    nonceWithdraw = Long.toString(nonceWithdrawLong);

    String sideChainTxid3 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid3 : " + sideChainTxid3);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid3, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideChainAddress,
        nonceWithdraw,
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw1 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw1.get().getResultValue() == 0);

    String sideChainTxid4 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid3 : " + sideChainTxid4);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(sideChainTxid4, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    int afterDepositSideChain4 = ByteArray
        .toInt(infoById4.get().getContractResult(0).toByteArray());
    logger.info("afterDepositSideChain3:" + afterDepositSideChain4);
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
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
