package stest.tron.wallet.common.deploy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol.Account;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class depositTokenFoundation {

  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final String tokenFoundationKey001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenOwnerKey");
  final byte[] tokenFoundationAddress001 = PublicMethed.getFinalAddress(tokenFoundationKey001);
  final String tokenFoundationId001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenId");
  final String tokenFoundationKey002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenOwnerKey2");
  final byte[] tokenFoundationAddress002 = PublicMethed.getFinalAddress(tokenFoundationKey002);
  final String tokenFoundationId002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.sideTokenId2");
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;
  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;
  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);

  @BeforeSuite
  public void beforeSuite() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
    String methodStr = "depositTRC10(uint64,uint64)";
    long inputTokenValue = 1000000000000L;

    int tryConut = 0;
    while (true) {
      // depositTRC10 for tokenFoundationAccount001
      String inputParam = tokenFoundationId001 + "," + inputTokenValue;
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
      String txid = PublicMethed
          .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
              0,
              input,
              maxFeeLimit, inputTokenValue, tokenFoundationId001, tokenFoundationAddress001,
              tokenFoundationKey001,
              blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingSideStubFull);
      Account FoundationAccount = PublicMethed
          .queryAccount(tokenFoundationKey001, blockingSideStubFull);
      try {
        String tokenid = FoundationAccount.getAssetV2Map().keySet().toArray()[0].toString();
        Long tokenValue = FoundationAccount.getAssetV2Map().get(tokenid);

        logger.info("FoundationAccount.getAssetIssuedID().toStringUtf8(): " + tokenid);
        logger.info("FoundationAccount.getAssetCount(): " + tokenValue);

        if (tokenValue != null && tokenValue >= inputTokenValue) {
          break;
        } else if (tryConut++ > 3) {
          logger.info("deposit Token 002 FAIED!!");
          Assert.assertTrue(false);
        }
      } catch (Exception e) {
        if (tryConut++ > 3) {
          logger.info("deposit Token 002 FAIED!!");
          Assert.assertTrue(false);
        }
      }
    }

    while (true) {
      // depositTRC10 for tokenFoundationAccount002
      String inputParam2 = tokenFoundationId002 + "," + inputTokenValue;
      byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam2, false));
      String txid2 = PublicMethed
          .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
              0,
              input2,
              maxFeeLimit, inputTokenValue, tokenFoundationId002, tokenFoundationAddress002,
              tokenFoundationKey002,
              blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingSideStubFull);

      Account FoundationAccount = PublicMethed
          .queryAccount(tokenFoundationKey002, blockingSideStubFull);
      try {
        String tokenid = FoundationAccount.getAssetV2Map().keySet().toArray()[0].toString();
        Long tokenValue = FoundationAccount.getAssetV2Map().get(tokenid);

        logger.info("FoundationAccount.getAssetIssuedID().toStringUtf8(): " + tokenid);
        logger.info("FoundationAccount.getAssetCount(): " + tokenValue);

        if (tokenValue != null && tokenValue >= inputTokenValue) {
          break;
        } else if (tryConut++ > 3) {
          logger.info("deposit Token 002 FAIED!!");
          Assert.assertTrue(false);
        }
      } catch (Exception e) {
        if (tryConut++ > 3) {
          logger.info("deposit Token 002 FAIED!!");
          Assert.assertTrue(false);
        }
      }

    }
  }


  @Test(enabled = true, description = "depositTokenFoundation")
  public void depositTokenFoundationTest() {

  }
}
