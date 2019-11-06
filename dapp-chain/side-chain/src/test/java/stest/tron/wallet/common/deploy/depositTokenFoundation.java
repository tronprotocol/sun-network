package stest.tron.wallet.common.deploy;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.protos.Protocol.Account;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class depositTokenFoundation {

  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);
  final String tokenFoundationKey001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  final byte[] tokenFoundationAddress001 = PublicMethed.getFinalAddress(tokenFoundationKey001);
  final String tokenFoundationId001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");
  final String tokenFoundationKey002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey2");
  final byte[] tokenFoundationAddress002 = PublicMethed.getFinalAddress(tokenFoundationKey002);
  final String tokenFoundationId002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId2");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  ByteString assetAccountId;
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


  @Test(enabled = true, description = "deploy Side Chain Gateway")
  public void test1DepositTrc20001() {
    String mainChainAddress = Configuration.getByPath("testng.conf")
        .getString("gateway_address.chainIdAddress");
    /*try {
      File mainChainFile = new File("/home/mainChainGatewayAddress");
      FileReader reader = new FileReader(mainChainFile);
      BufferedReader breader = new BufferedReader(reader);
      mainChainAddress = breader.readLine();
      breader.close();
    } catch (Exception e) {
      logger.info("Read main Gateway ContractAddress Failed");
      return;
    }*/
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
