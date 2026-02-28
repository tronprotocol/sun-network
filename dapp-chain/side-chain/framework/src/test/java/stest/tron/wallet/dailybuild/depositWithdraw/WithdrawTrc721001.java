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
public class WithdrawTrc721001 {


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
  private String depositNonce;
  private String withdrawNonce;
  private String mappingNonce;

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
  public void test1DepositTrc20001() {

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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);

    mappingNonce = ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray());
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

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertNotNull(sideContractAddress);

    // deposit TRC721 to sideChain
    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(deposittrx, blockingStubFull);
    depositNonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
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

    // withdraw TRC721
    methodStr = "withdrawal(uint256)";
    input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0, input,
            maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    logger.info("fee:" + infoById.get().getFee());
    withdrawNonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    // TRC721`s owner in mainChain should be Depositor
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    infoById = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));
  }


  @Test(enabled = true, description = "get DepositTrc721Msg MappingTrc721Msg and WithdrawTrc721Msg")
  public void test2getDepositAndWithdrawMsg() {

    // get DepositMsg
    String methodStr = "getDepositMsg(uint256)";
    String parame = depositNonce;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0, input,
            maxFeeLimit, 0, "0",
            testAddress001, testKey001, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    String addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(testAddress001), addressFinal);

    String depositValue = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    Assert.assertEquals(0, Integer.parseInt(depositValue, 16));

    String value1 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(3, Integer.parseInt(value1, 16));

    String value2 = ContractRestule.substring(1 + 64 * 3 + 23, 64 * 4);
    addressHex = "41" + value2;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(trc20Contract), addressFinal);

    String value3 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    Assert.assertEquals(0, Integer.parseInt(value3, 16));

    String value4 = ContractRestule.substring(1 + 64 * 5, 64 * 6);
    Assert.assertEquals(0, Integer.parseInt(value4, 16));

    String value5 = ContractRestule.substring(1 + 64 * 6, 64 * 7);
    Assert.assertEquals(1001, Integer.parseInt(value5, 16));

    // get WithdrawMsg
    methodStr = "getWithdrawMsg(uint256)";
    parame = withdrawNonce;
    input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress), 0, input,
            maxFeeLimit, 0, "0",
            testAddress001, testKey001, blockingSideStubFull);
    logger.info("return1: " + return2);
    logger.info(Hex.toHexString(return2.getConstantResult(0).toByteArray()));
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());

    ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(testAddress001), addressFinal);

    value1 = ContractRestule.substring(1 + 64 * 1 + 23, 64 * 2);
    addressHex = "41" + value1;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(trc20Contract), addressFinal);

    value2 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(0, Integer.parseInt(value2, 16));

    value3 = ContractRestule.substring(1 + 64 * 3, 64 * 4);
    Assert.assertEquals(1001, Integer.parseInt(value3, 16));

    value4 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    Assert.assertEquals(3, Integer.parseInt(value4, 16));

    value5 = ContractRestule.substring(1 + 64 * 5, 64 * 6);
    Assert.assertEquals(0, Integer.parseInt(value5, 16));

    // get DepositMsg
    methodStr = "getMappingMsg(uint256)";
    parame = mappingNonce;
    input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0, input,
            maxFeeLimit, 0, "0",
            testAddress001, testKey001, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(trc20Contract), addressFinal);

    depositValue = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    Assert.assertEquals(3, Integer.parseInt(depositValue, 16));

    value1 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(0, Integer.parseInt(value1, 16));
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
