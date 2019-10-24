package stest.tron.wallet.depositWithdraw;

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
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class ContractCallMain001 {


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


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("mainfullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);


  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);

  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");

  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);

  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);

  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] ChainIdAddressKey = WalletClient.decodeFromBase58Check(ChainIdAddress);

  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);

  String methodStr1 = null;
  String parame1 = null;

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

  @Test(enabled = true, description = "MainGateWay contract call function use contract account.")
  public void test1ContractFallback001() {

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 10000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    Account accountBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountBeforeBalance = accountBefore.getBalance();
    Assert.assertTrue(accountBeforeBalance == 10000000000L);

    // deploy testMainContract
    String contractName = "testMainContract";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50610a0b8061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100b85760003560e01c80639579dcce116100805780639579dcce146101cd578063a35ff076146101fc578063a4ab7a4814610224578063d878da7c146102ca578063ecdc0f75146102e7576100b8565b8063116191b6146100bd57806351093a76146100e15780635a3d588e1461016c5780637d9e705d1461018957806389882c48146101a6575b600080fd5b6100c561038d565b604080516001600160a01b039092168252519081900360200190f35b6100e961039c565b604051808315151515815260200180602001828103825283818151815260200191508051906020019080838360005b83811015610130578181015183820152602001610118565b50505050905090810190601f16801561015d5780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b6100e96004803603602081101561018257600080fd5b5035610476565b6100e96004803603602081101561019f57600080fd5b5035610560565b6100e9600480360360208110156101bc57600080fd5b503567ffffffffffffffff166105dd565b6100e9600480360360408110156101e357600080fd5b5067ffffffffffffffff81358116916020013516610666565b6102226004803603602081101561021257600080fd5b50356001600160a01b0316610765565b005b6100e96004803603602081101561023a57600080fd5b81019060208101813564010000000081111561025557600080fd5b82018360208201111561026757600080fd5b8035906020019184600183028401116401000000008311171561028957600080fd5b91908080601f016020809104026020016040519081016040528093929190818152602001838380828437600092019190915250929550610787945050505050565b6100e9600480360360208110156102e057600080fd5b5035610861565b6100e9600480360360208110156102fd57600080fd5b81019060208101813564010000000081111561031857600080fd5b82018360208201111561032a57600080fd5b8035906020019184600183028401116401000000008311171561034c57600080fd5b91908080601f0160208091040260200160405190810160405280939291908181526020018383808284376000920191909152509295506108d8945050505050565b6000546001600160a01b031681565b6000805460408051600481526024810182526020810180516001600160e01b0316634b398e9960e01b178152915181516060946001600160a01b0316936103e89392918291908083835b602083106104055780518252601f1990920191602091820191016103e6565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610467576040519150601f19603f3d011682016040523d82523d6000602084013e61046c565b606091505b5090939092509050565b6000805460408051602480820186905282518083039091018152604490910182526020810180516001600160e01b03166308c718a960e31b178152915181516060946001600160a01b0316936103e89392918291908083835b602083106104ee5780518252601f1990920191602091820191016104cf565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610550576040519150601f19603f3d011682016040523d82523d6000602084013e610555565b606091505b509094909350915050565b6000805460408051306024820152604480820186905282518083039091018152606490910182526020810180516001600160e01b0316637b99c94960e11b178152915181516060946001600160a01b0316936103e8939291829190808383602083106104ee5780518252601f1990920191602091820191016104cf565b600080546040805130602482015267ffffffffffffffff851660448083019190915282518083039091018152606490910182526020810180516001600160e01b0316634171a53760e01b178152915181516060946001600160a01b0316936103e8939291829190808383602083106104ee5780518252601f1990920191602091820191016104cf565b600080546040805167ffffffffffffffff8087166024830152851660448083019190915282518083039091018152606490910182526020810180516001600160e01b0316630c73e51f60e01b178152915181516060946001600160a01b0316936103e89392918291908083835b602083106106f25780518252601f1990920191602091820191016106d3565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610754576040519150601f19603f3d011682016040523d82523d6000602084013e610759565b606091505b50909590945092505050565b600080546001600160a01b0319166001600160a01b0392909216919091179055565b600080546040516020602482018181528551604484015285516060946001600160a01b0316936103e8938893928392606401918501908083838c5b838110156107da5781810151838201526020016107c2565b50505050905090810190601f1680156108075780820380516001836020036101000a031916815260200191505b5060408051601f198184030181529181526020820180516001600160e01b0316630659542360e31b17815290518251929550935083925090808383602083106104ee5780518252601f1990920191602091820191016104cf565b6000805460408051602480820186905282518083039091018152604490910182526020810180516001600160e01b0316634965ba5960e11b178152915181516060946001600160a01b0316936103e8939291829190808383602083106104ee5780518252601f1990920191602091820191016104cf565b600080546040516020602482018181528551604484015285516060946001600160a01b0316936103e8938893928392606401918501908083838c5b8381101561092b578181015183820152602001610913565b50505050905090810190601f1680156109585780820380516001836020036101000a031916815260200191505b5060408051601f198184030181529181526020820180516001600160e01b0316634cfa393d60e11b17815290518251929550935083925090808383602083106104ee5780518252601f1990920191602091820191016104cf56fea26474726f6e5820df8cf40a4808fefc61ad005199e167e31dc007806138d3bfb4a9a00c5460782864736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660056";
    String abi = "[{\"constant\":true,\"inputs\":[],\"name\":\"gateway\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[],\"name\":\"callDepositTRX\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nonce\",\"type\":\"uint256\"}],\"name\":\"callRetryDeposit\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenValue\",\"type\":\"uint256\"}],\"name\":\"callDepositTRC721\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenValue\",\"type\":\"uint64\"}],\"name\":\"callDepositTRC20\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenId\",\"type\":\"uint64\"},{\"name\":\"tokenValue\",\"type\":\"uint64\"}],\"name\":\"callDepositTRC10\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_gateway\",\"type\":\"address\"}],\"name\":\"setGatewayAddress\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"txId\",\"type\":\"bytes\"}],\"name\":\"callMappingTRC721\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nonce\",\"type\":\"uint256\"}],\"name\":\"callRetryMapping\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"txId\",\"type\":\"bytes\"}],\"name\":\"callMappingTRC20\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]\n";

    byte[] contractAddress = PublicMethed.deployContractForMain(contractName, abi, code, "",
        maxFeeLimit, 10000000000L, 0, 10000,
        "0", 0, null, testDepositTrx,
        testDepositAddress, blockingStubFull);

    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    logger.info("contractAddress:"+Base58.encode58Check(contractAddress));
    Assert.assertNotNull(contractAddress);
    /*Assert.assertTrue(PublicMethed
        .sendcoin(contractAddress, 10000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));*/
    String methodStr = "setGatewayAddress(address)";
    String parame1 = "\"" + Base58.encode58Check(mainGateWayAddressKey) + "\"";

    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame1, false));

    long callValue = 0;
    String txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String methodStr1 = "callDepositTRX()";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, "", false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input1,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    String expectContractResult = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000006408c379a0000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000126e6f7420616c6c6f7720636f6e7472616374000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    Assert.assertEquals(expectContractResult, contractResult);
    String inputTokenID = "1000001";
    long inputTokenValue = 1;
    String methodStr2 = "callDepositTRC10(uint64,uint64)";
    String parame2 = inputTokenID + "," + inputTokenValue;

    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame2, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input2,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(
        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000006408c379a000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000016746f6b656e496420213d206d73672e746f6b656e69640000000000000000000000000000000000000000000000000000000000000000000000000000",
        contractResult);

    String methodStr3 = "callDepositTRC721(uint256)";
    String parame3 = "1";

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, parame3, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input3,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr4 = "callDepositTRC721(uint256)";
    String parame4 = "1";

    byte[] input4 = Hex.decode(AbiUtil.parseMethod(methodStr4, parame4, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input4,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr5 = "callMappingTRC20(bytes)";
    String parame5 = "\"" + txid + "\"";

    byte[] input5 = Hex.decode(AbiUtil.parseMethod(methodStr5, parame5, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input5,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr6 = "callMappingTRC721(bytes)";
    String parame6 = "\"" + txid + "\"";

    byte[] input6 = Hex.decode(AbiUtil.parseMethod(methodStr6, parame6, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input6,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr7 = "callRetryDeposit(uint256)";
    String parame7 = "1";

    byte[] input7 = Hex.decode(AbiUtil.parseMethod(methodStr7, parame7, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input7,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);

    String methodStr8 = "callRetryMapping(uint256)";
    String parame8 = "1";

    byte[] input8 = Hex.decode(AbiUtil.parseMethod(methodStr8, parame8, false));

    txid = PublicMethed
        .triggerContract(contractAddress,
            callValue,
            input8,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(expectContractResult, contractResult);
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
