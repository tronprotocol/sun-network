package stest.tron.wallet.dailybuild.tvmnewcommand.newGrammar;

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
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class constructorDefaultsTest {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] dev001Address = ecKey1.getAddress();
  String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

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
    PublicMethedForDailybuild.printAddress(dev001Key);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = true, description = "Constructor default test")
  public void Test01ConstructorDefault() {
    Assert.assertTrue(PublicMethedForDailybuild
        .sendcoin(dev001Address, 200000000L, fromAddress, testKey002, blockingStubFull));
    AccountResourceMessage accountResource = PublicMethedForDailybuild.getAccountResource(dev001Address,
        blockingStubFull);
    String filePath = "./src/test/resources/soliditycode/ConstructorDefaults.sol";
    String contractName = "testIsContract";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();
    String constructorStr = "constructor(bool)";
    String data = "0";
    String txid = PublicMethedForDailybuild
        .deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
            maxFeeLimit, 0L, 100, null, dev001Key, dev001Address, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    byte[] contractaddress = null;
    Optional<TransactionInfo> info = PublicMethedForDailybuild
        .getTransactionInfoById(txid, blockingStubFull);
    logger.info(info.toString());
    Assert.assertTrue(info.get().getResultValue() == 0);
    data = "false";
    txid = PublicMethedForDailybuild
        .deployContractWithConstantParame(contractName, abi, code, constructorStr, data, "",
            maxFeeLimit, 0L, 100, null, dev001Key, dev001Address, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    info = PublicMethedForDailybuild
        .getTransactionInfoById(txid, blockingStubFull);
    logger.info(info.toString());
    Assert.assertTrue(info.get().getResultValue() == 0);

  }

  /**
   * constructor.
   */

  @AfterClass
  public void shutdown() throws InterruptedException {
    long balance = PublicMethedForDailybuild.queryAccount(dev001Key, blockingStubFull).getBalance();
    PublicMethedForDailybuild.sendcoin(fromAddress, balance, dev001Address, dev001Key,
        blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


