package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.protos.Protocol.TransactionInfo.code.FAILED;

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
import org.tron.api.GrpcAPI.AccountResourceMessage;
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
public class RetryTrc721001 {


  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  final String gateWatOwnerSideAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
  private final String testOracle = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracle);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  private final byte[] gateWaySideOwnerAddress = PublicMethed
      .getFinalAddress(gateWatOwnerSideAddressKey);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] testAddress001 = ecKey1.getAddress();
  String testKey001 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);

  String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideGatewayAddressKey = WalletClient.decodeFromBase58Check(sideGatewayAddress);
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] depositAddress2 = ecKey2.getAddress();
  String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  String nonce = null;
  String nonceWithdraw = null;
  String nonceMap = null;
  byte[] sideContractAddress = null;
  byte[] trc721Contract = null;
  long oracleSideBeforeWithdrawSendBalance = 0;
  String methodStr1 = null;
  String parame3 = null;
  String methodStrSide = null;
  String parameSide1 = null;
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame3 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame3, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
  }

  @Test(enabled = true, description = "Deposit Trc721")
  public void test1RetryTrc721001() {

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
    String txid = PublicMethed.triggerContract(mainGateWayAddressKey, callValue, input,
        maxFeeLimit, 0, "", testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
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
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

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
    trc721Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc721Contract);

    //mint 721
    String parame1 = "\"" + Base58.encode58Check(testAddress001) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc721Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
    Assert.assertNotNull(mintTxid);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    // mapping contract721 to sideChain
    String mapTxid = PublicMethed
        .mappingTrc721(mainGateWayAddressKey, deployTxid, 1000000000,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("mapTxid:" + mapTxid);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    Assert.assertNotNull(mapTxid);
    nonceMap = ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray());
    logger.info("nonceMap:" + nonceMap);

    //retry mapping trc10
    String retryMaptxid = PublicMethed.retryMapping(mainGateWayAddress,
        nonceMap, maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid);
    Optional<TransactionInfo> infoByIdretryMaptxid = PublicMethed
        .getTransactionInfoById(retryMaptxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid.get().getResultValue() == 0);

    // get 721Contract in sideChain
    String parame2 = "\"" + Base58.encode58Check(trc721Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideGatewayAddressKey, 0, input2,
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
        .depositTrc721(WalletClient.encode58Check(trc721Contract), mainGateWayAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertNotNull(deposittrx);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());
    nonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);

    // TRC721`s owner in sideChain should be Depositor
    String arg = "1001";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
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
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    //retry deposit trx  with no retryfee
    String retryDepositTxid = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return3.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    // withdraw TRC721
    methodStr = "withdrawal(uint256)";
    input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0, input, maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    nonceWithdraw = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());

    // TRC721`s owner in mainChain should be Depositor
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
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

    Account accountRetryWithdraw = PublicMethed.queryAccount(testAddress001,blockingSideStubFull);
    Long balanceBeforeRetryWithdraw = accountRetryWithdraw.getBalance();
    //retry  Withdraw 721  with no retryfee

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, testAddress001, testKey001, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);

    Long balanceAfterRetryWithdraw = PublicMethed.queryAccount(testAddress001,
        blockingSideStubFull).getBalance();
    Assert.assertEquals(balanceAfterRetryWithdraw.longValue(),
        balanceBeforeRetryWithdraw - infoByIdretryWithdraw.get().getFee());


    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
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

    //setRetryFee

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame3 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame3, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "retryFee()";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input5, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return6.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(setRetryFee));
    logger.info("retryFee:" + retryFee1);
    //bonus
    byte[] input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    //retry deposit trx, <setRetryFee
    Account accountMainBeforeRetry = PublicMethed
        .queryAccount(testAddress001, blockingStubFull);
    long accountMainBeforeRetryBalance = accountMainBeforeRetry.getBalance();
    logger.info("accountMainBeforeRetryBalance:" + accountMainBeforeRetryBalance);
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee - 1,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);
    fee1 = infoByIdretryDeposit.get().getFee();

    // TRC721`s owner in mainChain should be Depositor
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore1, bonusBefore);
    Account accountMainAfterRetry = PublicMethed
        .queryAccount(testAddress001, blockingStubFull);
    long accountMainAfterRetryBalance = accountMainAfterRetry.getBalance();
    logger.info("accountMainAfterRetryBalance:" + accountMainAfterRetryBalance);
    Assert.assertEquals(accountMainBeforeRetryBalance - fee1, accountMainAfterRetryBalance);

    //retry deposit trx, =setRetryFee
    Account accountMainBeforeRetry1 = PublicMethed
        .queryAccount(testAddress001, blockingStubFull);
    long accountMainBeforeRetryBalance1 = accountMainBeforeRetry1.getBalance();
    logger.info("accountMainBeforeRetryBalance1:" + accountMainBeforeRetryBalance1);
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() != 1);
    fee1 = infoByIdretryDeposit.get().getFee();

    // TRC721`s owner in mainChain should be Depositor
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Assert.assertEquals(bonusBefore1 + setRetryFee, bonusBefore2);
    Account accountMainAfterRetry1 = PublicMethed
        .queryAccount(testAddress001, blockingStubFull);
    long accountMainAfterRetryBalance1 = accountMainAfterRetry1.getBalance();
    logger.info("accountMainAfterRetryBalance1:" + accountMainAfterRetryBalance1);
    Assert.assertEquals(accountMainBeforeRetryBalance1 - fee1 - setRetryFee,
        accountMainAfterRetryBalance1);

    //retry deposit trx, >=setRetryFee
    Account accountMainBeforeRetry2 = PublicMethed
        .queryAccount(testAddress001, blockingStubFull);
    long accountMainBeforeRetryBalance2 = accountMainBeforeRetry2.getBalance();
    logger.info("accountMainBeforeRetryBalance2:" + accountMainBeforeRetryBalance2);
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee + 1,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() != 1);
    fee1 = infoByIdretryDeposit.get().getFee();

    // TRC721`s owner in mainChain should be Depositor
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore3);
    Assert.assertEquals(bonusBefore2 + setRetryFee, bonusBefore3);
    Account accountMainAfterRetry2 = PublicMethed
        .queryAccount(testAddress001, blockingStubFull);
    long accountMainAfterRetryBalance2 = accountMainAfterRetry2.getBalance();
    logger.info("accountMainAfterRetryBalance2:" + accountMainAfterRetryBalance2);
    Assert.assertEquals(accountMainBeforeRetryBalance2 - fee1 - setRetryFee,
        accountMainAfterRetryBalance2);

  }


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc721 with nonce exception ")
  public void test2RetryTrc721002() {

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame3 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame3, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);

    //other account Retry Deposit
    Account accountMainBeforeRetry = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountMainBeforeRetryBalance = accountMainBeforeRetry.getBalance();
    logger.info("accountMainBeforeRetryBalance:" + accountMainBeforeRetryBalance);
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] depositAddress2 = ecKey2.getAddress();
    String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, 2000000000L, foundationAddress001, foundationKey001,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    String retryDepositTxid1 = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce, maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid:" + retryDepositTxid1);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);
    long infoByIdretryDepositFee = infoByIdretryDeposit.get().getFee();
    logger.info("infoByIdretryDepositFee:" + infoByIdretryDepositFee);
    Account accountMainAfterRetry = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountMainAfterRetryBalance = accountMainAfterRetry.getBalance();
    logger.info("accountMainAfterRetryBalance:" + accountMainAfterRetryBalance);
    Assert.assertEquals(accountMainBeforeRetryBalance,
        accountMainAfterRetryBalance);

    //other account Retry Mapping

    String retryMapTxid1 = PublicMethed.retryMapping(mainGateWayAddress,
        nonceMap, maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    logger.info("retryMapTxid1:" + retryMapTxid1);
    Optional<TransactionInfo> infoByIdretryMapTxid1 = PublicMethed
        .getTransactionInfoById(retryMapTxid1, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid1.get().getResultValue() == 0);

    //other account Retry Withdraw
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1300000000;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoByIdWithdrawDeposit = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdWithdrawDeposit.get().getResultValue() == 0);

    //Deposit noce value is 0
    String smallNonce = "0000000000000000000000000000000000000000000000000000000000000000";
    logger.info("smallNonce:" + smallNonce);
    String retryDepositTxid3 = PublicMethed.retryDeposit(mainGateWayAddress,
        smallNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid3:" + retryDepositTxid3);
    Optional<TransactionInfo> infoByIdretryDepositTxid3 = PublicMethed
        .getTransactionInfoById(retryDepositTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid3.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid3.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid3.get().getResMessage().toStringUtf8());

    //Retrymapping  noce value is 0
    String retryMapTxid3 = PublicMethed.retryMapping(mainGateWayAddress,
        smallNonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMapTxid3:" + retryMapTxid3);
    Optional<TransactionInfo> infoByIdretryMapTxid3 = PublicMethed
        .getTransactionInfoById(retryMapTxid3, blockingStubFull);
    Assert.assertEquals(1, infoByIdretryMapTxid3.get().getResultValue());
    Assert.assertEquals(FAILED, infoByIdretryMapTxid3.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid3.get().getResMessage().toStringUtf8());

    //Withdraw noce value is 0
    String retryWithdrawTxid3 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        smallNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid3:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid3 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid3, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid3.get().getResultValue() == 0);

    //Deposit noce value is 1*10**20+1*10**6（nonexistent）
    String maxNonce = "0000000000000000000000000000000000000000000000056bc75e2d636b8d80";
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid4 = PublicMethed.retryDeposit(mainGateWayAddress,
        maxNonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid4:" + retryDepositTxid4);
    Optional<TransactionInfo> infoByIdretryDepositTxid4 = PublicMethed
        .getTransactionInfoById(retryDepositTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid4.get().getResMessage().toStringUtf8());

    //Retrymapping  noce value is 1*10**20+1*10**6（nonexistent）
    String retryMapTxid4 = PublicMethed.retryMapping(mainGateWayAddress,
        maxNonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMapTxid1:" + retryMapTxid4);
    Optional<TransactionInfo> infoByIdretryMapTxid4 = PublicMethed
        .getTransactionInfoById(retryMapTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryMapTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid4.get().getResMessage().toStringUtf8());

    //Withdraw noce value is 1*10**20+1*10**6（nonexistent）
    String retryWithdrawTxid4 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        maxNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryDepositTxid2:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid4 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid4, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid4.get().getResMessage().toStringUtf8());

    //Deposit noce value is 1*10**20
    String initialNonce = "0000000000000000000000000000000000000000000000056bc75e2d63100000";
    logger.info("initialNonce:" + initialNonce);
    String retryDepositTxid5 = PublicMethed.retryDeposit(mainGateWayAddress,
        initialNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid5:" + retryDepositTxid5);
    Optional<TransactionInfo> infoByIdretryDepositTxid5 = PublicMethed
        .getTransactionInfoById(retryDepositTxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid5.get().getResultValue() == 0);

    //Retrymapping  noce value is 1*10**20
    String retryMapTxid5 = PublicMethed.retryMapping(mainGateWayAddress,
        initialNonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMapTxid5:" + retryMapTxid5);
    Optional<TransactionInfo> infoByIdretryMapTxid5 = PublicMethed
        .getTransactionInfoById(retryMapTxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid5.get().getResultValue() == 0);

    //Withdraw noce value is 1*10**20
    String retryWithdrawTxid5 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        initialNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid5:" + retryWithdrawTxid5);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid5 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid5, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid5.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid5.get().getResMessage().toStringUtf8());

    //Deposit noce value is -1
    String minusNonce = PublicMethed.numToHex64(-1L);
    logger.info("minusNonce:" + minusNonce);
    String retryDepositTxid6 = PublicMethed.retryDeposit(mainGateWayAddress,
        minusNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid4:" + retryDepositTxid6);
    Optional<TransactionInfo> infoByIdretryDepositTxid6 = PublicMethed
        .getTransactionInfoById(retryDepositTxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid6.get().getResMessage().toStringUtf8());

    //Retrymapping  noce value is is-1
    String retryMapTxid6 = PublicMethed.retryMapping(mainGateWayAddress,
        minusNonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMapTxid1:" + retryMapTxid6);
    Optional<TransactionInfo> infoByIdretryMapTxid6 = PublicMethed
        .getTransactionInfoById(retryMapTxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryMapTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid6.get().getResMessage().toStringUtf8());

    //Withdraw noce value is -1
    String retryWithdrawTxid6 = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        minusNonce, maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid6:" + retryWithdrawTxid6);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid6 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid6, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid6.get().getResMessage().toStringUtf8());
  }


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc721 with mainOralce value is 0 ")
  public void test3RetryTrc721003() {
    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame3 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame3, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);

    // TRC721`s owner in mainChain should be depositer
    String arg = "1001";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    String ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());

    String addressFinal = Base58
        .encode58Check(ByteArray.fromHexString("41" + ContractRestule.substring(24)));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);
    Assert.assertTrue(PublicMethed.freezeBalanceGetEnergy(testOracleAddress, 10000000,
        0, 0, testOracle, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account oracleMainBeforeSend = PublicMethed.queryAccount(testOracleAddress, blockingStubFull);
    long oracleMainBeforeSendBalance = oracleMainBeforeSend.getBalance();

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, oracleMainBeforeSendBalance, testOracleAddress, testOracle,
            blockingStubFull));
    AccountResourceMessage oracleMainBeforeDeposit = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingStubFull);
    long oracleMainBeforeDepositEnergyLimit = oracleMainBeforeDeposit.getEnergyLimit();
    long oracleMainBeforeDepositEnergyUsage = oracleMainBeforeDeposit.getEnergyUsed();
    long oracleMainBeforeDepositNetUsed = oracleMainBeforeDeposit.getNetUsed();
    long oracleMainBeforeDepositNetLimit = oracleMainBeforeDeposit.getNetLimit();
    Assert.assertEquals(oracleMainBeforeDepositEnergyLimit, 0);
    Assert.assertEquals(oracleMainBeforeDepositEnergyUsage, 0);
    Assert.assertTrue(oracleMainBeforeDepositNetUsed < oracleMainBeforeDepositNetLimit);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    // deposit TRC721 to sideChain
    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc721Contract), mainGateWayAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertNotNull(deposittrx);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    // TRC721`s owner in sideChain should be Depositor
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    String tmpAddress = ByteArray.toHexString(infoById2.get().getContractResult(0).toByteArray());
    addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress.substring(24)));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertNotNull(ownerTrx);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    // TRC721`s owner in mainChain should be mainGateway
    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return3.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    // withdraw TRC721
    String methodStr = "withdrawal(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0, input, maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    nonceWithdraw = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());

    // TRC721`s owner in mainChain should be mainGateway
    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return4.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    // TRC721`s owner in sideChain should be REVERT
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
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

    Assert.assertTrue(PublicMethed
        .sendcoin(testOracleAddress, oracleMainBeforeSendBalance - 200000, depositAddress2,
            testKeyFordeposit2,
            blockingStubFull));
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, testAddress001, testKey001, blockingSideStubFull);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);

    // TRC721`s owner in mainChain should be depositer
    TransactionExtention return5 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return5.getConstantResult(0).toByteArray());

    String addressFinal4 = Base58
        .encode58Check(ByteArray.fromHexString("41" + ContractRestule.substring(24)));
    logger.info("address_final: " + addressFinal4);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal4);

  }


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trc721 with sideOralce value is 0 ")
  public void test4RetryTrc721004() {
    Assert.assertTrue(PublicMethed.freezeBalanceSideChainGetEnergy(testOracleAddress, 100000000,
        3, 0, testOracle, chainIdAddressKey, blockingSideStubFull));

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Account oracleSideBeforeSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    long oracleSideBeforeSendBalance = oracleSideBeforeSend.getBalance();

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeSendBalance, testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleMainBeforeDeposit = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingSideStubFull);
    long oracleMainBeforeDepositEnergyLimit = oracleMainBeforeDeposit.getEnergyLimit();
    long oracleMainBeforeDepositEnergyUsage = oracleMainBeforeDeposit.getEnergyUsed();
    long oracleMainBeforeDepositNetUsed = oracleMainBeforeDeposit.getNetUsed();
    long oracleMainBeforeDepositNetLimit = oracleMainBeforeDeposit.getNetLimit();
    Assert.assertEquals(oracleMainBeforeDepositEnergyLimit, 0);
    Assert.assertEquals(oracleMainBeforeDepositEnergyUsage, 0);
    Assert.assertTrue(oracleMainBeforeDepositNetUsed < oracleMainBeforeDepositNetLimit);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    // deposit TRC721 to sideChain
    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc721Contract), mainGateWayAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertNotNull(deposittrx);
    logger.info("infoById : " + infoById);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());
    nonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonce:" + nonce);

    // check Deposit Msg when deposit failed
    String[] Msg = {
        WalletClient.encode58Check(testAddress001), "0",
        "3", WalletClient.encode58Check(trc721Contract), "0", "0", "1001"
    };
    Assert.assertTrue(PublicMethed.checkDepositMsg(nonce, mainGateWayAddress, testAddress001,
        testKey001, blockingStubFull, Msg));

    // TRC721`s owner in mainChain should be mainGateway
    String arg = "1001";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    String ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    String tmpAddress = ContractRestule.substring(24);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    // TRC721`s owner in sideChain should be REVERT
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
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

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));
    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    //setRetryFee

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 2;
    parame3 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame3, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStr2 = "retryFee()";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input5, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long retryFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return6.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee1, Long.valueOf(setRetryFee));
    logger.info("retryFee:" + retryFee1);
    //bonus
    byte[] input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

    //retry deposit trx <setRetryFee
    String retryDepositTxid = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce, maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 1);

    //retry deposit trx >setRetryFee
    retryDepositTxid = PublicMethed.retryDepositForRetryFee(mainGateWayAddress,
        nonce, setRetryFee + 1,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // TRC721`s owner in sideChain should be Depositor
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    tmpAddress = ByteArray.toHexString(infoById2.get().getContractResult(0).toByteArray());
    addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress.substring(24)));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());
    Assert.assertNotNull(ownerTrx);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal);

    // TRC721`s owner in mainChain should be mainGateway
    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return3.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    //bonus
    input6 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0, input6,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + setRetryFee, bonusBefore1);

    Account oracleSideBeforeWithdrawSend = PublicMethed
        .queryAccount(testOracleAddress, blockingSideStubFull);
    oracleSideBeforeWithdrawSendBalance = oracleSideBeforeWithdrawSend.getBalance();
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(depositAddress2, oracleSideBeforeWithdrawSendBalance,
            testOracleAddress,
            testOracle, chainIdAddressKey,
            blockingSideStubFull));
    AccountResourceMessage oracleSideBeforeWithdraw = PublicMethed
        .getAccountResource(testOracleAddress,
            blockingSideStubFull);
    long oracleSideBeforeWithdrawnEnergyLimit = oracleSideBeforeWithdraw.getEnergyLimit();
    long oracleSideBeforeWithdrawEnergyUsage = oracleSideBeforeWithdraw.getEnergyUsed();
    long oracleSideBeforeWithdrawNetUsed = oracleSideBeforeWithdraw.getNetUsed();
    long oracleSideBeforeWithdrawNetLimit = oracleSideBeforeWithdraw.getNetLimit();
    Assert.assertEquals(oracleSideBeforeWithdrawnEnergyLimit, 0);
    Assert.assertEquals(oracleSideBeforeWithdrawEnergyUsage, 0);
    Assert.assertTrue(oracleSideBeforeWithdrawNetUsed < oracleSideBeforeWithdrawNetLimit);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    // withdraw TRC721
    String methodStr = "withdrawal(uint256)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "1001", false));
    String withdrawTxid1 = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0, input, maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    nonceWithdraw = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("nonceWithdraw:" + nonceWithdraw);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());

    // check Withdraw Msg when withdraw failed
    String withdrawNonce = ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray());
    String[] MsgWithdraw = {
        WalletClient.encode58Check(testAddress001),
        WalletClient.encode58Check(trc721Contract), "0", "1001", "3", "0"
    };
    Assert
        .assertTrue(PublicMethed.checkWithdrawMsg(withdrawNonce, sideGatewayAddress, testAddress001,
            testKey001, blockingSideStubFull, MsgWithdraw));

    // TRC721`s owner in mainChain should be mainGateway
    TransactionExtention return4 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return4.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString("41" + tmpAddress));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    // TRC721`s owner in sideChain should be REVERT
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, chainIdAddressKey, 0l, input1,
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

    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeWithdrawSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull));

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 2;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    String txid = PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    String methodStrSide2 = "retryFee()";
    byte[] inputSide2 = Hex.decode(AbiUtil.parseMethod(methodStrSide2, "", false));

    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress), 0l, inputSide2, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    Long retryFee3 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertEquals(retryFee3, Long.valueOf(parameSide1));
    logger.info("retryFee3:" + retryFee3);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey,
            blockingSideStubFull);

    long bonusSideAfter = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideBefore:" + bonusSideAfter);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //<setRetryFeeSide
    String retryWithdrawTxid = PublicMethed.retryWithdraw(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, maxFeeLimit, testAddress001, testKey001, blockingSideStubFull);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 1);

    //>setRetryFeeSide
    retryWithdrawTxid = PublicMethed.retryWithdrawForRetryFee(chainIdAddress, sideGatewayAddress,
        nonceWithdraw, setRetryFeeSide,
        maxFeeLimit, testAddress001, testKey001, blockingSideStubFull);

    try {
      Thread.sleep(60000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);

    // TRC721`s owner in mainChain should be depositer
    TransactionExtention return5 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return5.getConstantResult(0).toByteArray());

    String addressFinal4 = Base58
        .encode58Check(ByteArray.fromHexString("41" + ContractRestule.substring(24)));
    logger.info("address_final: " + addressFinal4);
    Assert.assertEquals(Base58.encode58Check(testAddress001), addressFinal4);

    input3 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(sideGatewayAddress),
            0, input3,
            maxFeeLimit, 0, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey,
            blockingSideStubFull);

    long bonusSideAfter1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusSideAfter1:" + bonusSideAfter1);
    Assert.assertEquals(bonusSideAfter + setRetryFeeSide, bonusSideAfter1);

  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    Account depositAddress2MainAccount = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull);
    long depositAddress2MainBalance = depositAddress2MainAccount.getBalance();
    logger.info("depositAddress2MainBalance:" + depositAddress2MainBalance);
    if (depositAddress2MainBalance > 2000000) {
      Assert.assertTrue(PublicMethed
          .sendcoin(testOracleAddress, depositAddress2MainBalance - 1000000, depositAddress2,
              testKeyFordeposit2,
              blockingStubFull));
    }
    Account depositAddress2SideAccount = PublicMethed
        .queryAccount(depositAddress2, blockingSideStubFull);
    long depositAddress2SideBalance = depositAddress2SideAccount.getBalance();
    logger.info("depositAddress2SideBalance:" + depositAddress2SideBalance);
    if (depositAddress2SideBalance > 2000000) {
      Assert.assertTrue(PublicMethed
          .sendcoinForSidechain(testOracleAddress, depositAddress2SideBalance - 1000000,
              depositAddress2,
              testKeyFordeposit2, chainIdAddressKey, blockingSideStubFull));
    }

    methodStr1 = "setRetryFee(uint256)";
    long setRetryFee = 0;
    parame3 = String.valueOf(setRetryFee);

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame3, false));

    PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input3,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    methodStrSide = "setRetryFee(uint256)";
    long setRetryFeeSide = 0;
    parameSide1 = String.valueOf(setRetryFeeSide);

    byte[] inputSide = Hex.decode(AbiUtil.parseMethod(methodStrSide, parameSide1, false));

    PublicMethed
        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, inputSide, 1000000000,
            0l, "0", gateWaySideOwnerAddress, gateWatOwnerSideAddressKey, blockingSideStubFull);
    PublicMethed
        .sendcoinForSidechain(testOracleAddress, oracleSideBeforeWithdrawSendBalance - 200000,
            depositAddress2,
            testKeyFordeposit2, chainIdAddressKey,
            blockingSideStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
