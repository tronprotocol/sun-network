package org.tron.core.actuator;

import static org.tron.core.Constant.SUN_TOKEN_ID;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.extern.slf4j.Slf4j;
import org.tron.common.utils.ByteUtil;
import org.tron.common.utils.StringUtil;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionResultCapsule;
import org.tron.core.db.Manager;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.core.exception.ContractExeException;
import org.tron.core.exception.ContractValidateException;
import org.tron.protos.Contract.FundInjectContract;
import org.tron.protos.Protocol.Transaction.Result.code;

@Slf4j(topic = "actuator")
public class FundInjectActuator extends AbstractActuator {

  FundInjectActuator(Any contract, Manager dbManager) {
    super(contract, dbManager);
  }

  @Override
  public boolean execute(TransactionResultCapsule result) throws ContractExeException {
    long fee = calcFee();
    try {
      int chargingType = dbManager.getDynamicPropertiesStore().getSideChainChargingType();
      FundInjectContract fundInjectContract = contract.unpack(FundInjectContract.class);
      long amount = fundInjectContract.getAmount();
      byte[] ownerAddress = fundInjectContract.getOwnerAddress().toByteArray();

      dbManager.adjustBalance(ownerAddress, -fee, chargingType);
      dbManager.adjustFund(fee);
      result.setStatus(fee, code.SUCESS);
      dbManager.adjustBalance(ownerAddress, -amount, chargingType);
      dbManager.adjustFund(amount);
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
    if (this.contract == null) {
      throw new ContractValidateException("No contract!");
    }
    if (this.dbManager == null) {
      throw new ContractValidateException("No dbManager!");
    }
    if (!contract.is(FundInjectContract.class)) {
      throw new ContractValidateException(
          "contract type error,expected type [FundInjectContract],real type[" + contract
              .getClass() + "]");
    }
    long fee = calcFee();
    final FundInjectContract fundInjectContract;
    try {
      fundInjectContract = this.contract.unpack(FundInjectContract.class);
    } catch (InvalidProtocolBufferException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    byte[] ownerAddress = fundInjectContract.getOwnerAddress().toByteArray();
    if (!ByteUtil.equals(ownerAddress,
        this.dbManager.getDynamicPropertiesStore().getFundInjectAddress())) {
      throw new ContractValidateException("Only Founder is allowed to inject fund");
    }
    if (!Wallet.addressValid(ownerAddress)) {
      throw new ContractValidateException("Invalid address");
    }
    AccountCapsule accountCapsule = dbManager.getAccountStore().get(ownerAddress);
    if (accountCapsule == null) {
      String readableOwnerAddress = StringUtil.createReadableString(ownerAddress);
      throw new ContractValidateException(
          "Account[" + readableOwnerAddress + "] not exists");
    }
    long amount = fundInjectContract.getAmount();
    if (amount <= 0) {
      throw new ContractValidateException("fund amount must be positive");
    }

    if (amount < 1000_000L) {
      throw new ContractValidateException("fund amount must be larger than 1TRX");
    }

    if (amount > accountCapsule.getBalance()) {
      throw new ContractValidateException("fund amount must be less than accountBalance");
    }

    int chargingType = dbManager.getDynamicPropertiesStore().getSideChainChargingType();
    long balance;
    if (chargingType == 0)
      balance = accountCapsule.getBalance();
    else
      balance = accountCapsule.getAssetMapV2().getOrDefault(SUN_TOKEN_ID, 0L) ;

    try {
      if (balance < Math.addExact(amount, fee)) {
        throw new ContractValidateException(
            "Validate FundInject error, balance is not sufficient.");
      }
      long fund = this.dbManager.getDynamicPropertiesStore().getFund();
      long newFund = Math.addExact(fund, amount);
    } catch (ArithmeticException e) {
      logger.debug(e.getMessage(), e);
      throw new ContractValidateException(e.getMessage());
    }
    return true;
  }

  @Override
  public ByteString getOwnerAddress() throws InvalidProtocolBufferException {
    return contract.unpack(FundInjectContract.class).getOwnerAddress();
  }

  @Override
  public long calcFee() {
    return 0;
  }
}
