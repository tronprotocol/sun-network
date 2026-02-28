package org.tron.core.actuator;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.Commons;
import org.tron.common.utils.StringUtil;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.store.AccountStore;
import org.tron.core.store.DynamicPropertiesStore;
import org.tron.protos.Protocol.Transaction.Contract.ContractType;

import org.tron.protos.Protocol.Transaction.Result.code;
import org.tron.protos.contract.IncentiveContract;

import java.util.Objects;

@Slf4j(topic = "actuator")
public class FundInjectActuator extends AbstractActuator {

  public FundInjectActuator() {
    super(ContractType.FundInjectContract, IncentiveContract.FundInjectContract.class);
  }

  @Override
  public boolean execute(Object object) throws ContractExeException {
    TransactionResultCapsule result = (TransactionResultCapsule)object;
    if (Objects.isNull(result)){
      throw new RuntimeException("TransactionResultCapsule is null");
    }
    long fee = calcFee();
    try {
      AccountStore accountStore = chainBaseManager.getAccountStore();
      DynamicPropertiesStore dynamicStore = chainBaseManager.getDynamicPropertiesStore();
      IncentiveContract.FundInjectContract fundInjectContract = contract.getParameter().unpack(IncentiveContract.FundInjectContract.class);
      int chargingType = chainBaseManager.getDynamicPropertiesStore().getSideChainChargingType();
      long amount = fundInjectContract.getAmount();
      byte[] ownerAddress = fundInjectContract.getOwnerAddress().toByteArray();

      Commons.adjustBalance(accountStore, ownerAddress, -fee, chargingType);
      Commons.adjustFund(dynamicStore, fee);
      result.setStatus(fee, code.SUCESS);
      Commons.adjustBalance(accountStore, ownerAddress, -amount, chargingType);
      Commons.adjustFund(dynamicStore, amount);

      //dbManager.adjustBalance(ownerAddress, -fee, chargingType);
      //dbManager.adjustFund(fee);
      //result.setStatus(fee, code.SUCESS);
      //dbManager.adjustBalance(ownerAddress, -amount, chargingType);
      //dbManager.adjustFund(amount);
    } catch (BalanceInsufficientException e) {
      logger.debug(e.getMessage(), e);
      result.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    } catch (ArithmeticException e) {
      logger.debug(e.getMessage(), e);
      result.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      result.setStatus(fee, code.FAILED);
      throw new ContractExeException(e.getMessage());
    }
    return true;
  }

  @Override
  public boolean validate() throws ContractValidateException {
    if (chainBaseManager == null) {
      throw new ContractValidateException("No account store or dynamic store!");
    }

    if (this.any == null) {
      throw new ContractValidateException("No contract!");
    }

    if (!contract.getParameter().is(IncentiveContract.FundInjectContract.class)) {
      throw new ContractValidateException(
          "contract type error,expected type [FundInjectContract],real type[" + contract
              .getClass() + "]");
    }
    long fee = calcFee();
    final IncentiveContract.FundInjectContract fundInjectContract;
    try {
      fundInjectContract = this.contract.getParameter().unpack(IncentiveContract.FundInjectContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    byte[] ownerAddress = fundInjectContract.getOwnerAddress().toByteArray();
    if (!Commons.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid address");
    }
//    if (!ByteUtil.equals(ownerAddress,
//        this.dbManager.getDynamicPropertiesStore().getFundInjectAddress())) {
//      throw new ContractValidateException("Only Founder is allowed to inject fund");
//    }
    AccountCapsule accountCapsule = chainBaseManager.getAccountStore().get(ownerAddress);
    if (accountCapsule == null) {
      String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
      throw new ContractValidateException(
          "Account[" + readableOwnerAddress + "] not exists");
    }
    long amount = fundInjectContract.getAmount();
    if (amount <= 0) {
      throw new ContractValidateException("fund amount must be positive");
    }

    if (amount < 1_000_000L) {
      throw new ContractValidateException("fund amount must be larger than 1TRX/SunToken");
    }

    long balance = accountCapsule.getBalanceByChargeType(chainBaseManager);
    if (amount > balance) {
      throw new ContractValidateException("fund amount must be less than accountBalance");
    }

    try {
      if (balance < Math.addExact(amount, fee)) {
        throw new ContractValidateException(
            "Validate FundInject error, balance is not sufficient.");
      }
      long fund = this.chainBaseManager.getDynamicPropertiesStore().getFund();
      long newFund = Math.addExact(fund, amount);
    } catch (ArithmeticException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.getParameter().unpack(IncentiveContract.FundInjectContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }
}
