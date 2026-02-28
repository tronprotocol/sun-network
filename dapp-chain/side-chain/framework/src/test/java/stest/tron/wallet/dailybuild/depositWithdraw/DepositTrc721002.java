package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.hamcrest.core.StringContains.containsString;

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
public class DepositTrc721002 {

  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress001 = ecKey1.getAddress();
  String depositKey001 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  String deployTxid = null;
  byte[] trc20Contract = null;
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
//    PublicMethed.printAddress(depositKey001);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "Deposit without mapping")
  public void test001DepositTrc721002() {
    PublicMethed.printAddress(depositKey001);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress001, 11000_000_000L, foundationAddress001, foundationKey001,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(depositAddress001, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress001, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", depositAddress001, depositKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress001, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress001, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
    Assert.assertEquals(callValue, accountSideBeforeBalance);

    String contractName = "trc721";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC721");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC721");
    String parame = "\"" + Base58.encode58Check(depositAddress001) + "\",\"nmb721wm\",\"nmbwm\"";

    deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code,
            "constructor(address,string,string)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, depositKey001, depositAddress001
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    String parame1 = "\"" + Base58.encode58Check(depositAddress001) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc20Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
            depositAddress001, depositKey001, blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
    Assert.assertNotNull(mintTxid);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());
  }

  @Test(enabled = true, description = "Deposit Trc721 without mapping")
  public void test002DepositTrc721002() {
    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1000000000,
            depositAddress001, depositKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert
        .assertThat(ByteArray.toStr(infoById.get().getContractResult(0).toByteArray()),
            containsString("not an allowed token"));

    deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1000000000,
            foundationAddress001, foundationKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));

    deposittrx = PublicMethed
        .depositTrc721(mainChainAddress, mainChainAddress, 1001,
            1000000000,
            depositAddress001, depositKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert
        .assertThat(ByteArray.toStr(infoById.get().getContractResult(0).toByteArray()),
            containsString("not an allowed token"));


  }


  @Test(enabled = true, description = "Deposit Trc721 After mapping")
  public void test003DepositTrc721002() {

    String mapTxid = PublicMethed
        .mappingTrc721(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress001, depositKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Assert.assertNotNull(mapTxid);

    String parame2 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            depositAddress001, depositKey001, blockingSideStubFull);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);

    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1000000000,
            foundationAddress001, foundationKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));

    deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1,
            foundationAddress001, foundationKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert
        .assertThat(ByteArray.toStr(infoById.get().getResMessage().toByteArray()),
            containsString("Not enough energy for 'PUSH1' operation executing"));
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
