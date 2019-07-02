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
public class deploySideGateway {


  private final String testDepositTrx = "324a2052e491e99026442d81df4d2777292840c1b3949e20696c49096c6bacb7";
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

  private String fullnode = Configuration.getByPath("testng.conf")
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
    channelFull = ManagedChannelBuilder.forTarget(fullnode)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
  }

  @Test(enabled = true, description = "deploy Side Chain Gateway")
  public void test1DepositTrc20001() {
    String mainChainAddress = "";
    try {
      File mainChainFile = new File("/home/mainChainGatewayAddress");
      FileReader reader = new FileReader(mainChainFile);
      BufferedReader breader = new BufferedReader(reader);
      mainChainAddress = breader.readLine();
      breader.close();
    }catch (Exception e){
      logger.info("Read main Gateway ContractAddress Failed");
      return;
    }

    int count = 0;
    String mainChainGatewayAddress = null;
    while (count<3) {
      PublicMethed.printAddress(testKeyFordeposit);

      Account accountOralce = PublicMethed.queryAccount(depositAddress, blockingStubFull);
      long OralceBalance = accountOralce.getBalance();
      logger.info("OralceBalance: " + OralceBalance);

      String contractName = "gateWaysidechainContract";
      String code = Configuration.getByPath("testng.conf")
          .getString("code.code_SideGateway");
      String abi = Configuration.getByPath("testng.conf")
          .getString("abi.abi_SideGateway");
      String parame = "\"" + Base58.encode58Check(testDepositAddress) + "\"";

      String deployTxid = PublicMethed
          .deploySideContractWithConstantParame(contractName, abi, code, "constructor(address)",
              parame, "",
              maxFeeLimit,
              0L, 100, null, testDepositTrx, testDepositAddress,mainChainAddress
              , blockingStubFull);
      PublicMethed.waitProduceNextBlock(blockingStubFull);

      Optional<TransactionInfo> infoById = PublicMethed
          .getTransactionInfoById(deployTxid, blockingStubFull);
      logger.info("infoById: " + infoById);
      byte[] mainChainGateway = infoById.get().getContractAddress().toByteArray();
      mainChainGatewayAddress = WalletClient.encode58Check(mainChainGateway);
      if(infoById.get().getResultValue() != 0 || mainChainGateway.equals("3QJmnh")){
        count +=1;
        continue;
      }else {
        break;
      }
    }

    String outputPath = "./src/test/resources/sideChainGatewayAddress" ;
    try {
      File mainChainFile = new File(outputPath);
      Boolean cun = mainChainFile.createNewFile();
      FileWriter writer = new FileWriter(mainChainFile);
      BufferedWriter out = new BufferedWriter(writer);
      out.write(mainChainGatewayAddress);

      out.close();
      writer.close();
    }catch (Exception e){
      e.printStackTrace();
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