package stest.tron.wallet.depositWithdraw;


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
public class Withdrawfee001 {

  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private static final long now = System.currentTimeMillis();
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static final long TotalSupply = 1000L;
  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

//    ECKey ecKey2 = new ECKey(Utils.getRandom());
//    byte[] depositAddress1 = ecKey1.getAddress();
//    String testKeyFordeposit1 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
//
//    ECKey ecKey3 = new ECKey(Utils.getRandom());
//    byte[] depositAddress2 = ecKey1.getAddress();
//    String testKeyFordeposit2 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);

  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  private final String mainGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final byte[] mainGateWayOwnerAddress = PublicMethed.getFinalAddress(mainGateWayOwner);

  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);

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
//    PublicMethed.printAddress(testKeyFordeposit);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
    byte[] input = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
  }


  @Test(enabled = true, description = "withdrawfee001")
  public void withdraw01fee001() {

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

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

//        Assert.assertEquals(0, infoById.get().getResultValue());
//        Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
//        Assert.assertEquals(callValue, accountSideBeforeBalance);

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

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(deposittrx, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "200", false));
    String txid2 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input10, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());
//value<fee
    String withdrawTrc20Txid = PublicMethed.withdrawtrc20fee(ChainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, 100, testKeyFordeposit, blockingStubFull,
        blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(1, infoByIdwithdrawTrc20.get().getResultValue());
    byte[] input14 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response5 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input14,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
//        Integer.parseInt(String s, 16);
//logger.info(("ss:"+response5.getMessage().toStringUtf8()));

    long bonsBefore = ByteArray.toLong(response5.getConstantResult(0).toByteArray());
    logger.info("bonsBefore:" + bonsBefore);

    // value=fee
    Account accountBeforeSideWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountBeforeSideWithdrawBalance = accountBeforeSideWithdraw.getBalance();
    logger.info("accountBeforeSideWithdrawBalance:" + accountBeforeSideWithdrawBalance);

    String withdrawTrc20Txid1 = PublicMethed.withdrawtrc20fee(ChainIdAddress,
        sideChainAddress, "200",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, 200, testKeyFordeposit, blockingStubFull,
        blockingSideStubFull);
    logger.info("withdrawTrc20Txid1:" + withdrawTrc20Txid1);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoByIdwithdrawTrc21 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid1, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc21.get().getResultValue());
    long fee1 = infoByIdwithdrawTrc21.get().getFee();

    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    logger.info("accountSideAfterWithdrawBalance:" + accountSideAfterWithdrawBalance);
    Assert.assertEquals(accountBeforeSideWithdrawBalance - 200 - fee1,
        accountSideAfterWithdrawBalance);

    byte[] input15 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input15,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonusAfter = ByteArray.toLong(response6.getConstantResult(0).toByteArray());
    logger.info("bonusAfter:" + bonusAfter);
    Assert.assertEquals(bonsBefore + 200,
        bonusAfter);

    //value>fee

    Account accountBeforeWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountBeforeWithdrawBalance = accountBeforeWithdraw.getBalance();
    logger.info("accountBeforeWithdrawBalance:" + accountBeforeWithdrawBalance);
    String withdrawTrc20Txid2 = PublicMethed.withdrawtrc20fee(ChainIdAddress,
        sideChainAddress, "200",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, 300, testKeyFordeposit, blockingStubFull,
        blockingSideStubFull);
    logger.info("withdrawTrc20Txid2:" + withdrawTrc20Txid2);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoByIdwithdrawTrc22 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid2, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc22.get().getResultValue());
    fee1 = infoByIdwithdrawTrc22.get().getFee();

    response6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input15,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonusAfter1 = ByteArray.toLong(response6.getConstantResult(0).toByteArray());
    logger.info("bonusAfter1:" + bonusAfter1);
    Assert.assertEquals(bonusAfter + 200,
        bonusAfter1);
    Account accountAfterWithdraw = PublicMethed
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountAfterWithdrawBalance = accountAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountAfterWithdrawBalance);
    Assert.assertEquals(accountBeforeWithdrawBalance - 200 - fee1, accountAfterWithdrawBalance);
    byte[] input13 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid13 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input13, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    Optional<TransactionInfo> infoById13 = PublicMethed
        .getTransactionInfoById(txid13, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById13.get().getResult().name());
    Assert.assertEquals(0, infoById13.get().getResultValue());


  }

  @Test(enabled = true, description = "withdrawfee001")
  public void withdraw02fee002() {
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

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
//
//        Assert.assertEquals(0, infoById.get().getResultValue());
//        Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
//        Assert.assertEquals(callValue, accountSideBeforeBalance+accountSideBeforeBalance);

    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "200", false));
    String txid2 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input10, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());
//value<fee
    long withdrawValue = 1;
    String txid4 = PublicMethed
        .withdrawTrxfee(ChainIdAddress,
            sideGatewayAddress,
            withdrawValue, 100,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);

    Optional<TransactionInfo> infoById14 = PublicMethed
        .getTransactionInfoById(txid4, blockingSideStubFull);
    Assert.assertEquals(1, infoById14.get().getResultValue());

//value=fee
    Account accountBefore1 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance1 = accountBefore1.getBalance();
    Account accountSideBefore1 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance1 = accountSideBefore1.getBalance();

    byte[] input15 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input15,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonusAfter = ByteArray.toLong(response6.getConstantResult(0).toByteArray());
    logger.info("bonusAfter:" + bonusAfter);

    String txid5 = PublicMethed
        .withdrawTrxfee(ChainIdAddress,
            sideGatewayAddress,
            withdrawValue, 200,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById15 = PublicMethed
        .getTransactionInfoById(txid5, blockingSideStubFull);
    Assert.assertEquals(0, infoById15.get().getResultValue());
    long fee1 = infoById15.get().getFee();
    Account accountBefore2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance2 = accountBefore2.getBalance();
    Account accountSideBefore2 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance2 = accountSideBefore2.getBalance();
    Assert.assertEquals(accountSideBeforeBalance1 - fee1 - 200 - withdrawValue,
        accountSideBeforeBalance2);
    Assert.assertEquals(accountBeforeBalance1 + withdrawValue, accountBeforeBalance2);

    response6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input15,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonusAfter1 = ByteArray.toLong(response6.getConstantResult(0).toByteArray());
    logger.info("bonusAfter1:" + bonusAfter1);
    Assert.assertEquals(bonusAfter + 200, bonusAfter1);

//value>fee

    Account accountBefore3 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance3 = accountBefore3.getBalance();
    logger.info("accountBeforeBalance3:" + accountBeforeBalance3);
    Account accountSideBefore3 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance3 = accountSideBefore3.getBalance();

    String txid6 = PublicMethed
        .withdrawTrxfee(ChainIdAddress,
            sideGatewayAddress,
            withdrawValue, 300,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById6 = PublicMethed
        .getTransactionInfoById(txid6, blockingSideStubFull);
    Assert.assertEquals(0, infoById6.get().getResultValue());
    fee1 = infoById6.get().getFee();

    response6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input15,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonusAfter2 = ByteArray.toLong(response6.getConstantResult(0).toByteArray());
    logger.info("bonusAfter1:" + bonusAfter2);
    Assert.assertEquals(bonusAfter1 + 200, bonusAfter2);
    Account accountBefore4 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance4 = accountBefore4.getBalance();
    Account accountSideBefore4 = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance4 = accountSideBefore4.getBalance();
    logger.info("accountBeforeBalance4:" + accountBeforeBalance4);

    Assert.assertEquals(accountBeforeBalance3 + withdrawValue + 100, accountBeforeBalance4);
    Assert.assertEquals(accountSideBeforeBalance3 - withdrawValue - fee1 - 300,
        accountSideBeforeBalance4);

    byte[] input16 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid13 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input16, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    Optional<TransactionInfo> infoById13 = PublicMethed
        .getTransactionInfoById(txid13, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById13.get().getResult().name());
    Assert.assertEquals(0, infoById13.get().getResultValue());


  }


  @Test(enabled = false, description = "withdrawfee001")
  public void withdraw03fee003() {

  }

  @Test(enabled = true, description = "setwithdrawfee001")
  public void withdraw04feetrc04() {
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

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
//
//        Assert.assertEquals(0, infoById.get().getResultValue());
//        Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
//        Assert.assertEquals(callValue, accountSideBeforeBalance);
    //not owner
    byte[] input15 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid8 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input15, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    Optional<TransactionInfo> infoById8 = PublicMethed
        .getTransactionInfoById(txid8, blockingSideStubFull);
    logger.info("txid8:" + txid8);

    Assert.assertEquals("FAILED", infoById8.get().getResult().name());
    Assert.assertEquals(1, infoById8.get().getResultValue());
//fee=-1
    byte[] input6 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "-1", false));
    String txid6 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input6, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    Optional<TransactionInfo> infoById6 = PublicMethed
        .getTransactionInfoById(txid6, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById6.get().getResult().name());
    Assert.assertEquals(0, infoById6.get().getResultValue());

    byte[] input20 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid20 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input20, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    Optional<TransactionInfo> infoById20 = PublicMethed
        .getTransactionInfoById(txid20, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById20.get().getResult().name());
    Assert.assertEquals(0, infoById20.get().getResultValue());
  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    byte[] input = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }


}

