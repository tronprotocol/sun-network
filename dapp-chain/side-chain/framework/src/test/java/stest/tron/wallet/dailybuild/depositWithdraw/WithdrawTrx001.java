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
public class WithdrawTrx001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
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
  }

  @Test(enabled = true, description = "Withdraw Trx")
  public void test1WithdrawTrx001() {

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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    depositNonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
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
    long withdrawValue = 1;
    String txid1 = PublicMethed
        .withdrawTrx(ChainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    withdrawNonce = ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray());
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

  }

  @Test(enabled = true, description = "get DepositMsg and WithdrawMsg")
  public void test2getDepositAndWithdrawMsg() {

    // get DepositMsg
    String methodStr = "getDepositMsg(uint256)";
    String parame = depositNonce;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0, input,
            maxFeeLimit, 0, "0",
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    String addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(depositAddress), addressFinal);

    String depositValue = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    logger.info(depositValue);
    int value = Integer.parseInt(depositValue, 16);
    Assert.assertEquals(1500000000, value);

    String value1 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(0, Integer.parseInt(value1, 16));

    String value2 = ContractRestule.substring(1 + 64 * 3, 64 * 4);
    Assert.assertEquals(0, Integer.parseInt(value2, 16));

    String value3 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    Assert.assertEquals(0, Integer.parseInt(value3, 16));

    String value4 = ContractRestule.substring(1 + 64 * 5, 64 * 6);
    Assert.assertEquals(0, Integer.parseInt(value4, 16));

    String value5 = ContractRestule.substring(1 + 64 * 6, 64 * 7);
    Assert.assertEquals(0, Integer.parseInt(value5, 16));

    // get WithdrawMsg
    methodStr = "getWithdrawMsg(uint256)";
    parame = withdrawNonce;
    input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress), 0, input,
            maxFeeLimit, 0, "0",
            depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("return1: " + return2);
    logger.info(Hex.toHexString(return2.getConstantResult(0).toByteArray()));
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());

    ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(depositAddress), addressFinal);

    value1 = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    Assert.assertEquals(0, Integer.parseInt(value1, 16));

    value2 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(0, Integer.parseInt(value2, 16));

    value3 = ContractRestule.substring(1 + 64 * 3, 64 * 4);
    Assert.assertEquals(1, Integer.parseInt(value3, 16));

    value4 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    Assert.assertEquals(0, Integer.parseInt(value4, 16));

    value5 = ContractRestule.substring(1 + 64 * 5, 64 * 6);
    Assert.assertEquals(0, Integer.parseInt(value5, 16));
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
