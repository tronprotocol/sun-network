package stest.tron.wallet.dailybuild.depositWithdraw;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.List;
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
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;


@Slf4j
public class DelegateCallMain001 {


  final byte[] mainGateWayAddress = WalletClient.decodeFromBase58Check(
      Configuration.getByPath("testng.conf").getString("gateway_address.key1"));
  final byte[] sideGatewayAddress = WalletClient.decodeFromBase58Check(
      Configuration.getByPath("testng.conf").getString("gateway_address.key2"));
  final byte[] chainIdAddress = WalletClient.decodeFromBase58Check(
      Configuration.getByPath("testng.conf").getString("gateway_address.chainIdAddress"));
  private final String testDepositTrxKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrxKey);
  private final String testOracleKey = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracleKey);
  private final String mainGateWayOwnerKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final byte[] mainGateWayOwnerAddress = PublicMethed.getFinalAddress(mainGateWayOwnerKey);
  private final String sideGateWayOwnerKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwnerKey);
  ECKey testEcKey1 = new ECKey(Utils.getRandom());
  byte[] testAddress1 = testEcKey1.getAddress();
  String testKey1 = ByteArray.toHexString(testEcKey1.getPrivKeyBytes());
  ECKey testEcKey2 = new ECKey(Utils.getRandom());
  byte[] testAddress2 = testEcKey2.getAddress();
  String testKey2 = ByteArray.toHexString(testEcKey2.getPrivKeyBytes());
  ECKey testEcKey3 = new ECKey(Utils.getRandom());
  byte[] testAddress3 = testEcKey3.getAddress();
  String testKey3 = ByteArray.toHexString(testEcKey3.getPrivKeyBytes());
  String parame1 = null;
  String methodStr1 = null;
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
    PublicMethed.printAddress(testKey1);
    PublicMethed.printAddress(testKey2);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "DelegateCall for increase method and mapping data structure in mainChain")
  public void test1DelegateCallMain001() {
    methodStr1 = "setDepositMinTrx(uint256)";
    parame1 = "2";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(mainGateWayAddress,
            0,
            input1,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    logger.info("ownerTrx : " + txid1);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    String methodStr2 = "depositMinTrx()";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input2, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    Long MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(MinTrx, Long.valueOf(parame1));

    // deploy MainChainGateway
    String contractName = "MainChainGateway";
    String filePath = "src/test/resources/soliditycode/MainChainGatewaym001.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000,
        "0", 0, null, mainGateWayOwnerKey,
        mainGateWayOwnerAddress, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parameN = "\"" + Base58.encode58Check(testOracleAddress) + "\"";

    byte[] inputN = Hex.decode(AbiUtil.parseMethod("oracleIndex(address)", parameN, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, inputN, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    int oracleIndexResult = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == 1);

    String parame2 = "\"" + Base58.encode58Check(contractAddress) + "\"";

    input1 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame2, false));
    String ownerTrx1 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input1,
            1000000000,
            0l, "0", testOracleAddress, testOracleKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoOwnerTrx1 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingStubFull);
    Assert.assertEquals(0, infoOwnerTrx1.get().getResultValue());

    input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    String ContractRestule1 = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress1 = ContractRestule1.substring(24);
    logger.info(tmpAddress1);
    String addressHex1 = "41" + tmpAddress1;
    logger.info("address_hex1: " + addressHex1);
    String addressFinal1 = Base58.encode58Check(ByteArray.fromHexString(addressHex1));
    logger.info("address_final1: " + addressFinal1);

    byte[] sideContractAddress1 = WalletClient.decodeFromBase58Check(addressFinal1);
    Assert.assertNotNull(sideContractAddress1);
    Assert.assertEquals(Base58.encode58Check(contractAddress), addressFinal1);

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input2, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    Assert.assertEquals(MinTrx, Long.valueOf(parame1));

    String methodStr3 = "setIt(uint256)";
    String parame4 = "3";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr3, parame4, false));

    String txid2 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input5, maxFeeLimit, 0, "", mainGateWayOwnerAddress,
            mainGateWayOwnerKey, blockingStubFull);
    logger.info("ownerTrx : " + txid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertEquals(0, infoById3.get().getResultValue());

    List<String> retList = PublicMethedForDailybuild
        .getStrings(infoById3.get().getLogList().get(0).getData().toByteArray());
    Long actualSalt = ByteArray.toLong(ByteArray.fromHexString(retList.get(0)));
    Assert.assertTrue(Long.valueOf(parame4) == actualSalt);

    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, inputN, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    int oracleIndexResultAfter = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == oracleIndexResultAfter);

    String setXc = "setXc(address,uint256)";
    String parame5 = "\"" + Base58.encode58Check(mainGateWayAddress) + "\"," + 1001;

    byte[] input4 = Hex.decode(AbiUtil.parseMethod(setXc, parame5, false));

    String txid3 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input4,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    logger.info("ownerTrx : " + txid3);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    String getXc = "getXc(address)";
    String parame6 = "\"" + Base58.encode58Check(mainGateWayAddress) + "\"";
    byte[] input6 = Hex.decode(AbiUtil.parseMethod(getXc, parame6, false));

    String txid4 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input6,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    logger.info("ownerTrx : " + txid4);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());
    List<String> retList4 = PublicMethedForDailybuild
        .getStrings(infoById4.get().getLogList().get(0).getData().toByteArray());
    Long actualSalt1 = ByteArray.toLong(ByteArray.fromHexString(retList4.get(0)));
    logger.info("actualSalt1:" + actualSalt1);
    Assert.assertTrue(1001 == actualSalt1);

    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress1, 2000000000L, testDepositAddress, testDepositTrxKey,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 2000000000L);
    Account accountSideBefore = PublicMethed.queryAccount(testAddress1, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);
    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("transferTokenContractAddress:" + mainGateWayAddress);
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1500000000;
    String txid = PublicMethed
        .triggerContract(mainGateWayAddress,
            callValue,
            input,
            maxFeeLimit, 0, "", testAddress1, testKey1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(testAddress1, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(testAddress1), accountSideAfterAddress);
    Assert.assertEquals(accountSideBeforeBalance + 1500000000, accountSideAfterBalance);

    logger.info("sideGatewayAddress:" + sideGatewayAddress);
    long withdrawValue = 100;
    txid1 = PublicMethed
        .withdrawTrx1(chainIdAddress, sideGatewayAddress, withdrawValue, maxFeeLimit, testAddress1,
            testKey1, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(testAddress1, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(testAddress1), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterBalance - fee1 - withdrawValue,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterBalance + withdrawValue);
  }

  @Test(enabled = true, description = "DelegateCall for reduction method and codeversion in mainChain")
  public void test1DelegateCallMain002() {
    byte[] input = Hex.decode(AbiUtil.parseMethod("getCodeVersion()", "", false));
    TransactionExtention extention = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    String data = ByteArray
        .toHexString(extention.getConstantResult(0).substring(64, 69).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("1.0.3", PublicMethed.hexStringToString(data));

    // deploy MainChainGateway
    String contractName = "MainChainGateway";
    String filePath = "src/test/resources/soliditycode/MainChainGatewaym002.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] contractAddress = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000,
        "0", 0, null, mainGateWayOwnerKey,
        mainGateWayOwnerAddress, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parameN = "\"" + Base58.encode58Check(testOracleAddress) + "\"";

    byte[] inputN = Hex.decode(AbiUtil.parseMethod("isOracle(address)", parameN, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, inputN, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    int oracleIndexResult = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == 1);

    String parame2 = "\"" + Base58.encode58Check(contractAddress) + "\"";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame2, false));
    String ownerTrx1 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input1,
            1000000000,
            0l, "0", testOracleAddress, testOracleKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoOwnerTrx1 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingStubFull);
    Assert.assertEquals(0, infoOwnerTrx1.get().getResultValue());

    input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    String ContractRestule1 = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress1 = ContractRestule1.substring(24);
    logger.info(tmpAddress1);
    String addressHex1 = "41" + tmpAddress1;
    logger.info("address_hex1: " + addressHex1);
    String addressFinal1 = Base58.encode58Check(ByteArray.fromHexString(addressHex1));
    logger.info("address_final1: " + addressFinal1);

    byte[] sideContractAddress1 = WalletClient.decodeFromBase58Check(addressFinal1);
    Assert.assertNotNull(sideContractAddress1);
    Assert.assertEquals(Base58.encode58Check(contractAddress), addressFinal1);

    String methodStr3 = "setIt(uint256)";
    String parame3 = "3";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, parame3, false));

    String txid2 = PublicMethed
        .triggerContract(mainGateWayAddress,
            0,
            input3,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    logger.info("txid2 : " + txid2);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertEquals(1, infoById3.get().getResultValue());
    Assert.assertTrue(infoById3.get().getInternalTransactionsList().get(0).getRejected());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById3.get().getResMessage().toByteArray()));
    data = ByteArray
        .toHexString(infoById3.get().getContractResult(0).substring(135, 163).toByteArray());
    logger.info("data:" + data);
    Assert
        .assertEquals("\u001Bnot allow function fallback", PublicMethed.hexStringToString(data));

    TransactionExtention extention2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    data = ByteArray
        .toHexString(extention2.getConstantResult(0).substring(64, 69).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("1.0.4", PublicMethed.hexStringToString(data));

    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, inputN, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    int oracleIndexResultAfter = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == oracleIndexResultAfter);
  }

  @Test(enabled = true, description = "DelegateCall on stop and pause in mainChain")
  public void test1DelegateCallMain003() {
    String parame = "true";
    byte[] input = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame, false));
    String txid = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());

    // deploy MainChainGateway
    String contractName = "MainChainGateway";
    String filePath = "src/test/resources/soliditycode/MainChainGatewaym004.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress2 = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000,
        "0", 0, null, mainGateWayOwnerKey,
        mainGateWayOwnerAddress, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parame2 = "\"" + Base58.encode58Check(contractAddress2) + "\"";

    byte[] input5 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame2, false));
    String ownerTrx1 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input5,
            1000000000,
            0l, "0", testOracleAddress, testOracleKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoOwnerTrx1 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingStubFull);
    Assert.assertEquals(0, infoOwnerTrx1.get().getResultValue());

    byte[] input6 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input6, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    String ContractResult1 = Hex.toHexString(return2.getConstantResult(0).toByteArray());

    String tmpAddress2 = ContractResult1.substring(24);
    logger.info(tmpAddress2);
    String addressHex2 = "41" + tmpAddress2;
    logger.info("address_hex1: " + addressHex2);
    String addressFinal2 = Base58.encode58Check(ByteArray.fromHexString(addressHex2));
    logger.info("address_final2: " + addressFinal2);

    byte[] sideContractAddress1 = WalletClient.decodeFromBase58Check(addressFinal2);
    Assert.assertNotNull(sideContractAddress1);
    Assert.assertEquals(Base58.encode58Check(contractAddress2), addressFinal2);

    String parame1 = "false";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame1, false));

    String txid1 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input1,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals(0, infoById1.get().getResultValue());

    String parame5 = "true";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod("setPause(bool)", parame5, false));

    String txid2 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input2,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    String hex = infoById2.get().getContractAddress().toStringUtf8();
    logger.info("result:" + hex);

    Assert.assertEquals(0, infoById2.get().getResultValue());

    contractAddress2 = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000,
        "0", 0, null, mainGateWayOwnerKey,
        mainGateWayOwnerAddress, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parame6 = "\"" + Base58.encode58Check(contractAddress2) + "\"";

    byte[] input7 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame6, false));
    String ownerTrx7 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input7,
            1000000000,
            0l, "0", testOracleAddress, testOracleKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoOwnerTrx7 = PublicMethed
        .getTransactionInfoById(ownerTrx7, blockingStubFull);
    Assert.assertEquals(0, infoOwnerTrx7.get().getResultValue());

    input6 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input6, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    ContractResult1 = Hex.toHexString(return2.getConstantResult(0).toByteArray());

    tmpAddress2 = ContractResult1.substring(24);
    logger.info(tmpAddress2);
    addressHex2 = "41" + tmpAddress2;
    logger.info("address_hex1: " + addressHex2);
    addressFinal2 = Base58.encode58Check(ByteArray.fromHexString(addressHex2));
    logger.info("address_final2: " + addressFinal2);

    sideContractAddress1 = WalletClient.decodeFromBase58Check(addressFinal2);
    Assert.assertNotNull(sideContractAddress1);
    Assert.assertEquals(Base58.encode58Check(contractAddress2), addressFinal2);

    String parame3 = "false";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("setPause(bool)", parame3, false));

    String txid3 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input3,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    String hex3 = infoById3.get().getContractAddress().toStringUtf8();
    logger.info("result:" + hex3);

    Assert.assertEquals(0, infoById3.get().getResultValue());
    Assert.assertFalse(infoById3.get().getInternalTransactionsList().get(0).getRejected());
  }

  @Test(enabled = true, description = "DelegateCall for modify method in mainChain")
  public void test1DelegateCallMain004() {
    Account accountMain = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainBalance = accountMain.getBalance();

    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress1, 2000000000L, testDepositAddress, testDepositTrxKey,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertEquals(accountMainBalance + 2000000000L, accountMainBeforeBalance);
    Account accountSideBefore = PublicMethed.queryAccount(testAddress1, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();

    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("transferTokenContractAddress:" + mainGateWayAddress);
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 1500000000;
    String txid = PublicMethed
        .triggerContract(mainGateWayAddress,
            callValue,
            input,
            maxFeeLimit, 0, "", testAddress1, testKey1, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(testAddress1, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    Assert.assertEquals(accountSideBeforeBalance + 1500000000, accountSideAfterBalance);

    long withdrawValue = 1;
    String txid1 = PublicMethed
        .withdrawTrx1(chainIdAddress, sideGatewayAddress, withdrawValue, maxFeeLimit,
            testAddress1, testKey1, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById1 = PublicMethed
        .getTransactionInfoById(txid1, blockingSideStubFull);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    long fee1 = infoById1.get().getFee();
    logger.info("fee1:" + fee1);
    Account accountSideAfterWithdraw = PublicMethed
        .queryAccount(testAddress1, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(testAddress1), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterBalance - fee1 - withdrawValue,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(testAddress1, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterBalance + 2 * withdrawValue);
  }

  @Test(enabled = true, description = "DelegateCall for numOracles in mainChain")
  public void test1DelegateCallMain005() {
    String methodStr1 = "numOracles()";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, "", false));

    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    Long numOracles = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertTrue(1L == Long.valueOf(numOracles));

    //not exist account trigger numOracles
    ECKey ecKey = new ECKey(Utils.getRandom());
    byte[] testAddress = ecKey.getAddress();
    String testKeyFortest = ByteArray.toHexString(ecKey.getPrivKeyBytes());

    return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, 1000000000,
            0l, "0", testAddress, testKeyFortest, blockingStubFull);
    numOracles = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertTrue(1L == Long.valueOf(numOracles));

    String methodStr2 = "isOracle(address)";
    String parame2 = "\"" + Base58.encode58Check(testOracleAddress) + "\"";

    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame2, false));

    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input2, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    Long isOracle = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertTrue(1L == Long.valueOf(isOracle));

    //not exist account trigger isOracle(address)
    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input2, 1000000000,
            0l, "0", testAddress, testKeyFortest, blockingStubFull);
    isOracle = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return2.getConstantResult(0).toByteArray())));
    Assert.assertTrue(1L == Long.valueOf(isOracle));

    // is not Oracle address
    String parame3 = "\"" + Base58.encode58Check(mainGateWayAddress) + "\"";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame3, false));

    TransactionExtention return3 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input3, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    Long isOracle1 = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    Assert.assertTrue(0L == Long.valueOf(isOracle1));
  }

  @Test(enabled = true, description = "DelegateCall for transferOwnership in sideChain")
  public void test1DelegateCallMain006() {
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("numOracles()", "", false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    int numOraclesResult = ByteArray.toInt(return1.getConstantResult(0).toByteArray());
    Assert.assertTrue(numOraclesResult == 1);

    PublicMethed.printAddress(testKey2);
    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress2, 10000000000L, testDepositAddress, testDepositTrxKey,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parame = "true";
    byte[] input = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame, false));
    String txid = PublicMethed
        .triggerContract(mainGateWayAddress,
            0,
            input,
            maxFeeLimit, 0, "", testAddress2, testKey2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(1, infoById.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById.get().getResMessage().toByteArray()));
    String data = ByteArray
        .toHexString(infoById.get().getContractResult(0).substring(135, 155).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0013msg.sender != owner", PublicMethed.hexStringToString(data));
    Assert.assertTrue(infoById.get().getInternalTransactionsList().get(0).getRejected());

    String parame2 = "\"" + Base58.encode58Check(testAddress2) + "\"";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod("transferOwnership(address)", parame2, false));
    String txid2 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input2, maxFeeLimit, 0, "", mainGateWayOwnerAddress,
            mainGateWayOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());
    String parame3 = "true";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame3, false));
    String txid3 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input3, maxFeeLimit, 0, "", sideGateWayOwnerAddress,
            mainGateWayOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(txid3, blockingStubFull);
    Assert.assertEquals(1, infoById3.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById3.get().getResMessage().toByteArray()));
    data = ByteArray
        .toHexString(infoById3.get().getContractResult(0).substring(135, 155).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0013msg.sender != owner", PublicMethed.hexStringToString(data));
    Assert.assertTrue(infoById3.get().getInternalTransactionsList().get(0).getRejected());

    String txid4 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input3, maxFeeLimit, 0, "", testAddress2,
            testKey2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById4 = PublicMethed
        .getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertEquals(0, infoById4.get().getResultValue());
    Assert.assertFalse(infoById4.get().getInternalTransactionsList().get(0).getRejected());

    String parame5 = "false";
    byte[] input5 = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame5, false));
    String txid5 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input5, maxFeeLimit, 0, "", testAddress2,
            testKey2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById5 = PublicMethed
        .getTransactionInfoById(txid5, blockingStubFull);
    Assert.assertEquals(0, infoById5.get().getResultValue());
    Assert.assertFalse(infoById5.get().getInternalTransactionsList().get(0).getRejected());

    // deploy SideChainGateway
    String contractName = "MainChainGateway";
    String filePath = "src/test/resources/soliditycode/MainChainGatewaym006.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] contractAddress = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000, "0", 0, null, testKey2, testAddress2, blockingStubFull);

    String parame6 = "\"" + Base58.encode58Check(contractAddress) + "\"";
    byte[] input6 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame6, false));
    String txid6 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input6, maxFeeLimit, 0, "", testOracleAddress,
            testOracleKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById6 = PublicMethed
        .getTransactionInfoById(txid6, blockingStubFull);
    Assert.assertEquals(0, infoById6.get().getResultValue());

    input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, maxFeeLimit, 0l, "0", mainGateWayOwnerAddress,
            mainGateWayOwnerKey, blockingStubFull);
    String ContractRestule = Hex.toHexString(return2.getConstantResult(0).toByteArray());
    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertNotNull(sideContractAddress);
    Assert.assertEquals(Base58.encode58Check(contractAddress), addressFinal);

    Assert.assertTrue(PublicMethed
        .sendcoin(testAddress3, 10000000000L, testDepositAddress, testDepositTrxKey,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parame7 = "\"" + Base58.encode58Check(testAddress3) + "\"";
    byte[] input7 = Hex.decode(AbiUtil.parseMethod("transferOwnership(address)", parame7, false));
    String txid7 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input7, maxFeeLimit, 0, "", testAddress2,
            testKey2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById7 = PublicMethed
        .getTransactionInfoById(txid7, blockingStubFull);
    Assert.assertEquals(0, infoById7.get().getResultValue());
    Assert.assertFalse(infoById7.get().getInternalTransactionsList().get(0).getRejected());
    List<String> retList = PublicMethedForDailybuild
        .getStrings(infoById7.get().getLogList().get(0).getData().toByteArray());
    Long actualSalt = ByteArray.toLong(ByteArray.fromHexString(retList.get(0)));
    Assert.assertTrue(Long.valueOf("1") == actualSalt);

    String parame8 = "true";
    byte[] input8 = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame8, false));
    String txid8 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input8, maxFeeLimit, 0, "", testAddress2,
            testKey2, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById8 = PublicMethed
        .getTransactionInfoById(txid8, blockingStubFull);
    Assert.assertEquals(1, infoById8.get().getResultValue());
    Assert.assertEquals("REVERT opcode executed",
        ByteArray.toStr(infoById8.get().getResMessage().toByteArray()));
    data = ByteArray
        .toHexString(infoById8.get().getContractResult(0).substring(135, 155).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("\u0013msg.sender != owner", PublicMethed.hexStringToString(data));
    Assert.assertTrue(infoById8.get().getInternalTransactionsList().get(0).getRejected());

    String txid9 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input8, maxFeeLimit, 0, "", testAddress3,
            testKey3, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById9 = PublicMethed
        .getTransactionInfoById(txid9, blockingStubFull);
    Assert.assertEquals(0, infoById9.get().getResultValue());
    Assert.assertFalse(infoById9.get().getInternalTransactionsList().get(0).getRejected());

    String parame10 = "false";
    byte[] input10 = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame10, false));
    String txid10 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input10, maxFeeLimit, 0, "", testAddress3,
            testKey3, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById10 = PublicMethed
        .getTransactionInfoById(txid10, blockingStubFull);
    Assert.assertEquals(0, infoById10.get().getResultValue());
    Assert.assertFalse(infoById10.get().getInternalTransactionsList().get(0).getRejected());

    String parame11 = "\"" + Base58.encode58Check(mainGateWayOwnerAddress) + "\"";

    byte[] input11 = Hex.decode(AbiUtil.parseMethod("transferOwnership(address)", parame11, false));
    String txid11 = PublicMethed
        .triggerContract(mainGateWayAddress, 0, input11, maxFeeLimit, 0, "", testAddress3,
            testKey3, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById11 = PublicMethed
        .getTransactionInfoById(txid11, blockingStubFull);
    Assert.assertEquals(0, infoById11.get().getResultValue());
    Assert.assertFalse(infoById11.get().getInternalTransactionsList().get(0).getRejected());
  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    String parame2 = "\"T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb\"";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame2, false));
    String ownerTrx1 = PublicMethed
        .triggerContract(mainGateWayAddress, 0l, input1,
            1000000000,
            0l, "0", testOracleAddress, testOracleKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoOwnerTrx1 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingStubFull);
    Assert.assertEquals(0, infoOwnerTrx1.get().getResultValue());

    input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            mainGateWayAddress, 0l, input1, 1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);

    String ContractRestule1 = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress1 = ContractRestule1.substring(24);
    logger.info(tmpAddress1);
    String addressHex1 = "41" + tmpAddress1;
    logger.info("address_hex1: " + addressHex1);
    String addressFinal1 = Base58.encode58Check(ByteArray.fromHexString(addressHex1));
    logger.info("address_final1: " + addressFinal1);

    byte[] sideContractAddress1 = WalletClient.decodeFromBase58Check(addressFinal1);
    Assert.assertNotNull(sideContractAddress1);
    Assert.assertEquals("T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb", addressFinal1);

    methodStr1 = "setDepositMinTrx(uint256)";
    parame1 = "1";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(mainGateWayAddress,
            0,
            input2,
            maxFeeLimit, 0, "", mainGateWayOwnerAddress, mainGateWayOwnerKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
