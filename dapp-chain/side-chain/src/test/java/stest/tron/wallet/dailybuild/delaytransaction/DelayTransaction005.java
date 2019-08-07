package stest.tron.wallet.dailybuild.delaytransaction;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.SmartContract;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class DelayTransaction005 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final byte[] toAddress = PublicMethedForDailybuild.getFinalAddress(testKey003);

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private static final long now = System.currentTimeMillis();
  Long delaySecond = 10L;

  private long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(1);
  private Long delayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.delayTransactionFee");
  private Long cancleDelayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.cancleDelayTransactionFee");
  ByteString assetId;
  private byte[] contractAddress = null;
  SmartContract smartContract;

  ECKey ecKey = new ECKey(Utils.getRandom());
  byte[] smartContractOwnerAddress = ecKey.getAddress();
  String smartContractOwnerKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */

  @BeforeClass(enabled = false)
  public void beforeClass() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = false, description = "Delay update energy limit contract")
  public void test1DelayUpdateSetting() {
    //get account
    ecKey = new ECKey(Utils.getRandom());
    smartContractOwnerAddress = ecKey.getAddress();
    smartContractOwnerKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(smartContractOwnerKey);


    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(smartContractOwnerAddress, 2048000000, fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.freezeBalance(smartContractOwnerAddress,10000000L,3,
        smartContractOwnerKey,blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String contractName = "TRONTOKEN";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractScenario004_deployErc20TronToken");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractScenario004_deployErc20TronToken");
    contractAddress = PublicMethedForDailybuild.deployContract(contractName, abi, code, "", maxFeeLimit,
        0L, 100, 999L,"0",0,null,
        smartContractOwnerKey, smartContractOwnerAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    smartContract = PublicMethedForDailybuild.getContract(contractAddress, blockingStubFull);
    Long oldOriginEnergyLimit = 567L;
    Assert.assertTrue(PublicMethedForDailybuild.updateEnergyLimit(contractAddress,oldOriginEnergyLimit,
        smartContractOwnerKey,smartContractOwnerAddress,blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    smartContract = PublicMethedForDailybuild.getContract(contractAddress, blockingStubFull);
    Assert.assertTrue(smartContract.getOriginEnergyLimit() == oldOriginEnergyLimit);

    Long newOriginEnergyLimit = 8765L;
    final String txid = PublicMethedForDailybuild.updateEnergyLimitDelayGetTxid(contractAddress,
        newOriginEnergyLimit, delaySecond,smartContractOwnerKey,smartContractOwnerAddress,
        blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    smartContract = PublicMethedForDailybuild.getContract(contractAddress, blockingStubFull);
    Assert.assertTrue(smartContract.getOriginEnergyLimit() == oldOriginEnergyLimit);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    smartContract = PublicMethedForDailybuild.getContract(contractAddress, blockingStubFull);
    logger.info("newOriginEnergyLimit: " + smartContract.getOriginEnergyLimit());
    Assert.assertTrue(smartContract.getOriginEnergyLimit() == newOriginEnergyLimit);

    Long netFee = PublicMethedForDailybuild.getTransactionInfoById(txid,blockingStubFull).get().getReceipt()
        .getNetFee();
    Long fee = PublicMethedForDailybuild.getTransactionInfoById(txid,blockingStubFull).get().getFee();
    Assert.assertTrue(fee - netFee == delayTransactionFee);

  }

  @Test(enabled = false, description = "Cancel delay energy limit contract")
  public void test2CancelDelayUpdateSetting() {
    //get account
    final Long oldOriginEnergyLimit = smartContract.getOriginEnergyLimit();
    final Long newOriginEnergyLimit = 466L;

    String txid = PublicMethedForDailybuild.updateEnergyLimitDelayGetTxid(contractAddress,newOriginEnergyLimit,
        delaySecond,smartContractOwnerKey,smartContractOwnerAddress,blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Account ownerAccount = PublicMethedForDailybuild.queryAccount(smartContractOwnerKey,blockingStubFull);
    final Long beforeCancelBalance = ownerAccount.getBalance();


    Assert.assertFalse(PublicMethedForDailybuild.cancelDeferredTransactionById(txid,fromAddress,testKey002,
        blockingStubFull));
    final String cancelTxid = PublicMethedForDailybuild.cancelDeferredTransactionByIdGetTxid(txid,
        smartContractOwnerAddress,smartContractOwnerKey,blockingStubFull);
    Assert.assertFalse(PublicMethedForDailybuild.cancelDeferredTransactionById(txid,smartContractOwnerAddress,
        smartContractOwnerKey,blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    smartContract = PublicMethedForDailybuild.getContract(contractAddress, blockingStubFull);
    logger.info("newOriginEnergyLimit: " + smartContract.getOriginEnergyLimit());
    Assert.assertTrue(smartContract.getOriginEnergyLimit() == oldOriginEnergyLimit);

    final Long netFee = PublicMethedForDailybuild.getTransactionInfoById(cancelTxid,blockingStubFull).get()
        .getReceipt().getNetFee();
    final Long fee = PublicMethedForDailybuild.getTransactionInfoById(cancelTxid,blockingStubFull).get()
        .getFee();
    logger.info("net fee : " + PublicMethedForDailybuild.getTransactionInfoById(cancelTxid,blockingStubFull)
        .get().getReceipt().getNetFee());
    logger.info("Fee : " + PublicMethedForDailybuild.getTransactionInfoById(cancelTxid,blockingStubFull)
        .get().getFee());

    ownerAccount = PublicMethedForDailybuild.queryAccount(smartContractOwnerKey,blockingStubFull);
    Long afterCancelBalance = ownerAccount.getBalance();
    Assert.assertTrue(fee - netFee == cancleDelayTransactionFee);
    Assert.assertTrue(fee == beforeCancelBalance - afterCancelBalance);

    logger.info("beforeCancelBalance: " + beforeCancelBalance);
    logger.info("afterCancelBalance : " + afterCancelBalance);
  }

  /**
   * constructor.
   */

  @AfterClass(enabled = false)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


