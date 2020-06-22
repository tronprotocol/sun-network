package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;
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
import org.tron.api.GrpcAPI.Return;
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
public class WithdrawTrx002 {


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

  @Test(enabled = true, description = "Withdraw Trx address is not exist")
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
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - callValue);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
    Assert.assertEquals(callValue, accountSideAfterBalance);

    logger.info("sideGatewayAddress:" + sideGatewayAddress);
    //address is not exist

    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] withdrawAddress = ecKey2.getAddress();
    String withdrawAddressKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    Assert.assertTrue(PublicMethed
        .sendcoin(withdrawAddress, 100000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String txid1 = PublicMethed
        .withdrawTrx(ChainIdAddress,
            sideGatewayAddress,
            callValue,
            maxFeeLimit, withdrawAddress, withdrawAddressKey, blockingStubFull,
            blockingSideStubFull);

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
    Assert.assertEquals(callValue, accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance, accountMainAfterBalance);


  }


  @Test(enabled = true, description = "Withdraw Trx value exception")
  public void test1WithdrawTrx002() {
    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    logger.info("accountMainBeforeBalance:" + accountMainBeforeBalance);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    Assert.assertEquals(1500000000, accountSideBeforeBalance);

    //balance<value
    long callValue1 = accountSideBeforeBalance + 1;
    Return response = PublicMethed
        .withdrawTrxForReturn(ChainIdAddress,
            sideGatewayAddress,
            callValue1,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull,
            blockingSideStubFull);

    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals(
        "contract validate error : Validate InternalTransfer error, balance is not sufficient.",
        response.getMessage().toStringUtf8());

    //value  is -1
    long callValue2 = -1;

    Return response1 = PublicMethed
        .withdrawTrxForReturn(ChainIdAddress,
            sideGatewayAddress,
            callValue2,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull,
            blockingSideStubFull);

    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response1.getCode());
    Assert.assertEquals(
        "contract validate error : callValue must be >= 0",
        response1.getMessage().toStringUtf8());

    // value is 0
    long callValue3 = 0;
    String txidn = PublicMethed
        .withdrawTrx(ChainIdAddress,
            sideGatewayAddress,
            callValue3,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull,
            blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txidn, blockingSideStubFull);
    Assert.assertTrue(infoById.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoById.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoById.get().getResMessage().toStringUtf8());
    long fee = infoById.get().getFee();
    // value is Long.MAX_VALUE+1
    long callValue4 = Long.MAX_VALUE + 1;
    Return response3 = PublicMethed
        .withdrawTrxForReturn(ChainIdAddress,
            sideGatewayAddress,
            callValue4,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull,
            blockingSideStubFull);

    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response3.getCode());
    Assert.assertEquals(
        "contract validate error : callValue must be >= 0",
        response3.getMessage().toStringUtf8());

    // value is Long.MIN_VALUE - 1
    long callValue5 = Long.MIN_VALUE - 1;
    Return response4 = PublicMethed
        .withdrawTrxForReturn(ChainIdAddress,
            sideGatewayAddress,
            callValue5,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull,
            blockingSideStubFull);

    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response4.getCode());
    Assert.assertEquals(
        "contract validate error : Validate InternalTransfer error, balance is not sufficient.",
        response4.getMessage().toStringUtf8());
    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideBeforeBalance - fee, accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance, accountMainBeforeBalance);


  }


  @Test(enabled = true, description = "Withdraw Trx with feelimit=1")
  public void test1WithdrawTrx003() {
    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    logger.info("accountMainBeforeBalance:" + accountMainBeforeBalance);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    //feelimit is 1
    long callValue1 = accountSideBeforeBalance;
    long fee_limit = 1;
    String txid = PublicMethed
        .withdrawTrx(ChainIdAddress,
            sideGatewayAddress,
            callValue1,
            fee_limit, depositAddress, testKeyFordeposit, blockingStubFull,
            blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    logger.info("accountSideAfterWithdrawBalance:" + accountSideAfterWithdrawBalance);

    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideBeforeBalance, accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance, accountMainBeforeBalance);


  }

  @Test(enabled = false, description = "Withdraw Trx with feelimit=1")
  public void test1WithdrawTrx004() {
    /*Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById("e647483fcb35a7b10f10202775a2b2e6780175d5eee1b00e52dd97d04f7d5ac6", blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Long nonceMapLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceMapLong);
//    nonceMap = Long.toString(nonceMapLong);

    logger.info("1:"+infoById1.get().getContractResult(0).toByteArray());
    logger.info("2:"+ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray()));
    // check Deposit Msg when deposit failed
    String mappingNonce = ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray());
    logger.info("mappingNonce:" + mappingNonce);*/

    String s = numToHex64(-1L);
    logger.info("s:"+s);
  }
  //使用1字节就可以表示b
  public static String numToHex64(Long b) {
    return String.format("%064x", b);//2表示需要两个16进行数
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
