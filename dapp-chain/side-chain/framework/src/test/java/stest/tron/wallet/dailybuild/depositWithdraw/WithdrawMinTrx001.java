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
public class WithdrawMinTrx001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideGatewayAddress);
  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
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

  @Test(enabled = true, description = "WithdrawMinTrx normal.")
  public void test1WithdrawMinTrx001() {

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

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
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
    long withdrawValue = 10;
    String txid1 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
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

    parame1 = "10";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    String txid2 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);
    long fee2 = infoById3.get().getFee();
    logger.info("fee2:" + fee2);
    Account accountSideAfterWithdraw1 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance1 = accountSideAfterWithdraw1.getBalance();
    Assert.assertEquals(accountSideAfterWithdrawBalance - fee2 - withdrawValue,
        accountSideAfterWithdrawBalance1);
    Account accountMainAfterWithdraw1 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance1 = accountMainAfterWithdraw1.getBalance();
    logger.info("accountAfterWithdrawBalance1:" + accountMainAfterWithdrawBalance1);
    Assert.assertEquals(accountMainAfterWithdrawBalance1,
        accountMainAfterWithdrawBalance + withdrawValue);

    //value>WithdrawMinTrx
    withdrawValue = 11;
    txid2 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);
    long fee3 = infoById3.get().getFee();
    logger.info("fee3:" + fee3);
    Account accountSideAfterWithdraw2 = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance2 = accountSideAfterWithdraw2.getBalance();
    Assert.assertEquals(accountSideAfterWithdrawBalance1 - fee3 - withdrawValue,
        accountSideAfterWithdrawBalance2);
    Account accountMainAfterWithdraw2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance2 = accountMainAfterWithdraw2.getBalance();
    logger.info("accountMainAfterWithdrawBalance2:" + accountMainAfterWithdrawBalance2);
    Assert.assertEquals(accountMainAfterWithdrawBalance1 + withdrawValue,
        accountMainAfterWithdrawBalance2);

    //value<WithdrawMinTrx
    Account account = PublicMethed.queryAccount(depositAddress,blockingSideStubFull);
    Long balanceBefore = account.getBalance();
    withdrawValue = 1;
    txid2 = PublicMethed
        .withdrawTrx(chainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 1);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById3.get().getResMessage().toByteArray()));

    Long balnceAfter = PublicMethed.queryAccount(depositAddress,blockingSideStubFull).getBalance();
    Assert.assertEquals(balanceBefore - infoById3.get().getFee(),balnceAfter.longValue());

  }

  @Test(enabled = true, description = "WithdrawMinTrx with triggerAccount exception and "
      + "minTrx Value range")
  public void test2WithdrawMinTrx002() {

    //not gateWay owner trigger setDepositMinTrx method
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));

    String txid1 = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() != 0);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById1.get().getResMessage().toByteArray()));

    parame1 = "-1";
    input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    //parame1 = "-9223372036854775808";
    long setWithdrawMinTrx1 = Long.MIN_VALUE;
    parame1 = String.valueOf(setWithdrawMinTrx1);

    input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    setWithdrawMinTrx1 = Long.MAX_VALUE;
    parame1 = String.valueOf(setWithdrawMinTrx1);
    input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    setWithdrawMinTrx1 = Long.MAX_VALUE + 1;
    parame1 = String.valueOf(setWithdrawMinTrx1);
    input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    setWithdrawMinTrx1 = Long.MAX_VALUE - 1;
    parame1 = String.valueOf(setWithdrawMinTrx1);
    input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());
  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {

    parame1 = "1";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
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
