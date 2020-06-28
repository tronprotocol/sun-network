package stest.tron.wallet.common.deploy;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.HashMap;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.SideChainProposalList;
import org.tron.api.WalletGrpc;
import org.tron.protos.Protocol.SideChainParameters;
import stest.tron.wallet.common.client.Configuration;
import stest.tron.wallet.common.client.WalletClient;
import stest.tron.wallet.common.client.utils.PublicMethed;

@Slf4j
public class deployFundInject {

  final String mainGateWayAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.key1");
  final String ChainIdAddress = Configuration.getByPath("testng.conf")
      .getString("gateway_address.chainIdAddress");
  private final String testDepositTrx = Configuration.getByPath("testng.conf")
      .getString("witness.key2");
  private final byte[] testDepositAddress = PublicMethed.getFinalAddress(testDepositTrx);
  private final String foundationKey003 = Configuration.getByPath("testng.conf")
      .getString("foundationAccount.key2");
  private final byte[] foundationAddress003 = PublicMethed.getFinalAddress(foundationKey003);
  private Long maxFeeLimit = Configuration.getByPath("testng.conf")
      .getLong("defaultParameter.maxFeeLimit");
  private ManagedChannel channelFull = null;
  private WalletGrpc.WalletBlockingStub blockingStubFull = null;

  private String fullnode1 = Configuration.getByPath("testng.conf")
      .getStringList("fullnode.ip.list").get(1);


  @BeforeSuite
  public void beforeSuite() {
    channelFull = ManagedChannelBuilder.forTarget(fullnode1)
        .usePlaintext(true)
        .build();
    blockingStubFull = WalletGrpc.newBlockingStub(channelFull);
    SideChainParameters sideChainParameters = blockingStubFull
        .getSideChainParameters(EmptyMessage.newBuilder().build());
    Optional<SideChainParameters> getChainParameters = Optional.ofNullable(sideChainParameters);
    long fundBefore = Long.valueOf(getChainParameters.get().getChainParameter(33).getValue());

    Assert.assertTrue(PublicMethed.fundInject(foundationAddress003, foundationKey003,
        300000000000L, WalletClient.decodeFromBase58Check(ChainIdAddress),
        blockingStubFull));
    PublicMethed.waitProduceNextBlock(blockingStubFull);

    sideChainParameters = blockingStubFull
        .getSideChainParameters(EmptyMessage.newBuilder().build());
    getChainParameters = Optional.ofNullable(sideChainParameters);
    long fundAfter = Long.valueOf(getChainParameters.get().getChainParameter(33).getValue());
    Assert.assertTrue(fundAfter > fundBefore);

    for (int i = 0; i < 3; i++) {
      String chainIdAddress = Configuration.getByPath("testng.conf")
          .getString("gateway_address.chainIdAddress");
      String FundAccount = WalletClient.encode58Check(foundationAddress003);
      HashMap<Long, String> proposalMap = new HashMap<Long, String>();
      logger.info("testDepositTrx: " + testDepositTrx);
      logger.info("FundAccount: " + FundAccount);
      proposalMap.put(1000007L, FundAccount);
      org.testng.Assert.assertTrue(PublicMethed.sideChainCreateProposal(testDepositAddress,
          testDepositTrx, chainIdAddress, proposalMap, blockingStubFull));
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //Get proposal list
      SideChainProposalList proposalList = blockingStubFull
          .listSideChainProposals(EmptyMessage.newBuilder().build());
      Optional<SideChainProposalList> listProposals = Optional.ofNullable(proposalList);
      Integer proposalId = listProposals.get().getProposalsCount();
      logger.info(Integer.toString(proposalId));

      //Get proposal list after approve
      proposalList = blockingStubFull.listSideChainProposals(EmptyMessage.newBuilder().build());
      listProposals = Optional.ofNullable(proposalList);

      String[] witnessKey = {
          Configuration.getByPath("testng.conf")
              .getString("witness.key1"),
          Configuration.getByPath("testng.conf")
              .getString("witness.key2"),
      };
      byte[] witnessAddress;
      for (String key : witnessKey) {
        witnessAddress = PublicMethed.getFinalAddress(key);
        PublicMethed.approveProposal(witnessAddress, key, chainIdAddress, proposalId,
            true, blockingStubFull);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      int count = 0;
      while (count++ < 150) {
        proposalList = blockingStubFull.listSideChainProposals(EmptyMessage.newBuilder().build());
        if (proposalList.getProposalsList().get(0).getState().name() == "APPROVED") {
          break;
        } else {
          try {
            Thread.sleep(1000);

          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      if (count >= 150) {
        continue;
      }

      proposalMap.clear();
      proposalMap.put(1000008L, "1");
      org.testng.Assert.assertTrue(PublicMethed.sideChainCreateProposal(testDepositAddress,
          testDepositTrx, chainIdAddress, proposalMap, blockingStubFull));
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      //Get proposal list
      proposalList = blockingStubFull
          .listSideChainProposals(EmptyMessage.newBuilder().build());
      listProposals = Optional.ofNullable(proposalList);
      proposalId = listProposals.get().getProposalsCount();
      logger.info(Integer.toString(proposalId));

      //Get proposal list after approve
      proposalList = blockingStubFull.listSideChainProposals(EmptyMessage.newBuilder().build());
      listProposals = Optional.ofNullable(proposalList);

      for (String key : witnessKey) {
        witnessAddress = PublicMethed.getFinalAddress(key);
        PublicMethed.approveProposal(witnessAddress, key, chainIdAddress, proposalId,
            true, blockingStubFull);
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      count = 0;
      while (count++ < 150) {
        proposalList = blockingStubFull.listSideChainProposals(EmptyMessage.newBuilder().build());
        if (proposalList.getProposalsList().get(0).getState().name() == "APPROVED") {
          break;
        } else {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
      if (count >= 150) {
        continue;
      } else {
        break;
      }

    }


  }

  @Test(enabled = true)
  public void test1DepositTrc20001() {

  }
}
