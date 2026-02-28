package stest.tron.wallet.dailybuild.tvmnewcommand.setTokenOwnerAddress;

import static org.hamcrest.CoreMatchers.containsString;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.spongycastle.util.encoders.Hex;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.TransactionInfo;
import org.tron.protos.contract.SmartContractOuterClass.SmartContract;
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
  final String gateWayOwnerKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  final byte[] gateWayOwnerAddress = PublicMethed.getFinalAddress(gateWayOwnerKey);
  private final String fundationKey001 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] fundationAddress001 = PublicMethed.getFinalAddress(fundationKey001);
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

  private ECKey ecKey1 = new ECKey(Utils.getRandom());
  private byte[] dev001Address = ecKey1.getAddress();
  private String dev001Key = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

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

    Assert.assertTrue(
        PublicMethedForDailybuild.sendcoin(dev001Address, 10000_000_000L, fundationAddress001,
            fundationKey001, blockingSideStubFull));
    Assert.assertTrue(
        PublicMethed.sendcoin(dev001Address, 10000_000_000L, fundationAddress001,
            fundationKey001, blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
  }

  @Test(enabled = true, description = "normal Address trigger setTokenOwnerAddress Function")
  public void setTokenOwnerAddress001() {

    String parame2 =
        "\"" + sideGatewayAddress + "\",\"" + WalletClient.encode58Check(fundationAddress001)
            + "\"";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setTokenOwner(address,address)", parame2,
        false));
    String ownerTrx1 = PublicMethed
        .triggerContractSideChain(sideChainAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", fundationAddress001, fundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingSideStubFull);

    logger.info("infoById3:" + infoById3);

    String data = ByteArray
        .toHexString(infoById3.get().getContractResult(0).substring(68, 85).toByteArray());
    logger.info("data:" + data);
    Assert.assertEquals("msg.sender != own", PublicMethed.hexStringToString(data));

  }

  @Test(enabled = true, description = "normal Contract trigger 0x10002 ")
  public void setTokenOwnerAddress002() {

    String filePath = "./src/test/resources/soliditycode/setTokenOwnerAddress001.sol";
    String contractName = "Ballot";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);

    String code = retMap.get("byteCode").toString();
    String abi = retMap.get("abI").toString();

    final String transferTokenTxid = PublicMethedForDailybuild
        .deployContractAndGetTransactionInfoById(contractName, abi, code, "",
            maxFeeLimit, 0L, 0, 10000,
            "0", 0, null, dev001Key,
            dev001Address, blockingSideStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethedForDailybuild
        .getTransactionInfoById(transferTokenTxid, blockingSideStubFull);

    if (infoById.get().getResultValue() != 0) {
      Assert.fail("deploy transaction failed with message: " + infoById.get().getResMessage());
    }

    TransactionInfo transactionInfo = infoById.get();
    logger.info("EnergyUsageTotal: " + transactionInfo.getReceipt().getEnergyUsageTotal());
    logger.info("NetUsage: " + transactionInfo.getReceipt().getNetUsage());

    byte[] factoryContractAddress = infoById.get().getContractAddress().toByteArray();
    SmartContract smartContract = PublicMethedForDailybuild.getContract(factoryContractAddress,
        blockingSideStubFull);
    Assert.assertNotNull(smartContract.getAbi());

    String parame2 =
        "\"" + sideGatewayAddress + "\",\"" + WalletClient.encode58Check(fundationAddress001)
            + "\"";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setTokenOwner(address,address)", parame2,
        false));
    String ownerTrx1 = PublicMethed
        .triggerContractSideChain(factoryContractAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", fundationAddress001, fundationKey001, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingSideStubFull);

    logger.info("infoById3:" + infoById3);

    String data = infoById3.get().getResMessage().toStringUtf8();
    Assert.assertThat(data, containsString("[updatecontractowner method]caller must be gateway"));

  }

  @Test(enabled = true, description = "Contract OriginAddress isno Gateway ")
  public void setTokenOwnerAddress003() {

    String contractName = "trc20Contract";
    String code = Configuration.getByPath("testng.conf")
        .getString("code.code_ContractTRC20");
    String abi = Configuration.getByPath("testng.conf")
        .getString("abi.abi_ContractTRC20");
    String parame = "\"" + Base58.encode58Check(dev001Address) + "\"";

    String deployTxid = PublicMethed
        .deployContractWithConstantParame(contractName, abi, code, "TronToken(address)",
            parame, "",
            maxFeeLimit,
            0L, 100, null, dev001Key, dev001Address
            , blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(deployTxid, blockingStubFull);
    byte[] trc20Contract = infoById.get().getContractAddress().toByteArray();
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(trc20Contract);

    String mapTxid5 = PublicMethed
        .mappingTrc20(mainGateWayAddressKey, deployTxid, 1000000000,
            dev001Address, dev001Key, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById5 = PublicMethed
        .getTransactionInfoById(mapTxid5, blockingStubFull);
    Assert.assertEquals("SUCESS", infoById5.get().getResult().name());
    Assert.assertEquals(0, infoById5.get().getResultValue());

    String parame1 = "\"" + Base58.encode58Check(trc20Contract) + "\"";
    byte[] input2 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddress, 0, input2,
            maxFeeLimit,
            0, "0",
            dev001Address, dev001Key, blockingSideStubFull);
    logger.info(Hex.toHexString(return1.getConstantResult(0).toByteArray()));
    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    byte[] sideContractAddress = WalletClient.decodeFromBase58Check(addressFinal);
    Assert.assertEquals(0, infoById.get().getResultValue());
    Assert.assertNotNull(sideContractAddress);

    SmartContract contract = PublicMethed.getContract(sideContractAddress, blockingSideStubFull);
    Assert.assertEquals(WalletClient.encode58Check(contract.getOriginAddress().toByteArray()),
        WalletClient.encode58Check(dev001Address));

    String parame2 =
        "\"" + addressFinal + "\",\"" + WalletClient.encode58Check(fundationAddress001)
            + "\"";

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("setTokenOwner(address,address)", parame2,
        false));
    String ownerTrx1 = PublicMethed
        .triggerContractSideChain(sideChainAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", gateWayOwnerAddress, gateWayOwnerKey, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingSideStubFull);

    logger.info("infoById3:" + infoById3);

    String data = infoById3.get().getResMessage().toStringUtf8();
    Assert.assertThat(data, containsString(
        "[updatecontractowner method]target contract not exists or address not in gatewayList"));

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
