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
public class DepositMinTrx001 {


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
  String methodStr1 = null;
  String parame1 = null;
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
    parame1 = "0";
    methodStr1 = "setDepositMinTrx(uint256)";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
  }

  @Test(enabled = true, description = "DepositMinTrx normal.")
  public void test1DepositMinTrx001() {

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
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
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

    methodStr1 = "setDepositMinTrx(uint256)";
    parame1 = "2";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

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
    String methodStr2 = "depositMinTrx()";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input2, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(MinTrx, Long.valueOf(parame1));

    // =MinTrx
    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    long fee2 = infoById2.get().getFee();
    logger.info("fee2:" + fee2);
    Account accountAfter1 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance1 = accountAfter1.getBalance();
    logger.info("accountAfterBalance1:" + accountAfterBalance1);
    Assert.assertEquals(accountAfterBalance - fee2 - 2, accountAfterBalance1);
    Account accountSideAfter1 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance1 = accountSideAfter1.getBalance();
    Assert.assertEquals(accountSideAfterBalance + 2, accountSideAfterBalance1);

    // >MinTrx
    callValue = 10;
    String txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);
    long fee3 = infoById3.get().getFee();
    logger.info("fee3:" + fee3);

    Account accountAfter2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance2 = accountAfter2.getBalance();
    logger.info("accountAfterBalance2:" + accountAfterBalance2);
    Assert.assertEquals(accountAfterBalance1 - fee3 - callValue, accountAfterBalance2);
    Account accountSideAfter2 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance2 = accountSideAfter2.getBalance();
    Assert.assertEquals(accountSideAfterBalance1 + callValue, accountSideAfterBalance2);

    // >MinTrx
    callValue = 11;
    String txid5 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById5 = PublicMethed
        .getTransactionInfoById(txid5, blockingStubFull);
    Assert.assertTrue(infoById5.get().getResultValue() == 0);
    long fee5 = infoById5.get().getFee();
    logger.info("fee5:" + fee5);

    Account accountAfter3 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountAfterBalance3 = accountAfter3.getBalance();
    logger.info("accountAfterBalance3:" + accountAfterBalance3);
    Assert.assertEquals(accountAfterBalance2 - fee5 - callValue, accountAfterBalance3);
    Account accountSideAfter3 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance3 = accountSideAfter3.getBalance();
    Assert.assertEquals(accountSideAfterBalance2 + callValue, accountSideAfterBalance3);

    // <MinTrx
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
    Assert.assertEquals(1, infoById4.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById4.get().getResMessage().toByteArray()));
    String data = ByteArray
        .toHexString(infoById4.get().getContractResult(0).substring(67,108).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("(must between depositMinTrx and uint64Max", PublicMethed.hexStringToString(data));


  }

  @Test(enabled = true, description = "DepositMinTrx with triggerAccount exception and "
      + "minTrx Value range")
  public void test2DepositMinTrx002() {
    //not gateWay owner trigger setDepositMinTrx method
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals(1, infoById1.get().getResultValue());
    String data = ByteArray
        .toHexString(infoById1.get().getContractResult(0).substring(67,87).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0013msg.sender != owner", PublicMethed.hexStringToString(data));

    //setDepositMinTrx is -1
    parame1 = "-1";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input2,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("txid2:"+txid2);
    String methodStr2 = "depositMinTrx()";
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long minTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("minTrx:" + minTrx);
    Assert.assertEquals(Long.valueOf(-1), minTrx);

    // Long.MIN_VALUE
    long setDepositMinTrx1 = Long.MIN_VALUE;
    parame1 = String.valueOf(setDepositMinTrx1);
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    minTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("minTrx:" + minTrx);

    // Long.MAX_VALUE
    setDepositMinTrx1 = Long.MAX_VALUE;
    parame1 = String.valueOf(setDepositMinTrx1);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    minTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("minTrx:" + minTrx);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);

    // Long.MAX_VALUE + 1
    setDepositMinTrx1 = Long.MAX_VALUE + 1;
    parame1 = String.valueOf(setDepositMinTrx1);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    minTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("minTrx:" + minTrx);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);

    // Long.MIN_VALUE - 1
    setDepositMinTrx1 = Long.MIN_VALUE - 1;
    parame1 = String.valueOf(setDepositMinTrx1);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    minTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("minTrx:" + minTrx);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);


  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    parame1 = "1";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
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
