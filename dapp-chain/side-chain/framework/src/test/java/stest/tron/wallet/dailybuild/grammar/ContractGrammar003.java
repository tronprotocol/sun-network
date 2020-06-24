package stest.tron.wallet.dailybuild.grammar;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Hash;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractGrammar003 {


  private final String testNetAccountKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testNetAccountAddress = PublicMethedForDailybuild
      .getFinalAddress(testNetAccountKey);
  byte[] contractAddress = null;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] grammarAddress3 = ecKey1.getAddress();
  String testKeyForGrammarAddress3 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull1 = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);
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
    PublicMethedForDailybuild.printAddress(testKeyForGrammarAddress3);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);
  }


  @Test(enabled = true, description = "Complex structure")
  public void test1Grammar014() {
    ecKey1 = new ECKey(Utils.getRandom());
    grammarAddress3 = ecKey1.getAddress();
    testKeyForGrammarAddress3 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress3, 100000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String filePath = "src/test/resources/soliditycode/contractGrammar003test1Grammar014.sol";
    String contractName = "A";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String contractName1 = "B";
    HashMap retMap1 = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();
    byte[] contractAddress1 = PublicMethedForDailybuild
        .deployContract(contractName1, abi1, code1, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Optional<TransactionInfo> infoById1 = null;
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    Long returnnumber = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(infoById.get().getResultValue() == 0);
    Assert.assertTrue(returnnumber == 0);

    Long returnnumber1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    Assert.assertTrue(returnnumber1 == 0);
    Optional<TransactionInfo> infoById4 = null;
    String initParmes = "\"" + Base58.encode58Check(contractAddress1) + "\",\"1\"";
    String txid4 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "callTest(address,uint256)", initParmes, false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById4 = PublicMethedForDailybuild.getTransactionInfoById(txid4, blockingStubFull);

    Assert.assertTrue(infoById4.get().getResultValue() == 0);

    String txid5 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById5 = null;
    infoById5 = PublicMethedForDailybuild.getTransactionInfoById(txid5, blockingStubFull);
    Long returnnumber5 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById5.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber5 == 0);

    String txid6 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById6 = null;
    infoById6 = PublicMethedForDailybuild.getTransactionInfoById(txid6, blockingStubFull);
    Long returnnumber6 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById6.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber6 == 1);

    String txid7 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "callcodeTest(address,uint256)", initParmes, false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById7 = null;
    infoById7 = PublicMethedForDailybuild.getTransactionInfoById(txid7, blockingStubFull);

    Assert.assertTrue(infoById7.get().getResultValue() == 0);

    String txid8 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById8 = null;
    infoById8 = PublicMethedForDailybuild.getTransactionInfoById(txid8, blockingStubFull);
    Long returnnumber8 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById8.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber8 == 1);

    String txid9 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById9 = null;
    infoById9 = PublicMethedForDailybuild.getTransactionInfoById(txid9, blockingStubFull);
    Long returnnumber9 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById9.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber9 == 1);

    String txid10 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "delegatecallTest(address,uint256)", initParmes, false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById10 = null;
    infoById10 = PublicMethedForDailybuild.getTransactionInfoById(txid10, blockingStubFull);

    Assert.assertTrue(infoById10.get().getResultValue() == 0);

    String txid11 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById11 = null;
    infoById11 = PublicMethedForDailybuild.getTransactionInfoById(txid11, blockingStubFull);
    Long returnnumber11 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById11.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber11 == 1);

    String txid12 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById12 = null;
    infoById12 = PublicMethedForDailybuild.getTransactionInfoById(txid12, blockingStubFull);
    Long returnnumber12 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById12.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber12 == 1);

    String initParmes1 = "\"" + Base58.encode58Check(contractAddress1) + "\"";
    String txid13 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "callAddTest(address)", initParmes1, false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById13 = null;
    infoById13 = PublicMethedForDailybuild.getTransactionInfoById(txid13, blockingStubFull);

    Assert.assertTrue(infoById13.get().getResultValue() == 0);

    String txid14 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById14 = null;
    infoById14 = PublicMethedForDailybuild.getTransactionInfoById(txid14, blockingStubFull);
    Long returnnumber14 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById14.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber14 == 1);

    String txid15 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getnumberForB()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById15 = null;
    infoById15 = PublicMethedForDailybuild.getTransactionInfoById(txid15, blockingStubFull);
    Long returnnumber15 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById15.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber15 == 3);
  }


  @Test(enabled = true, description = "Fallback function ")
  public void test2Grammar015() {
    String filePath = "src/test/resources/soliditycode/contractGrammar003test2Grammar015.sol";
    String contractName = "ExecuteFallback";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "callExistFunc()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    String i = ByteArray.toHexString(Hash.sha3("ExistFuncCalled(bytes,uint256)".getBytes()));
    String resultvalue = ByteArray
        .toHexString(infoById.get().getLogList().get(0).getTopicsList().get(0).toByteArray());

    Assert.assertTrue(infoById.get().getResultValue() == 0);
    Assert.assertEquals(i, resultvalue);

    Optional<TransactionInfo> infoById1 = null;
    String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "callNonExistFunc()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    String value = ByteArray.toHexString(Hash.sha3("FallbackCalled(bytes)".getBytes()));
    String resultvalue1 = ByteArray
        .toHexString(infoById1.get().getLogList().get(0).getTopicsList().get(0).toByteArray());

    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    Assert.assertEquals(value, resultvalue1);

  }

  @Test(enabled = true, description = "Permission control ")
  public void test3Grammar016() {
    String filePath = "src/test/resources/soliditycode/contractGrammar003test3Grammar016.sol";
    String contractName = "D";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "readData()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);

    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String contractName1 = "E";
    HashMap retMap1 = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();

    byte[] contractAddress1 = PublicMethedForDailybuild
        .deployContract(contractName1, abi1, code1, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = null;
    String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "g()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);

    Assert.assertTrue(infoById1.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById2 = null;
    String num = "3";
    String txid2 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "setData(uint256)", num, false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById2 = PublicMethedForDailybuild.getTransactionInfoById(txid2, blockingStubFull);

    Assert.assertTrue(infoById2.get().getResultValue() == 0);

    String txid3 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getData()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById3 = null;
    infoById3 = PublicMethedForDailybuild.getTransactionInfoById(txid3, blockingStubFull);
    Long returnnumber3 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById3.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber3 == 3);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);

  }

  @Test(enabled = true, description = "Structure")
  public void test4Grammar017() {
    String filePath = "src/test/resources/soliditycode/contractGrammar003test4Grammar017.sol";
    String contractName = "CrowdFunding";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress1 = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Account info;
    String initParmes = "\"" + Base58.encode58Check(grammarAddress3) + "\",\"1\"";
    Optional<TransactionInfo> infoById = null;
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "candidate(address,uint256)", initParmes, false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Long returnnumber1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));
    Assert.assertTrue(returnnumber1 == 1);

    String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "check(uint256)", "1", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = PublicMethedForDailybuild
        .getTransactionInfoById(txid1, blockingStubFull1);
    Long returnnumber2 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber2 == 1);

    String txid2 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "vote(uint256)", "1", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById2 = PublicMethedForDailybuild
        .getTransactionInfoById(txid2, blockingStubFull);

    Assert.assertTrue(infoById2.get().getResultValue() == 0);

  }

  @Test(enabled = true, description = "Built-in function")
  public void test5Grammar018() {
    String filePath = "src/test/resources/soliditycode/contractGrammar003test5Grammar018.sol";
    String contractName = "Grammar18";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "testAddmod()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Long returnnumber = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(infoById.get().getResultValue() == 0);
    Assert.assertTrue(returnnumber == 1);
    Optional<TransactionInfo> infoById1 = null;
    String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "testMulmod()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    Long returnnumber1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(infoById1.get().getResultValue() == 0);
    Assert.assertTrue(returnnumber1 == 2);

    String txid2 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "testKeccak256()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById2 = null;
    infoById2 = PublicMethedForDailybuild.getTransactionInfoById(txid2, blockingStubFull);

    Assert.assertTrue(infoById2.get().getResultValue() == 0);

    String txid3 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "testSha256()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById3 = null;
    infoById3 = PublicMethedForDailybuild.getTransactionInfoById(txid3, blockingStubFull);

    Assert.assertTrue(infoById3.get().getResultValue() == 0);

    String txid4 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "testSha3()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    Optional<TransactionInfo> infoById4 = null;
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById4 = PublicMethedForDailybuild.getTransactionInfoById(txid4, blockingStubFull);
    Assert.assertTrue(infoById4.get().getResultValue() == 0);
  }


  @Test(enabled = true, description = "Time unit")
  public void test6Grammar019() {

    String filePath = "src/test/resources/soliditycode/contractGrammar003test6Grammar019.sol";
    String contractName = "timetest";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "timetest()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 1);

  }


  @Test(enabled = true, description = "Trx and sun unit conversion.")
  public void test7Grammar020() {
    String filePath = "src/test/resources/soliditycode/contractGrammar003test7Grammar020.sol";
    String contractName = "trxtest";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress3,
            grammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "test()", "#", false,
        0, maxFeeLimit, grammarAddress3, testKeyForGrammarAddress3, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);

  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    PublicMethedForDailybuild
        .freedResource(grammarAddress3, testKeyForGrammarAddress3, testNetAccountAddress,
            blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
