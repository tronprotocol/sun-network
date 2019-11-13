package stest.tron.wallet.common.deploy;

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
public class DelegateCall {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideGatewayAddress);
  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  private final String testOracle = Configuration.getByPath("testng.conf")
      .getString("oralceAccountKey.key1");
  private final byte[] testOracleAddress = PublicMethed.getFinalAddress(testOracle);
  private final String mainGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final byte[] mainGateWayOwnerAddress = PublicMethed.getFinalAddress(mainGateWayOwner);
  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String parame1 = null;
  String methodStr1 = null;
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
    PublicMethed.printAddress(testKeyFordeposit);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "DelegateCall for 1.0.2 in mainChain")
  public void test1DelegateCallMain001() {

    methodStr1 = "setDepositMinTrx(uint256)";
    parame1 = "2";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
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
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input2, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    Long MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return1.getConstantResult(0).toByteArray())));
    Assert.assertEquals(MinTrx, Long.valueOf(parame1));

    // deploy MainChainGateway
    String contractName = "MainChainGateway";
    String filePath = "src/test/resources/soliditycode/MainChainGateway.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000,
        "0", 0, null, gateWatOwnerAddressKey,
        gateWatOwnerAddress, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String parameN = "\"" + Base58.encode58Check(testOracleAddress) + "\"";

    byte[] inputN = Hex.decode(AbiUtil.parseMethod("oracleIndex(address)", parameN, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, inputN, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    int oracleIndexResult = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == 1);

    String parame2 = "\"" + Base58.encode58Check(contractAddress) + "\"";

    input1 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame2, false));
    String ownerTrx1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input1,
            1000000000,
            0l, "0", testOracleAddress, testOracle, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoOwnerTrx1 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingStubFull);
    Assert.assertEquals(0, infoOwnerTrx1.get().getResultValue());

    input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    return1 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input1, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);

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
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input2, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    MinTrx = ByteArray.toLong(ByteArray
        .fromHexString(Hex.toHexString(return3.getConstantResult(0).toByteArray())));
    Assert.assertEquals(MinTrx, Long.valueOf(parame1));

    return2 = PublicMethed
        .triggerContractForTransactionExtention(
            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, inputN, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    int oracleIndexResultAfter = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == oracleIndexResultAfter);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 2000000000L);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
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
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
//    Assert.assertEquals(1500000000, accountSideAfterBalance);

    logger.info("sideGatewayAddress:" + sideGatewayAddress);
    long withdrawValue = 100;
    txid1 = PublicMethed
        .withdrawTrx(ChainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
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
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterBalance - fee1 - withdrawValue,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterWithdrawBalance,
        accountMainAfterBalance + withdrawValue);
  }

  @Test(enabled = true, description = "DelegateCall for 1.0.2 in sideChain")
  public void test1DelegateCallSide001() {
    parame1 = "10";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    String ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    logger.info("ownerTrx : " + ownerTrx);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethed
        .getTransactionInfoById(ownerTrx, blockingSideStubFull);
    Assert.assertEquals(0, infoById2.get().getResultValue());

    TransactionExtention transactionExtention = PublicMethedForDailybuild
        .triggerConstantContractForExtention(sideChainAddressKey,
            "withdrawMinTrx()", "", false,
            0, 0, "0", 0, gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    Assert.assertEquals(10,
        ByteArray.toInt(transactionExtention.getConstantResult(0).toByteArray()));

    // deploy SideChainGateway
    String contractName = "SideChainGateway";
    String filePath = "src/test/resources/soliditycode/SideChainGateway.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress = PublicMethed.deployContractForSide(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, 10000,
        "0", 0, null, gateWatOwnerAddressKey,
        gateWatOwnerAddress, chainIdAddressKey, blockingSideStubFull);

    String parameN = "\"" + Base58.encode58Check(testOracleAddress) + "\"";

    byte[] inputN = Hex.decode(AbiUtil.parseMethod("oracles(address)", parameN, false));
    TransactionExtention return2 = PublicMethed
        .triggerContractForTransactionExtention(
            sideChainAddressKey, 0l, inputN, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    int oracleIndexResult = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    logger.info("oracleIndexResult:"+oracleIndexResult);
    Assert.assertTrue(oracleIndexResult == 1);

    parame1 = "\"" + Base58.encode58Check(contractAddress) + "\"";

    input1 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame1, false));
    ownerTrx = PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input1,
            1000000000,
            0l, "0", testOracleAddress, testOracle, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(
            sideChainAddressKey, 0l, input1, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertNotNull(sideContractAddress);
    Assert.assertEquals(Base58.encode58Check(contractAddress), addressFinal);

    transactionExtention = PublicMethedForDailybuild
        .triggerConstantContractForExtention(sideChainAddressKey,
            "withdrawMinTrx()", "", false,
            0, 0, "0", 0, gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);

    Assert.assertEquals(10,
        ByteArray.toInt(transactionExtention.getConstantResult(0).toByteArray()));

    return2 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddressKey, 0l, inputN, 1000000000,
            0l, "0", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingSideStubFull);
    int oracleIndexResultAfter = ByteArray.toInt(return2.getConstantResult(0).toByteArray());
    Assert.assertTrue(oracleIndexResult == oracleIndexResultAfter);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 2000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 2000000000L);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
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
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountMainAfterBalance:" + accountMainAfterBalance);
    Assert.assertEquals(accountMainAfterBalance, accountMainBeforeBalance - fee - 1500000000);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
    Assert.assertEquals(1500000000, accountSideAfterBalance);

    logger.info("sideGatewayAddress:" + sideGatewayAddress);
    long withdrawValue = 100;
    String txid1 = PublicMethed
        .withdrawTrx(ChainIdAddress,
            sideGatewayAddress,
            withdrawValue,
            maxFeeLimit, depositAddress, testKeyFordeposit, blockingStubFull, blockingSideStubFull);
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
        .queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterWithdrawBalance = accountSideAfterWithdraw.getBalance();
    ByteString addressAfterWithdraw = accountSideAfterWithdraw.getAddress();
    String addressAfterWithdrawAddress = Base58
        .encode58Check(addressAfterWithdraw.toByteArray());
    logger.info("addressAfterWithdrawAddress:" + addressAfterWithdrawAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), addressAfterWithdrawAddress);
    Assert.assertEquals(accountSideAfterBalance - fee1 - withdrawValue,
        accountSideAfterWithdrawBalance);
    Account accountMainAfterWithdraw = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterWithdrawBalance = accountMainAfterWithdraw.getBalance();
    logger.info("accountAfterWithdrawBalance:" + accountMainAfterWithdrawBalance);
    Assert.assertEquals(accountMainAfterBalance + withdrawValue,
        accountMainAfterWithdrawBalance);


  }

  @AfterClass
  public void shutdown() throws InterruptedException {
    methodStr1 = "setDepositMinTrx(uint256)";
    parame1 = "1";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, parame1, false));

    String txid1 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            0,
            input1,
            maxFeeLimit, 0, "", gateWatOwnerAddress, gateWatOwnerAddressKey, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    byte[] input2 = Hex.decode(AbiUtil.parseMethod("setWithdrawMinTrx(uint256)", parame1, false));
    PublicMethed
        .triggerContractSideChain(sideChainAddressKey,
            WalletClient.decodeFromBase58Check(chainIdAddress), 0l, input2,
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
