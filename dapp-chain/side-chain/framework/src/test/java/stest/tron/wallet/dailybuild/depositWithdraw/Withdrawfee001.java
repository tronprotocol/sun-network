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
public class Withdrawfee001 {


  private static final long now = System.currentTimeMillis();
  private static final long TotalSupply = 1000L;
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
  private final String testKeyFordeposit1 = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key3");
  private final byte[] depositAddress1 = PublicMethed.getFinalAddress(testKeyFordeposit1);
  private final String testDepositTrx1 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress1 = PublicMethed.getFinalAddress(testDepositTrx1);
  private final String mainGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final byte[] mainGateWayOwnerAddress = PublicMethed.getFinalAddress(mainGateWayOwner);
  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);
  ByteString assetAccountId;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] testAddress001 = ecKey2.getAddress();
  String testKey001 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
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
    byte[] input = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
  }


  @Test(enabled = true, description = "withdrawfee001")
  public void withdraw01fee001() {
    PublicMethed.printAddress(testKeyFordeposit);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 11000_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 11000_000_000L);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

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

    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    Long balanceBeforeWithdraw = accountBefore.getBalance();

    //value<fee
    String withdrawTrc20Txid = PublicMethed.withdrawtrc20fee(ChainIdAddress,
        sideChainAddress, "100",
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, 100, testKeyFordeposit, blockingStubFull,
        blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(1, infoByIdwithdrawTrc20.get().getResultValue());

    Long balanceAfterWithdraw =
        PublicMethed.queryAccount(depositAddress, blockingSideStubFull).getBalance();
    Assert.assertEquals(balanceAfterWithdraw.longValue(),
        balanceBeforeWithdraw - infoByIdwithdrawTrc20.get().getFee());

    byte[] input14 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response5 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input14,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);

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

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById14 = PublicMethed
        .getTransactionInfoById(txid4, blockingSideStubFull);
    Assert.assertEquals(1, infoById14.get().getResultValue());
    String msg = ByteArray.toStr(ByteArray.fromHexString(
        Hex.toHexString(infoById14.get().getContractResult(0).toByteArray()).substring(136, 222)));
    Assert.assertEquals("value must be >= withdrawMinTrx+withdrawFee", msg);

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


  @Test(enabled = true, description = "withdrawfee001")
  public void withdraw03fee003() {

    PublicMethed.printAddress(testKeyFordeposit);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 11000_000_000L, foundationAddress001, foundationKey001,
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

    Assert.assertEquals(0, infoById.get().getResultValue());
//    Assert.assertEquals(10000_000_000L - fee, accountBeforeBalance);
//    Assert.assertEquals(callValue, accountSideBeforeBalance);

    // deploy 721contract
    String contractName = "trc721";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC721");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC721");
    String parame = "\"" + Base58.encode58Check(depositAddress) + "\",\"nmb721wm\",\"nmbwm\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code,
            "constructor(address,string,string)",
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

    //mint 721
    String parame1 = "\"" + Base58.encode58Check(depositAddress) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc20Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
            depositAddress, testKeyFordeposit, blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
    Assert.assertNotNull(mintTxid);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    // mapping contract721 to sideChain
    String mapTxid = PublicMethed
        .mappingTrc721(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
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
            depositAddress, testKeyFordeposit, blockingSideStubFull);
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
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertNotNull(infoById);
    logger.info("contractResult:" + ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray()));
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    // TRC721`s owner in sideChain should be Depositor
    String arg = "1001";
    TransactionExtention transactionExtention = PublicMethed
        .triggerConstantContractForExtention(sideContractAddress, "ownerOf(uint256)", arg, false,
            0l, maxFeeLimit, "0", 0l, depositAddress, testKeyFordeposit, blockingSideStubFull);
    tmpAddress = ByteArray.toHexString(transactionExtention.getConstantResult(0).toByteArray());
    tmpAddress = tmpAddress.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressFinal);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    // TRC721`s owner in mainChain should be mainGateway
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainChainAddress, addressFinal);

    byte[] input16 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response05 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input16,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonsBeforetrc10 = ByteArray.toLong(response05.getConstantResult(0).toByteArray());
    logger.info("bonsBeforetrc10:" + bonsBeforetrc10);

    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "200", false));
    String txid02 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input10, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById02 = PublicMethed
        .getTransactionInfoById(txid02, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById02.get().getResult().name());
    Assert.assertEquals(0, infoById02.get().getResultValue());

    // value<fee

    Account account = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    Long balanceBefore = account.getBalance();

    methodStr = "withdrawal(uint256)";
    input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChainfee(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0, input,
            maxFeeLimit,
            0, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    logger.info("fee:" + infoById.get().getFee());
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(1, infoById.get().getResultValue());

    Long balnceAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull).getBalance();
    Assert.assertEquals(balanceBefore - infoById.get().getFee(), balnceAfter.longValue());

    // value=fee
    methodStr = "withdrawal(uint256)";
    input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid2 = PublicMethed
        .triggerContractSideChainfee(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 200, input,
            maxFeeLimit,
            0, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("withdrawTxid2: " + withdrawTxid2);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid2, blockingSideStubFull);
    logger.info("fee:" + infoById.get().getFee());
    Assert.assertNotNull(withdrawTxid2);
    Assert.assertEquals(0, infoById.get().getResultValue());

    TransactionExtention response17 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input16,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonustrc10After1 = ByteArray.toLong(response17.getConstantResult(0).toByteArray());
    logger.info("bonustrc721After1:" + bonustrc10After1);
    Assert.assertEquals(bonsBeforetrc10 + 200, bonustrc10After1);

    // value>fee
// deposit TRC721 to sideChain
    String deposittrx3 = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1001,
            1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info(deposittrx3);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(deposittrx3, blockingStubFull);
    Assert.assertNotNull(deposittrx3);
    Assert.assertEquals(0, infoById3.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById3.get().getResult().name());

    byte[] input12 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "200", false));
    String txid04 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input12, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById04 = PublicMethed
        .getTransactionInfoById(txid04, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById04.get().getResult().name());
    Assert.assertEquals(0, infoById04.get().getResultValue());

    methodStr = "withdrawal(uint256)";
    input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid05 = PublicMethed
        .triggerContractSideChainfee(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 500, input,
            maxFeeLimit,
            0, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("withdrawTxid05: " + withdrawTxid05);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById05 = PublicMethed
        .getTransactionInfoById(withdrawTxid05, blockingSideStubFull);
    logger.info("fee:" + infoById05.get().getFee());
    Assert.assertNotNull(withdrawTxid05);
    Assert.assertEquals(0, infoById05.get().getResultValue());
    TransactionExtention response06 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input16,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonustrc10After2 = ByteArray.toLong(response06.getConstantResult(0).toByteArray());
    logger.info("bonustrc721After2:" + bonustrc10After2);
    Assert.assertEquals(bonustrc10After1 + 200, bonustrc10After2);

    byte[] input11 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid03 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input11, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById03 = PublicMethed
        .getTransactionInfoById(txid03, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById03.get().getResult().name());
    Assert.assertEquals(0, infoById03.get().getResultValue());


  }

  @Test(enabled = true, description = "setwithdrawfee001")
  public void withdraw04feetrc04() {
    PublicMethed.printAddress(testKeyFordeposit);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress1, 11000_000_000L, testDepositAddress1, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountAfter = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    logger.info("accountAfterBalance:" + accountAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    logger.info("accountSideAfterBalance:" + accountSideAfterBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
        maxFeeLimit, 0, "", depositAddress1, testKeyFordeposit1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());
    long fee = infoById.get().getFee();

    Account accountBefore = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    //not owner
    byte[] input15 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid8 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input15, 1000000000,
            0l, "0", depositAddress1, testKeyFordeposit1, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    logger.info("param is -1 --txid:" + txid6);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById6 = PublicMethed
        .getTransactionInfoById(txid6, blockingSideStubFull);
    Assert.assertEquals("FAILED", infoById6.get().getResult().name());
    Assert.assertEquals(1, infoById6.get().getResultValue());
    String msg = Hex.toHexString(infoById6.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135, 170)));
    Assert.assertEquals("\u0001less than 100 TRX", msg);

    //fee=100000001L
    input6 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)",
        "100000001", false));
    txid6 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input6, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById6 = PublicMethed
        .getTransactionInfoById(txid6, blockingSideStubFull);
    Assert.assertEquals("FAILED", infoById6.get().getResult().name());
    Assert.assertEquals(1, infoById6.get().getResultValue());
    msg = Hex.toHexString(infoById6.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135, 170)));
    Assert.assertEquals("\u0001less than 100 TRX", msg);

    //fee=99999999L
    byte[] input20 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "99999999", false));
    String txid20 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input20, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    Optional<TransactionInfo> infoById20 = PublicMethed
        .getTransactionInfoById(txid20, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById20.get().getResult().name());
    Assert.assertEquals(0, infoById20.get().getResultValue());
  }

  @Test(enabled = true, description = "setwithdrawfee001")
  public void withdraw04feetrc05() {

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress1, 3100_000_000L, testDepositAddress1, testDepositTrx1,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainBalance = accountMainBefore.getBalance();
    assetAccountId = PublicMethed
        .queryAccount(depositAddress1, blockingStubFull).getAssetIssuedID();
    logger.info("The token ID: " + assetAccountId.toStringUtf8());
    Long depositMainTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingStubFull);
    Long depositSideTokenBefore = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingSideStubFull);

    Account accountSideBefore = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    //    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);
    logger.info("depositMainTokenBefore:" + depositMainTokenBefore);
    logger.info("depositSideAddressTokenBefore:" + depositSideTokenBefore);
    //    Assert.assertTrue(depositSideTokenBefore == 0);

    String methodStr = "depositTRC10(uint64,uint64)";

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 10;
    String inputParam = inputTokenID + "," + inputTokenValue;
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress1, testKeyFordeposit1,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById;
    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress1, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountMainAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBalance - fee);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress1, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress1), accountSideAfterAddress);
    //    Assert.assertEquals(0, accountSideAfterBalance);
    Long depositSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingSideStubFull);
    //    Assert.assertTrue(inputTokenValue == depositSideTokenAfter);
    Long depositMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingStubFull);
    Assert.assertTrue(depositMainTokenBefore - inputTokenValue == depositMainTokenAfter);
    logger.info("depositMainTokenAfter:" + depositMainTokenAfter);
    logger.info("depositSideTokenAfter:" + depositSideTokenAfter);
    //    Assert.assertTrue(depositSideTokenAfter == inputTokenValue);

    String methodStr1 = "depositTRX()";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, "", false));

    long callValue1 = 1500000000;
    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue1,
            input1,
            maxFeeLimit, 0, "", depositAddress1, testKeyFordeposit1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    byte[] input16 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response05 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input16,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonsBeforetrc10 = ByteArray.toLong(response05.getConstantResult(0).toByteArray());
    logger.info("bonsBeforetrc10:" + bonsBeforetrc10);

    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "200", false));
    String txid02 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input10, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById02 = PublicMethed
        .getTransactionInfoById(txid02, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById02.get().getResult().name());
    Assert.assertEquals(0, infoById02.get().getResultValue());

    // value<fee
    methodStr = "withdrawal(uint256)";
    String withdrawToken = Long.toString(inputTokenValue - 1);

    String withdrawTxid1 = PublicMethed
        .withdrawTrc10fee(inputTokenID, withdrawToken, ChainIdAddress,
            sideGatewayAddress,
            0,
            0, maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull,
            blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    logger.info("fee:" + infoById.get().getFee());
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(1, infoById.get().getResultValue());
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    //value=fee
    Long withdrawSideTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingSideStubFull);
    logger.info("withdrawSideTokenAfter:" + withdrawSideTokenAfter);
    long withdrawMainTokenAfter = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingStubFull);
    logger.info("withdrawMainTokenAfter:" + withdrawMainTokenAfter);

    String withdrawToken1 = Long.toString(inputTokenValue - 1);
    String txid5 = PublicMethed
        .withdrawTrc10fee(inputTokenID, withdrawToken1, ChainIdAddress,
            sideGatewayAddress,
            0,
            200, maxFeeLimit, depositAddress1, testKeyFordeposit1, blockingStubFull,
            blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById5 = PublicMethed
        .getTransactionInfoById(txid5, blockingStubFull);
    Assert.assertTrue(infoById5.get().getResultValue() == 0);
    long fee2 = infoById5.get().getFee();
    logger.info("fee2:" + fee2);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById5.get().getResultValue());
    Account accountWithdrawSideAfter = PublicMethed
        .queryAccount(depositAddress1, blockingSideStubFull);
    long accountWithdrawSideAfterBalance = accountWithdrawSideAfter.getBalance();
    ByteString addressWithdrawSideAfter = accountWithdrawSideAfter.getAddress();
    String addressWithdrawSideAfterAddress = Base58
        .encode58Check(addressWithdrawSideAfter.toByteArray());
    logger.info("addressWithdrawSideAfterAddress:" + addressWithdrawSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress1), addressWithdrawSideAfterAddress);
    Long withdrawSideTokenAfter1 = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingSideStubFull);

    long withdrawMainTokenAfter1 = PublicMethed
        .getAssetIssueValue(depositAddress1, assetAccountId, blockingStubFull);
    logger.info("withdrawSideTokenAfter1:" + withdrawSideTokenAfter1);
    logger.info("withdrawMainTokenAfter1:" + withdrawMainTokenAfter1);
    Assert.assertTrue(withdrawSideTokenAfter - inputTokenValue + 1 == withdrawSideTokenAfter1);
    Assert.assertTrue(withdrawMainTokenAfter + inputTokenValue - 1 == withdrawMainTokenAfter1);

    byte[] input17 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));

    TransactionExtention response06 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input17,
            maxFeeLimit, 0, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    long bonsaftertrc10 = ByteArray.toLong(response06.getConstantResult(0).toByteArray());
    logger.info("bonsaftertrc10:" + bonsaftertrc10);
    Assert.assertEquals(bonsBeforetrc10 + 200, bonsaftertrc10);

    byte[] input11 = Hex.decode(AbiUtil.parseMethod("setWithdrawFee(uint256)", "0", false));
    String txid03 = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            ChainIdAddressKey, 0l, input11, 1000000000,
            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById03 = PublicMethed
        .getTransactionInfoById(txid03, blockingSideStubFull);
    Assert.assertEquals("SUCESS", infoById02.get().getResult().name());
    Assert.assertEquals(0, infoById03.get().getResultValue());


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



