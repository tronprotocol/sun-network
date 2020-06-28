package stest.tron.wallet.dailybuild.multisign;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;
import stest.tron.wallet.common.client.utils.PublicMethedForMutiSign;

@Slf4j
public class MultiSign20 {

  private final String testKey002 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] fromAddress = PublicMethedForDailybuild.getFinalAddress(testKey002);

  private final String witnessKey001 = Configuration.getByPath("testng.conf")
      .getString("witness.key1");
  private final byte[] witnessAddress001 = PublicMethedForDailybuild.getFinalAddress(witnessKey001);
  private final String contractTronDiceAddr = "TMYcx6eoRXnePKT1jVn25ZNeMNJ6828HWk";
  private long multiSignFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.multiSignFee");
  private long updateAccountPermissionFee = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.updateAccountPermissionFee");
  private ECKey ecKey1 = new ECKey(Utils.getRandom());
  private byte[] ownerAddress = ecKey1.getAddress();
  private String ownerKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  private ECKey ecKey2 = new ECKey(Utils.getRandom());
  private byte[] normalAddr001 = ecKey2.getAddress();
  private String normalKey001 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

  private ECKey tmpEcKey01 = new ECKey(Utils.getRandom());
  private byte[] tmpAddr01 = tmpEcKey01.getAddress();
  private String tmpKey01 = ByteArray.toHexString(tmpEcKey01.getPrivKeyBytes());

  private ECKey tmpEcKey02 = new ECKey(Utils.getRandom());
  private byte[] tmpAddr02 = tmpEcKey02.getAddress();
  private String tmpKey02 = ByteArray.toHexString(tmpEcKey02.getPrivKeyBytes());

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf").getStringList("fullnode.ip.list")
      .get(0);
  private long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");


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

    channelFull = ManagedChannelBuilder.forTarget(fullnode).usePlaintext(true).build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = true, description = "Owner address is witness")
  public void testOwnerAddress01() {
    // address = witness
    ownerKey = witnessKey001;
    ownerAddress = new WalletClient(ownerKey).getAddress();
    long needCoin = updateAccountPermissionFee * 2;

    PublicMethedForDailybuild
        .freezeBalanceForReceiver(fromAddress, 50000000L, 0, 1, ByteString.copyFrom(ownerAddress),
            testKey002, blockingStubFull);
    PublicMethedForDailybuild
        .freezeBalanceForReceiver(fromAddress, 50000000L, 0, 0, ByteString.copyFrom(ownerAddress),
            testKey002, blockingStubFull);
    PublicMethedForDailybuild
        .sendcoin(ownerAddress, needCoin, fromAddress, testKey002, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Long balanceBefore = PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
        .getBalance();
    logger.info("balanceBefore: " + balanceBefore);
    List<String> ownerPermissionKeys = new ArrayList<>();

    PublicMethedForDailybuild.printAddress(ownerKey);
    ownerPermissionKeys.add(ownerKey);

    logger.info("** update owner and active permission to two address");
    String accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":1,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(testKey002)
            + "\",\"weight\":1}]},"
            + "\"witness_permission\":{\"type\":1,\"permission_name\":\"owner\","
            + "\"threshold\":1,\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(testKey002) + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey)
            + "\",\"weight\":1}" + "]}]}";

    Assert.assertTrue(PublicMethedForMutiSign
        .accountPermissionUpdate(accountPermissionJson, ownerAddress, ownerKey, blockingStubFull,
            ownerPermissionKeys.toArray(new String[ownerPermissionKeys.size()])));

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    ownerPermissionKeys.clear();
    ownerPermissionKeys.add(testKey002);

    Assert.assertEquals(1, PublicMethedForMutiSign.getActivePermissionKeyCount(
        PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
            .getActivePermissionList()));

    Assert.assertEquals(1,
        PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull).getOwnerPermission()
            .getKeysCount());

    Assert.assertEquals(1,
        PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
            .getWitnessPermission()
            .getKeysCount());

    PublicMethedForMutiSign.printPermissionList(
        PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
            .getActivePermissionList());

    System.out.printf(PublicMethedForMutiSign.printPermission(
        PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
            .getOwnerPermission()));

    System.out.printf(PublicMethedForMutiSign.printPermission(
        PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
            .getWitnessPermission()));

    logger.info("** trigger a permission transaction");
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":1,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"witness_permission\":{\"type\":1,\"permission_name\":\"witness\","
            + "\"threshold\":1,\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey) + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey)
            + "\",\"weight\":1}" + "]}]}";

    Assert.assertTrue(PublicMethedForMutiSign
        .accountPermissionUpdate(accountPermissionJson, ownerAddress, ownerKey, blockingStubFull,
            ownerPermissionKeys.toArray(new String[ownerPermissionKeys.size()])));

    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    Long balanceAfter = PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
        .getBalance();
    logger.info("balanceAfter: " + balanceAfter);
    Assert.assertEquals(balanceBefore - balanceAfter, needCoin);

  }

  @Test(enabled = true, description = "Owner address is witness with exception condition")
  public void testOwnerAddress02() {
    // address = witness, without witness permission
    ownerKey = witnessKey001;
    ownerAddress = new WalletClient(ownerKey).getAddress();
    PublicMethedForDailybuild
        .sendcoin(ownerAddress, 1_000000, fromAddress, testKey002, blockingStubFull);
    Assert.assertTrue(
        PublicMethedForDailybuild.freezeBalanceForReceiver(fromAddress, 100000000000L, 0, 0,
            ByteString.copyFrom(ownerAddress), testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Long balanceBefore = PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
        .getBalance();
    logger.info("balanceBefore: " + balanceBefore);
    List<String> ownerPermissionKeys = new ArrayList<>();

    PublicMethedForDailybuild.printAddress(ownerKey);

    ownerPermissionKeys.add(ownerKey);

    String accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":1,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey)
            + "\",\"weight\":1}" + "]}]}";

    GrpcAPI.Return response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : witness permission is missed",
        response.getMessage().toStringUtf8());

    // address = witness, without active permission
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":1,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"witness_permission\":{\"type\":1,\"permission_name\":\"witness\","
            + "\"threshold\":1,\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey) + "\",\"weight\":1}]}}";
    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : active permission is missed",
        response.getMessage().toStringUtf8());

    // address = witness, without owner permission
    accountPermissionJson = "{\"witness_permission\":{\"type\":1,\"permission_name\":\"witness\","
        + "\"threshold\":1,\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
        .getAddressString(ownerKey)
        + "\",\"weight\":1}]},"
        + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
        + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
        + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
        + "\",\"weight\":1}" + "]}]}";
    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : owner permission is missed",
        response.getMessage().toStringUtf8());
    Long balanceAfter = PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
        .getBalance();
    logger.info("balanceAfter: " + balanceAfter);
    Assert.assertEquals(balanceBefore, balanceAfter);
    PublicMethedForDailybuild
        .unFreezeBalance(fromAddress, testKey002, 0, ownerAddress, blockingStubFull);
  }

  @Test(enabled = true, description = "Owner address is normal address with exception condition")
  public void testOwnerAddress03() {
    ECKey ecKey1 = new ECKey(Utils.getRandom());
    ownerAddress = ecKey1.getAddress();
    ownerKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
    Assert.assertTrue(
        PublicMethedForDailybuild
            .sendcoin(ownerAddress, 1_000_000, fromAddress, testKey002, blockingStubFull));
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    Long balanceBefore = PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
        .getBalance();
    logger.info("balanceBefore: " + balanceBefore);
    PublicMethedForDailybuild.printAddress(ownerKey);
    PublicMethedForDailybuild.printAddress(tmpKey02);
    List<String> ownerPermissionKeys = new ArrayList<>();

    PublicMethedForDailybuild.printAddress(ownerKey);

    ownerPermissionKeys.add(ownerKey);

    // address = normal address, with witness permission
    String accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":1,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"witness_permission\":{\"type\":1,\"permission_name\":\"witness\","
            + "\"threshold\":1,\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey) + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey)
            + "\",\"weight\":1}" + "]}]}";

    GrpcAPI.Return response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals(
        "contract validate error : account isn't witness can't set" + " witness permission",
        response.getMessage().toStringUtf8());

    // address = normal address, without owner permission
    accountPermissionJson =
        "{\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(ownerKey)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : owner permission is missed",
        response.getMessage().toStringUtf8());

    // address = normal address, without active permission
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":1,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]}}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : active permission is missed",
        response.getMessage().toStringUtf8());

    // address = contract address
    byte[] ownerAddress02 = contractTronDiceAddr.getBytes();
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";
    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddress02, ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : invalidate ownerAddress",
        response.getMessage().toStringUtf8());

    // address = not active address
    ECKey ecKeyTmp = new ECKey(Utils.getRandom());
    final byte[] ownerAddressTmp = ecKeyTmp.getAddress();
    final String ownerKeyTmp = ByteArray.toHexString(ecKeyTmp.getPrivKeyBytes());
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);

    PublicMethedForDailybuild.printAddress(ownerKey);
    PublicMethedForDailybuild.printAddress(tmpKey02);

    ownerPermissionKeys.add(ownerKey);

    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, ownerAddressTmp, ownerKeyTmp,
            blockingStubFull);
    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : ownerAddress account does not exist",
        response.getMessage().toStringUtf8());

    // address = not exist
    String fakeAddress = "THph9K2M2nLvkianrMGswRhz5hjSA9fuH1";
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, fakeAddress.getBytes(), ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : invalidate ownerAddress",
        response.getMessage().toStringUtf8());

    // address = long address
    fakeAddress = "TR3FAbhiSeP7kSh39RjGYpwCqfMDHPMhX4d121";

    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, fakeAddress.getBytes(), ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : invalidate ownerAddress",
        response.getMessage().toStringUtf8());

    // address = short address

    fakeAddress = "THph9K2M2nLvkianrMGswRhz5hj";

    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, fakeAddress.getBytes(), ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : invalidate ownerAddress",
        response.getMessage().toStringUtf8());

    // address =
    fakeAddress = "";
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, fakeAddress.getBytes(), ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : invalidate ownerAddress",
        response.getMessage().toStringUtf8());

    // address = null
    fakeAddress = null;

    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    boolean ret = false;
    try {
      PublicMethedForDailybuild
          .accountPermissionUpdateForResponse(accountPermissionJson, fakeAddress.getBytes(),
              ownerKey, blockingStubFull);
    } catch (NullPointerException e) {
      logger.info("NullPointerException !");
      ret = true;
    }
    Assert.assertTrue(ret);

    // address = "1ab(*c"
    fakeAddress = "1ab(*c";
    accountPermissionJson =
        "{\"owner_permission\":{\"type\":0,\"permission_name\":\"owner\",\"threshold\":2,\"keys\":["
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(witnessKey001)
            + "\",\"weight\":1},"
            + "{\"address\":\"" + PublicMethedForDailybuild.getAddressString(ownerKey)
            + "\",\"weight\":1}]},"
            + "\"active_permissions\":[{\"type\":2,\"permission_name\":\"active0\",\"threshold\":1,"
            + "\"operations\":\"3f3d1ec0036001000000000000000000000000000000000000000000000000c0\","
            + "\"keys\":[" + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(witnessKey001)
            + "\",\"weight\":1}," + "{\"address\":\"" + PublicMethedForDailybuild
            .getAddressString(tmpKey02)
            + "\",\"weight\":1}" + "]}]}";

    response = PublicMethedForDailybuild
        .accountPermissionUpdateForResponse(accountPermissionJson, fakeAddress.getBytes(), ownerKey,
            blockingStubFull);

    Assert.assertFalse(response.getResult());
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : invalidate ownerAddress",
        response.getMessage().toStringUtf8());

    Long balanceAfter = PublicMethedForDailybuild.queryAccount(ownerAddress, blockingStubFull)
        .getBalance();
    logger.info("balanceAfter: " + balanceAfter);
    Assert.assertEquals(balanceBefore, balanceAfter);
  }

  @AfterMethod
  public void aftertest() {
    PublicMethedForDailybuild
        .unFreezeBalance(fromAddress, testKey002, 0, ownerAddress, blockingStubFull);
    PublicMethedForDailybuild
        .unFreezeBalance(fromAddress, testKey002, 1, ownerAddress, blockingStubFull);
    PublicMethedForDailybuild.freedResource(ownerAddress, ownerKey, fromAddress, blockingStubFull);
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
