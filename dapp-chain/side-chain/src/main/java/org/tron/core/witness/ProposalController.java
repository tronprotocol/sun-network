package org.tron.core.witness;

import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.Wallet;
import org.tron.core.capsule.ProposalCapsule;
import org.tron.core.db.Manager;
import org.tron.protos.Protocol.SideChainProposal.State;

@Slf4j(topic = "witness")
public class ProposalController {

  @Setter
  @Getter
  private Manager manager;

  public static ProposalController createInstance(Manager manager) {
    ProposalController instance = new ProposalController();
    instance.setManager(manager);
    return instance;
  }


  public void processProposals() {
    long latestProposalNum = manager.getDynamicPropertiesStore().getLatestProposalNum();
    if (latestProposalNum == 0) {
      logger.info("latestProposalNum is 0,return");
      return;
    }

    long proposalNum = latestProposalNum;

    ProposalCapsule proposalCapsule = null;

    while (proposalNum > 0) {
      try {
        proposalCapsule = manager.getProposalStore()
            .get(ProposalCapsule.calculateDbKey(proposalNum));
      } catch (Exception ex) {
        logger.error("", ex);
        continue;
      }

      if (proposalCapsule.hasProcessed()) {
        logger
            .info("Proposal has processed，id:[{}],skip it and before it",
                proposalCapsule.getID());
        //proposals with number less than this one, have been processed before
        break;
      }

      if (proposalCapsule.hasCanceled()) {
        logger.info("Proposal has canceled，id:[{}],skip it", proposalCapsule.getID());
        proposalNum--;
        continue;
      }

      long currentTime = manager.getDynamicPropertiesStore().getNextMaintenanceTime();
      if (proposalCapsule.hasExpired(currentTime)) {
        processProposal(proposalCapsule);
        proposalNum--;
        continue;
      }

      proposalNum--;
      logger.info("Proposal has not expired，id:[{}],skip it", proposalCapsule.getID());
    }
    logger.info("Processing proposals done, oldest proposal[{}]", proposalNum);
  }

  public void processProposal(ProposalCapsule proposalCapsule) {

    List<ByteString> activeWitnesses = this.manager.getWitnessScheduleStore().getActiveWitnesses();
    if (proposalCapsule.hasMostApprovals(activeWitnesses)) {
      logger.info(
          "Processing proposal,id:{},it has received most approvals, "
              + "begin to set dynamic parameter:{}, "
              + "and set proposal state as APPROVED",
          proposalCapsule.getID(), proposalCapsule.getParameters());
      setDynamicParameters(proposalCapsule);
      proposalCapsule.setState(State.APPROVED);
      manager.getProposalStore().put(proposalCapsule.createDbKey(), proposalCapsule);
    } else {
      logger.info(
          "Processing proposal,id:{}, "
              + "it has not received enough approvals, set proposal state as DISAPPROVED",
          proposalCapsule.getID());
      proposalCapsule.setState(State.DISAPPROVED);
      manager.getProposalStore().put(proposalCapsule.createDbKey(), proposalCapsule);
    }

  }

  public void setDynamicParameters(ProposalCapsule proposalCapsule) {
    Map<Long, String> map = proposalCapsule.getInstance().getParametersMap();
    for (Map.Entry<Long, String> entry : map.entrySet()) {

      switch (entry.getKey().intValue()) {
        case (0): {
          manager.getDynamicPropertiesStore()
              .saveMaintenanceTimeInterval(Long.valueOf(entry.getValue()));
          break;
        }
        case (1): {
          manager.getDynamicPropertiesStore()
              .saveAccountUpgradeCost(Long.valueOf(entry.getValue()));
          break;
        }
        case (2): {
          manager.getDynamicPropertiesStore()
              .saveCreateAccountFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (3): {
          manager.getDynamicPropertiesStore().saveTransactionFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (4): {
          manager.getDynamicPropertiesStore().saveAssetIssueFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (5): {
          manager.getDynamicPropertiesStore()
              .saveWitnessPayPerBlock(Long.valueOf(entry.getValue()));
          break;
        }
        case (6): {
          manager.getDynamicPropertiesStore()
              .saveWitnessStandbyAllowance(Long.valueOf(entry.getValue()));
          break;
        }
        case (7): {
          manager.getDynamicPropertiesStore()
              .saveCreateNewAccountFeeInSystemContract(Long.valueOf(entry.getValue()));
          break;
        }
        case (8): {
          manager.getDynamicPropertiesStore()
              .saveCreateNewAccountBandwidthRate(Long.valueOf(entry.getValue()));
          break;
        }
        // default to allow
//        case (9): {
//          manager.getDynamicPropertiesStore().saveAllowCreationOfContracts(entry.getValue());
//          break;
//        }
        case (10): {
          if (manager.getDynamicPropertiesStore().getRemoveThePowerOfTheGr() == 0) {
            manager.getDynamicPropertiesStore()
                .saveRemoveThePowerOfTheGr(Long.valueOf(entry.getValue()));
          }
          break;
        }
        case (11): {
          manager.getDynamicPropertiesStore()
              .saveEnergyFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (12): {
          manager.getDynamicPropertiesStore()
              .saveExchangeCreateFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (13): {
          manager.getDynamicPropertiesStore()
              .saveMaxCpuTimeOfOneTx(Long.valueOf(entry.getValue()));
          break;
        }
        case (14): {
          manager.getDynamicPropertiesStore().saveAllowUpdateAccountName(Long.valueOf(entry.getValue()));
          break;
        }
        // default to allow same token
//        case (15): {
//          manager.getDynamicPropertiesStore().saveAllowSameTokenName(entry.getValue());
//          break;
//        }
//        case (16): {
//          manager.getDynamicPropertiesStore()
//              .saveAllowDelegateResource(Long.valueOf(entry.getValue()));
//          break;
//        }
        case (17): {
          manager.getDynamicPropertiesStore()
              .saveTotalEnergyLimit(Long.valueOf(entry.getValue()));
          break;
        }
//        case (18): {
//          manager.getDynamicPropertiesStore()
//              .saveAllowTvmTransferTrc10(Long.valueOf(entry.getValue()));
//          break;
//        }
        case (19): {
          manager.getDynamicPropertiesStore()
              .saveTotalEnergyLimit2(Long.valueOf(entry.getValue()));
          break;
        }
//        case (20): {
//          if (manager.getDynamicPropertiesStore().getAllowMultiSign() == 0) {
//            manager.getDynamicPropertiesStore().saveAllowMultiSign(Long.valueOf(entry.getValue()));
//          }
//          break;
//        }
        case (21): {
          if (manager.getDynamicPropertiesStore().getAllowAdaptiveEnergy() == 0) {
            manager.getDynamicPropertiesStore()
                .saveAllowAdaptiveEnergy(Long.valueOf(entry.getValue()));
          }
          break;
        }
        case (22): {
          manager.getDynamicPropertiesStore()
              .saveUpdateAccountPermissionFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (23): {
          manager.getDynamicPropertiesStore()
              .saveMultiSignFee(Long.valueOf(entry.getValue()));
          break;
        }
        case (24): {
          manager.getDynamicPropertiesStore().saveAllowProtoFilterNum(Long.valueOf(entry.getValue()));
          break;
        }
        case (25): {
          manager.getDynamicPropertiesStore().saveAllowAccountStateRoot(Long.valueOf(entry.getValue()));
          break;
        }
//        case (26): {
//          //should not to do anything, we initiated this value in dynamic store
////          manager.getDynamicPropertiesStore().addSystemContractAndSetPermission(48);
//          break;
//        }
        case (1_000_000): {
          manager.getDynamicPropertiesStore()
              .saveChargingSwitch(Long.valueOf(entry.getValue()));
          break;
        }
        case (1_000_001): {
          // replace all side chain gateway address
          List<String> list = Arrays.asList(entry.getValue().split(","));
          List<byte[]> byteList = list.stream().map(element -> Wallet
              .decodeFromBase58Check(element)).collect(Collectors.toList());
          manager.getDynamicPropertiesStore().saveSideChainGateWayList(byteList);
          break;
        }
        case (1_000_002): {
          // replace all main chain gateway address
          List<String> list = Arrays.asList(entry.getValue().split(","));
          List<byte[]> byteList = list.stream().map(element -> Wallet
              .decodeFromBase58Check(element)).collect(Collectors.toList());
          manager.getDynamicPropertiesStore().saveMainChainGateWayList(byteList);
          break;
        }
        case (1_000_003): {
          // replace all side chain proposal expire time
          manager.getDynamicPropertiesStore()
                  .saveProposalExpireTime(Long.valueOf(entry.getValue()));
          break;
        }
        case (1_000_004): {
          manager.getDynamicPropertiesStore()
              .saveVoteWitnessSwitch(Long.valueOf(entry.getValue()));
          break;
        }
        case (1_000_005): {
          manager.getDynamicPropertiesStore()
              .saveMaxGateWayContractSize(Long.valueOf(entry.getValue()));
          break;
        }
//        case (1_000_006): {
//          manager.getDynamicPropertiesStore()
//                  .saveSideChainChargingBandwidth(Long.valueOf(entry.getValue()));
//          break;
//        }
        case (1_000_007): {
          manager.getDynamicPropertiesStore()
              .saveFundInjectAddress(Wallet
                  .decodeFromBase58Check(entry.getValue()));
          break;
        }
        case (1_000_008): {
          manager.getDynamicPropertiesStore()
              .saveFundDistributeEnableSwitch(Long.valueOf(entry.getValue()));
          break;
        }
        case (1_000_009): {
          manager.getDynamicPropertiesStore()
              .saveDayToSustainByFund(Long.valueOf(entry.getValue()));
          break;
        }
        case (1_000_010): {
          manager.getDynamicPropertiesStore()
              .savePercentToPayWitness(Long.valueOf(entry.getValue()));
          break;
        }
        default:
          break;
      }
    }
  }


}
