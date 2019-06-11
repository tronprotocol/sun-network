package stest.tron.wallet.common.deposit;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class DepositTrx002 {


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


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);


  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());


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

  @Test(enabled = true, description = "Deposit Trx with exception")
  public void test1DepositTrx001() {

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    ByteString accountMainBefore = accountBefore.getAddress();
    long accountBeforeBalance = accountBefore.getBalance();
    String accountMainBeforeAddress = Base58.encode58Check(accountMainBefore.toByteArray());
    logger.info("accountBeforeBalance:" + accountBeforeBalance);
    logger.info("accountMainBeforeAddress:" + accountMainBeforeAddress);
    Assert.assertTrue(accountBeforeBalance == 0);
    Assert.assertEquals("3QJmnh", accountMainBeforeAddress);

    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);
    Assert.assertTrue(accountSideBeforeBalance == 0);

    final String transferTokenContractAddress = Configuration.getByPath("testng.conf")
        .getString("gateway_address.key1");
    logger.info("transferTokenContractAddress:" + transferTokenContractAddress);
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1;
    Return response = PublicMethed
        .triggerContractForReturn(WalletClient.decodeFromBase58Check(transferTokenContractAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : account not exists",
        response.getMessage().toStringUtf8());

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

//    Optional<TransactionInfo> infoById = PublicMethed
//        .getTransactionInfoById(txid, blockingStubFull);
//    Assert.assertTrue(infoById.get().getResultValue() == 1);
//    long fee = infoById.get().getFee();
//    logger.info("fee:" + fee);
//    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
//    long accountMainAfterBalance = accountMainAfter.getBalance();
//    logger.info("accountAfterBalance:" + accountMainAfterBalance);
//    Assert.assertEquals(accountMainAfterBalance, 0);
//
//    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
//    long accountSideAfterBalance = accountSideAfter.getBalance();
//    ByteString addressAfter = accountSideAfter.getAddress();
//    String accountSideAfterAddress = Base58.encode58Check(addressAfter.toByteArray());
//    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
//    Assert.assertEquals("3QJmnh", Base58.encode58Check(depositAddress));
//    Assert.assertEquals(0, accountSideAfterBalance);

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
