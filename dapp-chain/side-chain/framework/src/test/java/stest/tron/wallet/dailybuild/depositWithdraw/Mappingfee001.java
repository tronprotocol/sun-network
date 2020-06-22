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
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class Mappingfee001 {

  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String mainGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final byte[] mainGateWayOwnerAddress = PublicMethed.getFinalAddress(mainGateWayOwner);
  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
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
//    PublicMethed.printAddress(testKeyFordeposit);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "0", false));
    PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input1,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
  }

  /**
   * constructor.
   */


  @Test(enabled = true, description = "Deposit Trc20")
  public void mappingfeetrc20001() {

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
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);
    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "100", false));
    String txid2 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input10,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    logger.info("txid2:" + txid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());

    String methodStr2 = "mappingFee()";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return6 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input5, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    Long mapFee1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return6.getConstantResult(0).toByteArray())));
    Assert.assertEquals(mapFee1, Long.valueOf("100"));
    logger.info("mapFee1:" + mapFee1);

    //bonus
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input4,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    long bonusBefore = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore:" + bonusBefore);

//fee<mappingfee
    String mapTxid1 = PublicMethed
        .mappingTrc20fee(mainChainAddressKey, deployTxid, 50, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(mapTxid1, blockingStubFull);
    Assert.assertEquals("FAILED", infoById3.get().getResult().name());
    Assert.assertEquals(1, infoById3.get().getResultValue());

    //fee>mappingfee
    Account accountMainBeforeMap1 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeMapBalance1 = accountMainBeforeMap1.getBalance();
    logger.info("accountSideBeforeMapBalance1:" + accountMainBeforeMapBalance1);
    String mapTxid2 = PublicMethed
        .mappingTrc20fee(mainChainAddressKey, deployTxid, mapFee1 + 100, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(mapTxid2, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById4.get().getResult().name());
    Assert.assertEquals(0, infoById4.get().getResultValue());
    long mappingTrc20fee = infoById4.get().getFee();

    Account accountMainBeforeMap2 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeMapBalance2 = accountMainBeforeMap2.getBalance();
    logger.info("accountMainBeforeMapBalance2:" + accountMainBeforeMapBalance2);

    Assert.assertEquals(accountMainBeforeMapBalance1 - mappingTrc20fee - mapFee1,
        accountMainBeforeMapBalance2);
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
    Assert.assertNotEquals(addressFinal, "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");
    SmartContract contract = PublicMethed.getContract(sideContractAddress, blockingSideStubFull);
    Assert.assertEquals(WalletClient.encode58Check(contract.getOriginAddress().toByteArray()),
        WalletClient.encode58Check(depositAddress));

    //bonus
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input4,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore + 100, bonusBefore1);

    //fee=0

    String mapTxid = PublicMethed
        .mappingTrc20(mainChainAddressKey, deployTxid, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    Assert.assertEquals("FAILED", infoById1.get().getResult().name());
    Assert.assertEquals(1, infoById1.get().getResultValue());

    byte[] input15 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "0", false));
    PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input15,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

  }

  @Test(enabled = true, description = "Deposit Trc721")
  public void mappingfeetrc721002() {

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

    String parame1 = "\"" + Base58.encode58Check(depositAddress) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc20Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
            depositAddress, testKeyFordeposit, blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
    Assert.assertNotNull(mintTxid);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "100", false));
    String txid2 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input10,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    logger.info("txid2:" + txid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById2.get().getResult().name());
    Assert.assertEquals(0, infoById2.get().getResultValue());

    //fee<mappingfee
    //bonus
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input4,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);

    String mapTxid1 = PublicMethed
        .mappingTrc721fee(mainChainAddressKey, deployTxid, 50, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(mapTxid1, blockingStubFull);
    Assert.assertEquals("FAILED", infoById3.get().getResult().name());
    Assert.assertEquals(1, infoById3.get().getResultValue());

    //fee>mappingfee
    Account accountBeforeMap1 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeMapBalance1 = accountBeforeMap1.getBalance();

    String mapTxid2 = PublicMethed
        .mappingTrc721fee(mainChainAddressKey, deployTxid, 200, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(mapTxid2, blockingStubFull);
    Assert.assertNotNull(mapTxid2);
    Assert.assertEquals("SUCESS", infoById4.get().getResult().name());
    Assert.assertEquals(0, infoById4.get().getResultValue());

    long fee1 = infoById4.get().getFee();
    Account accountBeforeMap2 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeMapBalance2 = accountBeforeMap2.getBalance();
    Assert.assertEquals(accountBeforeMapBalance1 - fee1 - 100, accountBeforeMapBalance2);

    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input4,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Assert.assertEquals(bonusBefore1 + 100, bonusBefore2);

    String parame2 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
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

    Assert.assertNotEquals(addressFinal, "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");
    SmartContract contract = PublicMethed.getContract(sideContractAddress, blockingSideStubFull);
    Assert.assertEquals(WalletClient.encode58Check(contract.getOriginAddress().toByteArray()),
        WalletClient.encode58Check(depositAddress));

    byte[] input19 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "0", false));
    String txid9 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input19,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById9 = PublicMethed
        .getTransactionInfoById(txid9, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById9.get().getResult().name());
    Assert.assertEquals(0, infoById9.get().getResultValue());
  }

  @Test(enabled = true, description = "Deposit Trc721")
  public void mappingfeetrc20003() {

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

    String parame1 = "\"" + Base58.encode58Check(depositAddress) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc20Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
            depositAddress, testKeyFordeposit, blockingStubFull);
    infoById = PublicMethed.getTransactionInfoById(mintTxid, blockingStubFull);
    Assert.assertNotNull(mintTxid);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals("SUCESS", infoById.get().getResult().name());

    byte[] input15 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "100", false));
    String txid20 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input15,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("txid2:" + txid20);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById20 = PublicMethed
        .getTransactionInfoById(txid20, blockingStubFull);
    Assert.assertEquals("FAILED", infoById20.get().getResult().name());
    Assert.assertEquals(1, infoById20.get().getResultValue());

    byte[] input16 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "100", false));
    String txid16 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input16,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    logger.info("txid2:" + txid20);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById16 = PublicMethed
        .getTransactionInfoById(txid16, blockingStubFull);
    Assert.assertEquals("FAILED", infoById20.get().getResult().name());
    Assert.assertEquals(1, infoById16.get().getResultValue());

    byte[] input20 = Hex.decode(AbiUtil.parseMethod(
        "setMappingFee(uint256)", "1000000001", false));
    String txid10 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input20,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById10 = PublicMethed
        .getTransactionInfoById(txid10, blockingStubFull);
    Assert.assertEquals("FAILED", infoById10.get().getResult().name());
    Assert.assertEquals(1, infoById10.get().getResultValue());
    String msg = Hex.toHexString(infoById10.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135, 172)));
    Assert.assertEquals("\u0002less than 1000 TRX", msg);

    byte[] input21 = Hex.decode(AbiUtil.parseMethod(
        "setMappingFee(uint256)", "999999999", false));
    String txid11 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input21,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById11 = PublicMethed
        .getTransactionInfoById(txid11, blockingStubFull);
    Assert.assertNotNull(txid11);
    Assert.assertEquals("SUCESS", infoById11.get().getResult().name());
    Assert.assertEquals(0, infoById11.get().getResultValue());

    byte[] input19 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "200", false));
    String txid9 = PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input19,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById9 = PublicMethed
        .getTransactionInfoById(txid9, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById9.get().getResult().name());
    Assert.assertEquals(0, infoById9.get().getResultValue());

    //fee>mappingfee
    byte[] input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    TransactionExtention response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input4,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Account accountBeforeMap = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeMapBalance = accountBeforeMap.getBalance();
    String mapTxid = PublicMethed
        .mappingTrc721fee(mainChainAddressKey, deployTxid, 300, 1000000000,
            depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(mapTxid, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById1.get().getResult().name());
    Assert.assertEquals(0, infoById1.get().getResultValue());
    long fee1 = infoById1.get().getFee();

    Account accountBeforeMap1 = PublicMethed
        .queryAccount(depositAddress, blockingStubFull);
    long accountBeforeMapBalance1 = accountBeforeMap1.getBalance();
    Assert.assertEquals(accountBeforeMapBalance - fee1 - 200, accountBeforeMapBalance1);
    input4 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input4,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    long bonusBefore3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore3);
    Assert.assertEquals(bonusBefore2 + 200, bonusBefore3);

    String parame2 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame2, false));
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

    Assert.assertNotEquals(addressFinal, "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");
    SmartContract contract = PublicMethed.getContract(sideContractAddress, blockingSideStubFull);
    Assert.assertEquals(WalletClient.encode58Check(contract.getOriginAddress().toByteArray()),
        WalletClient.encode58Check(depositAddress));
  }

  @Test(enabled = true, description = "Mapping with triggerAccount exception and "
      + "minTrx Value range")
  public void mappingfeetrc20004() {
    String methodStr1 = "setMappingFee(uint256)";
    long setDepositFee = 2;
    String parame1 = String.valueOf(setDepositFee);
    //not gateWay owner trigger setDepositMinTrx method

    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input1,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() != 0);
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById1.get().getResMessage().toByteArray()));

    //setDepositMinTrx is -1
    parame1 = "-1";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input2,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    logger.info("param is -1 --txid:" + txid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 1);
    Assert.assertEquals("FAILED", infoById1.get().getResult().name());
    Assert.assertEquals(1, infoById1.get().getResultValue());
    String msg = Hex.toHexString(infoById1.get().getContractResult(0).toByteArray());
    msg = ByteArray.toStr(ByteArray.fromHexString(msg.substring(135, 170)));
    Assert.assertEquals("\u0002less than 1000 TR", msg);

    String methodStr2 = "depositFee()";
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

//Long.min
    setDepositFee = Long.MIN_VALUE;
    logger.info("setDepositFee:" + setDepositFee);
    parame1 = String.valueOf(setDepositFee);
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 1);
    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input4, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    long MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("MinTrx:" + Long.valueOf(MinTrx));
//Long.max

//    parame1 = "9223372036854775807";
    setDepositFee = Long.MAX_VALUE;
    logger.info("setDepositFee:" + setDepositFee);

    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 1);
    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input4, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("MinTrx:" + Long.valueOf(MinTrx));

//Long.max+1

    setDepositFee = Long.MAX_VALUE + 1;
    logger.info("setDepositFee:" + setDepositFee);

    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 1);
    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input4, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("MinTrx:" + Long.valueOf(MinTrx));

    //Long.min-1

    setDepositFee = Long.MIN_VALUE - 1;
    logger.info("setDepositFee:" + setDepositFee);

    parame1 = String.valueOf(setDepositFee);
    input3 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    txid3 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainChainAddress),
            0,
            input3,
            maxFeeLimit, 0, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById1 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 1);
    return3 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress), 0l, input4, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    logger.info("MinTrx:" + Long.valueOf(MinTrx));


  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setMappingFee(uint256)", "0", false));
    PublicMethed
        .triggerContract(mainChainAddressKey, 0l, input1,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }


}
