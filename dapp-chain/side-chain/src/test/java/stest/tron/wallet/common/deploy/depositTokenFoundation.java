package stest.tron.wallet.common.deploy;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.matcher.CollectionOneToOneMatcher;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class depositTokenFoundation {

  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;
  ByteString assetAccountId;

  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);

  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");

  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.ChainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);

  final String tokenFoundationKey001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey");
  final byte[] tokenFoundationAddress001 = PublicMethed.getFinalAddress(tokenFoundationKey001);
  final String tokenFoundationId001 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId");

  final String tokenFoundationKey002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenOwnerKey2");
  final byte[] tokenFoundationAddress002 = PublicMethed.getFinalAddress(tokenFoundationKey001);
  final String tokenFoundationId002 = Configuration.getByPath("testng.conf")
      .getString("tokenFoundationAccount.slideTokenId2");

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
    while (true){
      // depositTRC10 for tokenFoundationAccount001
      String inputParam = tokenFoundationId001 + "," + inputTokenValue;
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
      String txid = PublicMethed
          .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
              0,
              input,
              maxFeeLimit, inputTokenValue, tokenFoundationId001, tokenFoundationAddress001, tokenFoundationKey001,
              blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingSideStubFull);
      Account FoundationAccount = PublicMethed.queryAccount(tokenFoundationKey001,blockingSideStubFull);
      if (FoundationAccount.getAssetIssuedID().toStringUtf8().equals(tokenFoundationId001) &&
          FoundationAccount.getAssetCount() >= inputTokenValue ){
        break;
      }else if (tryConut++ > 3){
        logger.info("deposit Token 001 FAIED!!");
        Assert.assertTrue(false);
      }
    }

    while (true){
      // depositTRC10 for tokenFoundationAccount002
      String inputParam2 = tokenFoundationId002 + "," + inputTokenValue;
      byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam2, false));
      String txid2 = PublicMethed
          .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
              0,
              input2,
              maxFeeLimit, inputTokenValue, tokenFoundationId002, tokenFoundationAddress002, tokenFoundationKey002,
              blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingSideStubFull);
      Account FoundationAccount = PublicMethed.queryAccount(tokenFoundationKey002,blockingSideStubFull);
      if (FoundationAccount.getAssetIssuedID().toStringUtf8().equals(tokenFoundationId002) &&
          FoundationAccount.getAssetCount() >= inputTokenValue ){
        break;
      }else if (tryConut++ > 3){
        logger.info("deposit Token 002 FAIED!!");
        Assert.assertTrue(false);
      }
    }

  }
}
