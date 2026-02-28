package stest.tron.wallet.dailybuild.depositWithdraw;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Map;
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
public class DepositFeeTrc20001 {


  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
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
  String methodStr1 = null;
  String parame2 = null;
  String methodStr3 = null;
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

  @Test(enabled = true, description = "DepositFeeTrc20 normal.")
  public void test1DepositFeeTrc20001() {
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
    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0, input1,
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

    byte[] input2 = Hex.decode(AbiUtil.parseMethod("balanceOf(address)", parame, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input2, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20Before = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return2.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20Before:" + mainTrc20Before);

    String depositTrc20Id = PublicMethed
        .depositTrc20(WalletClient.encode58Check(trc20Contract), mainChainAddress, 1000, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(depositTrc20Id, blockingSideStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());

    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    int sideTrc20After = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(1000, sideTrc20After);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input2, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20After = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20After:" + mainTrc20After);

    methodStr1 = "setDepositFee(uint256)";
    long setDepositFee = 2;
    parame1 = String.valueOf(setDepositFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);
    long fee1 = infoById3.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "depositFee()";
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input4, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long getDepositFee = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return4.getConstantResult(0).toByteArray())));
    Assert.assertTrue(getDepositFee == setDepositFee);
    logger.info("depositFee:" + getDepositFee);
    byte[] input5 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    Assert.assertTrue(mainTrc20Before - 1000 == mainTrc20After);
    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    logger.info("accountMainBeforeBalance:" + accountMainBeforeBalance);

    //=0
    Map<String, String> depositTrc20 = PublicMethed
        .depositTrc20ForDepositFee(WalletClient.encode58Check(trc20Contract), mainChainAddress,
            1000, 0, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId = depositTrc20.get("depositId");
    String approveId = depositTrc20.get("approveId");
    logger.info("depositId:"+depositId);
    logger.info("approveId:"+approveId);
    Optional<TransactionInfo> depositInfo = PublicMethed
        .getTransactionInfoById(depositId, blockingStubFull);
    Assert.assertTrue(depositInfo.get().getResultValue() == 1);
    String data = ByteArray
        .toHexString(depositInfo.get().getContractResult(0).substring(67,97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));

    long depositFee = depositInfo.get().getFee();
    Optional<TransactionInfo>  approveInfo = PublicMethed
        .getTransactionInfoById(approveId, blockingStubFull);
    long approveFee = approveInfo.get().getFee();
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountMainAfterBalance:"+accountMainAfterBalance);
    Assert.assertEquals(accountMainBeforeBalance - depositFee - approveFee, accountMainAfterBalance);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    int sideTrc20After1 = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(sideTrc20After, sideTrc20After1);

    TransactionExtention return5 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input2, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20After1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return5.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20After1:" + mainTrc20After1);
    Assert.assertEquals(mainTrc20After, mainTrc20After1);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore, bonusBefore1);

    Account accountMainAfter1 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance1 = accountMainAfter1.getBalance();
    logger.info("accountMainAfterBalance1:"+accountMainAfterBalance1);

    //value>setDepositFee
    depositTrc20 = PublicMethed
        .depositTrc20ForDepositFee(WalletClient.encode58Check(trc20Contract), mainChainAddress,
            1000, setDepositFee + 1, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId1 = depositTrc20.get("depositId");
    String approveId1 = depositTrc20.get("approveId");
    logger.info("depositId1:"+depositId1);
    logger.info("approveId1:"+approveId1);
    Optional<TransactionInfo> depositInfo1 = PublicMethed
        .getTransactionInfoById(depositId1, blockingStubFull);
    Assert.assertTrue(depositInfo1.get().getResultValue() == 0);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    int sideTrc20After2 = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(sideTrc20After1 + 1000, sideTrc20After2);

    return5 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input2, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20After2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return5.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20After2:" + mainTrc20After2);
    Assert.assertTrue(mainTrc20After1 - 1000 == mainTrc20After2);

    long depositFee1 = depositInfo1.get().getFee();
    logger.info("depositFee1:"+depositFee1);
    Optional<TransactionInfo>  approveInfo1 = PublicMethed
        .getTransactionInfoById(approveId1, blockingStubFull);
    long approveFee1 = approveInfo1.get().getFee();
    logger.info("approveFee1:"+approveFee1);
    Account accountMainAfter2 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance2 = accountMainAfter2.getBalance();
    logger.info("accountMainAfterBalance2:" + accountMainAfterBalance2);
    Assert.assertEquals(accountMainAfterBalance1 - depositFee1 - approveFee1 - setDepositFee, accountMainAfterBalance2);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Assert.assertEquals(bonusBefore1 + setDepositFee, bonusBefore2);

    //value=setDepositFee
    depositTrc20 = PublicMethed
        .depositTrc20ForDepositFee(WalletClient.encode58Check(trc20Contract), mainChainAddress,
            1000, setDepositFee, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId2 = depositTrc20.get("depositId");
    String approveId2 = depositTrc20.get("approveId");
    logger.info("depositId2:"+depositId2);
    logger.info("approveId2:"+approveId2);
    Optional<TransactionInfo> depositInfo2 = PublicMethed
        .getTransactionInfoById(depositId2, blockingStubFull);
    Assert.assertTrue(depositInfo2.get().getResultValue() == 0);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    int sideTrc20After3 = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(sideTrc20After2 + 1000, sideTrc20After3);

    return5 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input2, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20After3 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return5.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20After3:" + mainTrc20After3);
    Assert.assertTrue(mainTrc20After2 - 1000 == mainTrc20After3);

    long depositFee2 = depositInfo2.get().getFee();
    Optional<TransactionInfo>  approveInfo2 = PublicMethed
        .getTransactionInfoById(approveId2, blockingStubFull);
    long approveFee2 = approveInfo2.get().getFee();
    Account accountMainAfter3 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance3 = accountMainAfter3.getBalance();
    logger.info("accountMainAfterBalance3:" + accountMainAfterBalance3);
    Assert.assertEquals(accountMainAfterBalance2 - depositFee2 - approveFee2 - setDepositFee, accountMainAfterBalance3);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore3:" + bonusBefore3);
    Assert.assertEquals(bonusBefore2 + setDepositFee, bonusBefore3);

    //value<setDepositFee
    depositTrc20 = PublicMethed
        .depositTrc20ForDepositFee(WalletClient.encode58Check(trc20Contract), mainChainAddress,
            1000, setDepositFee - 1, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId3 = depositTrc20.get("depositId");
    String approveId3 = depositTrc20.get("approveId");
    logger.info("depositId3:"+depositId3);
    logger.info("approveId3:"+approveId3);
    Optional<TransactionInfo> depositInfo3 = PublicMethed
        .getTransactionInfoById(depositId3, blockingStubFull);
    Assert.assertTrue(depositInfo3.get().getResultValue() == 1);
    data = ByteArray
        .toHexString(depositInfo3.get().getContractResult(0).substring(67,97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));

    long depositFee3 = depositInfo3.get().getFee();
    Optional<TransactionInfo>  approveInfo3 = PublicMethed
        .getTransactionInfoById(approveId3, blockingStubFull);
    long approveFee3 = approveInfo3.get().getFee();
    Account accountMainAfter4 = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance4 = accountMainAfter4.getBalance();
    logger.info("accountMainAfterBalance4:" + accountMainAfterBalance4);
    Assert.assertEquals(accountMainAfterBalance3 - depositFee3 - approveFee3, accountMainAfterBalance4);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
            1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    int sideTrc20After4 = ByteArray.toInt(infoById2.get().getContractResult(0).toByteArray());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertEquals(sideTrc20After3, sideTrc20After4);

    return5 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input2, 1000000000,
            0l, "0", depositAddress, testKeyFordeposit, blockingStubFull);
    Long mainTrc20After4 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(return5.getConstantResult(0).toByteArray())));
    logger.info("mainTrc20After4:" + mainTrc20After4);
    Assert.assertEquals(mainTrc20After3, mainTrc20After4);

    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore4 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore4:" + bonusBefore4);
    Assert.assertEquals(bonusBefore3, bonusBefore4);
  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    parame2 = "0";
    methodStr1 = "setDepositFee(uint256)";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame2, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
