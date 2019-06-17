package stest.tron.wallet.common.deposit;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class DepositTrc10002 {

  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private static final long now = System.currentTimeMillis();
  private static String tokenName = "testAssetIssue_" + Long.toString(now);
  private static ByteString assetAccountId = null;
  private static final long TotalSupply = 1000L;
  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);


  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  ECKey ecKey2 = new ECKey(Utils.getRandom());
  byte[] depositAddress2 = ecKey2.getAddress();
  String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);

  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);

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
    PublicMethed.printAddress(testKeyFordeposit);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 100000_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress2, 1100_000_000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    long start = System.currentTimeMillis() + 2000;
    long end = System.currentTimeMillis() + 1000000000;

    Assert.assertTrue(PublicMethed.createAssetIssue(depositAddress2, tokenName, TotalSupply, 1,
        10000, start, end, 1, description, url, 100000L, 100000L,
        1L, 1L, testKeyFordeposit2, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true, description = "depositor account is not exit")
  public void test1DepositTrc10001() {

    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 2;
    ECKey ecKey2 = new ECKey(Utils.getRandom());
    byte[] depositAddress2 = ecKey2.getAddress();
    String testKeyFordeposit2 = ByteArray.toHexString(ecKey2.getPrivKeyBytes());

    long callValue = 1;
    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress2, testKeyFordeposit2,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : account not exists",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "tokenId not exit")
  public void test2DepositTrc10002() {

    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = "" + 234000058;
    long inputTokenValue = 2;

    long callValue = 1;
    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : No asset !",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "depositor hasnot tokenId")
  public void test3DepositTrc10003() {

    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 2;

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : Owner no asset!",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = false, description = "tokenId is null")
  public void test4DepositTrc10004() {

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long inputTokenValue = 2;
    String txid = PublicMethed
        .triggerContract(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, null, depositAddress, testKeyFordeposit,
            blockingStubFull);
    try {
      Return response = PublicMethed
          .triggerContractForReturn(mainChainAddressKey, 0, input,
              maxFeeLimit, inputTokenValue, null, depositAddress, testKeyFordeposit,
              blockingStubFull);
    } catch (NullPointerException e) {
      Assert.assertTrue(true);
    }
  }

  @Test(enabled = true, description = "tokenId is 100000")
  public void test5DepositTrc10005() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = "" + 100000;
    long inputTokenValue = 2;

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must > 1000000",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "tokenId is 0")
  public void test6DepositTrc10006() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = "0";
    long inputTokenValue = 2;

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals(
        "contract validate error : invalid arguments with tokenValue = 2, tokenId = 0",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "tokenId > Long.max")
  public void test7DepositTrc10007() {

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = "92233720368547758070";
    long inputTokenValue = 2;
    try {
      Return response = PublicMethed
          .triggerContractForReturn(mainChainAddressKey, 0, input,
              maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
              blockingStubFull);
    } catch (Exception e) {
      Assert.assertEquals("For input string: \"92233720368547758070\"", e.getMessage());
      return;
    }
    Assert.fail("no exception, check transaction");
  }

  @Test(enabled = true, description = "tokenId < Long.min")
  public void test8DepositTrc10008() {

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = "-9223372036857775708";
    logger.info(inputTokenID);
    long inputTokenValue = 2;
    try {
      Return response = PublicMethed
          .triggerContractForReturn(mainChainAddressKey, 0, input,
              maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
              blockingStubFull);
    } catch (Exception e) {
      Assert.assertEquals("For input string: \"-9223372036857775708\"", e.getMessage());
      return;
    }
    Assert.fail("no exception, check transaction");

  }

  @Test(enabled = true, description = "value > balance")
  public void test9DepositTrc10009() {

    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = 10;
    PublicMethed.transferAsset(depositAddress, assetAccountId.toByteArray(), tokenvalue,
        depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
//    long inputTokenValue = ;

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, tokenvalue + 10, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : assetBalance is not sufficient.",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = false, description = "tokenvalue is null")
  public void test10DepositTrc100010() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = 10;
    PublicMethed.transferAsset(depositAddress, assetAccountId.toByteArray(), tokenvalue,
        depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    Long inputTokenValue = null;
    try {
      Return response = PublicMethed
          .triggerContractForReturn(mainChainAddressKey, 0, input,
              maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
              blockingStubFull);
    } catch (NullPointerException e) {
      Assert.assertTrue(true);
    }
  }

  @Test(enabled = true, description = "tokenvalue is 0")
  public void test11DepositTrc100011() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = 10;
    PublicMethed.transferAsset(depositAddress, assetAccountId.toByteArray(), tokenvalue,
        depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 0;

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(0, response.getCode().getNumber());
  }

  @Test(enabled = true, description = "tokenvalue is -2")
  public void test12DepositTrc100012() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = 10;
    PublicMethed.transferAsset(depositAddress, assetAccountId.toByteArray(), tokenvalue,
        depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = -2;

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenValue must >= 0",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "tokenvalue > Long.max")
  public void test13DepositTrc100013() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = 10;
    PublicMethed.transferAsset(depositAddress, assetAccountId.toByteArray(), tokenvalue,
        depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    Long inputTokenValue = Long.MAX_VALUE + 100;
    logger.info("MAX_VALUE " + Long.MAX_VALUE);
    logger.info("MAX_VALUE " + (Long.MAX_VALUE + 100));

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenValue must >= 0",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "tokenvalue > Long.min")
  public void test14DepositTrc100014() {
    assetAccountId = PublicMethed
        .queryAccount(depositAddress2, blockingStubFull).getAssetIssuedID();
    logger.info("The token name: " + tokenName);
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = 10;
    PublicMethed.transferAsset(depositAddress, assetAccountId.toByteArray(), tokenvalue,
        depositAddress2, testKeyFordeposit2, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    Long inputTokenValue = Long.MIN_VALUE - 100;

    logger.info("MAX_VALUE " + Long.MIN_VALUE);
    logger.info("MAX_VALUE " + (Long.MIN_VALUE - 100));

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : assetBalance is not sufficient.",
        response.getMessage().toStringUtf8());
  }

}
