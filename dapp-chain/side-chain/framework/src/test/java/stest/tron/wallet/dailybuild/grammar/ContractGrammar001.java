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
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result.contractResult;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractGrammar001 {


  private final String testNetAccountKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testNetAccountAddress = PublicMethedForDailybuild
      .getFinalAddress(testNetAccountKey);
  byte[] contractAddress = null;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] grammarAddress = ecKey1.getAddress();
  String testKeyForGrammarAddress = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
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
  private String compilerVersion = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.solidityCompilerVersion");

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
    PublicMethedForDailybuild.printAddress(testKeyForGrammarAddress);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test(enabled = true, description = "Support function type")
  public void test1Grammar001() {
    ecKey1 = new ECKey(Utils.getRandom());
    grammarAddress = ecKey1.getAddress();
    testKeyForGrammarAddress = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress, 100000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String filePath = "src/test/resources/soliditycode/contractGrammar001test1Grammar001.sol";
    String contractName = "FunctionSelector";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress,
            grammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid = "";
    String num = "true" + "," + "10";
    txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "select(bool,uint256)", num, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Long returnnumber = ByteArray.toLong(ByteArray.fromHexString(ByteArray.toHexString(
        infoById.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber == 20);

    String num2 = "false" + "," + "10";
    txid = PublicMethedForDailybuild.triggerContract(contractAddress,
        "select(bool,uint256)", num2, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    logger.info("infoById：" + infoById);
    Optional<Transaction> ById = PublicMethedForDailybuild
        .getTransactionById(txid, blockingStubFull);
    logger.info("getRet：" + ById.get().getRet(0));
    logger.info("getNumber：" + ById.get().getRet(0).getContractRet().getNumber());
    logger.info("getContractRetValue：" + ById.get().getRet(0).getContractRetValue());
    logger.info("getContractRet：" + ById.get().getRet(0).getContractRet());

    Assert.assertEquals(ById.get().getRet(0).getContractRet().getNumber(),
        contractResult.SUCCESS_VALUE);
    Assert.assertEquals(ById.get().getRet(0).getContractRetValue(), 1);
    Assert.assertEquals(ById.get().getRet(0).getContractRet(), contractResult.SUCCESS);

    Assert
        .assertEquals(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray()),
            "0000000000000000000000000000000000000000000000000000000000000064");
    Assert.assertEquals(contractResult.SUCCESS, infoById.get().getReceipt().getResult());

    logger.info("ById：" + ById);
    Assert.assertEquals(ById.get().getRet(0).getRet().getNumber(), 0);
    Assert.assertEquals(ById.get().getRet(0).getRetValue(), 0);

    Long returnnumber2 = ByteArray.toLong(ByteArray.fromHexString(
        ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber2 == 100);
  }

  @Test(enabled = true, description = "Ordinary library contract")
  public void test2Grammar002() {
    String filePath = "src/test/resources/soliditycode/contractGrammar001test2Grammar002.sol";
    String contractName = "Set";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress,
            grammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid = "";
    String num = "1";
    byte[] contractAddress1 = null;
    String filePath1 = "src/test/resources/soliditycode/contractGrammar001test2Grammar002.sol";
    String contractName1 = "C";
    HashMap retMap1 = PublicMethedForDailybuild.getBycodeAbiForLibrary(filePath1, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();
    String library = retMap1.get("library").toString();
    String libraryAddress = library + Base58.encode58Check(contractAddress);
    contractAddress1 = PublicMethedForDailybuild
        .deployContractForLibrary(contractName1, abi1, code1, "", maxFeeLimit,
            0L, 100, libraryAddress, testKeyForGrammarAddress,
            grammarAddress, compilerVersion, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    txid = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "register(uint256)", num, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull1);

    Assert.assertTrue(infoById.get().getResultValue() == 0);
  }

  @Test(enabled = true, description = "Library contract")
  public void test3Grammar003() {
    String filePath = "src/test/resources/soliditycode/contractGrammar001test3Grammar003.sol";
    String contractName = "Set";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress,
            grammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid = "";
    String num = "1";
    byte[] contractAddress1 = null;
    String contractName1 = "C";
    HashMap retMap1 = PublicMethedForDailybuild.getBycodeAbiForLibrary(filePath, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();
    String library = retMap1.get("library").toString();
    String libraryAddress = library
        + Base58.encode58Check(contractAddress);
    contractAddress1 = PublicMethedForDailybuild
        .deployContractForLibrary(contractName1, abi1, code1, "", maxFeeLimit,
            0L, 100, libraryAddress, testKeyForGrammarAddress,
            grammarAddress, compilerVersion, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    txid = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "register(uint256)", num, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull1);

    Assert.assertTrue(infoById.get().getResultValue() == 0);
  }


  @Test(enabled = true, description = "Extended type")
  public void test4Grammar004() {
    ecKey1 = new ECKey(Utils.getRandom());
    grammarAddress = ecKey1.getAddress();
    testKeyForGrammarAddress = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(grammarAddress, 100000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String filePath = "src/test/resources/soliditycode/contractGrammar001test4Grammar004.sol";
    String contractName = "Search";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress,
            grammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    byte[] contractAddress1 = null;
    String contractName1 = "C";
    HashMap retMap1 = PublicMethedForDailybuild.getBycodeAbiForLibrary(filePath, contractName1);
    String code1 = retMap1.get("byteCode").toString();
    String abi1 = retMap1.get("abI").toString();
    String library = retMap1.get("library").toString();
    String libraryAddress = null;
    libraryAddress = library
        + Base58.encode58Check(contractAddress);
    contractAddress1 = PublicMethedForDailybuild
        .deployContractForLibrary(contractName1, abi1, code1, "", maxFeeLimit,
            0L, 100, libraryAddress, testKeyForGrammarAddress,
            grammarAddress, compilerVersion, blockingStubFull);
    String txid = "";
    String num = "1";
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    txid = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "append(uint256)", num, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String num1 = "0";
    String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getData(uint256)", num1, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = null;
    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    Long returnnumber = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber == 1);

    String num2 = "1" + "," + "2";
    String txid2 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "replace(uint256,uint256)", num2, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById2 = null;
    infoById2 = PublicMethedForDailybuild.getTransactionInfoById(txid2, blockingStubFull);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);
    String txid3 = PublicMethedForDailybuild.triggerContract(contractAddress1,
        "getData(uint256)", num1, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById1 = null;
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid3, blockingStubFull);
    Long returnnumber1 = ByteArray.toLong(ByteArray
        .fromHexString(ByteArray.toHexString(infoById1.get().getContractResult(0).toByteArray())));

    Assert.assertTrue(returnnumber1 == 2);

  }

  @Test(enabled = true, description = "Solidity assembly")
  public void test5Grammar006() {
    String filePath = "src/test/resources/soliditycode/contractGrammar001test5Grammar006.sol";
    String contractName = "InfoFeed";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, testKeyForGrammarAddress,
            grammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String txid = "";
    String number = "1";
    final String txid1 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "f(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    final String txid2 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "d(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    final String txid3 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "d1(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    final String txid4 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "d2(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    final String txid5 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "d5(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    final String txid6 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "d4(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    final String txid8 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "d6(uint256)", number, false,
        0, maxFeeLimit, grammarAddress, testKeyForGrammarAddress, blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);

    Optional<TransactionInfo> infoById1 = PublicMethedForDailybuild
        .getTransactionInfoById(txid1, blockingStubFull1);
    Assert.assertTrue(infoById1.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById2 = PublicMethedForDailybuild
        .getTransactionInfoById(txid2, blockingStubFull1);
    Assert.assertTrue(infoById2.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById3 = PublicMethedForDailybuild
        .getTransactionInfoById(txid3, blockingStubFull1);
    Assert.assertTrue(infoById3.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById4 = PublicMethedForDailybuild
        .getTransactionInfoById(txid4, blockingStubFull1);
    Assert.assertTrue(infoById4.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById5 = PublicMethedForDailybuild
        .getTransactionInfoById(txid5, blockingStubFull1);
    Assert.assertTrue(infoById5.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById6 = PublicMethedForDailybuild
        .getTransactionInfoById(txid6, blockingStubFull1);
    Assert.assertTrue(infoById6.get().getResultValue() == 0);

    Optional<TransactionInfo> infoById8 = PublicMethedForDailybuild
        .getTransactionInfoById(txid8, blockingStubFull1);
    Assert.assertTrue(infoById8.get().getResultValue() == 0);


  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    PublicMethedForDailybuild
        .freedResource(grammarAddress, testKeyForGrammarAddress, testNetAccountAddress,
            blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
