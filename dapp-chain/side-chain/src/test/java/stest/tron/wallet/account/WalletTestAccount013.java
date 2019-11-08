package stest.tron.wallet.account;

import com.google.protobuf.ByteString;
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
import org.tron.api.GrpcAPI;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class WalletTestAccount013 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String testKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] toAddress = PublicMethedForDailybuild.getFinalAddress(testKey003);
  Optional<TransactionInfo> infoById = null;
  long account013BeforeBalance;
  long freezeAmount = 10000000L;
  long freezeDuration = 0;
  byte[] account013Address;
  String testKeyForAccount013;
  byte[] receiverDelegateAddress;
  String receiverDelegateKey;
  byte[] emptyAddress;
  String emptyKey;
  byte[] account4DelegatedResourceAddress;
  String account4DelegatedResourceKey;
  byte[] account5DelegatedResourceAddress;
  String account5DelegatedResourceKey;
  byte[] accountForDeployAddress;
  String accountForDeployKey;
  byte[] accountForAssetIssueAddress;
  String accountForAssetIssueKey;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = true)
  public void test1DelegateResourceForBandwidthAndEnergy() {
    //Create account013
    ECKey ecKey1 = new ECKey(Utils.getRandom());
    account013Address = ecKey1.getAddress();
    testKeyForAccount013 = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    //Create receiver
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    receiverDelegateAddress = ecKey2.getAddress();
    receiverDelegateKey = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
    //Create Empty account
    ECKey ecKey3 = new ECKey(Utils.getRandom());
    emptyAddress = ecKey3.getAddress();
    emptyKey = ByteArray.toHexString(ecKey3.getPrivKeyBytes());

    //sendcoin to Account013
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(account013Address,
        10000000000L, fromAddress, testKey002, blockingStubFull));
    //sendcoin to receiver
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(receiverDelegateAddress,
        10000000000L, toAddress, testKey003, blockingStubFull));

    //getAccountResource account013
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    AccountResourceMessage account013Resource = PublicMethedForDailybuild
        .getAccountResource(account013Address, blockingStubFull);
    logger.info("013 energy limit is " + account013Resource.getEnergyLimit());
    logger.info("013 net limit is " + account013Resource.getNetLimit());
    //getAccountResource receiver
    AccountResourceMessage receiverResource = PublicMethedForDailybuild
        .getAccountResource(receiverDelegateAddress, blockingStubFull);
    logger.info("receiver energy limit is " + receiverResource.getEnergyLimit());
    logger.info("receiver net limit is " + receiverResource.getNetLimit());
    Protocol.Account account013infoBefore =
        PublicMethedForDailybuild.queryAccount(account013Address, blockingStubFull);
    //get resources of account013 before DelegateResource
    account013BeforeBalance = account013infoBefore.getBalance();
    AccountResourceMessage account013ResBefore = PublicMethedForDailybuild
        .getAccountResource(account013Address, blockingStubFull);
    final long account013BeforeBandWidth = account013ResBefore.getNetLimit();
    AccountResourceMessage receiverResourceBefore = PublicMethedForDailybuild
        .getAccountResource(receiverDelegateAddress, blockingStubFull);
    long receiverBeforeBandWidth = receiverResourceBefore.getNetLimit();
    //Account013 DelegateResource for BandWidth to receiver
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account013Address, freezeAmount, freezeDuration, 0,
        ByteString.copyFrom(receiverDelegateAddress), testKeyForAccount013, blockingStubFull));
    Protocol.Account account013infoAfter =
        PublicMethedForDailybuild.queryAccount(account013Address, blockingStubFull);
    //get balance of account013 after DelegateResource
    long account013AfterBalance = account013infoAfter.getBalance();
    AccountResourceMessage account013ResAfter = PublicMethedForDailybuild
        .getAccountResource(account013Address, blockingStubFull);
    //get BandWidth of account013 after DelegateResource
    long account013AfterBandWidth = account013ResAfter.getNetLimit();
    AccountResourceMessage receiverResourceAfter = PublicMethedForDailybuild
        .getAccountResource(receiverDelegateAddress, blockingStubFull);
    //Bandwidth of receiver after DelegateResource
    long receiverAfterBandWidth = receiverResourceAfter.getNetLimit();
    //Balance of Account013 reduced amount same as DelegateResource
    Assert.assertTrue(account013BeforeBalance == account013AfterBalance + freezeAmount);
    //Bandwidth of account013 is equally before and after DelegateResource
    Assert.assertTrue(account013AfterBandWidth == account013BeforeBandWidth);
    //Bandwidth of receiver after DelegateResource is greater than before
    Assert.assertTrue(receiverAfterBandWidth > receiverBeforeBandWidth);
    Protocol.Account account013Before1 =
        PublicMethedForDailybuild.queryAccount(account013Address, blockingStubFull);
    //balance of account013 before DelegateResource
    long account013BeforeBalance1 = account013Before1.getBalance();
    AccountResourceMessage account013ResBefore1 = PublicMethedForDailybuild
        .getAccountResource(account013Address, blockingStubFull);
    //Energy of account013 before DelegateResource
    long account013BeforeEnergy = account013ResBefore1.getEnergyLimit();
    AccountResourceMessage receiverResourceBefore1 = PublicMethedForDailybuild
        .getAccountResource(receiverDelegateAddress, blockingStubFull);
    //Energy of receiver before DelegateResource
    long receiverBeforeEnergy = receiverResourceBefore1.getEnergyLimit();
    //Account013 DelegateResource Energy to receiver
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account013Address, freezeAmount, freezeDuration, 1,
        ByteString.copyFrom(receiverDelegateAddress), testKeyForAccount013, blockingStubFull));
    Protocol.Account account013infoAfter1 =
        PublicMethedForDailybuild.queryAccount(account013Address, blockingStubFull);
    //balance of account013 after DelegateResource
    long account013AfterBalance1 = account013infoAfter1.getBalance();
    AccountResourceMessage account013ResAfter1 = PublicMethedForDailybuild
        .getAccountResource(account013Address, blockingStubFull);
    long account013AfterEnergy = account013ResAfter1.getEnergyLimit();
    //Energy of account013 after DelegateResource
    AccountResourceMessage receiverResourceAfter1 = PublicMethedForDailybuild
        .getAccountResource(receiverDelegateAddress, blockingStubFull);
    //Energy of receiver after DelegateResource
    long receiverAfterEnergy = receiverResourceAfter1.getEnergyLimit();
    //Balance of Account013 reduced amount same as DelegateResource
    Assert.assertTrue(account013BeforeBalance1 == account013AfterBalance1 + freezeAmount);
    //Bandwidth of account013 is equally before and after DelegateResource
    Assert.assertTrue(account013AfterEnergy == account013BeforeEnergy);
    //Bandwidth of receiver after DelegateResource is greater than before
    Assert.assertTrue(receiverAfterEnergy > receiverBeforeEnergy);
    //account013 DelegateResource to Empty failed
    Assert.assertFalse(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account013Address, freezeAmount, freezeDuration, 0,
        ByteString.copyFrom(emptyAddress), testKeyForAccount013, blockingStubFull));
    //account013 DelegateResource to account013 failed
    Assert.assertFalse(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account013Address, freezeAmount, freezeDuration, 0,
        ByteString.copyFrom(account013Address), testKeyForAccount013, blockingStubFull));
    account013Resource = PublicMethedForDailybuild
        .getAccountResource(account013Address, blockingStubFull);
    logger.info("After 013 energy limit is " + account013Resource.getEnergyLimit());
    logger.info("After 013 net limit is " + account013Resource.getNetLimit());

    receiverResource = PublicMethedForDailybuild
        .getAccountResource(receiverDelegateAddress, blockingStubFull);
    logger.info("After receiver energy limit is " + receiverResource.getEnergyLimit());
    logger.info("After receiver net limit is " + receiverResource.getNetLimit());
  }

  @Test(enabled = true)
  public void test2getDelegatedResourceAndDelegateResourceAccountIndex() {
    //Create Account4
    ECKey ecKey4 = new ECKey(Utils.getRandom());
    account4DelegatedResourceAddress = ecKey4.getAddress();
    account4DelegatedResourceKey = ByteArray.toHexString(ecKey4.getPrivKeyBytes());
    //Create Account5
    ECKey ecKey5 = new ECKey(Utils.getRandom());
    account5DelegatedResourceAddress = ecKey5.getAddress();
    account5DelegatedResourceKey = ByteArray.toHexString(ecKey5.getPrivKeyBytes());

    //sendcoin to Account4
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(account4DelegatedResourceAddress,
        10000000000L, fromAddress, testKey002, blockingStubFull));

    //sendcoin to Account5
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(account5DelegatedResourceAddress,
        20000000000L, toAddress, testKey003, blockingStubFull));

    Protocol.Account account4infoBefore =
        PublicMethedForDailybuild.queryAccount(account4DelegatedResourceAddress, blockingStubFull);
    //Balance of Account4 before DelegateResource
    long account4BeforeBalance = account4infoBefore.getBalance();
    //account013 DelegateResource of bandwidth to Account4
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account013Address, freezeAmount, freezeDuration, 0, ByteString.copyFrom(
            account4DelegatedResourceAddress), testKeyForAccount013,
        blockingStubFull));
    //Account4 DelegateResource of energy to Account5
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account4DelegatedResourceAddress, freezeAmount, freezeDuration, 1, ByteString.copyFrom(
            account5DelegatedResourceAddress), account4DelegatedResourceKey,
        blockingStubFull));
    //check DelegatedResourceList，from:account013 to:Account4
    Optional<GrpcAPI.DelegatedResourceList> delegatedResourceResult1 =
        PublicMethedForDailybuild.getDelegatedResource(
            account013Address, account4DelegatedResourceAddress, blockingStubFull);
    long afterFreezeBandwidth =
        delegatedResourceResult1.get().getDelegatedResource(0).getFrozenBalanceForBandwidth();
    //check DelegatedResourceList，from:Account4 to:Account5
    Optional<GrpcAPI.DelegatedResourceList> delegatedResourceResult2 =
        PublicMethedForDailybuild.getDelegatedResource(account4DelegatedResourceAddress,
            account5DelegatedResourceAddress, blockingStubFull);
    long afterFreezeEnergy =
        delegatedResourceResult2.get().getDelegatedResource(0).getFrozenBalanceForEnergy();
    //FrozenBalanceForBandwidth > 0
    Assert.assertTrue(afterFreezeBandwidth > 0);
    //FrozenBalanceForEnergy > 0
    Assert.assertTrue(afterFreezeEnergy > 0);

    //check DelegatedResourceAccountIndex for Account4
    Optional<Protocol.DelegatedResourceAccountIndex> delegatedResourceIndexResult1 =
        PublicMethedForDailybuild.getDelegatedResourceAccountIndex(
            account4DelegatedResourceAddress, blockingStubFull);
    //result of From list, first Address is same as account013 address
    Assert.assertTrue(new String(account013Address).equals(new String(
        delegatedResourceIndexResult1.get().getFromAccounts(0).toByteArray())));
    //result of To list, first Address is same as Account5 address
    Assert.assertTrue(new String(account5DelegatedResourceAddress).equals(
        new String(delegatedResourceIndexResult1.get().getToAccounts(0).toByteArray())));

    //unfreezebalance of bandwidth from Account013 to Account4
    Assert.assertTrue(
        PublicMethedForDailybuild.unFreezeBalance(account013Address, testKeyForAccount013,
            0, account4DelegatedResourceAddress, blockingStubFull));
    //check DelegatedResourceAccountIndex of Account4
    Optional<Protocol.DelegatedResourceAccountIndex> delegatedResourceIndexResult1AfterUnfreeze =
        PublicMethedForDailybuild.getDelegatedResourceAccountIndex(
            account4DelegatedResourceAddress, blockingStubFull);
    //result of From list is empty
    Assert.assertTrue(delegatedResourceIndexResult1AfterUnfreeze.get()
        .getFromAccountsList().isEmpty());
    Assert.assertFalse(delegatedResourceIndexResult1AfterUnfreeze.get()
        .getToAccountsList().isEmpty());
    //Balance of Account013 after unfreezeBalance
    // (013 -> receiver(bandwidth), 013 -> receiver(Energy), 013 -> Account4(bandwidth))
    Assert.assertTrue(PublicMethedForDailybuild.queryAccount(account013Address, blockingStubFull)
        .getBalance() == account013BeforeBalance - 2 * freezeAmount);
    //bandwidth from Account013 to  Account4 gone
    Assert.assertTrue(PublicMethedForDailybuild.getAccountResource(account4DelegatedResourceAddress,
        blockingStubFull).getNetLimit() == 0);

    //unfreezebalance of Energy from Account4 to Account5
    Assert.assertTrue(PublicMethedForDailybuild.unFreezeBalance(
        account4DelegatedResourceAddress, account4DelegatedResourceKey,
        1, account5DelegatedResourceAddress, blockingStubFull));
    Protocol.Account account4infoAfterUnfreezeEnergy =
        PublicMethedForDailybuild.queryAccount(account4DelegatedResourceAddress, blockingStubFull);
    //balance of Account4 after unfreezebalance
    long account4BalanceAfterUnfreezeEnergy = account4infoAfterUnfreezeEnergy.getBalance();
    //balance of Account4 is same as before
    Assert.assertTrue(account4BeforeBalance == account4BalanceAfterUnfreezeEnergy);
    //Energy from Account4 to  Account5 gone
    Assert.assertTrue(PublicMethedForDailybuild.getAccountResource(
        account5DelegatedResourceAddress, blockingStubFull).getEnergyLimit() == 0);

    //Unfreezebalance of Bandwidth from Account4 to Account5 fail
    Assert.assertFalse(PublicMethedForDailybuild.unFreezeBalance(account4DelegatedResourceAddress,
        account4DelegatedResourceKey, 0, account5DelegatedResourceAddress, blockingStubFull));
  }

//  @Test(enabled = true)
//  public void test3PrepareToken() {
//    //Create Account7
//    ECKey ecKey7 = new ECKey(Utils.getRandom());
//    accountForAssetIssueAddress = ecKey7.getAddress();
//    accountForAssetIssueKey = ByteArray.toHexString(ecKey7.getPrivKeyBytes());
//    //sendcoin to Account7
//    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(accountForAssetIssueAddress,
//        10000000000L, toAddress, testKey003, blockingStubFull));
//    //account013 DelegateResource of bandwidth to accountForAssetIssue
//    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(
//        account013Address, 1000000000L, freezeDuration, 0,
//        ByteString.copyFrom(accountForAssetIssueAddress),
//        testKeyForAccount013, blockingStubFull));
//    //accountForAssetIssue AssetIssue
//    long now = System.currentTimeMillis();
//    String name = "testAccount013_" + Long.toString(now);
//    long totalSupply = 100000000000L;
//    String description = "zfbnb";
//    String url = "aaa.com";
//    Assert.assertTrue(PublicMethedForDailybuild.createAssetIssue(accountForAssetIssueAddress,
//        name, totalSupply, 1, 1, System.currentTimeMillis() + 2000,
//        System.currentTimeMillis() + 1000000000, 1, description, url,
//        2000L, 2000L, 500L, 1L,
//        accountForAssetIssueKey, blockingStubFull));
//
//  }
//
//  @Test(enabled = true)
//  public void test4DelegateResourceAboutTransferAsset() {
//    //Wait for 3s
//    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
//    //get AssetIssue Id
//    Protocol.Account getAssetIdFromThisAccount;
//    getAssetIdFromThisAccount = PublicMethedForDailybuild.queryAccount(
//        accountForAssetIssueAddress, blockingStubFull);
//    ByteString assetAccountId = getAssetIdFromThisAccount.getAssetIssuedID();
//    //Account5 Participate AssetIssue
//    Assert.assertTrue(PublicMethedForDailybuild.participateAssetIssue(
//        accountForAssetIssueAddress, assetAccountId.toByteArray(), 1000000,
//        account5DelegatedResourceAddress, account5DelegatedResourceKey, blockingStubFull));
//    //get account013，accountForAssetIssue，Account5 account resources before transferAssets
//    final long account013CurrentBandwidth = PublicMethedForDailybuild.getAccountResource(
//        account013Address, blockingStubFull).getNetUsed();
//    long accountForAssetIssueCurrentBandwidth = PublicMethedForDailybuild.getAccountResource(
//        accountForAssetIssueAddress, blockingStubFull).getNetUsed();
//    final long account5CurrentBandwidth = PublicMethedForDailybuild.getAccountResource(
//        account5DelegatedResourceAddress, blockingStubFull).getNetUsed();
//    //Account5 transfer Assets receiver
//    Assert.assertTrue(PublicMethedForDailybuild.transferAsset(receiverDelegateAddress,
//        assetAccountId.toByteArray(), 100000, account5DelegatedResourceAddress,
//        account5DelegatedResourceKey, blockingStubFull));
//
//    PublicMethedForDailybuild.printAddress(accountForAssetIssueKey);
//    PublicMethedForDailybuild.printAddress(account5DelegatedResourceKey);
//
//    //get account013，accountForAssetIssue，Account5 resource after transferAsset
//    final long account013CurrentBandwidthAfterTrans = PublicMethedForDailybuild.getAccountResource(
//        account013Address, blockingStubFull).getNetUsed();
//    final long accountForAssetIssueCurrentBandwidthAfterTrans = PublicMethedForDailybuild.getAccountResource(
//        accountForAssetIssueAddress, blockingStubFull).getFreeNetUsed();
//    final long account5CurrentBandwidthAfterTrans = PublicMethedForDailybuild.getAccountResource(
//        account5DelegatedResourceAddress, blockingStubFull).getNetUsed();
//    AccountResourceMessage account5ResourceAfterTrans = PublicMethedForDailybuild.getAccountResource(
//        account5DelegatedResourceAddress, blockingStubFull);
//
//    String result = "";
//    if (account5ResourceAfterTrans.getAssetNetLimitCount() > 0) {
//      logger.info("getAssetNetLimitCount > 0 ");
//      for (String name1 : account5ResourceAfterTrans.getAssetNetLimitMap().keySet()) {
//        logger.info(name1);
//        result += account5ResourceAfterTrans.getAssetNetUsedMap().get(name1);
//
//      }
//    }
//    logger.info(result);
//    PublicMethedForDailybuild.printAddress(receiverDelegateKey);
//    PublicMethedForDailybuild.printAddress(account5DelegatedResourceKey);
//    long account5FreeAssetNetUsed = accountForAssetIssueCurrentBandwidthAfterTrans;
//
//    //check resource diff
//    Assert.assertTrue(Long.parseLong(result) > 0);
//    Assert.assertTrue(account013CurrentBandwidth == account013CurrentBandwidthAfterTrans);
//    Assert.assertTrue(account5CurrentBandwidth == account5CurrentBandwidthAfterTrans);
//  }

  @Test(enabled = true)
  public void test5CanNotDelegateResourceToContract() {
    //Create Account6
    ECKey ecKey6 = new ECKey(Utils.getRandom());
    accountForDeployAddress = ecKey6.getAddress();
    accountForDeployKey = ByteArray.toHexString(ecKey6.getPrivKeyBytes());
    //PublicMethedForDailybuild.printAddress(accountForDeployKey);
    //sendcoin to Account6
    Assert.assertTrue(PublicMethedForDailybuild.sendcoin(accountForDeployAddress,
        10000000000L, fromAddress, testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    //deploy contract under Account6
    Integer consumeUserResourcePercent = 0;
    Long maxFeeLimit = Configuration.getByPath("testng.conf")
        .getLong("defaultParameter.maxFeeLimit");
    String contractName = "TestSStore";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_WalletTestAccount013");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_WalletTestAccount013");

    logger.info("TestSStore");
    final byte[] contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "",
            maxFeeLimit, 0L, consumeUserResourcePercent, null, accountForDeployKey,
            accountForDeployAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    //Account4 DelegatedResource of Energy to Contract
    //After 3.6 can not delegate resource to contract
    Assert.assertFalse(PublicMethedForDailybuild.freezeBalanceForReceiver(
        account4DelegatedResourceAddress, freezeAmount, freezeDuration, 1,
        ByteString.copyFrom(contractAddress), account4DelegatedResourceKey, blockingStubFull));

    //Account4 DelegatedResource Energy to deploy
//    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceForReceiver(
//        account4DelegatedResourceAddress, freezeAmount, freezeDuration, 1,
//        ByteString.copyFrom(accountForDeployAddress),
//        account4DelegatedResourceKey, blockingStubFull));
//
//    //get Energy of Account013，Account4，Contract before trigger contract
//    final long account013CurrentEnergyUsed = PublicMethedForDailybuild.getAccountResource(
//        account013Address, blockingStubFull).getEnergyUsed();
//    final long account013CurrentBandwidthUsed = PublicMethedForDailybuild.getAccountResource(
//        account013Address, blockingStubFull).getFreeNetUsed();
//    final long account4CurrentEnergyUsed = PublicMethedForDailybuild.getAccountResource(
//        account4DelegatedResourceAddress, blockingStubFull).getEnergyUsed();
//    final long contractCurrentEnergyUsed = PublicMethedForDailybuild.getAccountResource(
//        contractAddress, blockingStubFull).getEnergyUsed();
//    final long deployCurrentEnergyUsed = PublicMethedForDailybuild.getAccountResource(
//        accountForDeployAddress, blockingStubFull).getEnergyUsed();
//
//    //Account013 trigger contract
//    String txid = PublicMethedForDailybuild.triggerContract(contractAddress,
//        "add2(uint256)", "1", false,
//        0, 1000000000L, "0", 0, account013Address, testKeyForAccount013, blockingStubFull);
//    logger.info(txid);
//    infoById = PublicMethedForDailybuild.getTransactionInfoById(txid, blockingStubFull);
//    logger.info(String.valueOf(infoById.get().getResultValue()));
//    Assert.assertTrue(infoById.get().getResultValue() == 0);
//    //get transaction info of Energy used and Bandwidth used
//    final long contractTriggerEnergyUsed = infoById.get().getReceipt().getOriginEnergyUsage();
//    final long contractTriggerBandwidthUsed = infoById.get().getReceipt().getNetUsage();
//
//    //get Energy of Account013，Account4，Contract after trigger contract
//    final long account013CurrentEnergyUsedAfterTrig = PublicMethedForDailybuild.getAccountResource(
//        account013Address, blockingStubFull).getEnergyUsed();
//    final long account013CurrentBandwidthUsedAfterTrig = PublicMethedForDailybuild.getAccountResource(
//        account013Address, blockingStubFull).getFreeNetUsed();
//    final long account4CurrentEnergyUsedAfterTrig = PublicMethedForDailybuild.getAccountResource(
//        account4DelegatedResourceAddress, blockingStubFull).getEnergyUsed();
//    final long contractCurrentEnergyUsedAfterTrig = PublicMethedForDailybuild.getAccountResource(
//        contractAddress, blockingStubFull).getEnergyUsed();
//    final long deployCurrentEnergyUsedAfterTrig = PublicMethedForDailybuild.getAccountResource(
//        accountForDeployAddress, blockingStubFull).getEnergyUsed();
//    //compare energy changed
//    Assert.assertTrue(account013CurrentEnergyUsed == account013CurrentEnergyUsedAfterTrig);
//    Assert.assertTrue(account4CurrentEnergyUsed == account4CurrentEnergyUsedAfterTrig);
//    Assert.assertTrue(contractCurrentEnergyUsed == contractCurrentEnergyUsedAfterTrig);
//    Assert.assertTrue(deployCurrentEnergyUsed
//        == deployCurrentEnergyUsedAfterTrig - contractTriggerEnergyUsed);
//    //compare bandwidth of Account013 before and after trigger contract
//    Assert.assertTrue(account013CurrentBandwidthUsed
//        == account013CurrentBandwidthUsedAfterTrig - contractTriggerBandwidthUsed);

  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}