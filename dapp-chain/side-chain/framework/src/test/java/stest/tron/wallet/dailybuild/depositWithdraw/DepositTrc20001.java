package stest.tron.wallet.dailybuild.depositWithdraw;

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
public class DepositTrc20001 {


  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "Deposit Trc20")
  public void test1DepositTrc20001() {
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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());

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

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(sideContractAddress);

    String arg = parame;
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", arg, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long a = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("a:" + a);

    String deposittrx = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info(deposittrx);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(deposittrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    String arg4 = parame;
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", arg, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input4,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);

    Assert.assertEquals(0, infoById3.get().getResultValue());
    Assert.assertEquals(1000, ByteArray.toInt(infoById3.get().getContractResult(0).toByteArray()));

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long b = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("b:" + b);

    Assert.assertEquals(a - 1000, ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray()))));


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
