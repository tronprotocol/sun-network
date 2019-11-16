package stest.tron.wallet.depositWithdraw;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.utils.ByteArray;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class setTokenOwnerAddressTess001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);
  final String sideGatewayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddress = WalletClient.decodeFromBase58Check(sideGatewayAddress);
  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String witnessKey001 = Configuration.getByPath("testng.conf")
      .getString("witness.key1");
  //Witness 47.93.33.201
  private final String witnessKey002 = Configuration.getByPath("testng.conf")
      .getString("witness.key2");
  //Witness 123.56.10.6
  private final String witnessKey003 = Configuration.getByPath("testng.conf")
      .getString("witness.key3");
  //Wtiness 39.107.80.135
  private final String witnessKey004 = Configuration.getByPath("testng.conf")
      .getString("witness.key4");
  //Witness 47.93.184.2
  private final String witnessKey005 = Configuration.getByPath("testng.conf")
      .getString("witness.key5");
  private final byte[] witness001Address = PublicMethedForDailybuild.getFinalAddress(witnessKey001);
  private final byte[] witness002Address = PublicMethedForDailybuild.getFinalAddress(witnessKey002);
  private final byte[] witness003Address = PublicMethedForDailybuild.getFinalAddress(witnessKey003);
  private final byte[] witness004Address = PublicMethedForDailybuild.getFinalAddress(witnessKey004);
  private final byte[] witness005Address = PublicMethedForDailybuild.getFinalAddress(witnessKey005);
  String[] mappingAddress = {
      "TJJoqE9KHqY7C1x2xBvWfDPEwZusPFANRq",
      "TKUF5ok2x71REobHKi7vhNqX8xoj55dMNM",
      "TU7tDFz7u4mgQKBHGBKTLgVkWCTZ5LpUmp",
      "TBg9L2zzYgpeF65Rx11iuQhH8p4tQZJVJt",
      "TKfmgxVGJsqUewEgZc2SLNRKhoSCcJ8yyA",
      "TVQ6jYV5yTtRsKcD8aRc1a4Kei4V45ixLn"
  };
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);
  }

  @Test
  public void precom() {

    String parame2 =
        "\"" + sideGatewayAddress + "\",\"" + WalletClient.encode58Check(testDepositAddress) + "\"";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setTokenOwner(address,address)", parame2,
        false));
    String ownerTrx1 = PublicMethed
        .triggerContractSideChain(sideChainAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", testDepositAddress, testDepositTrx, blockingSideStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingSideStubFull);

    logger.info("infoById3:" + infoById3);

    SmartContract ac = PublicMethed
        .getContract(WalletClient.decodeFromBase58Check(sideGatewayAddress), blockingSideStubFull);

    String ContractRestule = Hex.toHexString(ac.getOriginAddress().toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("OriginAddress: " + addressFinal);

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
