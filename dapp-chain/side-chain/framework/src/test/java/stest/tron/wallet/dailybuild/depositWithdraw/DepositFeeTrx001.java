package stest.tron.wallet.dailybuild.depositWithdraw;

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
public class DepositFeeTrx001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String methodStr1 = "setDepositFee(uint256)";
  String parame1 = "2";
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
  }

  @Test(enabled = true, description = "DepositMinTrx normal.")
  public void test1DepositFee001() {

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 100000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Assert.assertTrue(accountBeforeBalance == 100000000);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("mainGateWayAddress:" + mainGateWayAddress);
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 2;
    long depositFee = 0;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue + depositFee,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Assert.assertEquals(accountAfterBalance, 100000000 - fee - 2);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
    Assert.assertEquals(2, accountSideAfterBalance);

    methodStr1 = "setDepositFee(uint256)";
    long setDepositFee = 2;
    parame1 = String.valueOf(setDepositFee);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "depositFee()";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input2, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long depositFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(depositFee1, Long.valueOf(parame1));
    logger.info("setDepositFee:" + depositFee1);
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);
    //>depositFee <depositMintrx
    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue + depositFee,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 1);
    String data = ByteArray
        .toHexString(infoById2.get().getContractResult(0).substring(67,108).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("(must between depositMinTrx and uint64Max", PublicMethed.hexStringToString(data));
    long fee2 = infoById2.get().getFee();
    logger.info("fee2:" + fee2);
    Account accountAfter2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance2 = accountAfter2.getBalance();
    logger.info("accountAfterBalance2:" + accountAfterBalance2);
    Assert.assertEquals(accountAfterBalance - fee2, accountAfterBalance2);
    Account accountSideAfter2 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance2 = accountSideAfter2.getBalance();
    logger.info("accountSideAfterBalance2:" + accountSideAfterBalance2);
    Assert.assertEquals(accountSideAfterBalance, accountSideAfterBalance2);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter:" + bonusAfter);
    Assert.assertEquals(bonusBefore, bonusAfter);
    //>depositFee+depositMintrx

    depositFee = 10;
    String txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue + depositFee,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);
    long fee3 = infoById3.get().getFee();
    logger.info("fee3:" + fee3);

    Account accountAfter3 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance3 = accountAfter3.getBalance();
    logger.info("accountAfterBalance3:" + accountAfterBalance3);
    Assert.assertEquals(accountAfterBalance2 - fee3 - callValue - depositFee,
        accountAfterBalance3);
    Account accountSideAfter3 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance3 = accountSideAfter3.getBalance();
    Assert.assertEquals(accountSideAfterBalance2 + callValue + depositFee - setDepositFee,
        accountSideAfterBalance3);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter1:" + bonusAfter1);
    Assert.assertEquals(bonusAfter + setDepositFee, bonusAfter1);

    //<depositFee
    callValue = 1;
    String txid4 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertTrue(infoById4.get().getResultValue() == 1);
    data = ByteArray
        .toHexString(infoById4.get().getContractResult(0).substring(67, 97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));

    long fee4 = infoById4.get().getFee();
    logger.info("fee4:" + fee4);
    Account accountAfter4 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance4 = accountAfter4.getBalance();
    logger.info("accountAfterBalance4:" + accountAfterBalance4);
    Assert.assertEquals(accountAfterBalance3 - fee4, accountAfterBalance4);
    Account accountSideAfter4 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance4 = accountSideAfter4.getBalance();
    Assert.assertEquals(accountSideAfterBalance3, accountSideAfterBalance4);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter4 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter4:" + bonusAfter4);
    Assert.assertEquals(bonusAfter1, bonusAfter4);

    //=depositFee+depositMintrx
    callValue = 1;
    String txid5 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue + setDepositFee,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById5 = PublicMethed
        .getTransactionInfoById(txid5, blockingStubFull);
    Assert.assertTrue(infoById5.get().getResultValue() == 0);
    long fee5 = infoById5.get().getFee();
    logger.info("fee5:" + fee5);

    Account accountAfter5 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance5 = accountAfter5.getBalance();
    logger.info("accountAfterBalance5:" + accountAfterBalance5);
    Assert.assertEquals(accountAfterBalance4 - fee5 - callValue - setDepositFee,
        accountAfterBalance5);
    Account accountSideAfter5 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance5 = accountSideAfter5.getBalance();
    Assert.assertEquals(accountSideAfterBalance4 + callValue, accountSideAfterBalance5);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusAfter5 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusAfter5:" + bonusAfter5);
    Assert.assertEquals(bonusAfter4 + setDepositFee, bonusAfter5);
  }

  @Test(enabled = true, description = "DepositMinTrx with triggerAccount exception and "
      + "minTrx Value range")
  public void test2DepositFee002() {
    PublicMethed.sendcoin(depositAddress, 1000000000,
        testDepositAddress, testDepositTrx, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    //not gateWay owner trigger setDepositFee method
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    String data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,87).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0013msg.sender != owner", PublicMethed.hexStringToString(data));

    //setDepositFee is 100000001
    parame1 = "100000001";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input2,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,85).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0011less than 100 TRX", PublicMethed.hexStringToString(data));

    String methodStr2 = "depositFee()";
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long depositFee = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("depositFee:" + Long.valueOf(depositFee));

    // 99_999_999L
    parame1 = String.valueOf(99_999_999L);
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertNotNull(txid3);
    Assert.assertEquals(0, infoById1.get().getResultValue());

    //Long.min
    Long setDepositFee = Long.MIN_VALUE;
    logger.info("setDepositFee:" + setDepositFee);
    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,85).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0011less than 100 TRX", PublicMethed.hexStringToString(data));

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    depositFee = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("depositFee:" + Long.valueOf(depositFee));
    //Long.max

    //  parame1 = "9223372036854775807";
    setDepositFee = Long.MAX_VALUE;
    logger.info("setDepositFee:" + setDepositFee);

    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,85).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0011less than 100 TRX", PublicMethed.hexStringToString(data));

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    depositFee = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("depositFee:" + Long.valueOf(depositFee));

    //Long.max+1
    setDepositFee = Long.MAX_VALUE + 1;
    logger.info("setDepositFee-Long.MAX_VALUE+1:" + setDepositFee);

    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,85).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0011less than 100 TRX", PublicMethed.hexStringToString(data));

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    depositFee = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("depositFee:" + Long.valueOf(depositFee));

    //Long.min-1

    setDepositFee = Long.MIN_VALUE - 1;
    logger.info("setDepositFee-Long.MIN_VALUE-1:" + setDepositFee);

    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,85).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0011less than 100 TRX", PublicMethed.hexStringToString(data));

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    depositFee = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("depositFee:" + Long.valueOf(depositFee));


  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    parame1 = "0";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
