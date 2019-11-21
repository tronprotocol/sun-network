package stest.tron.wallet.dailybuild.manual;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Block;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class WalletTestAccount012 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("mainWitness.key25");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);

  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("mainWitness.key2");
  private final byte[] testAddress003 = PublicMethedForDailybuild.getFinalAddress(testKey003);

  private final String testKey004 = Configuration.getByPath("testng.conf")
      .getString("mainWitness.key3");
  private final byte[] testAddress004 = PublicMethedForDailybuild.getFinalAddress(testKey004);
  ArrayList<String> txidList = new ArrayList<String>();
  Optional<TransactionInfo> infoById = null;
  Long beforeTime;
  Long afterTime;
  Long beforeBlockNum;
  Long afterBlockNum;
  Block currentBlock;
  Long currentBlockNum;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull1 = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  //get account

  /**
   * constructor.
   */

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
    PublicMethedForDailybuild.printAddress(testKey002);
    PublicMethedForDailybuild.printAddress(testKey003);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);
    currentBlock = blockingStubFull1.getNowBlock(GrpcAPI.EmptyMessage.newBuilder().build());
    beforeBlockNum = currentBlock.getBlockHeader().getRawData().getNumber();
    beforeTime = System.currentTimeMillis();
  }

  @Test(enabled = false, threadPoolSize = 20, invocationCount = 20)
  public void storageAndCpu() {
    ECKey ecKey1 = new ECKey(Utils.getRandom());
    byte[] asset011Address = ecKey1.getAddress();
    String testKeyForAssetIssue011 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(testKeyForAssetIssue011);

    PublicMethedForDailybuild
        .sendcoin(asset011Address, 100000000000000L, fromAddress, testKey002, blockingStubFull);
    Random rand = new Random();
    Integer randNum = rand.nextInt(30) + 1;
    randNum = rand.nextInt(4000);

    Long maxFeeLimit = 1000000000L;
    String contractName = "StorageAndCpu" + Integer.toString(randNum);
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_WalletTestAccount012_storageAndCpu");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_WalletTestAccount012_storageAndCpu");
    byte[] contractAddress = PublicMethedForDailybuild.deployContract(contractName, abi, code,
        "", maxFeeLimit,
        0L, 100, null, testKeyForAssetIssue011, asset011Address, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    SmartContract smartContract = PublicMethedForDailybuild
        .getContract(contractAddress, blockingStubFull);
    String txid;

    Integer i = 1;
    AccountResourceMessage accountResource = PublicMethedForDailybuild
        .getAccountResource(asset011Address,
            blockingStubFull);
    accountResource = PublicMethedForDailybuild.getAccountResource(asset011Address,
        blockingStubFull);
    Long beforeEnergyLimit = accountResource.getEnergyLimit();
    Long afterEnergyLimit;
    Long beforeTotalEnergyLimit = accountResource.getTotalEnergyLimit();
    Account account = PublicMethedForDailybuild
        .queryAccount(testKeyForAssetIssue011, blockingStubFull);
    Long afterTotalEnergyLimit;
    while (i++ < 20000) {
      accountResource = PublicMethedForDailybuild.getAccountResource(asset011Address,
          blockingStubFull);
      beforeEnergyLimit = accountResource.getEnergyLimit();
      beforeTotalEnergyLimit = accountResource.getTotalEnergyLimit();
      String initParmes = "\"" + "21" + "\"";
      /*      txid = PublicMethedForDailybuild.triggerContract(contractAddress,
          "storage8Char()", "", false,
          0, maxFeeLimit, asset011Address, testKeyForAssetIssue011, blockingStubFull);*/
      PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
      PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull1);
      txid = PublicMethedForDailybuild.triggerContract(contractAddress,
          "add2(uint256)", initParmes, false,
          0, maxFeeLimit, asset011Address, testKeyForAssetIssue011, blockingStubFull);
      accountResource = PublicMethedForDailybuild.getAccountResource(asset011Address,
          blockingStubFull);
      //logger.info("Current limit is " + accountResource.getTotalEnergyLimit());
      //PublicMethedForDailybuild.freezeBalanceGetEnergy(asset011Address,1000000L,3,
      //    1,testKeyForAssetIssue011,blockingStubFull);

      accountResource = PublicMethedForDailybuild.getAccountResource(asset011Address,
          blockingStubFull);
      afterEnergyLimit = accountResource.getEnergyLimit();
      afterTotalEnergyLimit = accountResource.getTotalEnergyLimit();

      logger.info("Total energy limit is " + (float) afterTotalEnergyLimit / 50000000000L);
      Float rate =
          (float) (afterTotalEnergyLimit - beforeTotalEnergyLimit) / beforeTotalEnergyLimit;
      //logger.info("rate is " + rate);
      //Assert.assertTrue(rate >= 0.001001000 && rate <= 0.001001002);
      //txidList.add(txid);
      try {
        Thread.sleep(30);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      account = PublicMethedForDailybuild.queryAccount(testKeyForAssetIssue011, blockingStubFull);
      Float energyrate = (float) (beforeEnergyLimit) / account.getAccountResource()
          .getFrozenBalanceForEnergy().getFrozenBalance();
      //logger.info("energy rate is " + energyrate);
      if (i % 20 == 0) {
        PublicMethedForDailybuild.freezeBalanceForReceiver(fromAddress, 1000000L, 3, 1,
            ByteString.copyFrom(asset011Address), testKey002, blockingStubFull);
      }
    }
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