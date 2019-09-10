package stest.tron.wallet.dailybuild.delaytransaction;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
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
//import org.tron.protos.Protocol.DeferredTransaction;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;
import stest.tron.wallet.common.client.utils.Sha256Hash;

@Slf4j
public class DelayTransaction011 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(1);
  private Long delayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.delayTransactionFee");
  private Long cancleDelayTransactionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.cancleDelayTransactionFee");

  public static final long ONE_DELAY_SECONDS = 60 * 60 * 24L;
  //Optional<DeferredTransaction> deferredTransactionById = null;

  ECKey ecKey = new ECKey(Utils.getRandom());
  byte[] noBandwidthAddress = ecKey.getAddress();
  String noBandwidthKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());

  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] delayAccount2Address = ecKey2.getAddress();
  String delayAccount2Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

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

  @Test(enabled = false, description = "When Bandwidth not enough, create delay transaction.")
  public void test1BandwidthInDelayTransaction() {
    //get account
    ecKey = new ECKey(Utils.getRandom());
    noBandwidthAddress = ecKey.getAddress();
    noBandwidthKey = ByteArray.toHexString(ecKey.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(noBandwidthKey);
    ecKey2 = new ECKey(Utils.getRandom());
    delayAccount2Address = ecKey2.getAddress();
    delayAccount2Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    PublicMethedForDailybuild.printAddress(delayAccount2Key);


    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(noBandwidthAddress, 10000000000L,fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    while (PublicMethedForDailybuild.queryAccount(noBandwidthAddress,blockingStubFull).getFreeNetUsage()
        < 4700L) {
      PublicMethedForDailybuild.sendcoin(delayAccount2Address,1L,noBandwidthAddress,noBandwidthKey,
          blockingStubFull);
    }
    PublicMethedForDailybuild.sendcoin(delayAccount2Address,1L,noBandwidthAddress,noBandwidthKey,
        blockingStubFull);
    PublicMethedForDailybuild.sendcoin(delayAccount2Address,1L,noBandwidthAddress,noBandwidthKey,
        blockingStubFull);
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(fromAddress,PublicMethedForDailybuild.queryAccount(
        noBandwidthAddress,blockingStubFull).getBalance() - 3000L,noBandwidthAddress,
        noBandwidthKey,blockingStubFull));
    logger.info("balance is: " +  PublicMethedForDailybuild.queryAccount(noBandwidthAddress,
        blockingStubFull).getBalance());
    logger.info("Free net usage is " + PublicMethedForDailybuild.queryAccount(noBandwidthAddress,
        blockingStubFull).getFreeNetUsage());

    String updateAccountName = "account_" + Long.toString(System.currentTimeMillis());
    byte[] accountNameBytes = ByteArray.fromString(updateAccountName);
    String txid = PublicMethedForDailybuild.updateAccountDelayGetTxid(noBandwidthAddress,accountNameBytes,
        10L,noBandwidthKey,blockingStubFull);
    logger.info(txid);
    Assert.assertTrue(PublicMethedForDailybuild.getTransactionById(txid,blockingStubFull)
        .get().getRawData().getContractCount() == 0);

    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(noBandwidthAddress, 103332L - 550L,fromAddress,
        testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    txid = PublicMethedForDailybuild.updateAccountDelayGetTxid(noBandwidthAddress,accountNameBytes,
        10L,noBandwidthKey,blockingStubFull);

  }


  /**
     * constructor.
   * */

  @AfterClass(enabled = false)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


