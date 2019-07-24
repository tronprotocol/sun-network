package org.tron.core.actuator;

import static org.tron.core.actuator.ActuatorConstant.ACCOUNT_EXCEPTION_STR;
import static org.tron.core.actuator.ActuatorConstant.NOT_EXIST_STR;
import static org.tron.core.actuator.ActuatorConstant.WITNESS_EXCEPTION_STR;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.spongycastle.util.encoders.Hex;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.StringUtil;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.ProposalCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.config.Parameter.ChainParameters;
import org.tron.core.db.Manager;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract.SideChainProposalCreateContract;
import org.tron.protos.Protocol.AccountType;
import org.tron.protos.Protocol.Transaction.Result.code;

@Slf4j(topic = "actuator")
public class ProposalCreateActuator extends AbstractActuator {

  ProposalCreateActuator(final Any contract, final Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute(TransactionResultCapsule ret) throws ContractExeException {
    long fee = calcFee();
    try {
      final SideChainProposalCreateContract proposalCreateContract = this.contract
          .unpack(SideChainProposalCreateContract.class);
      long id = (Objects.isNull(getDeposit())) ?
          dbManager.getDynamicPropertiesStore().getLatestProposalNum() + 1 :
          getDeposit().getLatestProposalNum() + 1;
      ProposalCapsule proposalCapsule =
          new ProposalCapsule(proposalCreateContract.getOwnerAddress(), id);

      proposalCapsule.setParameters(proposalCreateContract.getParametersMap());

      long now = dbManager.getHeadBlockTimeStamp();
      long maintenanceTimeInterval = (Objects.isNull(getDeposit())) ?
          dbManager.getDynamicPropertiesStore().getMaintenanceTimeInterval() :
          getDeposit().getMaintenanceTimeInterval();
      proposalCapsule.setCreateTime(now);

      long currentMaintenanceTime =
          (Objects.isNull(getDeposit())) ? dbManager.getDynamicPropertiesStore()
              .getNextMaintenanceTime() :
              getDeposit().getNextMaintenanceTime();
      long now3 = now + dbManager.getDynamicPropertiesStore().getProposalExpireTime();
      long round = (now3 - currentMaintenanceTime) / maintenanceTimeInterval;
      long expirationTime =
          currentMaintenanceTime + (round + 1) * maintenanceTimeInterval;
      proposalCapsule.setExpirationTime(expirationTime);

      if (Objects.isNull(deposit)) {
        dbManager.getProposalStore().put(proposalCapsule.createDbKey(), proposalCapsule);
        dbManager.getDynamicPropertiesStore().saveLatestProposalNum(id);
      } else {
        deposit.putProposalValue(proposalCapsule.createDbKey(), proposalCapsule);
        deposit.putDynamicPropertiesWithLatestProposalNum(id);
      }

      ret.setStatus(fee, code.SUCESS);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      ret.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }
    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (this.contract == null) {
      throw new ContractValidateException("No contract!");
    }
    if (dbManager == null && (deposit == null || deposit.getDbManager() == null)) {
      throw new ContractValidateException("No dbManager!");
    }
    if (!this.contract.is(SideChainProposalCreateContract.class)) {
      throw new ContractValidateException(
          "contract type error,expected type [SideChainProposalCreateContract],real type[" + contract
              .getClass() + "]");
    }
    final SideChainProposalCreateContract contract;
    try {
      contract = this.contract.unpack(SideChainProposalCreateContract.class);
    } catch (InvalidProtocolBufferException e) {
      throw new ContractValidateException(e.getMessage());
    }

    byte[] ownerAddress = contract.getOwnerAddress().toByteArray();
    String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);

    if (!Wallet.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid address");
    }

    if (!Objects.isNull(deposit)) {
      if (Objects.isNull(deposit.getAccount(ownerAddress))) {
        throw new ContractValidateException(
            ACCOUNT_EXCEPTION_STR + readableOwnerAddress + NOT_EXIST_STR);
      }
    } else if (!dbManager.getAccountStore().has(ownerAddress)) {
      throw new ContractValidateException(
          ACCOUNT_EXCEPTION_STR + readableOwnerAddress + NOT_EXIST_STR);
    }

    if (!Objects.isNull(getDeposit())) {
      if (Objects.isNull(getDeposit().getWitness(ownerAddress))) {
        throw new ContractValidateException(
            WITNESS_EXCEPTION_STR + readableOwnerAddress + NOT_EXIST_STR);
      }
    } else if (!dbManager.getWitnessStore().has(ownerAddress)) {
      throw new ContractValidateException(
          WITNESS_EXCEPTION_STR + readableOwnerAddress + NOT_EXIST_STR);
    }

    if (contract.getParametersMap().size() == 0) {
      throw new ContractValidateException("This proposal has no parameter.");
    }

    for (Map.Entry<Long, String> entry : contract.getParametersMap().entrySet()) {
//      if (!validKey(entry.getKey())) {
//        throw new ContractValidateException("Bad chain parameter id");
//      }
      validateValue(entry);
    }

    return true;
  }

  private void validateValue(Map.Entry<Long, String> entry) throws ContractValidateException {

    switch (entry.getKey().intValue()) {
      case (0): {
        if (Long.valueOf(entry.getValue()) < 3 * 27 * 1000
            || Long.valueOf(entry.getValue()) > 24 * 3600 * 1000) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [3 * 27 * 1000,24 * 3600 * 1000]");
        }
        return;
      }
      case (1):
      case (2):
      case (3):
      case (4):
      case (5):
      case (6):
      case (7):
      case (8): {
        if (Long.valueOf(entry.getValue()) < 0
            || Long.valueOf(entry.getValue()) > 100_000_000_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100_000_000_000_000_000L]");
        }
        break;
      }
      case (10): {
        if (dbManager.getDynamicPropertiesStore().getRemoveThePowerOfTheGr() == -1) {
          throw new ContractValidateException(
              "This proposal has been executed before and is only allowed to be executed once");
        }

        if (Long.valueOf(entry.getValue()) != 1) {
          throw new ContractValidateException(
              "This value[REMOVE_THE_POWER_OF_THE_GR] is only allowed to be 1");
        }
        break;
      }
      case (11):
        if (Long.valueOf(entry.getValue()) <= 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is greater than 0");
        }
        break;
      case (12):
        break;
      case (13):
        if (Long.valueOf(entry.getValue()) < 10 || Long.valueOf(entry.getValue()) > 100) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [10,100]");
        }
        break;
      case (17): { // deprecated
        if (Long.valueOf(entry.getValue()) < 0
            || Long.valueOf(entry.getValue()) > 100_000_000_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100_000_000_000_000_000L]");
        }
        break;
      }
//      case (18): {
//        if (Long.valueOf(entry.getValue()) != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_TVM_TRANSFER_TRC10] is only allowed to be 1");
//        }
//        break;
//      }
      case (19): {
        if (Long.valueOf(entry.getValue()) < 0
            || Long.valueOf(entry.getValue()) > 100_000_000_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100_000_000_000_000_000L]");
        }
        break;
      }
//      case (20): {
//        if (Long.valueOf(entry.getValue()) != 1) {
//          throw new ContractValidateException(
//              "This value[ALLOW_MULTI_SIGN] is only allowed to be 1");
//        }
//        break;
//      }
      case (21): {
        if (Long.valueOf(entry.getValue()) != 1) {
          throw new ContractValidateException(
              "This value[ALLOW_ADAPTIVE_ENERGY] is only allowed to be 1");
        }
        break;
      }
      case (22): {
        if (Long.valueOf(entry.getValue()) < 0
            || Long.valueOf(entry.getValue()) > 100_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100_000_000_000L]");
        }
        break;
      }
      case (23): {
        if (Long.valueOf(entry.getValue()) < 0
            || Long.valueOf(entry.getValue()) > 100_000_000_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100_000_000_000L]");
        }
        break;
      }
      case (25): {
        if (Long.valueOf(entry.getValue()) != 1 && Long.valueOf(entry.getValue()) != 0) {
          throw new ContractValidateException(
              "This value[ALLOW_ACCOUNT_STATE_ROOT] is only allowed to be 1 or 0");
        }
        break;
      }
      case (1_000_000): {
        if (Long.valueOf(entry.getValue()) != 1 && Long.valueOf(entry.getValue()) != 0) {
          throw new ContractValidateException(
              "this value[ENERGY_CHARGING_SWITCH] is only allowed to be 1 or 0");
        }
        break;
      }
      case (1_000_001): {
        List<String> list = Arrays.asList(entry.getValue().split(","));
        Iterator<String> it = list.iterator();
        try {
          while (it.hasNext()) {
            if (!Wallet.addressValid(Wallet.decodeFromBase58Check(it.next()))) {
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
      case (1_000_002): {
        List<String> list = Arrays.asList(entry.getValue().split(","));
        Iterator<String> it = list.iterator();
        try {
          while (it.hasNext()) {
            if (!Wallet.addressValid(Wallet.decodeFromBase58Check(it.next()))) {
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
      case (1_000_003): {
        if (Long.valueOf(entry.getValue()) < 0 || Long.valueOf(entry.getValue()) > 259_200_000L) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,259_200_000L]");
        }
        break;
      }
      case (1_000_004): {
        if (Long.valueOf(entry.getValue()) != 1 && Long.valueOf(entry.getValue()) != 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid value is {0,1}");
        }
        break;
      }
      case (1_000_005): {
        if (Long.valueOf(entry.getValue()) < 0 || Long.valueOf(entry.getValue()) > 500 * 1024) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,512000]");
        }
        break;
      }
      case (1_000_006):{
        if (Long.valueOf(entry.getValue()) != 1 && Long.valueOf(entry.getValue()) != 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid value is {0,1}");
        }
        break;
      }
      case (1_000_007):{
        try {
          byte[] address = Wallet.decodeFromBase58Check(entry.getValue());
          if (!Wallet.addressValid(address)) {
            throw new ContractValidateException(
                "Invalid Fund Inject Address");
          }
          if (ByteUtil.equals(address,
              Hex.decode(Constant.TRON_ZERO_ADDRESS_HEX))) {
            throw new ContractValidateException("target Fund Inject Address should not be set to "
                + "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb");
          }

          if (this.dbManager.getAccountStore().get(address) == null) {
            throw new ContractValidateException("target Fund Inject Address not exist");
          }
          if (this.dbManager.getAccountStore().get(address).getType()
              == AccountType.Contract) {
            throw new ContractValidateException("target Fund Inject Address should not "
                + "be a contract");
          }
        } catch (Exception e){
          throw new ContractValidateException(
              "Invalid Fund Inject Address");
        }

        break;
      }
      case (1_000_008):{
        if (Long.valueOf(entry.getValue()) != 1 && Long.valueOf(entry.getValue()) != 0) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid value is {0,1}");
        }
        if (Long.valueOf(entry.getValue()) == 1 && ByteUtil.equals(this.dbManager.getDynamicPropertiesStore().getFundInjectAddress(),
            Hex.decode(Constant.TRON_ZERO_ADDRESS_HEX))) {
          throw new ContractValidateException(
              "Fund Inject Address should not be default T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb"
                  + " to enable Fund distribution switch"
          );
        }
        break;
      }
      case (1_000_009): {
        if (Long.valueOf(entry.getValue()) < 1 || Long.valueOf(entry.getValue()) > 365) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [1,365]");
        }
        break;
      }
      case (1_000_010): {
        if (Integer.valueOf(entry.getValue()) < 0 || Integer.valueOf(entry.getValue()) > 100) {
          throw new ContractValidateException(
              "Bad chain parameter value,valid range is [0,100]");
        }
        break;
      }
      default:
        throw new ContractValidateException(
            "non-exist proposal number");
    }
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(SideChainProposalCreateContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }

  private boolean validKey(long idx) {
    return idx >= 0 && idx < ChainParameters.values().length;
  }

}
