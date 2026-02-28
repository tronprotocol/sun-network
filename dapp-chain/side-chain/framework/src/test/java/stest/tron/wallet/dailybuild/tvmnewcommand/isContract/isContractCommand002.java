package stest.tron.wallet.dailybuild.tvmnewcommand.isContract;

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
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;


@Slf4j
public class isContractCommand002 {

  private final String testNetAccountKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testNetAccountAddress = PublicMethedForDailybuild
      .getFinalAddress(testNetAccountKey);
  byte[] contractAddress = null;
  byte[] selfdestructContractAddress = null;
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] contractExcAddress = ecKey1.getAddress();
  String contractExcKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  byte[] selfdestructContractExcAddress = ecKey1.getAddress();
  String selfdestructContractKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull1 = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(Parameter.CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass(enabled = true)
  public void beforeClass() {
    PublicMethedForDailybuild.printAddress(contractExcKey);
    PublicMethedForDailybuild.printAddress(selfdestructContractKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);
  }


  @Test(enabled = true, description = "Selfdestruct contract test isContract Command")
  public void test01SelfdestructContract() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(contractExcAddress, 10000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String filePath = "src/test/resources/soliditycode/TvmIsContract001.sol";
    String contractName = "testIsContract";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, contractExcKey,
            contractExcAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    String txid = "";
    String num = "\"" + Base58.encode58Check(contractAddress) + "\"";
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(selfdestructContractExcAddress, 10000000000L, testNetAccountAddress,
            testNetAccountKey,
            blockingStubFull));

    selfdestructContractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, selfdestructContractKey,
            selfdestructContractExcAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    txid = PublicMethedForDailybuild.triggerContract(selfdestructContractAddress,
        "testIsContractCommand(address)", num, false,
        0, maxFeeLimit, selfdestructContractExcAddress, selfdestructContractKey, blockingStubFull);
    logger.info("txid:" + txid);
    Optional<Protocol.TransactionInfo> infoById1 = null;
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
    Assert.assertEquals(1, ByteArray.toInt(infoById1.get().getContractResult(0).toByteArray()));
    logger.info(infoById1.toString());

    TransactionExtention transactionExtention = PublicMethedForDailybuild
        .triggerConstantContractForExtention(selfdestructContractAddress,
            "testIsContractView(address)", num, false,
            0, 0, "0", 0, selfdestructContractExcAddress, selfdestructContractKey,
            blockingStubFull);
    Assert.assertEquals("SUCCESS", transactionExtention.getResult().getCode().toString());
    Assert
        .assertEquals(1, ByteArray.toInt(transactionExtention.getConstantResult(0).toByteArray()));

    String txid1 = "";
    txid1 = PublicMethedForDailybuild.triggerContract(contractAddress,
        "selfdestructContract(address)", num, false,
        0, maxFeeLimit, contractExcAddress, contractExcKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    logger.info(infoById1.toString());

    txid1 = PublicMethedForDailybuild.triggerContract(selfdestructContractAddress,
        "testIsContractCommand(address)", num, false,
        0, maxFeeLimit, selfdestructContractExcAddress, selfdestructContractKey, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    infoById1 = PublicMethedForDailybuild.getTransactionInfoById(txid1, blockingStubFull);
    Assert.assertEquals(0, ByteArray.toInt(infoById1.get().getContractResult(0).toByteArray()));
    logger.info(infoById1.toString());

    transactionExtention = PublicMethedForDailybuild
        .triggerConstantContractForExtention(selfdestructContractAddress,
            "testIsContractView(address)", num, false,
            0, 0, "0", 0, selfdestructContractExcAddress, selfdestructContractKey,
            blockingStubFull);
    logger.info("transactionExtention:" + transactionExtention.toString());
    Assert.assertEquals("SUCCESS", transactionExtention.getResult().getCode().toString());
    Assert
        .assertEquals(0, ByteArray.toInt(transactionExtention.getConstantResult(0).toByteArray()));
  }

  @Test(enabled = true, description = "No constructor test isContract Command")
  public void test02NoConstructorContract() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(contractExcAddress, 10000000000L, testNetAccountAddress, testNetAccountKey,
            blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String filePath = "src/test/resources/soliditycode/TvmIsContract002.sol";
    String contractName = "testIsContract";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String txid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, contractExcKey,
            contractExcAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> info = PublicMethedForDailybuild
        .getTransactionInfoById(txid, blockingStubFull);
    logger.info(info.get().toString());
    Assert.assertEquals(0, info.get().getResultValue());
  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    long balance = PublicMethedForDailybuild.queryAccount(contractExcKey, blockingStubFull)
        .getBalance();
    PublicMethedForDailybuild
        .sendcoin(testNetAccountAddress, balance, contractExcAddress, contractExcKey,
            blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }


}
