package stest.tron.wallet.common.deploy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.WalletGrpc;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.Utils;
import org.tron.core.Wallet;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.Parameter.CommonConstant;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class approveGatewayProposal {


    private final String testDepositTrx = Configuration.getByPath("testng.conf")
        .getString("witness.key2");
    private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
    ECKey ecKey1 = new ECKey(Utils.getRandom());
    byte[] depositAddress = ecKey1.getAddress();
    String testKeyFordeposit = ByteArray.toHexString(ecKey1.getPrivKeyBytes());
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
    public void testApproveProposal_1000001L() {
        String mainChainAddress = Configuration.getByPath("testng.conf")
            .getString("gateway_address.chainIdAddress");
        String sideChainAddress = "";
        try {
            File sideChainFile = new File("/home/sideChainGatewayAddress");
            FileReader reader2 = new FileReader(sideChainFile);
            BufferedReader breader2 = new BufferedReader(reader2);
            sideChainAddress = breader2.readLine();
            breader2.close();
        } catch (Exception e) {
            logger.info("Read main/side Gateway ContractAddress Failed");
            return;
        }

        HashMap<Long, String> proposalMap = new HashMap<Long, String>();
        logger.info("sideChainAddress: " + sideChainAddress);
        logger.info("testDepositTrx: " + testDepositTrx);
        proposalMap.put(1000001L, sideChainAddress);
        org.testng.Assert.assertTrue(PublicMethed.sideChainCreateProposal(testDepositAddress,
            testDepositTrx, mainChainAddress, proposalMap, blockingStubFull));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Get proposal list
        SideChainProposalList proposalList = blockingStubFull
            .listSideChainProposals(EmptyMessage.newBuilder().build());
        Optional<SideChainProposalList> listProposals = Optional.ofNullable(proposalList);
        final Integer proposalId = listProposals.get().getProposalsCount();
        logger.info(Integer.toString(proposalId));

        //Get proposal list after approve
        proposalList = blockingStubFull.listSideChainProposals(EmptyMessage.newBuilder().build());
        listProposals = Optional.ofNullable(proposalList);
        logger.info(Integer.toString(listProposals.get().getProposals(0).getApprovalsCount()));

        String[] witnessKey = {
            Configuration.getByPath("testng.conf")
                .getString("witness.key1"),
            Configuration.getByPath("testng.conf")
                .getString("witness.key2"),
        };
        byte[] witnessAddress;
        for (String key : witnessKey) {
            witnessAddress = PublicMethed.getFinalAddress(key);
            PublicMethed.approveProposal(witnessAddress, key, mainChainAddress, proposalId,
                true, blockingStubFull);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(enabled = true)
    public void testApproveProposal_1000012L() {


        for (int i = 0;i<60; i++){
            SideChainProposalList sideChainProposalList = blockingStubFull
                .listSideChainProposals(EmptyMessage.newBuilder().build());
            Optional<SideChainProposalList> result = Optional.ofNullable(sideChainProposalList);
            if(result.get().getProposals(0).getState().name() == "APPROVED"){
                logger.info("proposal 1000012L has APPROVED ");
                break;
            }
            else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        String mainChainAddress = Configuration.getByPath("testng.conf")
            .getString("gateway_address.chainIdAddress");

        HashMap<Long, String> proposalMap = new HashMap<Long, String>();
        logger.info("testDepositTrx: " + testDepositTrx);
        proposalMap.put(1000012L, String.valueOf(1L));
        org.testng.Assert.assertTrue(PublicMethed.sideChainCreateProposal(testDepositAddress,
            testDepositTrx, mainChainAddress, proposalMap, blockingStubFull));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Get proposal list
        SideChainProposalList proposalList = blockingStubFull
            .listSideChainProposals(EmptyMessage.newBuilder().build());
        Optional<SideChainProposalList> listProposals = Optional.ofNullable(proposalList);
        final Integer proposalId = listProposals.get().getProposalsCount();
        logger.info(Integer.toString(proposalId));

        //Get proposal list after approve
        proposalList = blockingStubFull.listSideChainProposals(EmptyMessage.newBuilder().build());
        listProposals = Optional.ofNullable(proposalList);
        logger.info(Integer.toString(listProposals.get().getProposals(0).getApprovalsCount()));

        String[] witnessKey = {
            Configuration.getByPath("testng.conf")
                .getString("witness.key1"),
            Configuration.getByPath("testng.conf")
                .getString("witness.key2"),
        };
        byte[] witnessAddress;
        for (String key : witnessKey) {
            witnessAddress = PublicMethed.getFinalAddress(key);
            PublicMethed.approveProposal(witnessAddress, key, mainChainAddress, proposalId,
                true, blockingStubFull);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test(enabled = true)
    public void testListProposal() {
        SideChainProposalList sideChainProposalList = blockingStubFull
            .listSideChainProposals(EmptyMessage.newBuilder().build());
        Optional<SideChainProposalList> result = Optional.ofNullable(sideChainProposalList);
        if (result.isPresent()) {
            SideChainProposalList proposalList = result.get();

            for (int i = 0; i < proposalList.getProposalsCount(); i++) {
                logger.info("" + proposalList.getProposals(i).toString());
            }
        } else {
            logger.info("List witnesses " + " failed !!");
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