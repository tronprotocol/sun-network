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
public class DepositFeeTrc721001 {


  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final String foundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress001 = PublicMethed.getFinalAddress(foundationKey001);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] testAddress001 = ecKey1.getAddress();
  String testKey001 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  String methodStr1 = null;
  String parame2 = null;
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
  public void test1DepositTrc721001() {

    PublicMethed.printAddress(testKey001);

    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress001, 11000_000_000L, foundationAddress001, foundationKey001,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    Account accountBefore = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    logger.info("accountBeforeBalance:" + accountBeforeBalance);
    Account accountSideBefore = PublicMethed.queryAccount(testAddress001, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    long callValue = 1000_000_000L;
    String txid = PublicMethed.triggerContract(mainChainAddressKey, callValue, input,
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

    Account accountAfter = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance = accountAfter.getBalance();
    Account accountSideAfter = PublicMethed.queryAccount(testAddress001, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();

    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertEquals(accountBeforeBalance - callValue - fee, accountAfterBalance);
    Assert.assertEquals(callValue, accountSideAfterBalance);

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
    byte[] trc721Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc721Contract);

    //mint 721
    String parame1 = "\"" + Base58.encode58Check(testAddress001) + "\"," + 1001;
    String mintTxid = PublicMethed
        .triggerContract(trc721Contract, "mint(address,uint256)", parame1, false, 0, maxFeeLimit,
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
    String parame2 = "\"" + Base58.encode58Check(trc721Contract) + "\"";
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
        .depositTrc721(WalletClient.encode58Check(trc721Contract), mainChainAddress, 1001,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    logger.info(deposittrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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
    PublicMethed.waitProduceNextBlock(blockingStubFull);
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
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
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
    logger.info("withdrawTxid1: " + withdrawTxid1);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById = PublicMethed.getTransactionInfoById(withdrawTxid1, blockingSideStubFull);
    logger.info("fee:" + infoById.get().getFee());
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
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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

    Account accountAfter1 = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance1 = accountAfter1.getBalance();
    logger.info("accountAfterBalance1:"+accountAfterBalance1);

    // =0
    Map<String, String> deposit721 = PublicMethed
        .depositTrc721ForDepositFee(WalletClient.encode58Check(trc721Contract), mainChainAddress,
            1001, 0,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId = deposit721.get("depositId");
    String approveId = deposit721.get("approveId");
    logger.info("depositId:"+depositId);
    logger.info("approveId:"+approveId);
    Optional<TransactionInfo> depositInfo = PublicMethed
        .getTransactionInfoById(depositId, blockingStubFull);
    Assert.assertEquals(1, depositInfo.get().getResultValue());
    String data = ByteArray
        .toHexString(depositInfo.get().getContractResult(0).substring(67,97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));

    input5 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore1 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore1:" + bonusBefore1);
    Assert.assertEquals(bonusBefore, bonusBefore1);

    long depositFee = depositInfo.get().getFee();
    Optional<TransactionInfo> approveInfo = PublicMethed
        .getTransactionInfoById(approveId, blockingStubFull);
    long approveFee = approveInfo.get().getFee();
    Account accountAfter2 = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance2 = accountAfter2.getBalance();
    logger.info("accountAfterBalance2:"+accountAfterBalance2);
    Assert.assertEquals(accountAfterBalance1 - depositFee - approveFee, accountAfterBalance2);

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

    //<setDepositFee
    deposit721 = PublicMethed
        .depositTrc721ForDepositFee(WalletClient.encode58Check(trc721Contract), mainChainAddress,
            1001, setDepositFee - 1,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId1 = deposit721.get("depositId");
    String approveId1 = deposit721.get("approveId");
    logger.info("depositId1:"+depositId1);
    logger.info("approveId1:"+approveId1);
    Optional<TransactionInfo> depositInfo1 = PublicMethed
        .getTransactionInfoById(depositId1, blockingStubFull);
    Assert.assertNotNull(depositInfo1);
    Assert.assertEquals(1, depositInfo1.get().getResultValue());
    data = ByteArray
        .toHexString(depositInfo1.get().getContractResult(0).substring(67,97).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u001Dmsg.value need  >= depositFee", PublicMethed.hexStringToString(data));

    long depositFee1 = depositInfo1.get().getFee();
    Optional<TransactionInfo> approveInfo1 = PublicMethed
        .getTransactionInfoById(approveId1, blockingStubFull);
    long approveFee1 = approveInfo1.get().getFee();
    Account accountAfter3 = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance3 = accountAfter3.getBalance();
    logger.info("accountAfterBalance3:"+accountAfterBalance3);
    Assert.assertEquals(accountAfterBalance2 - depositFee1 - approveFee1, accountAfterBalance3);

    input5 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore2 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore2:" + bonusBefore2);
    Assert.assertEquals(bonusBefore1, bonusBefore2);

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

    // =setDepositFee
    Account accountMainBefore = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();

    deposit721 = PublicMethed
        .depositTrc721ForDepositFee(WalletClient.encode58Check(trc721Contract), mainChainAddress,
            1001, setDepositFee,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId2 = deposit721.get("depositId");
    String approveId2 = deposit721.get("approveId");
    logger.info("depositId2:"+depositId2);
    logger.info("approveId2:"+approveId2);
    Optional<TransactionInfo> depositInfo2 = PublicMethed
        .getTransactionInfoById(depositId2, blockingStubFull);
    Assert.assertNotNull(depositInfo2);
    Assert.assertEquals(0, depositInfo2.get().getResultValue());

    input5 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore3 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore3:" + bonusBefore3);
    Assert.assertEquals(bonusBefore2 + setDepositFee, bonusBefore3);

    long depositFee2 = depositInfo2.get().getFee();
    logger.info("depositFee2:"+depositFee2);
    Optional<TransactionInfo> approveInfo2 = PublicMethed
        .getTransactionInfoById(approveId2, blockingStubFull);
    long approveFee2 = approveInfo2.get().getFee();
    logger.info("approveFee2:"+approveFee2);
    Account accountAfter4 = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance4 = accountAfter4.getBalance();
    logger.info("accountAfterBalance4:"+accountAfterBalance4);
    Assert.assertEquals(accountMainBeforeBalance - depositFee2 - approveFee2 - setDepositFee, accountAfterBalance4);

    // TRC721`s owner in sideChain should be Depositor
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
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
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
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
    withdrawTxid1 = PublicMethed
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
    Assert.assertNotNull(withdrawTxid1);
    Assert.assertEquals(0, infoById.get().getResultValue());
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
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

    // >setDepositFee
    Account accountMainBefore1 = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountMainBeforeBalance1 = accountMainBefore1.getBalance();
    deposit721 = PublicMethed
        .depositTrc721ForDepositFee(WalletClient.encode58Check(trc721Contract), mainChainAddress,
            1001, setDepositFee + 1,
            1000000000,
            testAddress001, testKey001, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    String depositId3 = deposit721.get("depositId");
    String approveId3 = deposit721.get("approveId");
    logger.info("depositId3:"+depositId3);
    logger.info("approveId3:"+approveId3);
    Optional<TransactionInfo> depositInfo3 = PublicMethed
        .getTransactionInfoById(depositId3, blockingStubFull);
    Assert.assertNotNull(depositInfo3);
    Assert.assertEquals(0, depositInfo3.get().getResultValue());

    long depositFee3 = depositInfo3.get().getFee();
    Optional<TransactionInfo> approveInfo3 = PublicMethed
        .getTransactionInfoById(approveId3, blockingStubFull);
    long approveFee3 = approveInfo3.get().getFee();
    Account accountAfter5 = PublicMethed.queryAccount(testAddress001, blockingStubFull);
    long accountAfterBalance5 = accountAfter5.getBalance();
    logger.info("accountAfterBalance5:"+accountAfterBalance5);
    Assert.assertEquals(accountAfterBalance4 - depositFee3 - approveFee3 - setDepositFee, accountAfterBalance5);

    input5 = Hex.decode(AbiUtil.parseMethod("bonus()", "", false));
    response1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainChainAddress),
            0, input5,
            maxFeeLimit, 0, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

    long bonusBefore4 = ByteArray.toLong(response1.getConstantResult(0).toByteArray());
    logger.info("bonusBefore3:" + bonusBefore4);
    Assert.assertEquals(bonusBefore3 + setDepositFee, bonusBefore4);

    // TRC721`s owner in sideChain should be Depositor
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideContractAddress,
            WalletClient.decodeFromBase58Check(ChainIdAddress), 0l, input1,
            1000000000,
            0l, "0", testAddress001, testKey001, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    Assert.assertNotNull(ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());
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
    return2 = PublicMethed
        .triggerContractForTransactionExtention(trc721Contract, 0l, input1, 1000000000,
            0l, "0", testAddress001, testKey001, blockingStubFull);
    ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    tmpAddress = ContractRestule.substring(24);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    Assert.assertEquals(mainChainAddress, addressFinal);
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
