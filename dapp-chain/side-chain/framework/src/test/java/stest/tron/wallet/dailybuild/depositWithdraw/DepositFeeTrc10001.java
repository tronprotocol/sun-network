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
import org.tron.common.utils.ByteArray;
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
public class DepositFeeTrc10001 {


  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String testKeyFordeposit = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key3");
  private final byte[] depositAddress = PublicMethed.getFinalAddress(testKeyFordeposit);
  private final String testKeyFordeposit2 = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key4");
  private final byte[] depositAddress2 = PublicMethed.getFinalAddress(testKeyFordeposit2);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  ByteString assetAccountId;
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
  }

  @Test(enabled = true, description = "DepositFeeTrc10 normal.")
  public void test1DepositFeeTrc10001() {

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 3100_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBalance = accountMainBefore.getBalance();
    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();
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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById;
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
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
    Long depositSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Long depositMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenBefore - inputTokenValue == depositMainTokenAfter);
    logger.info("depositMainTokenAfter:" + depositMainTokenAfter);
    logger.info("depositSideTokenAfter:" + depositSideTokenAfter);
    Assert.assertTrue(depositSideTokenAfter == depositSideTokenBefore + inputTokenValue);

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

    //=0
    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 1);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));
    String data = ByteArray
        .toHexString(infoById.get().getContractResult(0).substring(67,97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));
    fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter1 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance1 = accountMainAfter1.getBalance();
    logger.info("accountMainAfterBalance1:" + accountMainAfterBalance1);
    Assert.assertEquals(accountMainAfterBalance - fee, accountMainAfterBalance1);
    Account accountSideAfter1 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance1 = accountSideAfter1.getBalance();
    Long depositSideTokenAfter1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Long depositMainTokenAfter1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("depositSideTokenAfter1:" + depositSideTokenAfter1);
    Assert.assertEquals(depositSideTokenAfter, depositSideTokenAfter1);
    logger.info("depositMainTokenAfter1:" + depositMainTokenAfter1);
    Assert.assertEquals(depositMainTokenAfter, depositMainTokenAfter1);

    //=setDepositFee
    long inputTokenValue2 = 11;
    String inputParam2 = inputTokenID + "," + inputTokenValue2;
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam2, false));
    String txid4 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            setDepositFee,
            input4,
            maxFeeLimit, inputTokenValue2, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance2 = accountMainAfter2.getBalance();
    logger.info("accountMainAfterBalance2:" + accountMainAfterBalance2);
    Assert.assertEquals(accountMainAfterBalance1 - fee - setDepositFee, accountMainAfterBalance2);
    Long depositSideTokenAfter2 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Long depositMainTokenAfter2 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("depositSideTokenAfter2:" + depositSideTokenAfter2);
    Assert.assertTrue(depositSideTokenAfter1 + inputTokenValue2 == depositSideTokenAfter2);
    logger.info("depositMainTokenAfter2:" + depositMainTokenAfter2);
    Assert.assertTrue(depositMainTokenAfter1 - inputTokenValue2 == depositMainTokenAfter2);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + setDepositFee, bonusBefore1);

    //>setDepositFee
    inputTokenValue2 = 11;
    inputParam2 = inputTokenID + "," + inputTokenValue2;
    input4 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam2, false));
    txid4 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            setDepositFee + 1,
            input4,
            maxFeeLimit, inputTokenValue2, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter3 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance3 = accountMainAfter3.getBalance();
    logger.info("accountMainAfterBalance3:" + accountMainAfterBalance3);
    Assert.assertEquals(accountMainAfterBalance2 - fee - setDepositFee, accountMainAfterBalance3);
    Long depositSideTokenAfter3 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Long depositMainTokenAfter3 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("depositSideTokenAfter3:" + depositSideTokenAfter3);
    Assert.assertTrue(depositSideTokenAfter2 + inputTokenValue2 == depositSideTokenAfter3);
    logger.info("depositMainTokenAfter3:" + depositMainTokenAfter3);
    Assert.assertTrue(depositMainTokenAfter2 - inputTokenValue2 == depositMainTokenAfter3);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore1 + setDepositFee, bonusBefore2);

    //<setDepositFee
    inputTokenValue2 = 11;
    inputParam2 = inputTokenID + "," + inputTokenValue2;
    input4 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam2, false));
    txid4 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            setDepositFee - 1,
            input4,
            maxFeeLimit, inputTokenValue2, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 1);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));
    data = ByteArray
        .toHexString(infoById.get().getContractResult(0).substring(67,97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));
    fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter4 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance4 = accountMainAfter4.getBalance();
    logger.info("accountMainAfterBalance4:" + accountMainAfterBalance4);
    Assert.assertEquals(accountMainAfterBalance3 - fee, accountMainAfterBalance4);
    Account accountSideAfter4 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance4 = accountSideAfter4.getBalance();
    logger.info("accountSideAfterBalance4:" + accountSideAfterBalance4);
    Long depositSideTokenAfter4 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Long depositMainTokenAfter4 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("depositSideTokenAfter4:" + depositSideTokenAfter4);
    Assert.assertEquals(depositSideTokenAfter3, depositSideTokenAfter4);
    logger.info("depositMainTokenAfter4:" + depositMainTokenAfter4);
    Assert.assertEquals(depositMainTokenAfter3, depositMainTokenAfter4);

  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    parame1 = "0";
    methodStr1 = "setDepositFee(uint256)";

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
