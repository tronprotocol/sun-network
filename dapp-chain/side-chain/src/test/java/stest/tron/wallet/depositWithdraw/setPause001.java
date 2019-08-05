package stest.tron.wallet.depositWithdraw;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
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
public class setPause001 {


  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key1");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelSolidity = null;

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingSideStubFull = null;

  private final String witnessA = Configuration.getByPath("testng.conf")
      .getString("witness.key1");
  private final byte[] witnessAddressA = PublicMethed.getFinalAddress(witnessA);

  private final String witnessB = Configuration.getByPath("testng.conf")
      .getString("witness.key2");
  private final byte[] witnessAddressB = PublicMethed.getFinalAddress(witnessB);
  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");


  private final String mainGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWayOwner.key1");
  private final byte[] mainGateWayOwnerAddress = PublicMethed.getFinalAddress(mainGateWayOwner);

  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWayOwner.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);

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
  }

  @Test(enabled = true, description = "setStop ")
  public void test1setStop001() {
    String parame = "false";
    byte[] input = Hex.decode(AbiUtil.parseMethod("setStop(bool)", parame, false));

    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Assert.assertEquals(0, infoById.get().getResultValue());

//    String txid1 = PublicMethed
//        .triggerContractSideChain(WalletClient.decodeFromBase58Check(sideGatewayAddress),
//            WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input, 1000000000,
//            0l, "0", sideGateWayOwnerAddress, sideGateWayOwner, blockingSideStubFull);

    String parame1 = "false";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setPause(bool)", parame1, false));

    String txid2 = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress), 0l, input1,
            1000000000,
            0l, "0", mainGateWayOwnerAddress, mainGateWayOwner, blockingStubFull);

  }

  @Test(enabled = false, description = "Fundinject ")
  public void test2Fundinject002() {

  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
