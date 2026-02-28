package org.tron.core.consensus;

import com.google.protobuf.ByteString;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.DBConfig;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.CodeCapsule;
import org.tron.core.capsule.ProposalCapsule;
import org.tron.core.capsule.WitnessCapsule;
import org.tron.core.config.args.Parameter.ForkBlockVersionEnum;
import org.tron.core.config.args.Witness;
import org.tron.core.db.Manager;
import org.tron.core.utils.ProposalUtil;
import org.tron.core.vm.config.GatewayCode;
import org.tron.protos.Protocol.AccountType;

/**
 * Notice:
 *
 * if you want to add a proposal,you just should add a enum ProposalType and add the valid in the
 * validator method, add the process in the process method
 */
@Slf4j
public class ProposalService extends ProposalUtil {

  public static boolean process(Manager manager, ProposalCapsule proposalCapsule) {
    Map<Long, String> map = proposalCapsule.getInstance().getParametersMap();
    boolean find = true;
    for (Map.Entry<Long, String> entry : map.entrySet()) {
      ProposalType proposalType = ProposalType.getEnumOrNull(entry.getKey());
      if (proposalType == null) {
        find = false;
        continue;
      }
      switch (proposalType) {
        case MAINTENANCE_TIME_INTERVAL: {
          manager.getDynamicPropertiesStore().saveMaintenanceTimeInterval(Long.valueOf(entry.getValue()));
          break;
        }
        case ACCOUNT_UPGRADE_COST: {
          manager.getDynamicPropertiesStore().saveAccountUpgradeCost(Long.valueOf(entry.getValue()));
          break;
        }
        case CREATE_ACCOUNT_FEE: {
          manager.getDynamicPropertiesStore().saveCreateAccountFee(Long.valueOf(entry.getValue()));
          break;
        }
        case TRANSACTION_FEE: {
          manager.getDynamicPropertiesStore().saveTransactionFee(Long.valueOf(entry.getValue()));
          break;
        }
        case ASSET_ISSUE_FEE: {
          manager.getDynamicPropertiesStore().saveAssetIssueFee(Long.valueOf(entry.getValue()));
          break;
        }
        case WITNESS_PAY_PER_BLOCK: {
          manager.getDynamicPropertiesStore().saveWitnessPayPerBlock(Long.valueOf(entry.getValue()));
          break;
        }
        case WITNESS_STANDBY_ALLOWANCE: {
          manager.getDynamicPropertiesStore().saveWitnessStandbyAllowance(Long.valueOf(entry.getValue()));
          break;
        }
        case CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT: {
          manager.getDynamicPropertiesStore()
              .saveCreateNewAccountFeeInSystemContract(Long.valueOf(entry.getValue()));
          break;
        }
        case CREATE_NEW_ACCOUNT_BANDWIDTH_RATE: {
          manager.getDynamicPropertiesStore().saveCreateNewAccountBandwidthRate(Long.valueOf(entry.getValue()));
          break;
        }
        // case ALLOW_CREATION_OF_CONTRACTS: {
        //   manager.getDynamicPropertiesStore().saveAllowCreationOfContracts(Long.valueOf(entry.getValue()));
        //   break;
        // }
        case REMOVE_THE_POWER_OF_THE_GR: {
          if (manager.getDynamicPropertiesStore().getRemoveThePowerOfTheGr() == 0) {
            manager.getDynamicPropertiesStore().saveRemoveThePowerOfTheGr(Long.valueOf(entry.getValue()));
          }
          break;
        }
        case ENERGY_FEE: {
          manager.getDynamicPropertiesStore().saveEnergyFee(Long.valueOf(entry.getValue()));
          break;
        }
        case EXCHANGE_CREATE_FEE: {
          manager.getDynamicPropertiesStore().saveExchangeCreateFee(Long.valueOf(entry.getValue()));
          break;
        }
        case MAX_CPU_TIME_OF_ONE_TX: {
          manager.getDynamicPropertiesStore().saveMaxCpuTimeOfOneTx(Long.valueOf(entry.getValue()));
          break;
        }
        case ALLOW_UPDATE_ACCOUNT_NAME: {
          manager.getDynamicPropertiesStore().saveAllowUpdateAccountName(Long.valueOf(entry.getValue()));
          break;
        }
        // case ALLOW_SAME_TOKEN_NAME: {
        //   manager.getDynamicPropertiesStore().saveAllowSameTokenName(Long.valueOf(entry.getValue()));
        //   break;
        // }
        // case ALLOW_DELEGATE_RESOURCE: {
        //   manager.getDynamicPropertiesStore().saveAllowDelegateResource(Long.valueOf(entry.getValue()));
        //   break;
        // }
        case TOTAL_ENERGY_LIMIT: {
          manager.getDynamicPropertiesStore().saveTotalEnergyLimit(Long.valueOf(entry.getValue()));
          break;
        }
        // case ALLOW_TVM_TRANSFER_TRC10: {
        //   manager.getDynamicPropertiesStore().saveAllowTvmTransferTrc10(Long.valueOf(entry.getValue()));
        //   break;
        // }
        case TOTAL_CURRENT_ENERGY_LIMIT: {
          manager.getDynamicPropertiesStore().saveTotalEnergyLimit2(Long.valueOf(entry.getValue()));
          break;
        }
        // case ALLOW_MULTI_SIGN: {
        //   if (manager.getDynamicPropertiesStore().getAllowMultiSign() == 0) {
        //     manager.getDynamicPropertiesStore().saveAllowMultiSign(Long.valueOf(entry.getValue()));
        //   }
        //   break;
        // }
        case ALLOW_ADAPTIVE_ENERGY: {
          if (manager.getDynamicPropertiesStore().getAllowAdaptiveEnergy() == 0) {
            manager.getDynamicPropertiesStore().saveAllowAdaptiveEnergy(Long.valueOf(entry.getValue()));
            //24 * 60 * 2 . one minute,1/2 total limit.
            manager.getDynamicPropertiesStore().saveAdaptiveResourceLimitTargetRatio(2880);
            manager.getDynamicPropertiesStore().saveTotalEnergyTargetLimit(
                manager.getDynamicPropertiesStore().getTotalEnergyLimit() / 2880);
            manager.getDynamicPropertiesStore().saveAdaptiveResourceLimitMultiplier(50);
          }
          break;
        }
        case UPDATE_ACCOUNT_PERMISSION_FEE: {
          manager.getDynamicPropertiesStore().saveUpdateAccountPermissionFee(Long.valueOf(entry.getValue()));
          break;
        }
        case MULTI_SIGN_FEE: {
          manager.getDynamicPropertiesStore().saveMultiSignFee(Long.valueOf(entry.getValue()));
          break;
        }
        case ALLOW_PROTO_FILTER_NUM: {
          manager.getDynamicPropertiesStore().saveAllowProtoFilterNum(Long.valueOf(entry.getValue()));
          break;
        }
        case ALLOW_ACCOUNT_STATE_ROOT: {
          manager.getDynamicPropertiesStore().saveAllowAccountStateRoot(Long.valueOf(entry.getValue()));
          break;
        }
        // case ALLOW_TVM_CONSTANTINOPLE: {
        //   manager.getDynamicPropertiesStore().saveAllowTvmConstantinople(Long.valueOf(entry.getValue()));
        //   TODO: sun-network
          // manager.getDynamicPropertiesStore().addSystemContractAndSetPermission(48);
          // break;
        // }
        case ALLOW_TVM_SOLIDITY_059: {
          manager.getDynamicPropertiesStore().saveAllowTvmSolidity059(Long.valueOf(entry.getValue()));
          break;
        }
        case ADAPTIVE_RESOURCE_LIMIT_TARGET_RATIO: {
          long ratio = 24 * 60 * Long.valueOf(entry.getValue());
          manager.getDynamicPropertiesStore().saveAdaptiveResourceLimitTargetRatio(ratio);
          manager.getDynamicPropertiesStore().saveTotalEnergyTargetLimit(
              manager.getDynamicPropertiesStore().getTotalEnergyLimit() / ratio);
          break;
        }
        case ADAPTIVE_RESOURCE_LIMIT_MULTIPLIER: {
          manager.getDynamicPropertiesStore().saveAdaptiveResourceLimitMultiplier(Long.valueOf(entry.getValue()));
          break;
        }
//        case ALLOW_CHANGE_DELEGATION: {
//          manager.getDynamicPropertiesStore().saveChangeDelegation(Long.valueOf(entry.getValue()));
//          // TODO: sun-network
//          manager.getDynamicPropertiesStore().addSystemContractAndSetPermission(49);
//          break;
//        }
        case WITNESS_127_PAY_PER_BLOCK: {
          manager.getDynamicPropertiesStore().saveWitness127PayPerBlock(Long.valueOf(entry.getValue()));
          break;
        }
//        case ALLOW_SHIELDED_TRANSACTION: {
//          if (manager.getDynamicPropertiesStore().getAllowShieldedTransaction() == 0) {
//            manager.getDynamicPropertiesStore().saveAllowShieldedTransaction(entry.getValue());
//            manager.getDynamicPropertiesStore().addSystemContractAndSetPermission(51);
//          }
//          break;
//        }
//        case SHIELDED_TRANSACTION_FEE: {
//          manager.getDynamicPropertiesStore().saveShieldedTransactionFee(entry.getValue());
//          break;
//        }
//        case SHIELDED_TRANSACTION_CREATE_ACCOUNT_FEE: {
//          manager.getDynamicPropertiesStore()
//              .saveShieldedTransactionCreateAccountFee(entry.getValue());
//          break;
//        }
        case FORBID_TRANSFER_TO_CONTRACT: {
          manager.getDynamicPropertiesStore().saveForbidTransferToContract(Long.valueOf(entry.getValue()));
          break;
        }

        /**
         *  Side Chain proposals
         */

        case ALLOW_CHARGING_FEE: {
          manager.getDynamicPropertiesStore()
              .saveChargingSwitch(Long.valueOf(entry.getValue()));
          break;
        }
        case SIDE_CHAIN_GATEWAY: {
          // replace all side chain gateway address
          List<String> list = Arrays.asList(entry.getValue().split(","));
          List<byte[]> byteList = list.stream().map(element -> Wallet
              .decodeFromBase58Check(element)).collect(Collectors.toList());
          manager.getDynamicPropertiesStore().saveSideChainGateWayList(byteList);
          break;
        }
        case MAIN_CHAIN_GATEWAY: {
          // replace all main chain gateway address
          List<String> list = Arrays.asList(entry.getValue().split(","));
          List<byte[]> byteList = list.stream().map(element -> Wallet
              .decodeFromBase58Check(element)).collect(Collectors.toList());
          manager.getDynamicPropertiesStore().saveMainChainGateWayList(byteList);
          break;
        }
        case PROPOSAL_EXPIRE_TIME: {
          // replace all side chain proposal expire time
          manager.getDynamicPropertiesStore()
              .saveProposalExpireTime(Long.valueOf(entry.getValue()));
          break;
        }
        case ALLOW_VOTE_WITNESS: {
          manager.getDynamicPropertiesStore()
              .saveVoteWitnessSwitch(Long.valueOf(entry.getValue()));
          break;
        }
        case MAX_GATEWAY_CONTRACT_SIZE: {
          manager.getDynamicPropertiesStore()
              .saveMaxGateWayContractSize(Long.valueOf(entry.getValue()));
          break;
        }
//        case (1_000_006): {
//          manager.getDynamicPropertiesStore()
//                  .saveSideChainChargingBandwidth(Long.valueOf(entry.getValue()));
//          break;
//        }
        case FUND_INJECT_ADDRESS: {
          manager.getDynamicPropertiesStore()
              .saveFundInjectAddress(Wallet
                  .decodeFromBase58Check(entry.getValue()));
          break;
        }
        case ALLOW_FUND_DISTRIBUTION: {
          manager.getDynamicPropertiesStore()
              .saveFundDistributeEnableSwitch(Long.valueOf(entry.getValue()));
          break;
        }
        case FUND_DISTRIBUTION_DAYS: {
          manager.getDynamicPropertiesStore()
              .saveDayToSustainByFund(Long.valueOf(entry.getValue()));
          break;
        }
        case WITNESS_REWARD_PERCENTAGE: {
          manager.getDynamicPropertiesStore()
              .savePercentToPayWitness(Long.valueOf(entry.getValue()));
          break;
        }
        case ALLOW_DAPP_152: {
          manager.getDynamicPropertiesStore()
              .saveAllowDappV152(Long.valueOf(entry.getValue()));
          break;
        }
        case WITNESS_MAX_NUMBER: {
          int oldNum = manager.getDynamicPropertiesStore().getWitnessMaxActiveNum();
          int newNum = Integer.parseInt(entry.getValue());
          if (newNum <= oldNum) {
            manager.getDynamicPropertiesStore()
                .saveWitnessMaxActiveNum(Integer.parseInt(entry.getValue()));
            break;
          }
          List<Witness> witnessList = DBConfig.getGenesisBlock().getWitnesses();
          if (witnessList.size() < newNum) {
            logger.error("size of witness in genesis block : {} must greater than value : {}",
                witnessList.size(), newNum);
            System.exit(1);
          }
          List<Witness> subWitnessList = witnessList.subList(oldNum, newNum);
          subWitnessList.forEach(key -> {
            byte[] keyAddress = key.getAddress();
            ByteString address = ByteString.copyFrom(keyAddress);

            final AccountCapsule accountCapsule;
            if (!manager.getAccountStore().has(keyAddress)) {
              accountCapsule = new AccountCapsule(ByteString.EMPTY,
                  address, AccountType.AssetIssue, 0L);
            } else {
              accountCapsule = manager.getAccountStore().getUnchecked(keyAddress);
            }
            accountCapsule.setIsWitness(true);
            manager.getAccountStore().put(keyAddress, accountCapsule);

            WitnessCapsule witnessCapsule = manager.getWitnessStore().get(keyAddress);
            if (Objects.isNull(witnessCapsule)) {
              witnessCapsule = new WitnessCapsule(address, key.getVoteCount(), key.getUrl());
            } else {
              witnessCapsule.setVoteCount(witnessCapsule.getVoteCount() + key.getVoteCount());
            }
            witnessCapsule.setIsJobs(true);
            manager.getWitnessStore().put(keyAddress, witnessCapsule);
          });
          manager.getDynamicPropertiesStore()
              .saveWitnessMaxActiveNum(Integer.parseInt(entry.getValue()));
          break;
        }
        case ALLOW_UPDATE_GATEWAY102: {
          manager.getDynamicPropertiesStore()
              .saveAllowUpdateGatewayV102(Long.valueOf(entry.getValue()));

          byte[] byteCode = ByteArray.fromHexString(GatewayCode.gatewayCode);

          CodeCapsule codeCapsule = new CodeCapsule(byteCode);
          manager.getCodeStore()
              .put(manager.getDynamicPropertiesStore().getSideChainGateWayList().get(0),
                  codeCapsule);
          break;
        }
        default:
          find = false;
          break;
      }
    }
    return find;
  }

}
