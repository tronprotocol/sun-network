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
public class WithdrawMinTrc10001 {


  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideGatewayAddressey = WalletClient.decodeFromBase58Check(sideGatewayAddress);
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
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
    byte[] input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
  }

  @Test(enabled = true, description = "WithdrawMinTrc10 normal.")
  public void test1WithdrawMinTrc10001() {

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

    logger.info("accountBeforeBalance:" + accountMainBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);
    logger.info("depositMainTokenBefore:" + depositMainTokenBefore);
    logger.info("depositSideAddressTokenBefore:" + depositSideTokenBefore);

    String methodStr = "depositTRC10(uint64,uint64)";

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 100;
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
    infoById = PublicMethed.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountMainAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBalance - fee);
    Long depositSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);
    Assert.assertTrue(depositSideTokenBefore + inputTokenValue == depositSideTokenAfter);
    Long depositMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenBefore - inputTokenValue == depositMainTokenAfter);
    logger.info("depositMainTokenAfter:" + depositMainTokenAfter);
    logger.info("depositSideTokenAfter:" + depositSideTokenAfter);

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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    //withdrawTrc10
    long withdrawTokenLong = 10;
    String withdrawToken = Long.toString(withdrawTokenLong);
    String txid2 = PublicMethed
        .withdrawTrc10(inputTokenID, withdrawToken, chainIdAddress,
            sideGatewayAddress,
            0,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    long fee2 = infoById2.get().getFee();
    logger.info("fee2:" + fee2);
    Account accountWithdrawSideAfter = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    ByteString addressWithdrawSideAfter = accountWithdrawSideAfter.getAddress();
    String addressWithdrawSideAfterAddress = Base58
        .encode58Check(addressWithdrawSideAfter.toByteArray());
    logger.info("addressWithdrawSideAfterAddress:" + addressWithdrawSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressWithdrawSideAfterAddress);
    Long withdrawSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("withdrawSideTokenAfter:" + withdrawSideTokenAfter);
    logger.info("withdrawMainTokenAfter:" + withdrawMainTokenAfter);
    Assert.assertTrue(depositSideTokenAfter - withdrawTokenLong == withdrawSideTokenAfter);
    Assert.assertTrue(depositMainTokenAfter + withdrawTokenLong == withdrawMainTokenAfter);

    parame1 = "10";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

    txid2 = PublicMethed
        .withdrawTrc10(inputTokenID, withdrawToken, chainIdAddress,
            sideGatewayAddress,
            0,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    long fee3 = infoById2.get().getFee();
    logger.info("fee3:" + fee3);
    Long withdrawSideTokenAfter1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingSideStubFull);

    Long withdrawMainTokenAfter1 = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);
    logger.info("withdrawSideTokenAfter:" + withdrawSideTokenAfter);
    logger.info("withdrawMainTokenAfter:" + withdrawMainTokenAfter);
    Assert.assertEquals(withdrawSideTokenAfter - withdrawTokenLong , withdrawSideTokenAfter1.longValue());
    Assert.assertEquals(withdrawMainTokenAfter + withdrawTokenLong , withdrawMainTokenAfter1.longValue());

    Account account = PublicMethed.queryAccount(depositAddress,blockingSideStubFull);
    Long balanceBefore = account.getBalance();

    Long callValue = 1L;
    String txid4 = PublicMethed
        .withdrawTrc10(inputTokenID, callValue + "", chainIdAddress,
            sideGatewayAddress,
            0,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(txid4, blockingSideStubFull);
    Assert.assertTrue(infoById4.get().getResultValue() == 1);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById4.get().getResMessage().toByteArray()));

    Long balnceAfter = PublicMethed.queryAccount(depositAddress,blockingSideStubFull).getBalance();
    Assert.assertEquals(balanceBefore - infoById4.get().getFee(),balnceAfter.longValue());


  }

  @Test(enabled = true, description = "WithdrawMinTrc10 with with triggerAccount exception and "
      + "minTrc10 Value range")
  public void test2WithdrawMinTrc10002() {
    //not gateWay owner trigger setDepositMinTrx method
    byte[] input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() != 0);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById1.get().getResMessage().toByteArray()));

    parame1 = "-1";
    input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

//    parame1 = "-9223372036854775808";

    long setWithdrawMinTrc101 = Long.MIN_VALUE;
    parame1 = String.valueOf(setWithdrawMinTrc101);

    input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

    //parame1 = "9223372036854775807";
    setWithdrawMinTrc101 = Long.MAX_VALUE;
    parame1 = String.valueOf(setWithdrawMinTrc101);
    input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

    setWithdrawMinTrc101 = Long.MAX_VALUE + 1;
    parame1 = String.valueOf(setWithdrawMinTrc101);
    input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

    setWithdrawMinTrc101 = Long.MIN_VALUE - 1;
    parame1 = String.valueOf(setWithdrawMinTrc101);
    input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    parame1 = "1";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc10(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideGatewayAddressey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
