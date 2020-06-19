package stest.tron.wallet.dailybuild.trctoken;

import com.google.protobuf.ByteString;
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
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractTrcToken050 {

  private static final long TotalSupply = 10000000L;
  private static final long now = System.currentTimeMillis();
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static ByteString assetAccountId = null;
  private final String testKey002 = Configuration.getByPath("testng.conf")
          .getString("foundationAccount.key2");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);
  private final String tokenOwnerKey = Configuration.getByPath("testng.conf")
          .getString("tokenFoundationAccount.slideTokenOwnerKey");
  private final byte[] tokenOnwerAddress = PublicMethedForDailybuild.getFinalAddress(tokenOwnerKey);
  private final String tokenId = Configuration.getByPath("testng.conf")
          .getString("tokenFoundationAccount.slideTokenId");
  String description = Configuration.getByPath("testng.conf")
          .getString("defaultParameter.assetDescription");
  String url = Configuration.getByPath("testng.conf")
          .getString("defaultParameter.assetUrl");
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] dev001Address = ecKey1.getAddress();
  String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] user001Address = ecKey2.getAddress();
  String user001Key = ByteArray.toHexString(ecKey2.getPrivKeyBytes());
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
          .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
          .getStringList("fullnode.ip.list").get(1);
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

    channelFull = ManagedChannelBuilder.forTarget(fullnode)
            .usePlaintext(true)
            .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    assetAccountId = ByteString.copyFromUtf8(tokenId);
    Assert.assertTrue(
            PublicMethedForDailybuild.transferAsset(dev001Address, assetAccountId.toByteArray(),
                    10000000L, tokenOnwerAddress, tokenOwnerKey, blockingStubFull));

  }


  @Test(enabled = true, description = "TransferToken to contract address ")
  public void deployTransferTokenContract() {

    Assert
            .assertTrue(PublicMethedForDailybuild.sendcoin(dev001Address, 2048000000, fromAddress,
                    testKey002, blockingStubFull));
    Assert
            .assertTrue(PublicMethedForDailybuild.sendcoin(user001Address, 4048000000L, fromAddress,
                    testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // freeze balance
    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(dev001Address, 204800000,
            0, 1, dev001Key, blockingStubFull));

    Assert.assertTrue(PublicMethedForDailybuild.freezeBalanceGetEnergy(user001Address, 2048000000,
            0, 1, user001Key, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // devAddress transfer token to A
    PublicMethedForDailybuild
            .transferAsset(user001Address, assetAccountId.toByteArray(), 101, dev001Address,
                    dev001Key, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    // deploy transferTokenContract

    String filePath = "./src/test/resources/soliditycode/contractTrcToken050.sol";
    String contractName = "tokenTest";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    byte[] transferTokenContractAddress;
    String txid = PublicMethedForDailybuild
            .deployContractAndGetTransactionInfoById(contractName, abi, code, "", maxFeeLimit,
                    0L, 100, 10000, assetAccountId.toStringUtf8(),
                    0, null, dev001Key, dev001Address,
                    blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
            .getTransactionInfoById(txid, blockingStubFull);
    transferTokenContractAddress = infoById.get().getContractAddress().toByteArray();
    logger.info("Deploy energytotal is " + infoById.get().getReceipt().getEnergyUsageTotal());

    // devAddress transfer token to userAddress
    PublicMethedForDailybuild
            .transferAsset(transferTokenContractAddress, assetAccountId.toByteArray(), 100,
                    user001Address,
                    user001Key,
                    blockingStubFull);

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Account info;
    AccountResourceMessage resourceInfo = PublicMethedForDailybuild
            .getAccountResource(user001Address,
                    blockingStubFull);
    info = PublicMethedForDailybuild.queryAccount(user001Address, blockingStubFull);
    Long beforeBalance = info.getBalance();
    Long beforeEnergyUsed = resourceInfo.getEnergyUsed();
    Long beforeNetUsed = resourceInfo.getNetUsed();
    Long beforeFreeNetUsed = resourceInfo.getFreeNetUsed();
    Long beforeAssetIssueCount = PublicMethedForDailybuild
            .getAssetIssueValue(user001Address, assetAccountId,
                    blockingStubFull);
    Long beforeAssetIssueContractAddress = PublicMethedForDailybuild
            .getAssetIssueValue(transferTokenContractAddress,
                    assetAccountId,
                    blockingStubFull);
    final Long beforeAssetIssueDev = PublicMethedForDailybuild
            .getAssetIssueValue(dev001Address, assetAccountId,
                    blockingStubFull);
    logger.info("beforeBalance:" + beforeBalance);
    logger.info("beforeEnergyUsed:" + beforeEnergyUsed);
    logger.info("beforeNetUsed:" + beforeNetUsed);
    logger.info("beforeFreeNetUsed:" + beforeFreeNetUsed);
    logger.info("beforeAssetIssueCount:" + beforeAssetIssueCount);
    logger.info("beforeAssetIssueContractAddress:" + beforeAssetIssueContractAddress);
    logger.info("beforeAssetIssueDev:" + beforeAssetIssueDev);

    // user trigger A to transfer token to B
    String param =
            "\"" + Base58.encode58Check(transferTokenContractAddress) + "\",\"" + assetAccountId
                    .toStringUtf8()
                    + "\",\"1\"";

    final String triggerTxid = PublicMethedForDailybuild
            .triggerContract(transferTokenContractAddress,
                    "TransferTokenTo(address,trcToken,uint256)",
                    param, false, 0, 100000000L, "0",
                    0, user001Address, user001Key,
                    blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Account infoafter = PublicMethedForDailybuild.queryAccount(user001Address, blockingStubFull);
    AccountResourceMessage resourceInfoafter = PublicMethedForDailybuild
            .getAccountResource(user001Address,
                    blockingStubFull);
    Long afterBalance = infoafter.getBalance();
    Long afterEnergyUsed = resourceInfoafter.getEnergyUsed();
    Long afterAssetIssueCount = PublicMethedForDailybuild
            .getAssetIssueValue(user001Address, assetAccountId,
                    blockingStubFull);
    Long afterNetUsed = resourceInfoafter.getNetUsed();
    Long afterFreeNetUsed = resourceInfoafter.getFreeNetUsed();
    Long afterAssetIssueContractAddress = PublicMethedForDailybuild
            .getAssetIssueValue(transferTokenContractAddress,
                    assetAccountId,
                    blockingStubFull);
    final Long afterAssetIssueDev = PublicMethedForDailybuild
            .getAssetIssueValue(dev001Address, assetAccountId,
                    blockingStubFull);
    logger.info("afterBalance:" + afterBalance);
    logger.info("afterEnergyUsed:" + afterEnergyUsed);
    logger.info("afterNetUsed:" + afterNetUsed);
    logger.info("afterFreeNetUsed:" + afterFreeNetUsed);
    logger.info("afterAssetIssueCount:" + afterAssetIssueCount);
    logger.info("afterAssetIssueContractAddress:" + afterAssetIssueContractAddress);
    logger.info("afterAssetIssueDev:" + afterAssetIssueDev);

    infoById = PublicMethedForDailybuild.getTransactionInfoById(triggerTxid, blockingStubFull);
    logger.info("Deploy energytotal is " + infoById.get().getReceipt().getEnergyUsageTotal());

    Assert.assertTrue(infoById.get().getResultValue() == 1);
    Assert.assertEquals(beforeAssetIssueCount, afterAssetIssueCount);
    Assert.assertTrue(beforeAssetIssueContractAddress == afterAssetIssueContractAddress);
    Assert.assertEquals(beforeAssetIssueDev, afterAssetIssueDev);

    PublicMethedForDailybuild.unFreezeBalance(dev001Address, dev001Key, 1,
            dev001Address, blockingStubFull);
    PublicMethedForDailybuild.unFreezeBalance(user001Address, user001Key, 1,
            user001Address, blockingStubFull);
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


