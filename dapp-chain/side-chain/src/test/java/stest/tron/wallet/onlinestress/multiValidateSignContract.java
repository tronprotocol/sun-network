package stest.tron.wallet.onlinestress;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
import org.tron.common.crypto.Hash;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter;
import stest.tron.wallet.common.client.utils.PublicMethedForDailybuild;

@Slf4j
public class multiValidateSignContract {

  private final String testNetAccountKey = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] testNetAccountAddress = PublicMethedForDailybuild
      .getFinalAddress(testNetAccountKey);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");

  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private ManagedChannel channelFull1 = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull1 = null;


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(0);
  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  byte[] contractAddress = null;


  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] contractExcAddress = ecKey1.getAddress();
  String contractExcKey = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
  String txid = "";

  @BeforeSuite
  public void beforeSuite() {
    Wallet wallet = new Wallet();
    Wallet.setAddressPreFixByte(Parameter.CommonConstant.ADD_PRE_FIX_BYTE_MAINNET);
  }

  /**
   * constructor.
   */
  @BeforeClass(enabled = true)
  public void beforeClass() {
    PublicMethedForDailybuild.printAddress(contractExcKey);
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    channelFull1 = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull1 = WalletGrpc.newBlockingStub(channelFull1);
    txid = PublicMethedForDailybuild
        .sendcoinGetTransactionId(contractExcAddress, 1000000000L, testNetAccountAddress,
            testNetAccountKey,
            blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
    String filePath = "src/test/resources/soliditycode/multivalidatesign001.sol";
    String contractName = "Demo";
    HashMap retMap = PublicMethedForDailybuild.getBycodeAbi(filePath, contractName);
    String code = "608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b506105a28061003a6000396000f3fe608060405234801561001057600080fd5b50d3801561001d57600080fd5b50d2801561002a57600080fd5b50600436106100505760003560e01c8063022ac30f14610055578063da586d8814610055575b600080fd5b61006861006336600461022d565b61007e565b60405161007591906103e1565b60405180910390f35b60006009848484604051600081526020016040526040516100a1939291906103ef565b6020604051602081039080840390855afa1580156100c3573d6000803e3d6000fd5b5050604051601f19015195945050505050565b80356100e18161051b565b6100ea816104af565b9392505050565b600082601f83011261010257600080fd5b813561011561011082610453565b61042c565b9150818183526020840193506020810190508385602084028201111561013a57600080fd5b60005b83811015610166578161015088826100d6565b845250602092830192919091019060010161013d565b5050505092915050565b600082601f83011261018157600080fd5b813561018f61011082610453565b81815260209384019390925082018360005b8381101561016657813586016101b788826101de565b84525060209283019291909101906001016101a1565b80356101d881610532565b92915050565b600082601f8301126101ef57600080fd5b81356101fd61011082610474565b9150808252602083016020830185838301111561021957600080fd5b6102248382846104d5565b50505092915050565b60008060006060848603121561024257600080fd5b600061024e86866101cd565b935050602084013567ffffffffffffffff81111561026b57600080fd5b61027786828701610170565b925050604084013567ffffffffffffffff81111561029457600080fd5b6102a0868287016100f1565b9150509250925092565b60006102b683836102ca565b505060200190565b60006100ea83836103a9565b6102d3816104af565b82525050565b60006102e4826104a2565b6102ee81856104a6565b93506102f98361049c565b8060005b8381101561032757815161031188826102aa565b975061031c8361049c565b9250506001016102fd565b509495945050505050565b600061033d826104a2565b61034781856104a6565b9350836020820285016103598561049c565b8060005b85811015610393578484038952815161037685826102be565b94506103818361049c565b60209a909a019992505060010161035d565b5091979650505050505050565b6102d3816104ba565b60006103b4826104a2565b6103be81856104a6565b93506103ce8185602086016104e1565b6103d781610511565b9093019392505050565b602081016101d882846103a0565b606081016103fd82866103a0565b818103602083015261040f8185610332565b9050818103604083015261042381846102d9565b95945050505050565b60405181810167ffffffffffffffff8111828210171561044b57600080fd5b604052919050565b600067ffffffffffffffff82111561046a57600080fd5b5060209081020190565b600067ffffffffffffffff82111561048b57600080fd5b506020601f91909101601f19160190565b60200190565b5190565b90815260200190565b60006101d8826104bd565b90565b6001600160a01b031690565b6001600160a81b031690565b82818337506000910152565b60005b838110156104fc5781810151838201526020016104e4565b8381111561050b576000848401525b50505050565b601f01601f191690565b610524816104c9565b811461052f57600080fd5b50565b610524816104ba56fea36474726f6e58200355930b389b97929c271e573071a5f1134174ac32dbba3dbe50f490f42f565a6c6578706572696d656e74616cf564736f6c637827302e352e392d646576656c6f702e323031392e382e32312b636f6d6d69742e31393035643732660064";
    String abi = "[{\"constant\":true,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testPure\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"pure\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"hash\",\"type\":\"bytes32\"},{\"name\":\"signatures\",\"type\":\"bytes[]\"},{\"name\":\"addresses\",\"type\":\"address[]\"}],\"name\":\"testArray\",\"outputs\":[{\"name\":\"\",\"type\":\"bytes32\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"}]";
    contractAddress = PublicMethedForDailybuild
        .deployContract(contractName, abi, code, "", maxFeeLimit,
            0L, 100, null, contractExcKey,
            contractExcAddress, blockingStubFull);
    PublicMethedForDailybuild.waitProduceNextBlock(blockingStubFull);
  }

  @Test(enabled = true, description = "Correct 32 signatures test pure multivalidatesign")
  public void test01Correct32signatures() {

    for(int k = 0; k < 100000L; k++){

      List<Object> signatures = new ArrayList<>();
      List<Object> addresses = new ArrayList<>();
      byte[] hash = Hash.sha3(txid.getBytes());
      for (int i = 0; i < 32; i++) {
        ECKey key = new ECKey();
        byte[] sign = key.sign(hash).toByteArray();
        signatures.add(Hex.toHexString(sign));
        addresses.add(Wallet.encode58Check(key.getAddress()));
      }
      List<Object> parameters = Arrays.asList("0x" + Hex.toHexString(hash), signatures, addresses);
      String input = PublicMethedForDailybuild.parametersString(parameters);
      TransactionExtention transactionExtention = PublicMethedForDailybuild
          .triggerConstantContractForExtention(contractAddress,
              "testPure(bytes32,bytes[],address[])", input, false,
              0, 0, "0", 0, contractExcAddress, contractExcKey, blockingStubFull);
      logger.info("transactionExtention  " + transactionExtention);
//      Assert.assertEquals("11111111111111111111111111100000", PublicMethedForDailybuild
//          .bytes32ToString(transactionExtention.getConstantResult(0).toByteArray()));
//      Assert.assertEquals("SUCCESS", transactionExtention.getResult().getCode().toString());

    }
  }


  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    long balance = PublicMethedForDailybuild.queryAccount(contractExcKey, blockingStubFull)
        .getBalance();
    PublicMethedForDailybuild
        .sendcoin(testNetAccountAddress, balance, contractExcAddress, contractExcKey,
            blockingStubFull);
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
    if (channelFull1 != null) {
      channelFull1.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
