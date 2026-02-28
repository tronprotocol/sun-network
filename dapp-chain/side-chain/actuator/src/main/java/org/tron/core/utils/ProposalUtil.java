package org.tron.core.utils;

import static org.tron.core.utils.ProposalUtil.ProposalType.validateStringContent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.Commons;
import org.tron.common.utils.DBConfig;
import org.tron.common.utils.ForkUtils;
import org.tron.core.ChainBaseManager;
import org.tron.core.Constant;
import org.tron.core.config.args.Parameter.ForkBlockVersionConsts;
import org.tron.core.config.args.Parameter.ForkBlockVersionEnum;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.vm.config.GatewayCode;
import org.tron.protos.Protocol.AccountType;

public class ProposalUtil {

  protected static final long LONG_VALUE = 100_000_000_000_000_000L;
  protected static final String BAD_PARAM_ID = "Bad chain parameter id";
  private static final String LONG_VALUE_ERROR =
      "Bad chain parameter value, valid range is [0," + LONG_VALUE + "]";

  public static void validator(ChainBaseManager manager, ForkUtils forkUtils,
      long code, String content)
      throws ContractValidateException {
    ProposalType proposalType = ProposalType.getEnum(code);
    long value = validateStringContent(proposalType, content);

    Integer witnessMaxActiveNum = manager.getDynamicPropertiesStore()
            .getWitnessMaxActiveNum();
    switch (proposalType) {
      case MAINTENANCE_TIME_INTERVAL: {
        if (value < 3 * witnessMaxActiveNum * 1000
                || value > 24 * 3600 * 1000) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [3 * " + witnessMaxActiveNum + " * 1000,24 * 3600 * 1000]");
        }
        return;
      }
      case ACCOUNT_UPGRADE_COST:
      case CREATE_ACCOUNT_FEE:
      case TRANSACTION_FEE:
      case ASSET_ISSUE_FEE:
      case WITNESS_PAY_PER_BLOCK:
      case WITNESS_STANDBY_ALLOWANCE:
      case CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT:
      case CREATE_NEW_ACCOUNT_BANDWIDTH_RATE: {
        if (value < 0 || value > LONG_VALUE) {
          throw new ContractValidateException(LONG_VALUE_ERROR);
        }
        break;
      }
//      case ALLOW_CREATION_OF_CONTRACTS: {
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_CREATION_OF_CONTRACTS] is only allowed to be 1");
//        }
//        break;
//      }
      case REMOVE_THE_POWER_OF_THE_GR: {
        if (manager.getDynamicPropertiesStore().getRemoveThePowerOfTheGr() == -1) {
          throw new ContractValidateException(
              "This proposal has been executed before and is only allowed to be executed once");
        }
        if (value != 1) {
          throw new ContractValidateException(
              "This value[REMOVE_THE_POWER_OF_THE_GR] is only allowed to be 1");
        }
        break;
      }
      case ENERGY_FEE:
        if (value <= 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is greater than 0");
        }
        break;
      case EXCHANGE_CREATE_FEE:
        break;
      case MAX_CPU_TIME_OF_ONE_TX:
        if (value < 10 || value > 100) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [10,100]");
        }
        break;
      case ALLOW_UPDATE_ACCOUNT_NAME: {
        if (value != 1) {
          throw new ContractValidateException(
              "This value[ALLOW_UPDATE_ACCOUNT_NAME] is only allowed to be 1");
        }
        break;
      }
//      case ALLOW_SAME_TOKEN_NAME: {
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_SAME_TOKEN_NAME] is only allowed to be 1");
//        }
//        break;
//      }
//      case ALLOW_DELEGATE_RESOURCE: {
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_DELEGATE_RESOURCE] is only allowed to be 1");
//        }
//        break;
//      }
      case TOTAL_ENERGY_LIMIT: {
        if (value < 0 || value > LONG_VALUE) {
          throw new ContractValidateException(LONG_VALUE_ERROR);
        }
        break;
      }
//      case ALLOW_TVM_TRANSFER_TRC10: {
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_TVM_TRANSFER_TRC10] is only allowed to be 1");
//        }
//        if (manager.getDynamicPropertiesStore().getAllowSameTokenName() == 0) {
//          throw new ContractValidateException("[ALLOW_SAME_TOKEN_NAME] proposal must be approved "
//              + "before [ALLOW_TVM_TRANSFER_TRC10] can be proposed");
//        }
//        break;
//      }
      case TOTAL_CURRENT_ENERGY_LIMIT: {
        if (value < 0 || value > LONG_VALUE) {
          throw new ContractValidateException(LONG_VALUE_ERROR);
        }
        break;
      }
//      case ALLOW_MULTI_SIGN: {
//        if (!forkUtils.pass(ForkBlockVersionEnum.VERSION_3_5)) {
//          throw new ContractValidateException("Bad chain parameter id: ALLOW_MULTI_SIGN");
//        }
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_MULTI_SIGN] is only allowed to be 1");
//        }
//        break;
//      }
      case ALLOW_ADAPTIVE_ENERGY: {
        if (value != 1) {
          throw new ContractValidateException(
              "This value[ALLOW_ADAPTIVE_ENERGY] is only allowed to be 1");
        }
        break;
      }
      case UPDATE_ACCOUNT_PERMISSION_FEE: {
        if (value < 0 || value > 100_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [0,100_000_000_000L]");
        }
        break;
      }
      case MULTI_SIGN_FEE: {
        if (value < 0 || value > 100_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [0,100_000_000_000L]");
        }
        break;
      }
      case ALLOW_PROTO_FILTER_NUM: {
        if (value != 1 && value != 0) {
          throw new ContractValidateException(
              "This value[ALLOW_PROTO_FILTER_NUM] is only allowed to be 1 or 0");
        }
        break;
      }
      case ALLOW_ACCOUNT_STATE_ROOT: {
        if (value != 1 && value != 0) {
          throw new ContractValidateException(
              "This value[ALLOW_ACCOUNT_STATE_ROOT] is only allowed to be 1 or 0");
        }
        break;
      }
//      case ALLOW_TVM_CONSTANTINOPLE: {
//        if (!forkUtils.pass(ForkBlockVersionEnum.VERSION_3_6)) {
//          throw new ContractValidateException(BAD_PARAM_ID);
//        }
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_TVM_CONSTANTINOPLE] is only allowed to be 1");
//        }
//        if (manager.getDynamicPropertiesStore().getAllowTvmTransferTrc10() == 0) {
//          throw new ContractValidateException(
//              "[ALLOW_TVM_TRANSFER_TRC10] proposal must be approved "
//                  + "before [ALLOW_TVM_CONSTANTINOPLE] can be proposed");
//        }
//        break;
//      }
      case ALLOW_TVM_SOLIDITY_059: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_0)) {

          throw new ContractValidateException(BAD_PARAM_ID);
        }
        if (value != 1) {
          throw new ContractValidateException(
              "This value[ALLOW_TVM_SOLIDITY_059] is only allowed to be 1");
        }
//        if (manager.getDynamicPropertiesStore().getAllowCreationOfContracts() == 0) {
//          throw new ContractValidateException(
//              "[ALLOW_CREATION_OF_CONTRACTS] proposal must be approved "
//                  + "before [ALLOW_TVM_SOLIDITY_059] can be proposed");
//        }
        break;
      }
      case ADAPTIVE_RESOURCE_LIMIT_TARGET_RATIO: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_0)) {
          throw new ContractValidateException(BAD_PARAM_ID);
        }
        if (value < 1 || value > 1_000) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [1,1_000]");
        }
        break;
      }
      case ADAPTIVE_RESOURCE_LIMIT_MULTIPLIER: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_0)) {
          throw new ContractValidateException(BAD_PARAM_ID);
        }
        if (value < 1 || value > 10_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [1,10_000]");
        }
        break;
      }
//      case ALLOW_CHANGE_DELEGATION: {
//        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_0)) {
//          throw new ContractValidateException(BAD_PARAM_ID);
//        }
//        if (value != 1 && value != 0) {
//          throw new ContractValidateException(
//              "This value[ALLOW_CHANGE_DELEGATION] is only allowed to be 1 or 0");
//        }
//        break;
//      }
      case WITNESS_127_PAY_PER_BLOCK: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_0)) {
          throw new ContractValidateException(BAD_PARAM_ID);
        }
        if (value < 0 || value > LONG_VALUE) {
          throw new ContractValidateException(LONG_VALUE_ERROR);
        }
        break;
      }
//      case ALLOW_SHIELDED_TRANSACTION: {
//        if (!forkUtils.pass(ForkBlockVersionEnum.VERSION_4_0)) {
//          throw new ContractValidateException(
//              "Bad chain parameter id [ALLOW_SHIELDED_TRANSACTION]");
//        }
//        if (value != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_SHIELDED_TRANSACTION] is only allowed to be 1");
//        }
//        break;
//      }
//      case SHIELDED_TRANSACTION_FEE: {
//        if (!forkUtils.pass(ForkBlockVersionEnum.VERSION_4_0)) {
//          throw new ContractValidateException("Bad chain parameter id [SHIELD_TRANSACTION_FEE]");
//        }
//        if (!dynamicPropertiesStore.supportShieldedTransaction()) {
//          throw new ContractValidateException(
//              "Shielded Transaction is not activated, can not set Shielded Transaction fee");
//        }
//        if (value < 0 || value > 10_000_000_000L) {
//          throw new ContractValidateException(
//              "Bad SHIELD_TRANSACTION_FEE parameter value, valid range is [0,10_000_000_000L]");
//        }
//        break;
//      }
//      case SHIELDED_TRANSACTION_CREATE_ACCOUNT_FEE: {
//        if (!forkUtils.pass(ForkBlockVersionEnum.VERSION_4_0)) {
//          throw new ContractValidateException("Bad chain parameter id [SHIELDED_TRANSACTION_CREATE_ACCOUNT_FEE]");
//        }
//        if (value < 0 || value > 10_000_000_000L) {
//          throw new ContractValidateException(
//              "Bad SHIELDED_TRANSACTION_CREATE_ACCOUNT_FEE parameter value,valid range is [0,10_000_000_000L]");
//        }
//        break;
//      }
      case FORBID_TRANSFER_TO_CONTRACT: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_0)) {

          throw new ContractValidateException(BAD_PARAM_ID);
        }
        if (value != 1) {
          throw new ContractValidateException(
              "This value[FORBID_TRANSFER_TO_CONTRACT] is only allowed to be 1");
        }
//        if (manager.getDynamicPropertiesStore().getAllowCreationOfContracts() == 0) {
//          throw new ContractValidateException(
//              "[ALLOW_CREATION_OF_CONTRACTS] proposal must be approved "
//                  + "before [FORBID_TRANSFER_TO_CONTRACT] can be proposed");
//        }
        break;
      }

      /**
       *  Side Chain proposals
       */
      case ALLOW_CHARGING_FEE: {
        if (value != 1 && value != 0) {
          throw new ContractValidateException(
              "this value[ENERGY_CHARGING_SWITCH] is only allowed to be 1 or 0");
        }
        break;
      }
      case SIDE_CHAIN_GATEWAY: {
        List<String> list = Arrays.asList(content.split(","));
        Iterator<String> it = list.iterator();
        try {
          while (it.hasNext()) {
            if (!Commons.addressValid(Commons.decodeFromBase58Check(it.next()))) {
              throw new ContractValidateException(
                  "Invalid gateway address");
            }
          }
        } catch (Exception e) {
          throw new ContractValidateException(
              "Invalid gateway address");
        }
        break;
      }
      case MAIN_CHAIN_GATEWAY: {
        List<String> list = Arrays.asList(content.split(","));
        Iterator<String> it = list.iterator();
        try {
          while (it.hasNext()) {
            if (!Commons.addressValid(Commons.decodeFromBase58Check(it.next()))) {
              throw new ContractValidateException(
                  "Invalid gateway address");
            }
          }
        } catch (Exception e) {
          throw new ContractValidateException(
              "Invalid gateway address");
        }
        break;
      }
      case PROPOSAL_EXPIRE_TIME: {
        if (value < 0 || value > 259_200_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,259_200_000L]");
        }
        break;
      }
      case ALLOW_VOTE_WITNESS: {
        if (value != 1 && value != 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid value is {0,1}");
        }
        break;
      }
      case MAX_GATEWAY_CONTRACT_SIZE: {
        if (value < 0 || value > 500 * 1024) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,512000]");
        }
        break;
      }
//      case (1_000_006):{
//        if (Long.valueOf(entry.getValue()) != 1 && Long.valueOf(entry.getValue()) != 0) {
//          throw new ContractValidateException(
//              "Bad chain parameter value,valid value is {0,1}");
//        }
//        break;
//      }
      case FUND_INJECT_ADDRESS: {
        try {
          byte[] address = Commons.decodeFromBase58Check(content);
          if (!Commons.addressValid(address)) {
            throw new ContractValidateException(
                "Invalid Fund Inject Address");
          }
          if (ByteUtil.equals(address,
              Hex.decode(Constant.TRON_ZERO_ADDRESS_HEX))) {
            throw new ContractValidateException("target Fund Inject Address should not be set to "
                + "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");
          }

          if (manager.getAccountStore().get(address) == null) {
            throw new ContractValidateException("target Fund Inject Address not exist");
          }
          if (manager.getAccountStore().get(address).getType()
              == AccountType.Contract) {
            throw new ContractValidateException("target Fund Inject Address should not "
                + "be a contract");
          }
        } catch (Exception e) {
          throw new ContractValidateException(
              "Invalid Fund Inject Address");
        }

        break;
      }
      case ALLOW_FUND_DISTRIBUTION: {
        if (value != 1 && value != 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid value is {0,1}");
        }
        if (value == 1 && ByteUtil
            .equals(manager.getDynamicPropertiesStore().getFundInjectAddress(),
                Hex.decode(Constant.TRON_ZERO_ADDRESS_HEX))) {
          throw new ContractValidateException(
              "Fund Inject Address should not be default T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb"
                  + " to enable Fund distribution switch"
          );
        }
        break;
      }
      case FUND_DISTRIBUTION_DAYS: {
        if (value < 1 || value > 365) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [1,365]");
        }
        break;
      }
      case WITNESS_REWARD_PERCENTAGE: {
        if (value < 0 || value > 100) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100]");
        }
        break;
      }
      case WITNESS_MAX_NUMBER: {
//        Integer witnessMaxActiveNum = manager.getDynamicPropertiesStore()
//            .getWitnessMaxActiveNum();
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_0_2)) {
          throw new ContractValidateException("Bad chain parameter id [WITNESS_MAX_ACTIVE_NUM]");
        }
        if (value < 5 || value > 27) {
          throw new ContractValidateException(
              "Bad chain parameter value, valid range is [5,27]");
        } else if (value > DBConfig.getGenesisBlock()
            .getWitnesses().size()) {
          throw new ContractValidateException(
              "Bad chain parameter value, must Less than Genesis Block Witnesses size");
        } else if (value <= witnessMaxActiveNum) {
          throw new ContractValidateException(
              "Bad chain parameter value, must greater than current value " + witnessMaxActiveNum);
        }
        break;
      }
      case ALLOW_UPDATE_GATEWAY102: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_0_2)) {
          throw new ContractValidateException(
              "Bad chain parameter id [updateGateway_v1_0_2]");
        }

        if (manager.getDynamicPropertiesStore().getAllowUpdateGatewayV102() == 1) {
          throw new ContractValidateException(
              "updateGateway_v1_0_2 is only allowed to be executed once");
        }

        if (value != 1) {
          throw new ContractValidateException(
              "updateGateway_v1_0_2 is only allowed to be 1");
        }

        if (manager.getDynamicPropertiesStore().getSideChainGateWayList().isEmpty()) {
          throw new ContractValidateException(
              "updateGateway_v1_0_2 should set side chain gateway before");
        }

        if (!GatewayCode.codeHash().equals(Constant.GATEWAY_CODE_V_1_0_2_HASH)) {
          throw new ContractValidateException(
              "GatewayCode does not match updateGateway_v1_0_2");
        }
        break;
      }
      case ALLOW_DAPP_152: {
        if (!forkUtils.pass(ForkBlockVersionEnum.DAPP_CHAIN_1_5_2)) {
          throw new ContractValidateException(BAD_PARAM_ID);
        }
        if (value != 1) {
          throw new ContractValidateException(
              "this value[ALLOW_DAPP_152] is only allowed to be 1");
        }
        break;
      }
      default:
        break;
    }
  }

  public enum ProposalType {
    MAINTENANCE_TIME_INTERVAL(0), //ms  ,0
    ACCOUNT_UPGRADE_COST(1), //drop ,1
    CREATE_ACCOUNT_FEE(2), //drop ,2
    TRANSACTION_FEE(3), //drop ,3
    ASSET_ISSUE_FEE(4), //drop ,4
    WITNESS_PAY_PER_BLOCK(5), //drop ,5
    WITNESS_STANDBY_ALLOWANCE(6), //drop ,6
    CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT(7), //drop ,7
    CREATE_NEW_ACCOUNT_BANDWIDTH_RATE(8), // 1 ~ ,8
    //ALLOW_CREATION_OF_CONTRACTS(9), // 0 / >0 ,9
    REMOVE_THE_POWER_OF_THE_GR(10),  // 1 ,10
    ENERGY_FEE(11), // drop, 11
    EXCHANGE_CREATE_FEE(12), // drop, 12
    MAX_CPU_TIME_OF_ONE_TX(13), // ms, 13
    ALLOW_UPDATE_ACCOUNT_NAME(14), // 1, 14
    //ALLOW_SAME_TOKEN_NAME(15), // 1, 15
    //ALLOW_DELEGATE_RESOURCE(16), // 0, 16
    TOTAL_ENERGY_LIMIT(17), // 50,000,000,000, 17
    //ALLOW_TVM_TRANSFER_TRC10(18), // 1, 18
    TOTAL_CURRENT_ENERGY_LIMIT(19), // 50,000,000,000, 19
    //ALLOW_MULTI_SIGN(20), // 1, 20
    ALLOW_ADAPTIVE_ENERGY(21), // 1, 21
    UPDATE_ACCOUNT_PERMISSION_FEE(22), // 100, 22
    MULTI_SIGN_FEE(23), // 1, 23
    ALLOW_PROTO_FILTER_NUM(24), // 1, 24
    ALLOW_ACCOUNT_STATE_ROOT(25), // 1, 25
    //ALLOW_TVM_CONSTANTINOPLE(26), // 1, 26
    //    ALLOW_SHIELDED_TRANSACTION(27), // 27
//    SHIELDED_TRANSACTION_FEE(28), // 28
    ADAPTIVE_RESOURCE_LIMIT_MULTIPLIER(29), // 1000, 29
//    ALLOW_CHANGE_DELEGATION(30), //1, 30
    WITNESS_127_PAY_PER_BLOCK(31), //drop, 31
    ALLOW_TVM_SOLIDITY_059(32), // 1, 32
    ADAPTIVE_RESOURCE_LIMIT_TARGET_RATIO(33), // 10, 33
    //    SHIELDED_TRANSACTION_CREATE_ACCOUNT_FEE(34); // 34
    FORBID_TRANSFER_TO_CONTRACT(35), // 1, 35


    // SideChain Proposal Type
    ALLOW_CHARGING_FEE(1_000_000), // 1, 1_000_000
    SIDE_CHAIN_GATEWAY(1_000_001), // bytes, 1_000_001
    MAIN_CHAIN_GATEWAY(1_000_002), // bytes, 1_000_002
    PROPOSAL_EXPIRE_TIME(1_000_003), // ms, 1_000_003
    ALLOW_VOTE_WITNESS(1_000_004), // 1, 1_000_004
    MAX_GATEWAY_CONTRACT_SIZE(1_000_005), // number of bytes, 1_000_005
    //ALLOW_CHARGING_BANDWIDTH(1_000_006), // 1, 1_000_006
    FUND_INJECT_ADDRESS(1_000_007), // bytes, 1_000_007
    ALLOW_FUND_DISTRIBUTION(1_000_008), // 1, 1_000_008
    FUND_DISTRIBUTION_DAYS(1_000_009), // 1, 1_000_009
    WITNESS_REWARD_PERCENTAGE(1_000_010), // number, 1_000_010
    WITNESS_MAX_NUMBER(1_000_011), // number, 1_000_011
    ALLOW_UPDATE_GATEWAY102(1_000_012), // 1 (hard fork), 1_000_012
    ALLOW_DAPP_152(1_000_013);
    //ALLOW_UPDATE_SUN_NETWORK_150(1_000_013); // 1 (hard fork), 1_000_013

    private long code;

    ProposalType(long code) {
      this.code = code;
    }

    public static boolean contain(long code) {
      for (ProposalType parameters : values()) {
        if (parameters.code == code) {
          return true;
        }
      }
      return false;
    }

    public static ProposalType getEnum(long code) throws ContractValidateException {
      for (ProposalType parameters : values()) {
        if (parameters.code == code) {
          return parameters;
        }
      }

      throw new ContractValidateException("non-exist proposal number " + code);
    }

    public static ProposalType getEnumOrNull(long code) {
      for (ProposalType parameters : values()) {
        if (parameters.code == code) {
          return parameters;
        }
      }
      return null;
    }

    public long getCode() {
      return code;
    }

    public static long validateStringContent(ProposalType proposalType, String content) throws ContractValidateException {
      long value = 0;
      switch (proposalType) {
        case REMOVE_THE_POWER_OF_THE_GR:
        case MAINTENANCE_TIME_INTERVAL:
        case ACCOUNT_UPGRADE_COST:
        case CREATE_ACCOUNT_FEE:
        case TRANSACTION_FEE:
        case ASSET_ISSUE_FEE:
        case WITNESS_PAY_PER_BLOCK:
        case WITNESS_STANDBY_ALLOWANCE:
        case CREATE_NEW_ACCOUNT_FEE_IN_SYSTEM_CONTRACT:
        case CREATE_NEW_ACCOUNT_BANDWIDTH_RATE:
        //case ALLOW_CREATION_OF_CONTRACTS:
        case ENERGY_FEE:
        case EXCHANGE_CREATE_FEE:
        case MAX_CPU_TIME_OF_ONE_TX:
        case ALLOW_UPDATE_ACCOUNT_NAME:
        //case ALLOW_SAME_TOKEN_NAME:
        //case ALLOW_DELEGATE_RESOURCE:
        case TOTAL_ENERGY_LIMIT:
        //case ALLOW_TVM_TRANSFER_TRC10:
        case TOTAL_CURRENT_ENERGY_LIMIT:
        //case ALLOW_MULTI_SIGN:
        case ALLOW_ADAPTIVE_ENERGY:
        case UPDATE_ACCOUNT_PERMISSION_FEE:
        case MULTI_SIGN_FEE:
        case ALLOW_PROTO_FILTER_NUM:
        case ALLOW_ACCOUNT_STATE_ROOT:
        //case ALLOW_TVM_CONSTANTINOPLE:
        case ALLOW_TVM_SOLIDITY_059:
        case ADAPTIVE_RESOURCE_LIMIT_TARGET_RATIO:
        case ADAPTIVE_RESOURCE_LIMIT_MULTIPLIER:
//        case ALLOW_CHANGE_DELEGATION:
        case WITNESS_127_PAY_PER_BLOCK:
        case FORBID_TRANSFER_TO_CONTRACT:

          /**
           *  Side Chain proposals
           */
        case ALLOW_CHARGING_FEE:
        case PROPOSAL_EXPIRE_TIME:
        case ALLOW_VOTE_WITNESS:
        case MAX_GATEWAY_CONTRACT_SIZE:
        case ALLOW_FUND_DISTRIBUTION:
        case FUND_DISTRIBUTION_DAYS:
        case WITNESS_REWARD_PERCENTAGE:
        case WITNESS_MAX_NUMBER:
        case ALLOW_UPDATE_GATEWAY102:
        case ALLOW_DAPP_152:
        //case ALLOW_UPDATE_SUN_NETWORK_150:
          try {
            value = Long.valueOf(content);
          }
          catch (NumberFormatException e) {
            throw new ContractValidateException("Invalid proposal content");
          }
          break;
        //
        case SIDE_CHAIN_GATEWAY:
        case MAIN_CHAIN_GATEWAY:
        case FUND_INJECT_ADDRESS:
        default:
          break;
      }
      return value;
    }
  }
}
