package stest.tron.wallet.common.deploy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
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
public class deploySideGateway {


    private final String oracleKey = Configuration.getByPath("testng.conf")
        .getString("oralceAccountKey.key1");
    private final byte[] oracleAddress = PublicMethed.getFinalAddress(oracleKey);
    private final String foundationKey003 = Configuration.getByPath("testng.conf")
        .getString("foundationAccount.key3");
    private final byte[] foundationAddress003 = PublicMethed.getFinalAddress(foundationKey003);
    private Long maxFeeLimit = Configuration.getByPath("testng.conf")
        .getLong("defaultParameter.maxFeeLimit");
    private ManagedChannel channelFull = null;
    private WalletGrpc.WalletBlockingStub blockingStubFull = null;


    private WalletSolidityGrpc.WalletSolidityBlockingStub blockingStubSolidity = null;

    private String fullnode = Configuration.getByPath("testng.conf")
        .getStringList("fullnode.ip.list").get(1);

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
    }

    @Test(enabled = true, description = "deploy Side Chain Gateway")
    public void test1DepositTrc20001() {
        String mainChainAddress = Configuration.getByPath("testng.conf")
            .getString("gateway_address.chainIdAddress");
    /*try {
      File mainChainFile = new File("/home/mainChainGatewayAddress");
      FileReader reader = new FileReader(mainChainFile);
      BufferedReader breader = new BufferedReader(reader);
      mainChainAddress = breader.readLine();
      breader.close();
    } catch (Exception e) {
      logger.info("Read main Gateway ContractAddress Failed");
      return;
    }*/

        int count = 0;
        String sideChainGatewayAddress = null;
        while (count < 3) {
            Account accountOralce = PublicMethed.queryAccount(oracleAddress, blockingStubFull);
            long oralceBalance = accountOralce.getBalance();
            logger.info("OralceBalance: " + oralceBalance);

            String contractName = "gateWaysidechainContract";
            String code = null;
            String abi = null;
            String parame = "\"" + Base58.encode58Check(oracleAddress) + "\"";

            try {
                code = PublicMethed.fileRead("/home/ABI_ByteCode/sidegateway/SideChainGateway.bin", false);
                abi = PublicMethed.fileRead("/home/ABI_ByteCode/sidegateway/SideChainGateway.abi", false);
            } catch (Exception e) {
                Assert.fail("Read ABI Failed");
                return;
            }

            String deployTxid = PublicMethed
                .deploySideContractWithConstantParame(contractName, abi, code, "#",
                    "#", "", maxFeeLimit, 0L, 100,
                    null, foundationKey003, foundationAddress003, mainChainAddress, blockingStubFull);
            PublicMethed.waitProduceNextBlock(blockingStubFull);
            PublicMethed.waitProduceNextBlock(blockingStubFull);

            Optional<TransactionInfo> infoById = PublicMethed
                .getTransactionInfoById(deployTxid, blockingStubFull);
            logger.info("infoById: " + infoById);
            byte[] sideChainGateway = infoById.get().getContractAddress().toByteArray();
            sideChainGatewayAddress = WalletClient.encode58Check(sideChainGateway);
            if (deployTxid == null || sideChainGateway.equals("3QJmnh")) {
                count += 1;
                continue;
            } else {
                byte[] input = Hex.decode(AbiUtil.parseMethod("addOracle(address)", parame, false));
                String triggerTxid1 = PublicMethed.triggerContractSideChain(sideChainGateway,
                    WalletClient.decodeFromBase58Check(mainChainAddress), 0, input, maxFeeLimit,
                    0, "0", foundationAddress003, foundationKey003, blockingStubFull);
                PublicMethed.waitProduceNextBlock(blockingStubFull);
                Optional<TransactionInfo> infoById1 = PublicMethed
                    .getTransactionInfoById(triggerTxid1, blockingStubFull);
                if (triggerTxid1 == null || infoById1.get().getResultValue() == 1) {
                    count += 1;
                    continue;
                } else {
                    break;
                }
            }
        }

        String outputPath = "./src/test/resources/sideChainGatewayAddress";
        try {
            File mainChainFile = new File(outputPath);
            Boolean cun = mainChainFile.createNewFile();
            FileWriter writer = new FileWriter(mainChainFile);
            BufferedWriter out = new BufferedWriter(writer);
            out.write(sideChainGatewayAddress);

            out.close();
            writer.close();
        } catch (Exception e) {
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