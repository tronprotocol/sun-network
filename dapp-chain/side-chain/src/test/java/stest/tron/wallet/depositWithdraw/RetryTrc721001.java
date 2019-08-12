package stest.tron.wallet.depositWithdraw;

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


  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
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

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] testAddress001 = ecKey1.getAddress();
  String testKey001 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);

  String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideGatewayAddressKey = WalletClient.decodeFromBase58Check(sideGatewayAddress);

  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.ChainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);

  String nonce = null;
  String nonceWithdraw = null;
  String nonceMap = null;

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
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
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
    Long nonceMapLong = ByteArray.toLong(ByteArray
        .fromHexString(
            ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceMapLong);
    nonceMap = Long.toString(nonceMapLong);

    //retry mapping trc10
    String retryMaptxid = PublicMethed.retryMapping(mainGateWayAddress,
        nonceMap,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryDepositTxid:" + retryMaptxid);
    Optional<TransactionInfo> infoByIdretryMaptxid = PublicMethed
        .getTransactionInfoById(retryMaptxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryMaptxid.get().getResultValue() == 0);

    // get 721Contract in sideChain
    String parame2 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
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

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertNotNull(sideContractAddress);

    // deposit TRC721 to sideChain
    String deposittrx = PublicMethed
        .depositTrc721(WalletClient.encode58Check(trc20Contract), mainGateWayAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(deposittrx, blockingStubFull);
    Assert.assertNotNull(deposittrx);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());
    Long nonceLong = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));
    logger.info("nonce:" + nonceLong);
    nonce = Long.toString(nonceLong);

    // TRC721`s owner in sideChain should be Depositor
    String arg = "1001";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("ownerOf(uint256)", arg, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, ChainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
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
    Assert.assertEquals(mainGateWayAddress, addressFinal);

    //retry deposit trx
    String retryDepositTxid = PublicMethed.retryDeposit(mainGateWayAddress,
        nonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoByIdretryDeposit = PublicMethed
        .getTransactionInfoById(retryDepositTxid, blockingStubFull);
    Assert.assertTrue(infoByIdretryDeposit.get().getResultValue() == 0);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(trc20Contract, 0l, input1, 1000000000,
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
        .triggerContractSideChain(sideContractAddress, ChainIdAddressKey, 0, input, maxFeeLimit,
            0, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("withdrawTxid: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    Long nonceWithdrawLong = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));
    logger.info("nonceWithdrawLong:" + nonceWithdrawLong);
    nonceWithdraw = Long.toString(nonceWithdrawLong);
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());

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
        .triggerContractSideChain(sideContractAddress, ChainIdAddressKey, 0l, input1,
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

    //retry  Withdraw 721

    String retryWithdrawTxid = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        nonceWithdraw,
        maxFeeLimit, testAddress001, testKey001, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdretryWithdraw = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdraw.get().getResultValue() == 0);

    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress, ChainIdAddressKey, 0l, input1,
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


  @Test(enabled = true, description = "Retry Deposit and Withdraw Trx with nonce exception ")
  public void test2RetryTrc721002() {

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
        nonce,
        maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
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
        nonceWithdraw,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);

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

    String retryWithdrawTxid = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        nonceWithdraw,
        maxFeeLimit, depositAddress2, testKeyFordeposit2, blockingSideStubFull);
    logger.info("retryWithdrawTxid:" + retryWithdrawTxid);
    Optional<TransactionInfo> infoByIdWithdrawDeposit = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid, blockingSideStubFull);
    Assert.assertTrue(infoByIdWithdrawDeposit.get().getResultValue() == 0);

    //Deposit noce value is
    String bigNonce = "100000";
    String retryDepositTxid2 = PublicMethed.retryDeposit(mainGateWayAddress,
        bigNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid2:" + retryDepositTxid2);
    Optional<TransactionInfo> infoByIdretryDepositTxid2 = PublicMethed
        .getTransactionInfoById(retryDepositTxid2, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid2.get().getResMessage().toStringUtf8());

    //Remapping noce value

    String retryMapTxid2 = PublicMethed.retryMapping(mainGateWayAddress,
        bigNonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMapTxid1:" + retryMapTxid2);
    Optional<TransactionInfo> infoByIdretryMapTxid2 = PublicMethed
        .getTransactionInfoById(retryMapTxid2, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMapTxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid2.get().getResMessage().toStringUtf8());

    //Withdraw noce value
    String retryWithdrawTxid2 = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        bigNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid2:" + retryWithdrawTxid2);
    Optional<TransactionInfo> infoByIdretryWithdrawTxid2 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid2, blockingSideStubFull);
    Assert.assertTrue(infoByIdretryWithdrawTxid2.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryWithdrawTxid2.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryWithdrawTxid2.get().getResMessage().toStringUtf8());

    //Deposit noce value is 0
    String smallNonce = Long.toString(0);
    logger.info("smallNonce:" + smallNonce);
    String retryDepositTxid3 = PublicMethed.retryDeposit(mainGateWayAddress,
        smallNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid3:" + retryDepositTxid3);
    Optional<TransactionInfo> infoByIdretryDepositTxid3 = PublicMethed
        .getTransactionInfoById(retryDepositTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid3.get().getResultValue() == 0);

    //Retrymapping  noce value is 0

    String retryMapTxid3 = PublicMethed.retryMapping(mainGateWayAddress,
        smallNonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    logger.info("retryMapTxid1:" + retryMapTxid3);
    Optional<TransactionInfo> infoByIdretryMapTxid3 = PublicMethed
        .getTransactionInfoById(retryMapTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid3.get().getResultValue() == 0);

    //Withdraw noce value is 0
    String retryWithdrawTxid3 = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        smallNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid2:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid3 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid3, blockingStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid3.get().getResultValue() == 0);

    //Deposit noce value is Long.max_value+1
    String maxNonce = Long.toString(Long.MAX_VALUE + 1);
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid4 = PublicMethed.retryDeposit(mainGateWayAddress,
        maxNonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid4:" + retryDepositTxid4);
    Optional<TransactionInfo> infoByIdretryDepositTxid4 = PublicMethed
        .getTransactionInfoById(retryDepositTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid4.get().getResMessage().toStringUtf8());

    //Retrymapping  noce value is is Long.max_value+1

    String retryMapTxid4 = PublicMethed.retryMapping(mainGateWayAddress,
        maxNonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryMapTxid1:" + retryMapTxid4);
    Optional<TransactionInfo> infoByIdretryMapTxid4 = PublicMethed
        .getTransactionInfoById(retryMapTxid4, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid4.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMapTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid4.get().getResMessage().toStringUtf8());

    //Withdraw noce value is Long.max_value+1
    String retryWithdrawTxid4 = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        maxNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid2:" + retryWithdrawTxid3);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid4 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid4, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid4.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid4.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid4.get().getResMessage().toStringUtf8());

    //Deposit noce value is Long.min_value-1
    String minNonce = Long.toString(Long.MIN_VALUE - 1);
    logger.info("maxNonce:" + maxNonce);
    String retryDepositTxid5 = PublicMethed.retryDeposit(mainGateWayAddress,
        minNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryDepositTxid4:" + retryDepositTxid5);
    Optional<TransactionInfo> infoByIdretryDepositTxid5 = PublicMethed
        .getTransactionInfoById(retryDepositTxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryDepositTxid5.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdretryDepositTxid5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryDepositTxid5.get().getResMessage().toStringUtf8());

    //Retrymapping  noce value is is Long.max_value+1

    String retryMapTxid5 = PublicMethed.retryMapping(mainGateWayAddress,
        minNonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    logger.info("retryMapTxid1:" + retryMapTxid5);
    Optional<TransactionInfo> infoByIdretryMapTxid5 = PublicMethed
        .getTransactionInfoById(retryMapTxid5, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid5.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMapTxid5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid5.get().getResMessage().toStringUtf8());

    //Withdraw noce value is Long.min_value-1
    String retryWithdrawTxid5 = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        minNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryDepositTxid2:" + retryWithdrawTxid5);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid5 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid5, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid5.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid5.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid5.get().getResMessage().toStringUtf8());

    //Deposit noce value is -1
    String minusNonce = Long.toString(-1);
    logger.info("minusNonce:" + minusNonce);
    String retryDepositTxid6 = PublicMethed.retryDeposit(mainGateWayAddress,
        minusNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingStubFull);
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
        minNonce,
        maxFeeLimit, testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("retryMapTxid1:" + retryMapTxid6);
    Optional<TransactionInfo> infoByIdretryMapTxid6 = PublicMethed
        .getTransactionInfoById(retryMapTxid6, blockingStubFull);
    Assert.assertTrue(infoByIdretryMapTxid6.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoByIdretryMapTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdretryMapTxid6.get().getResMessage().toStringUtf8());

    //Withdraw noce value is -1
    String retryWithdrawTxid6 = PublicMethed.retryWithdraw(ChainIdAddress, sideGatewayAddress,
        minusNonce,
        maxFeeLimit, foundationAddress001, foundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    logger.info("retryWithdrawTxid6:" + retryWithdrawTxid6);
    Optional<TransactionInfo> infoByIdrretryWithdrawTxid6 = PublicMethed
        .getTransactionInfoById(retryWithdrawTxid6, blockingSideStubFull);
    Assert.assertTrue(infoByIdrretryWithdrawTxid6.get().getResultValue() == 1);
    Assert.assertEquals(FAILED, infoByIdrretryWithdrawTxid6.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoByIdrretryWithdrawTxid6.get().getResMessage().toStringUtf8());
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
