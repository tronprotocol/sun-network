package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

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
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.SideChainParameters;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class FundInjectTrx001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String witnessA = Configuration.getByPath("testng.conf")
      .getString("witness.key1");
  private final byte[] witnessAddressA = PublicMethed.getFinalAddress(witnessA);
  private final String witnessB = Configuration.getByPath("testng.conf")
      .getString("witness.key2");
  private final byte[] witnessAddressB = PublicMethed.getFinalAddress(witnessB);
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

  @Test(enabled = true, description = "Fundinject ")
  public void test1Fundinject001() {

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

    Account accountWitnessAddressA = PublicMethed
        .queryAccount(witnessAddressA, blockingSideStubFull);
    long witnessAddressAllowance = accountWitnessAddressA.getAllowance();
    logger.info("witnessAddressAllowance:" + witnessAddressAllowance);

    SideChainParameters sideChainParameters = blockingSideStubFull
        .getSideChainParameters(EmptyMessage.newBuilder().build());
    Optional<SideChainParameters> getChainParameters = Optional.ofNullable(sideChainParameters);
    long fund = Long.valueOf(getChainParameters.get().getChainParameter(33).getValue());
    String fundAddress = getChainParameters.get().getChainParameter(34).getValue();
    long dayToSustainByFund = Long
        .valueOf(getChainParameters.get().getChainParameter(36).getValue());
    long percentToPayWitness = Long
        .valueOf(getChainParameters.get().getChainParameter(37).getValue());
    Assert.assertTrue(PublicMethed.fundInject(depositAddress, testKeyFordeposit, 1000000,
        WalletClient.decodeFromBase58Check(ChainIdAddress),
        blockingSideStubFull));
    Account fundAddressBefore = PublicMethed
        .queryAccount(WalletClient.decodeFromBase58Check(fundAddress), blockingSideStubFull);
    long fundAddressbeforeBalance = fundAddressBefore.getBalance();

    long payPerBlock = fund / ((86400 / 3) * dayToSustainByFund);
    long amountForWitness = payPerBlock * (percentToPayWitness / 100);
    long amountForfundInjectAddress = payPerBlock - amountForWitness;
    logger.info("fund:" + fund);
    logger.info("fundAddress:" + fundAddress);

    logger.info("percentToPayWitness:" + percentToPayWitness);

    logger.info("payPerBlock:" + payPerBlock);
    logger.info("amountForWitness:" + amountForWitness);
    logger.info("amountForfundInjectAddress:" + amountForfundInjectAddress);
//    Block nowBlock = blockingSideStubFull.getNowBlock(EmptyMessage.newBuilder().build());
//    final Long currentNum = nowBlock.getBlockHeader().getRawData().getNumber();
//
//    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
//    Block afterBlock = blockingSideStubFull.getNowBlock(EmptyMessage.newBuilder().build());
//    final Long aftertNum = afterBlock.getBlockHeader().getRawData().getNumber();
//    Account fundAddressAfetr = PublicMethed
//        .queryAccount(WalletClient.decodeFromBase58Check(fundAddress), blockingSideStubFull);
//    long fundAddressAfterBalance = fundAddressAfetr.getBalance();
//
//    Assert.assertEquals(fundAddressbeforeBalance + amountForfundInjectAddress,
//        fundAddressAfterBalance);
//    Account witnessAddressAfterA = PublicMethed.queryAccount(witnessAddressA, blockingSideStubFull);
//    long witnessAddressAfterAllowance = witnessAddressAfterA.getAllowance();
//    logger.info("witnessAddressAfterAllowance:" + witnessAddressAfterAllowance);
//    Assert.assertTrue(witnessAddressAllowance - (aftertNum - currentNum + 1) * amountForWitness
//        == witnessAddressAfterAllowance);
  }

  @Test(enabled = true, description = "Fundinject Exception")
  public void test2Fundinject002() {
    //value is 1
    Return response = PublicMethed.fundInjectForReturn(depositAddress, testKeyFordeposit, 1,
        WalletClient.decodeFromBase58Check(ChainIdAddress), blockingSideStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : fund amount must be larger than 1TRX/SunToken",
        response.getMessage().toStringUtf8());
    //other account

    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] injectAddress2 = ecKey2.getAddress();
    String testKeyInjectAddress2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

    Return response1 = PublicMethed.fundInjectForReturn(injectAddress2, testKeyInjectAddress2, 1,
        WalletClient.decodeFromBase58Check(ChainIdAddress), blockingSideStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response1.getCode());
//    Assert.assertEquals("contract validate error : account not exists",
//        response1.getMessage().toStringUtf8());

    //value is 1
    long value = 1500000000;
    Return response3 = PublicMethed
        .fundInjectForReturn(depositAddress, testKeyFordeposit, value + 1,
            WalletClient.decodeFromBase58Check(ChainIdAddress), blockingSideStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response3.getCode());
    Assert.assertEquals("contract validate error : fund amount must be less than accountBalance",
        response3.getMessage().toStringUtf8());

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
