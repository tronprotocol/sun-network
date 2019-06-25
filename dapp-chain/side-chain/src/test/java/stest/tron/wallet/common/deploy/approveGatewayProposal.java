package stest.tron.wallet.common.deploy;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
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
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.Transaction.Result.contractResult;
import org.tron.protos.Protocol.TransactionInfo;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.AbiUtil;
import stest.tron.wallet.common.client.utils.Base58;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class approveGatewayProposal {


  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("witness.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private String description = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetDescription");
  private String url = Configuration.getByPath("testng.conf")
      .getString("defaultParameter.assetUrl");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;


  private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);

  ECKey ecKey1 = new ECKey(Utils.getRandom());
  byte[] depositAddress = ecKey1.getAddress();
  String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());

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
//    PublicMethed.printAddress(testKeyFordeposit);
    channelFull = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }


  @Test(enabled = true)
  public void testApproveProposal() {
    String mainChainAddress = "";
    String sideChainAddress = "";
    try {
      File mainChainFile = new File("/home/mainChainGatewayAddress");
      FileReader reader = new FileReader(mainChainFile);
      BufferedReader breader = new BufferedReader(reader);
      mainChainAddress = breader.readLine();
      breader.close();
      File sideChainFile = new File("/home/sideChainGatewayAddress");
      FileReader reader2 = new FileReader(sideChainFile);
      BufferedReader breader2 = new BufferedReader(reader2);
      sideChainAddress = breader2.readLine();
      breader2.close();
    }catch (Exception e){
      logger.info("Read main/side Gateway ContractAddress Failed");
      return;
    }



    HashMap<Long, String> proposalMap = new HashMap<Long, String>();
    proposalMap.put(21L, sideChainAddress);
    org.testng.Assert.assertTrue(PublicMethed.sideChainCreateProposal(testDepositAddress,
        testDepositTrx,mainChainAddress, proposalMap, blockingStubFull));
    try {
      Thread.sleep(20000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
//    //Get proposal list
//    ProposalList proposalList = blockingStubFull.listProposals(EmptyMessage.newBuilder().build());
//    Optional<ProposalList> listProposals = Optional.ofNullable(proposalList);
//    final Integer proposalId = listProposals.get().getProposalsCount();
//    logger.info(Integer.toString(proposalId));
//
//    //Get proposal list after approve
//    proposalList = blockingStubFull.listProposals(EmptyMessage.newBuilder().build());
//    listProposals = Optional.ofNullable(proposalList);
//    logger.info(Integer.toString(listProposals.get().getProposals(0).getApprovalsCount()));

    String[] witnessKey = {
        "369F095838EB6EED45D4F6312AF962D5B9DE52927DA9F04174EE49F9AF54BC77",
        "9FD8E129DE181EA44C6129F727A6871440169568ADE002943EAD0E7A16D8EDAC",
    };
    byte[] witnessAddress;
    for (String key : witnessKey) {
      witnessAddress = PublicMethed.getFinalAddress(key);
      PublicMethed.approveProposal(witnessAddress, key, mainChainAddress,1000001,
          true, blockingStubFull);
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * constructor.
   */
  @AfterClass
  public void shutdown() throws InterruptedException {
    if (channelFull != null) {
      channelFull.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }
  }

}
