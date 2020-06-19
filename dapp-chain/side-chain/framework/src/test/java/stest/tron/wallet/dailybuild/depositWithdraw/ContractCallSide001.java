package stest.tron.wallet.dailybuild.depositWithdraw;

import com.google.protobuf.ByteString;
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
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class ContractCallSide001 {


  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final byte[] mainGateWayAddressKey = WalletClient.decodeFromBase58Check(mainGateWayAddress);
  final String chainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  final byte[] chainIdAddressKey = WalletClient.decodeFromBase58Check(chainIdAddress);
  final String sideGateWayAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideGateWayAddress = WalletClient.decodeFromBase58Check(sideGateWayAddressKey);
  final String gateWatOwnerAddressKey = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key1");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final byte[] gateWatOwnerAddress = PublicMethed.getFinalAddress(gateWatOwnerAddressKey);
  private final String sideGateWayOwner = Configuration.getByPath("testng.conf")
      .getString("gateWatOwnerAddressKey.key2");
  private final byte[] sideGateWayOwnerAddress = PublicMethed.getFinalAddress(sideGateWayOwner);
  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String sideChainAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key2");
  final byte[] sideChainAddressKey = WalletClient.decodeFromBase58Check(sideChainAddress);
  String methodStr1 = null;
  String parame1 = null;
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingSideStubFull = WalletGrpc.newBlockingStub(channelFull1);

    Assert.assertTrue(PublicMethed
        .sendcoin(depositAddress, 11000000000L, testDepositAddress, testDepositTrx,
            blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    Account accountMainBefore = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainBeforeBalance = accountMainBefore.getBalance();
    Assert.assertTrue(accountMainBeforeBalance == 11000000000L);
    Account accountSideBefore = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideBeforeBalance = accountSideBefore.getBalance();
    ByteString address = accountSideBefore.getAddress();
    String accountSideBeforeAddress = Base58.encode58Check(address.toByteArray());
    logger.info("accountSideBeforeAddress:" + accountSideBeforeAddress);
    Assert.assertEquals("3QJmnh", accountSideBeforeAddress);

    logger.info("accountBeforeBalance:" + accountMainBeforeBalance);
    logger.info("accountSideBeforeBalance:" + accountSideBeforeBalance);

    logger.info("transferTokenContractAddress:" + mainGateWayAddress);
    String methodStr = "depositTRX()";
    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

    long callValue = 10000000000L;
    String txid = PublicMethed
        .triggerContract(WalletClient.decodeFromBase58Check(mainGateWayAddress),
            callValue,
            input,
            maxFeeLimit, 0, "", depositAddress, testKeyFordeposit, blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    long fee = infoById.get().getFee();
    logger.info("fee:" + fee);
    Account accountMainAfter = PublicMethed.queryAccount(depositAddress, blockingStubFull);
    long accountMainAfterBalance = accountMainAfter.getBalance();
    logger.info("accountAfterBalance:" + accountMainAfterBalance);
    Assert.assertTrue(accountMainBeforeBalance - fee - 10000000000L == accountMainAfterBalance);
    Account accountSideAfter = PublicMethed.queryAccount(depositAddress, blockingSideStubFull);
    long accountSideAfterBalance = accountSideAfter.getBalance();
    ByteString addressSideAfter = accountSideAfter.getAddress();
    String accountSideAfterAddress = Base58.encode58Check(addressSideAfter.toByteArray());
    logger.info("accountSideAfterAddress:" + accountSideAfterAddress);
    Assert.assertEquals(Base58.encode58Check(depositAddress), accountSideAfterAddress);
    Assert.assertTrue(accountSideAfterBalance == 10000000000L);
  }

  @Test(enabled = true, description = "SideGateWay contract call function use contract account.")
  public void test1ContractFallback001() {

    // deploy testMainContract
    String contractName = "ContractCallWithdraw";
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506104cd8061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100715760003560e01c80630116373014610076578063116191b61461010157806375446e4914610125578063a35ff07614610142578063eb8d9b1a1461016a575b600080fd5b61007e61018d565b604051808315151515815260200180602001828103825283818151815260200191508051906020019080838360005b838110156100c55781810151838201526020016100ad565b50505050905090810190601f1680156100f25780820380516001836020036101000a031916815260200191505b50935050505060405180910390f35b610109610267565b604080516001600160a01b039092168252519081900360200190f35b61007e6004803603602081101561013b57600080fd5b5035610276565b6101686004803603602081101561015857600080fd5b50356001600160a01b0316610360565b005b61007e6004803603604081101561018057600080fd5b5080359060200135610382565b6000805460408051600481526024810182526020810180516001600160e01b0316632535eeb560e11b178152915181516060946001600160a01b0316936103e89392918291908083835b602083106101f65780518252601f1990920191602091820191016101d7565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610258576040519150601f19603f3d011682016040523d82523d6000602084013e61025d565b606091505b5090939092509050565b6000546001600160a01b031681565b6000805460408051602480820186905282518083039091018152604490910182526020810180516001600160e01b03166327352b6b60e21b178152915181516060946001600160a01b0316936103e89392918291908083835b602083106102ee5780518252601f1990920191602091820191016102cf565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610350576040519150601f19603f3d011682016040523d82523d6000602084013e610355565b606091505b509094909350915050565b600080546001600160a01b0319166001600160a01b0392909216919091179055565b600080546040805160248101869052604480820186905282518083039091018152606490910182526020810180516001600160e01b03166354137e4960e01b178152915181516060946001600160a01b0316936103e89392918291908083835b602083106104015780518252601f1990920191602091820191016103e2565b6001836020036101000a03801982511681845116808217855250505050505090500191505060006040518083038185875af1925050503d8060008114610463576040519150601f19603f3d011682016040523d82523d6000602084013e610468565b606091505b5090959094509250505056fea26474726f6e58206537679d37c83ad886586063b442713b0d65e797627f9c63de9aacc99fac24a264736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660056";
    String abi = "[{\"constant\":false,\"inputs\":[],\"name\":\"callWithdrawTRX\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":true,\"inputs\":[],\"name\":\"gateway\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"}],\"payable\":false,\"stateMutability\":\"view\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"nonce\",\"type\":\"uint256\"}],\"name\":\"callRetryWithdraw\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"_gateway\",\"type\":\"address\"}],\"name\":\"setGatewayAddress\",\"outputs\":[],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"tokenId\",\"type\":\"uint256\"},{\"name\":\"tokenValue\",\"type\":\"uint256\"}],\"name\":\"callWithdrawTRC10\",\"outputs\":[{\"name\":\"success\",\"type\":\"bool\"},{\"name\":\"data\",\"type\":\"bytes\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";

    byte[] contractAddress = PublicMethed.deployContract(contractName, abi, code, "",
        maxFeeLimit, 0L, 0, null, testDepositTrx,
        testDepositAddress, blockingSideStubFull);

    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    Assert.assertNotNull(contractAddress);
    Assert.assertTrue(PublicMethed
        .sendcoinForSidechain(contractAddress, 10000000000L, testDepositAddress, testDepositTrx,
            chainIdAddressKey, blockingSideStubFull));
    String methodStr = "setGatewayAddress(address)";
    String parame1 = "\"" + Base58.encode58Check(sideGateWayAddress) + "\"";

    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, parame1, false));

    long callValue = 0;
    String txid = PublicMethed
        .triggerContractSideChain(contractAddress, methodStr, parame1, false,
            callValue, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    Optional<TransactionInfo> infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String methodStr1 = "callWithdrawTRX()";
    byte[] input1 = Hex.decode(AbiUtil.parseMethod(methodStr1, "", false));

    txid = PublicMethed
        .triggerContractSideChain(contractAddress, methodStr1, "#", false,
            callValue, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    String contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    String expectContractResult = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000006408c379a0000000000000000000000000000000000000000000000000000000000000002000000000000000000000000000000000000000000000000000000000000000126e6f7420616c6c6f7720636f6e7472616374000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
    Assert.assertEquals(expectContractResult, contractResult);
    String inputTokenID = "1000001";
    long inputTokenValue = 1;
    String methodStr2 = "callWithdrawTRC10(uint256,uint256)";
    String parame2 = inputTokenID + "," + inputTokenValue;

    byte[] input2 = Hex.decode(AbiUtil.parseMethod(methodStr2, parame2, false));

    txid = PublicMethed
        .triggerContractSideChain(contractAddress, methodStr2, parame2, false,
            callValue, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
    Assert.assertTrue(infoById.get().getResultValue() == 0);
    contractResult = ByteArray
        .toHexString(infoById.get().getContractResult(0).toByteArray());
    logger.info("contractResult:" + contractResult);
    Assert.assertEquals(
        "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000006408c379a000000000000000000000000000000000000000000000000000000000000000200000000000000000000000000000000000000000000000000000000000000016746f6b656e496420213d206d73672e746f6b656e69640000000000000000000000000000000000000000000000000000000000000000000000000000",
        contractResult);

    String methodStr3 = "callRetryWithdraw(uint256)";
    String parame3 = "1";

    byte[] input3 = Hex.decode(AbiUtil.parseMethod(methodStr3, parame3, false));

    txid = PublicMethed
        .triggerContractSideChain(contractAddress, methodStr3, parame3, false,
            callValue, maxFeeLimit, depositAddress, testKeyFordeposit, blockingSideStubFull);
    PublicMethed.waitProduceNextBlock(blockingStubFull);
    PublicMethed.waitProduceNextBlock(blockingSideStubFull);

    infoById = PublicMethed
        .getTransactionInfoById(txid, blockingSideStubFull);
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
