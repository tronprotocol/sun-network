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
public class WithdrawTrc721002 {


  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] testAddress001 = ecKey1.getAddress();
  String testKey001 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  byte[] trc20Contract = null;
  byte[] sideContractAddress = null;
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
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

  @Test(enabled = true, description = "Deposit Trc721")
  public void test001DepositTrc20001() {

    PublicMethed.printAddress(testKey001);

    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress001, 11000_000_000L, foundationAddress001, foundationKey001,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(testAddress001, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(testAddress001, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
    Assert.assertEquals(callValue, accountSideBeforeBalance);

    // deploy 721contract
    String contractName = "trc721";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC721");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC721");
    String parame = "\"" + Base58.encode58Check(testAddress001) + "\",\"nmb721wm\",\"nmbwm\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code,
            "constructor(address,string,string)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, testKey001, testAddress001
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    //mint 721
    String parame1 = "\"" + Base58.encode58Check(testAddress001) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc20Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
            testAddress001, testKey001, blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
    Assert.assertNotNull(mintTxid);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    // mapping contract721 to sideChain
    String mapTxid = PublicMethed
        .mappingTrc721(mainChainAddressKey, deployTxid, 1000000000,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Assert.assertNotNull(mapTxid);

    // get 721Contract in sideChain
    String parame2 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input2,
            maxFeeLimit,
            0, "0",
            testAddress001, testKey001, blockingSideStubFull);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertNotNull(sideContractAddress);

    // deposit TRC721 to sideChain
    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertNotNull(deposittrx);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    // TRC721`s owner in sideChain should be Depositor
    String arg = "1001";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    tmpAddress = ByteArray.toHexString(infoById2.get().getContractResult(0).toByteArray());
    tmpAddress = tmpAddress.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertNotNull(ownerTrx);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    // TRC721`s owner in mainChain should be mainGateway
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainChainAddress, addressFinal);

  }

  @Test(enabled = true, description = "withdraw wrong account and wrong tokenID")
  public void test002DepositTrc20001() {
    // account donot own trc721, withdraw should be REVERT
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] testAddress002 = ecKey2.getAddress();
    String testKey002 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    PublicMethed.printAddress(testKey002);

    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress002, 11000_000_000L, foundationAddress001, foundationKey001,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(testAddress002, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(testAddress002, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertNotNull(txid);
    Assert.assertEquals(0, infoById.get().getResultValue());

    String withdrawtxid = PublicMethed
        .withdrawTrc(ChainIdAddress, sideContractAddress, "1001",
            maxFeeLimit,
            foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawtxid, blockingSideStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("FAILED", infoById.get().getResult().name());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));

    // withdraw with wrong tokenID
    String withdrawtxid2 = PublicMethed
        .withdrawTrc(ChainIdAddress, sideContractAddress, "1002",
            maxFeeLimit,
            testAddress001, testKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawtxid2, blockingSideStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("FAILED", infoById.get().getResult().name());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));

  }

  @Test(enabled = false, description = "withdraw twice")
  public void test003DepositTrc20001() {
    // withdraw TRC721
    String methodStr = "withdrawal(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0,
            input, maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());

    // TRC721`s owner in mainChain should be Depositor
    String arg = "1001";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    String ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    String tmpAddress = ContractRestule.substring(24);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    logger.info("infoById: " + infoById);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));

    // withdraw the TRC721 twice
    withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0, input,
            maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));
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
