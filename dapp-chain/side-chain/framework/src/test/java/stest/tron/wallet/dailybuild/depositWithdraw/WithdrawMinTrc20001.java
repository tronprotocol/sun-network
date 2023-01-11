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
public class WithdrawMinTrc20001 {


  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  String parame2 = null;
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
    parame2 = "0";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
  }

  @Test(enabled = true, description = "WithdrawTrc20")
  public void test1WithdrawMinTrc20001() {

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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Balance = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance:" + mainTrc20Balance);
    Assert.assertTrue(100000000000000000L == mainTrc20Balance);
    long depositValue = 1000;
    String depositTrc20txid = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, depositValue,
            1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infodeposittrx = PublicMethed
        .getTransactionInfoById(depositTrc20txid, blockingStubFull);
    logger.info("infodeposittrx:" + infodeposittrx);
    logger.info("resultValue:" + infodeposittrx.get().getResultValue());
    logger.info("contractResult:" + ByteArray
        .toHexString(infodeposittrx.get().getContractResult(0).toByteArray()));
    Assert.assertEquals(0, infodeposittrx.get().getResultValue());

    String sideChainTxid = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid : " + sideChainTxid);
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
    Assert.assertTrue(mainTrc20Balance - depositValue == mainTrc20Balance2);

    long withdrawValue = 100;
    String withdrawValueString = Long.toString(withdrawValue);
    String withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, withdrawValueString,
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    logger.info("withdrawTrc20Txid:" + withdrawTrc20Txid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc20.get().getResultValue());
    logger.info("infoByIdwithdrawTrc20Fee:" + infoByIdwithdrawTrc20.get().getFee());

    String sideChainTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid1 : " + sideChainTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(sideChainTxid1, blockingSideStubFull);
    int afterDepositSideChain1 = ByteArray
        .toInt(infoById3.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById3.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain - withdrawValue, afterDepositSideChain1);

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long afterMainTrc20Balance1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return4.getConstantResult(0).toByteArray())));
    logger.info("afterMainTrc20Balance1:" + afterMainTrc20Balance1);
    Assert.assertTrue(mainTrc20Balance2 + withdrawValue == afterMainTrc20Balance1);

    parame2 = "100";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());

    withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, withdrawValueString,
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc20.get().getResultValue());

    String sideChainTxid2 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid2 : " + sideChainTxid2);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid2, blockingSideStubFull);
    int afterDepositSideChain2 = ByteArray
        .toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain1 - withdrawValue, afterDepositSideChain2);

    return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    long afterMainTrc20Balance2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Balance2:" + mainTrc20Balance2);
    Assert.assertTrue(afterMainTrc20Balance1 + withdrawValue == afterMainTrc20Balance2);

    //value >WithdrawMinTrc10
    withdrawValue = 110;
    withdrawValueString = Long.toString(withdrawValue);
    withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, withdrawValueString,
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoByIdwithdrawTrc20 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertEquals(0, infoByIdwithdrawTrc20.get().getResultValue());

    sideChainTxid2 = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("sideChainTxid2 : " + sideChainTxid2);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(sideChainTxid2, blockingSideStubFull);
    int afterDepositSideChain3 = ByteArray
        .toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(afterDepositSideChain2 - withdrawValue, afterDepositSideChain3);

    return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    long afterMainTrc20Balance3 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("afterMainTrc20Balance3:" + afterMainTrc20Balance3);
    Assert.assertTrue(afterMainTrc20Balance2 + withdrawValue == afterMainTrc20Balance3);

    //value <WithdrawMinTrc10

    Account account = PublicMethed.queryAccount(depositAddress,blockingSideStubFull);
    Long balanceBefore = account.getBalance();
    logger.info("balanceBefore : " + balanceBefore);
    withdrawValue = 90;
    withdrawValueString = Long.toString(withdrawValue);

    withdrawTrc20Txid = PublicMethed.withdrawTrc20(chainIdAddress,
        sideChainAddress, withdrawValueString,
        WalletClient.encode58Check(sideContractAddress),
        maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById3 = PublicMethed
        .getTransactionInfoById(withdrawTrc20Txid, blockingSideStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 1);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById3.get().getResMessage().toByteArray()));

    Long balnceAfter = PublicMethed.queryAccount(depositAddress,blockingSideStubFull).getBalance();
    logger.info("balnceAfter : " + balnceAfter);
    Assert.assertEquals(balanceBefore - infoById3.get().getFee(),balnceAfter.longValue());

  }

  @Test(enabled = true, description = "WithdrawTrc20 with triggerAccount exception and "
      + "minTrc20 Value range")
  public void test2WithdrawMinTrc20002() {
    //not gateWay owner trigger setDepositMinTrx method
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() != 0);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById1.get().getResMessage().toByteArray()));

    parame2 = "-1";
    input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());

    // parame2 = "-9223372036854775808";
    long setWithdrawMinTrc201 = Long.MIN_VALUE;
    parame2 = String.valueOf(setWithdrawMinTrc201);
    input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById4 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());

    //parame2 = "9223372036854775807";

    setWithdrawMinTrc201 = Long.MAX_VALUE;
    parame2 = String.valueOf(setWithdrawMinTrc201);
    input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById4 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());

    setWithdrawMinTrc201 = Long.MAX_VALUE + 1;
    parame2 = String.valueOf(setWithdrawMinTrc201);
    input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById4 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());

    setWithdrawMinTrc201 = Long.MIN_VALUE - 1;
    parame2 = String.valueOf(setWithdrawMinTrc201);
    input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById4 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());
  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    parame2 = "1";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrc20(uint256)", parame2, false));
    PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input3,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
