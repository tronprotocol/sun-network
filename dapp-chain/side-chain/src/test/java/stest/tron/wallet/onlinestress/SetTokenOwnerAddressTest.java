package stest.tron.wallet.onlinestress;

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
import org.tron.api.GrpcAPI.TransactionExtention;
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
public class SetTokenOwnerAddressTest {


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
  public void changeOracle() {

    String[] oldOracleAddress = {
        "TAoLn3VNX2zG9653jhqmF3gAU8eEBGbW7c",
        "TAovhx3CJmuHjLpqfkS1Rtk7jHP7R6NsYe",
        "TAoXG5TvUEV6ZGxKQPnySh5GSs4QTLTq9q",
        "TAoJCXhF1815im33SiP1w1kf88wVHHN2Wo",
    };

    for (String oracleAddress : oldOracleAddress) {
      String parame2 = "\"" + oracleAddress + "\"";

      byte[] input1 = Hex.decode(AbiUtil.parseMethod("delOracle(address)", parame2, false));
      String ownerTrx1 = PublicMethed
          .triggerContractSideChain(sideChainAddress,
              chainIdAddressKey, 0l,
              input1,
              1000000000,
              0l, "0", witness001Address, witnessKey001, blockingSideStubFull);
    }

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String[] newOracleAddress = {
        "TJEuSMoC7tbs99XkbGhSDk7cM1xnxR931s",
        "TSYvXycFYNvtyLWciaoqmCHBZFsSNBoATJ",
        "TAf7g6tTJyhAycXhd72DyCREn8YnjZdXdB",
        "TUiLejEL9F5ih7MeuuqPL7c5WPBUYyFYiR"
    };

    for (String oracleAddress : newOracleAddress) {
      String parame2 = "\"" + oracleAddress + "\"";

      byte[] input1 = Hex.decode(AbiUtil.parseMethod("addOracle(address)", parame2, false));
      String ownerTrx1 = PublicMethed
          .triggerContractSideChain(sideChainAddress, chainIdAddressKey, 0l, input1,
              1000000000,
              0l, "0", witness001Address, witnessKey001, blockingSideStubFull);
    }

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    for (String oracleAddress : newOracleAddress) {
      String parame2 = "\"" + oracleAddress + "\"";

      byte[] input1 = Hex.decode(AbiUtil.parseMethod("oracles(address)", parame2, false));
      TransactionExtention return1 = PublicMethed
          .triggerContractForTransactionExtention(sideChainAddress, 0l, input1,
              1000000000,
              0l, "0", witness001Address, witnessKey001, blockingSideStubFull);
      String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

      logger.info(oracleAddress + " : " + ContractRestule);
    }

    for (String oracleAddress : oldOracleAddress) {
      String parame2 = "\"" + oracleAddress + "\"";

      byte[] input1 = Hex.decode(AbiUtil.parseMethod("oracles(address)", parame2, false));
      TransactionExtention return1 = PublicMethed
          .triggerContractForTransactionExtention(sideChainAddress, 0l, input1,
              1000000000,
              0l, "0", witness001Address, witnessKey001, blockingSideStubFull);
      String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

      logger.info(oracleAddress + " : " + ContractRestule);
    }


  }

  @Test
  public void setlogicAddress() {
    String[] newOracleKey = {
//        "8f48434338fc2b8e6e164e3495416de6d2fdfb0e176f82094c1c45405bb26afa",
//        "457e7f85919f15f81bcd451b12c9c168237defa15466f6f108960375f032a04a",
//        "01887d3cdb0d2b61ddb2ba15c096772805d55acd3b7bff31742cc5e034f500bd",
//        "be23e1e5b7152675c1bc247e2a0117bd554a5b7a0b46a1a44cf62af66f79e183",
//        "5c0bae4950aaf4c840c9f52ac6bbbb01e4b703d0c5d58d794862b0033fb38ee5",
        "324a2052e491e99026442d81df4d2777292840c1b3949e20696c49096c6bacb7"
    };

    for (String oracleKey : newOracleKey) {
      byte[] oracleAddress = PublicMethedForDailybuild.getFinalAddress(oracleKey);

      String parame2 = "\"" + "TBESTv3wZkhdqLq6v6qistZ8DCzKLeV5UN" + "\"";

      byte[] input1 = Hex.decode(AbiUtil.parseMethod("setLogicAddress(address)", parame2, false));
      String ownerTrx1 = PublicMethed
          .triggerContract(mainGateWayAddressKey, 0l, input1,
              1000000000,
              0l, "0", oracleAddress, oracleKey, blockingStubFull);
    }

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    byte[] input1 = Hex.decode(AbiUtil.parseMethod("logicAddress()", "", false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(mainGateWayAddressKey, 0l, input1,
            1000000000,
            0l, "0", witness001Address, witnessKey001, blockingStubFull);

    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);
  }


  @Test
  public void getmain() {
    for (int i = 0; i < 10; i++) {
      byte[] input1 = Hex.decode(AbiUtil.parseMethod("mainContractList(uint256)", i + "", false));
      TransactionExtention return1 = PublicMethed
          .triggerContractForTransactionExtention(sideChainAddress, 0l, input1,
              1000000000,
              0l, "0", witness001Address, witnessKey001, blockingSideStubFull);

      String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

      String tmpAddress = ContractRestule.substring(24);
      logger.info(tmpAddress);
      String addressHex = "41" + tmpAddress;
      logger.info("address_hex: " + addressHex);
      String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
      logger.info("address_final: " + addressFinal);
    }
  }

  @Test
  public void precom() {
//    String contractName = "Ballot";
//    String filePath = "src/test/resources/soliditycode/preContract02.sol";
//    String code = "60806040526103f3806100136000396000f3fe608060405234801561001057600080fd5b50600436106100365760003560e01c806383be33c01461003b578063cd025b7614610174575b600080fd5b6100f16004803603604081101561005157600080fd5b6001600160a01b03823516919081019060408101602082013564010000000081111561007c57600080fd5b82018360208201111561008e57600080fd5b803590602001918460018302840111640100000000831117156100b057600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295506101a4945050505050565b604051808315151515815260200180602001828103825283818151815260200191508051906020019080838360005b83811015610138578181015183820152602001610120565b50505050905090810190601f1680156101655780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b6101a26004803603604081101561018a57600080fd5b506001600160a01b03813581169160200135166102ee565b005b60006060836001600160a01b0316836040516024018080602001828103825283818151815260200191508051906020019080838360005b838110156101f35781810151838201526020016101db565b50505050905090810190601f1680156102205780820380516001836020036101000a031916815260200191505b5060408051601f198184030181529181526020820180516001600160e01b0316634cfa393d60e11b178152905182519295509350839250908083835b6020831061027b5780518252601f19909201916020918201910161025c565b6001836020036101000a0380198251168184511680821785525050505050509050019150506000604051808303816000865af19150503d80600081146102dd576040519150601f19603f3d011682016040523d82523d6000602084013e6102e2565b606091505b50915091509250929050565b604080516001600160a01b038481166020808401919091529084168284015282518083038401815260609092019283905281516201000293918291908401908083835b602083106103505780518252601f199092019160209182019101610331565b6001836020036101000a0380198251168184511680821785525050505050509050019150506000604051808303816000865af19150503d80600081146103b2576040519150601f19603f3d011682016040523d82523d6000602084013e6103b7565b606091505b505050505056fea265627a7a72315820392d8448b1ddc87f94041e7dbfdde13f0a9f6901c3456df7a8780b88a17134cd64736f6c634300050c0032";
//    String abi = "[{\"inputs\":[],\"payable\":true,\"stateMutability\":\"payable\",\"type\":\"constructor\"},{\"constant\":false,\"inputs\":[{\"internalType\":\"address\",\"name\":\"gateWayAddress\",\"type\":\"address\"},{\"internalType\":\"bytes\",\"name\":\"Txid\",\"type\":\"bytes\"}],\"name\":\"callMaping\",\"outputs\":[{\"internalType\":\"bool\",\"name\":\"\",\"type\":\"bool\"},{\"internalType\":\"bytes\",\"name\":\"\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"internalType\":\"address\",\"name\":\"tokenAddress\",\"type\":\"address\"},{\"internalType\":\"address\",\"name\":\"tokenOwner\",\"type\":\"address\"}],\"name\":\"setTokenOwner\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
//
//    byte[] contractAddress = PublicMethed.deployContractForSide(contractName, abi, code, "",
//        maxFeeLimit, 0L, 0, 10000,
//        "0", 0, null, witnessKey001,
//        witness001Address, chainIdAddressKey, blockingSideStubFull);

//    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    String parame1 = "\"TKUF5ok2x71REobHKi7vhNqX8xoj55dMNM\"";
    byte[] input1 = Hex
        .decode(AbiUtil.parseMethod("mainToSideContractMap(address)", parame1, false));
    TransactionExtention return1 = PublicMethed
        .triggerContractForTransactionExtention(sideChainAddress, 0l, input1,
            1000000000,
            0l, "0", witness001Address, witnessKey001, blockingSideStubFull);

    String ContractRestule = Hex.toHexString(return1.getConstantResult(0).toByteArray());

    String tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    String addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("address_final: " + addressFinal);

    SmartContract ac = PublicMethed
        .getContract(WalletClient.decodeFromBase58Check(addressFinal), blockingSideStubFull);

    ContractRestule = Hex.toHexString(ac.getOriginAddress().toByteArray());

    tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    String OriginAddress = Base58.encode58Check(ByteArray.fromHexString(addressHex));
    logger.info("OriginAddress: " + OriginAddress);

    String parame2 = "\"" + addressFinal + "\",\"TJEuSMoC7tbs99XkbGhSDk7cM1xnxR931s\"";

    input1 = Hex.decode(AbiUtil.parseMethod("setTokenOwner(address,address)", parame2,
        false));
    String ownerTrx1 = PublicMethed
        .triggerContractSideChain(sideChainAddress, chainIdAddressKey, 0l, input1,
            1000000000,
            0l, "0", witness001Address, witnessKey001, blockingSideStubFull);

    Optional<TransactionInfo> infoById3 = PublicMethed
        .getTransactionInfoById(ownerTrx1, blockingSideStubFull);

    logger.info("infoById3:" + infoById3);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    ac = PublicMethed
        .getContract(WalletClient.decodeFromBase58Check(addressFinal), blockingSideStubFull);

    ContractRestule = Hex.toHexString(ac.getOriginAddress().toByteArray());

    tmpAddress = ContractRestule.substring(24);
    logger.info(tmpAddress);
    addressHex = "41" + tmpAddress;
    logger.info("address_hex: " + addressHex);
    addressFinal = Base58.encode58Check(ByteArray.fromHexString(addressHex));
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
