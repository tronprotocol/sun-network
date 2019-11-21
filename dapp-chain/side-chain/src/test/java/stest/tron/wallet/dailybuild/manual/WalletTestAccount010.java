package stest.tron.wallet.dailybuild.manual;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
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
import org.tron.protos.Protocol.Account;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class WalletTestAccount010 {

  private static final long now = System.currentTimeMillis();
  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] toAddress = PublicMethedForDailybuild.getFinalAddress(testKey003);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] account010Address = ecKey1.getAddress();
  String account010Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] account010SecondAddress = ecKey2.getAddress();
  String account010SecondKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  ECKey ecKey3 = new ECKey(Utils.getRandom());
  byte[] account010InvalidAddress = ecKey3.getAddress();
  String account010InvalidKey = ByteArray.toHexString(ecKey3.getPrivKeyBytes());
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);

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
    PublicMethedForDailybuild.printAddress(account010Key);
    PublicMethedForDailybuild.printAddress(account010SecondKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);


  }

  @Test(enabled = false)
  public void testGetStorage() {
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(account010Address, 100000000,
        fromAddress, testKey002, blockingStubFull));
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(account010SecondAddress, 100000000,
        fromAddress, testKey002, blockingStubFull));
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(account010InvalidAddress, 100000000,
        fromAddress, testKey002, blockingStubFull));
    Account account010Info = PublicMethedForDailybuild
        .queryAccount(account010Key, blockingStubFull);
    Assert.assertTrue(account010Info.getAccountResource().getStorageLimit() == 0);
    Assert.assertTrue(account010Info.getAccountResource().getLatestExchangeStorageTime() == 0);

    Assert.assertTrue(
        PublicMethedForDailybuild.buyStorage(100000000L, account010Address, account010Key,
            blockingStubFull));

    account010Info = PublicMethedForDailybuild.queryAccount(account010Key, blockingStubFull);
    Assert.assertTrue(account010Info.getAccountResource().getStorageLimit() > 0);
    Assert.assertTrue(account010Info.getAccountResource().getLatestExchangeStorageTime() > 0);

    AccountResourceMessage account010Resource = PublicMethedForDailybuild
        .getAccountResource(account010Address,
            blockingStubFull);
    Assert.assertTrue(account010Resource.getStorageLimit() > 0);
  }

  @Test(enabled = false)
  public void testSellStorage() {
    AccountResourceMessage account010Resource = PublicMethedForDailybuild
        .getAccountResource(account010Address,
            blockingStubFull);
    Long storageLimit = account010Resource.getStorageLimit();
    Account account001Info = PublicMethedForDailybuild
        .queryAccount(account010Key, blockingStubFull);
    Assert.assertTrue(account001Info.getBalance() == 0);
    //When there is no enough storage,sell failed.
    Assert.assertFalse(
        PublicMethedForDailybuild.sellStorage(storageLimit + 1, account010Address, account010Key,
            blockingStubFull));
    //Can not sell 0 storage
    Assert.assertFalse(PublicMethedForDailybuild.sellStorage(0, account010Address, account010Key,
        blockingStubFull));
    //Sell all storage.
    Assert.assertTrue(
        PublicMethedForDailybuild.sellStorage(storageLimit, account010Address, account010Key,
            blockingStubFull));
    account010Resource = PublicMethedForDailybuild.getAccountResource(account010Address,
        blockingStubFull);
    storageLimit = account010Resource.getStorageLimit();
    Assert.assertTrue(storageLimit == 0);
    account001Info = PublicMethedForDailybuild.queryAccount(account010Key, blockingStubFull);
    Assert.assertTrue(account001Info.getBalance() > 0);


  }


  /**
   * constructor.
   */

  @AfterClass(enabled = true)
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}


