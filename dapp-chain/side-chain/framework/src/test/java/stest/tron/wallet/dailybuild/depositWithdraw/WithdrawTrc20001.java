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
public class WithdrawTrc20001 {


  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "WithdrawTrc20")
  public void test1WithdrawTrc20001() {

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
    trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    mappingNonce = ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray());
    logger.info("mappingNonce:" + mappingNonce);

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

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance:" + mainTrc20Balance);
    Assert.assertEquals(100000000000000000L , mainTrc20Balance.longValue());

    String depositTrc20txid = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infodeposittrx = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);
    depositNonce = ByteArray.toHexString(infodeposittrx.get().getContractResult(0).toByteArray());
    logger.info("depositNonce:" + depositNonce);
    Assert.assertEquals(0, infodeposittrx.get().getResultValue());

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid : " + sideChainTxid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid, blockingSideStubFull);
    int afterDepositSideChain = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(1000, afterDepositSideChain);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance2:" + mainTrc20Balance2);
    Assert.assertEquals(mainTrc20Balance - 1000 , mainTrc20Balance2.longValue());

    String withdrawTrc20Txid = PublicMethed.withdrawTrc20(ChainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    withdrawNonce = ByteArray.toHexString(infoByIdwithdrawTrc20.get().getContractResult(0).toByteArray());
    logger.info("withdrawNonce:" + withdrawNonce);

    Assert.assertEquals(0, infoByIdwithdrawTrc20.get().getResultValue());
    logger.info("infoByIdwithdrawTrc20Fee:" + infoByIdwithdrawTrc20.get().getFee());

    byte[] input4 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input4,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    int afterWithdrawsidechain = ByteArray
        .toInt(infoById3.get().getContractResult(0).toByteArray());
    Assert.assertEquals(afterDepositSideChain - 100, afterWithdrawsidechain);

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input4, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long afterWithdrawmainBalance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return4.getConstantResult(0).toByteArray())));
    logger.info("afterWithdrawmainBalance:" + afterWithdrawmainBalance);
    Assert.assertTrue(mainTrc20Balance - 900 == afterWithdrawmainBalance);

  }

  @Test(enabled = true, description = "get DepositTrc20Msg MappingTrc20Msg and WithdrawTrc20Msg")
  public void test2getDepositAndWithdrawMsg() {

    // get DepositMsg
    String methodStr = "getDepositMsg(uint256)";
    String parame = depositNonce;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0, input,
            maxFeeLimit, 0, "0",
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("return1: " + return1);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());
    logger.info("ContractRestule："+ContractRestule);
    String ownerAddress = ContractRestule.substring(24, 64);
    logger.info(ownerAddress);
    String addressHex = "41" + ownerAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(depositAddress), addressFinal);

    String value5 = ContractRestule.substring(1 + 64 * 1, 64 * 2);
    Assert.assertEquals(0, Integer.parseInt(value5, 16));

    String value1 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(2, Integer.parseInt(value1, 16));

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

    String depositValue = ContractRestule.substring(1 + 64 * 6, 64 * 7);
    Assert.assertEquals(1000, Integer.parseInt(depositValue, 16));

    // get WithdrawMsg
    methodStr = "getWithdrawMsg(uint256)";
    parame = withdrawNonce;
    input = Hex.decode(AbiUtil.parseMethod(methodStr, parame, true));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideChainAddress), 0, input,
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

    value1 = ContractRestule.substring(1 + 64 * 1 + 23, 64 * 2);
    addressHex = "41" + value1;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(WalletClient.encode58Check(trc20Contract), addressFinal);

    value2 = ContractRestule.substring(1 + 64 * 2, 64 * 3);
    Assert.assertEquals(0, Integer.parseInt(value2, 16));

    value3 = ContractRestule.substring(1 + 64 * 3, 64 * 4);
    Assert.assertEquals(100, Integer.parseInt(value3, 16));

    value4 = ContractRestule.substring(1 + 64 * 4, 64 * 5);
    Assert.assertEquals(2, Integer.parseInt(value4, 16));

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
            depositAddress, testKeyFordeposit, blockingStubFull);
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
    Assert.assertEquals(2, Integer.parseInt(depositValue, 16));

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
