package stest.tron.wallet.dailybuild.depositWithdraw;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
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
public class ContractCallMain001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  String methodStr1 = null;
  String parame1 = null;
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

  @Test(enabled = true, description = "MainGateWay contract call function use contract account.")
  public void test1ContractFallback001() {

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 10000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Assert.assertTrue(accountBeforeBalance == 10000000000L);

    // deploy testMainContract
    String contractName = "contractCallMain";
    String filePath = "src/test/resources/soliditycode/contractCallMain.sol";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] contractAddress = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 10000000000L, 0, 10000,
        "0", 0, null, testDepositTrx,
        testDepositAddress, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("contractAddress:" + Base58.encode58Check(contractAddress));
    Assert.assertNotNull(contractAddress);
    /*Assert.assertTrue(PublicMethed
        .sendcoin(contractAddress, 10000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));*/
    String methodStr = "setGatewayAddress(address)";
    String parame1 = "\"" + Base58.encode58Check(mainGateWayAddressKey) + "\"";

    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame1, false));

    long callValue = 0;
    String txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String methodStr1 = "callDepositTRX()";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, "", false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input1,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    String expectContractResult = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000006408c379a0000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000126e6f7420616c6c6f7720636f6e7472616374000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    Assert.assertEquals(expectContractResult, contractResult);
    String inputTokenID = "1000001";
    long inputTokenValue = 1;
    String methodStr2 = "callDepositTRC10(uint64,uint64)";
    String parame2 = inputTokenID + "," + inputTokenValue;

    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame2, false));
    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input2,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(
        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000006408c379a000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000016746f6b656e496420213d206d73672e746f6b656e69640000000000000000000000000000000000000000000000000000000000000000000000000000",
        contractResult);

    String methodStr3 = "callDepositTRC721(uint256)";
    String parame3 = "1";

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, parame3, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input3,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr4 = "callDepositTRC721(uint256)";
    String parame4 = "1";

    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr4, parame4, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input4,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr5 = "callMappingTRC20(bytes)";
    String parame5 = "\"" + txid + "\"";

    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr5, parame5, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input5,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr6 = "callMappingTRC721(bytes)";
    String parame6 = "\"" + txid + "\"";

    byte[] input6 = Hex.decode(AbiUtil.parseMethod(methodStr6, parame6, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input6,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr7 = "callRetryDeposit(uint256)";
    String parame7 = "1";

    byte[] input7 = Hex.decode(AbiUtil.parseMethod(methodStr7, parame7, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input7,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr8 = "callRetryMapping(uint256)";
    String parame8 = "1";

    byte[] input8 = Hex.decode(AbiUtil.parseMethod(methodStr8, parame8, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input8,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);
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
