package stest.tron.wallet.dailybuild.depositWithdraw;

import static org.tron.api.GrpcAPI.Return.response_code.CONTRACT_VALIDATE_ERROR;
import static org.tron.protos.Protocol.TransactionInfo.code.FAILED;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
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
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class DepositTrc10001 {

  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String testKeyFordeposit = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key1");
  private final byte[] depositAddress = PublicMethed.getFinalAddress(testKeyFordeposit);
  private final String testKeyFordeposit2 = Configuration.getByPath("testng.conf")
      .getString("mainNetAssetAccountKey.key2");
  private final byte[] depositAddress2 = PublicMethed.getFinalAddress(testKeyFordeposit2);
  String mainChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainChainAddressKey = WalletClient.decodeFromBase58Check(mainChainAddress);
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  ByteString assetAccountId = null;
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);

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
    PublicMethed.printAddress(testKeyFordeposit2);

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

  }

  @Test(enabled = true, description = "Depositor account is not exit")
  public void testDepositTrc10001() {

    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    String methodStr = "depositTRC10()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = 2;
    ECKey ecKey3 = new ECKey(Utils.getRandom());
    byte[] depositAddress3 = ecKey3.getAddress();
    String testKeyFordeposit3 = ByteArray.toHexString(ecKey3.getPrivKeyBytes());

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress3, testKeyFordeposit3,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : account not exists",
        response.getMessage().toStringUtf8());
  }

  @Test(enabled = true, description = "Depositor with tokenId exception")
  public void testDepositTrc10002() {

//System has not tokenId
    String inputTokenID = "" + 234000058;
    long inputTokenValue = 2;
    String inputParam = inputTokenID + "," + inputTokenValue;

    String methodStr = "depositTRC10(uint64,uint64)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : No asset !",
        response.getMessage().toStringUtf8());

//Depositor has not tokenId

    String inputTokenID1 = "" + 1000001;

    long inputTokenValue1 = 2;
    String inputParam1 = inputTokenID1 + "," + inputTokenValue1;

    String methodStr1 = "depositTRC10(uint64,uint64)";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, inputParam1, false));

    Return response1 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input1,
            maxFeeLimit, inputTokenValue1, inputTokenID1, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response1.getCode());
    Assert.assertEquals("contract validate error : assetBalance must greater than 0.",
        response1.getMessage().toStringUtf8());

//TokenId is 1000000
    String inputTokenID2 = "" + 1000000;
    long inputTokenValue2 = 2;
    String inputParam2 = inputTokenID2 + "," + inputTokenValue2;

    String methodStr2 = "depositTRC10(uint64,uint64)";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, inputParam2, false));

    Return response2 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input2,
            maxFeeLimit, inputTokenValue2, inputTokenID2, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : tokenId must be > 1000000",
        response2.getMessage().toStringUtf8());

//TokenId is 0

    String inputTokenID3 = "" + 0;
    long inputTokenValue3 = 2;
    String inputParam3 = inputTokenID3 + "," + inputTokenValue3;

    String methodStr3 = "depositTRC10(uint64,uint64)";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, inputParam3, false));

    Return response3 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input3,
            maxFeeLimit, inputTokenValue3, inputTokenID3, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response3.getCode());
    Assert.assertEquals(
        "contract validate error : invalid arguments with tokenValue = 2, tokenId = 0",
        response3.getMessage().toStringUtf8());

//TokenId is -1

    String inputTokenID4 = "-1";
    long inputTokenValue4 = 2;
    String inputParam4 = inputTokenID4 + "," + inputTokenValue4;

    String methodStr4 = "depositTRC10(uint64,uint64)";
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr4, inputParam4, false));

    Return response4 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input4,
            maxFeeLimit, inputTokenValue4, inputTokenID4, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response4.getCode());
    Assert.assertEquals(
        "contract validate error : tokenId must be > 1000000",
        response4.getMessage().toStringUtf8());
  }


  @Test(enabled = true, description = "Depositor with Value exception ")
  public void testDepositTrc10003() {
//Value > balance
    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    long tokenvalue = PublicMethed
        .getAssetIssueValue(depositAddress, assetAccountId, blockingStubFull);

    String inputTokenID = assetAccountId.toStringUtf8();
    long inputTokenValue = tokenvalue + 10000000;
    String inputParam = inputTokenID + "," + inputTokenValue;

    String methodStr = "depositTRC10(uint64,uint64)";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));

    Return response = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input,
            maxFeeLimit, inputTokenValue, inputTokenID, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response.getCode());
    Assert.assertEquals("contract validate error : assetBalance is not sufficient.",
        response.getMessage().toStringUtf8());

    //Tokenvalue is 0
    assetAccountId = PublicMethed
        .queryAccount(depositAddress, blockingStubFull).getAssetIssuedID();
    logger.info("The token ID: " + assetAccountId.toStringUtf8());

    PublicMethed.waitProduceNextBlock(blockingStubFull);

    String inputTokenID1 = assetAccountId.toStringUtf8();
    long inputTokenValue1 = 0;
    String inputParam1 = inputTokenID1 + "," + inputTokenValue1;

    String methodStr1 = "depositTRC10(uint64,uint64)";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, inputParam1, false));
    String txid = PublicMethed
        .triggerContract(mainChainAddressKey, 0, input1,
            maxFeeLimit, inputTokenValue1, inputTokenID1, depositAddress, testKeyFordeposit,
            blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() != 0);
    Assert.assertEquals(FAILED, infoById.get().getResult());
    Assert.assertEquals("REVERT opcode executed",
        infoById.get().getResMessage().toStringUtf8());

    //Tokenvalue is -2

    String inputTokenID2 = assetAccountId.toStringUtf8();
    long inputTokenValue2 = -2;
    String inputParam2 = inputTokenID + "," + inputTokenValue;

    String methodStr2 = "depositTRC10(uint64,uint64)";
    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, inputParam2, false));
    Return response2 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input2,
            maxFeeLimit, inputTokenValue2, inputTokenID2, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response2.getCode());
    Assert.assertEquals("contract validate error : tokenValue must be >= 0",
        response2.getMessage().toStringUtf8());

//Tokenvalue > Long.max

    String inputTokenID3 = assetAccountId.toStringUtf8();
    Long inputTokenValue3 = Long.MAX_VALUE + 100;
    logger.info("MAX_VALUE " + Long.MAX_VALUE);
    logger.info("MAX_VALUE " + (Long.MAX_VALUE + 100));

    String inputParam3 = inputTokenID3 + "," + inputTokenValue3;

    String methodStr3 = "depositTRC10(uint64,uint64)";
    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, inputParam3, false));

    Return response3 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input3,
            maxFeeLimit, inputTokenValue3, inputTokenID3, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response3.getCode());
    Assert.assertEquals("contract validate error : tokenValue must be >= 0",
        response3.getMessage().toStringUtf8());

    //Tokenvalue < Long.min

    String inputTokenID4 = assetAccountId.toStringUtf8();
    Long inputTokenValue4 = Long.MIN_VALUE - 100;

    logger.info("MAX_VALUE " + Long.MIN_VALUE);
    logger.info("MAX_VALUE " + (Long.MIN_VALUE - 100));
    String inputParam4 = inputTokenID4 + "," + inputTokenValue4;

    String methodStr4 = "depositTRC10(uint64,uint64)";
    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr4, inputParam4, false));
    Return response4 = PublicMethed
        .triggerContractForReturn(mainChainAddressKey, 0, input4,
            maxFeeLimit, inputTokenValue4, inputTokenID4, depositAddress, testKeyFordeposit,
            blockingStubFull);
    Assert.assertEquals(CONTRACT_VALIDATE_ERROR, response4.getCode());
    Assert.assertEquals("contract validate error : assetBalance is not sufficient.",
        response4.getMessage().toStringUtf8());
  }


}
