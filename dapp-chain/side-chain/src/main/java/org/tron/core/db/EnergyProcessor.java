package org.tron.core.db;

import static java.lang.Long.max;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.tron.core.Constant;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.config.Parameter.AdaptiveResourceLimitConstants;
import org.tron.core.exception.AccountResourceInsufficientException;
import org.tron.core.exception.ContractValidateException;
import org.tron.core.exception.TooBigTransactionResultException;
import org.tron.protos.Protocol.Account.AccountResource;
import org.tron.protos.Protocol.Transaction.Contract;

@Slf4j(topic = "DB")
public class EnergyProcessor extends ResourceProcessor {

  public EnergyProcessor(Manager manager) {
    super(manager);
  }

  @Override
  public void updateUsage(AccountCapsule accountCapsule) {
    long now = dbManager.getWitnessController().getHeadSlot();
    updateUsage(accountCapsule, now);
  }

  private void updateUsage(AccountCapsule accountCapsule, long now) {
    AccountResource accountResource = accountCapsule.getAccountResource();

    long oldEnergyUsage = accountResource.getEnergyUsage();
    long latestConsumeTime = accountResource.getLatestConsumeTimeForEnergy();

    accountCapsule.setEnergyUsage(increase(oldEnergyUsage, 0, latestConsumeTime, now));
  }

  public void updateTotalEnergyAverageUsage() {
    long now = dbManager.getWitnessController().getHeadSlot();
    long blockEnergyUsage = dbManager.getDynamicPropertiesStore().getBlockEnergyUsage();
    long totalEnergyAverageUsage = dbManager.getDynamicPropertiesStore()
        .getTotalEnergyAverageUsage();
    long totalEnergyAverageTime = dbManager.getDynamicPropertiesStore().getTotalEnergyAverageTime();

    long newPublicEnergyAverageUsage = increase(totalEnergyAverageUsage, blockEnergyUsage,
        totalEnergyAverageTime, now, averageWindowSize);

    dbManager.getDynamicPropertiesStore().saveTotalEnergyAverageUsage(newPublicEnergyAverageUsage);
    dbManager.getDynamicPropertiesStore().saveTotalEnergyAverageTime(now);
  }

  public void updateAdaptiveTotalEnergyLimit() {
    long totalEnergyAverageUsage = dbManager.getDynamicPropertiesStore()
        .getTotalEnergyAverageUsage();
    long targetTotalEnergyLimit = dbManager.getDynamicPropertiesStore().getTotalEnergyTargetLimit();
    long totalEnergyCurrentLimit = dbManager.getDynamicPropertiesStore()
        .getTotalEnergyCurrentLimit();
    long totalEnergyLimit = dbManager.getDynamicPropertiesStore().getTotalEnergyLimit();

    long result;
    if (totalEnergyAverageUsage > targetTotalEnergyLimit) {
      result = totalEnergyCurrentLimit * AdaptiveResourceLimitConstants.CONTRACT_RATE_NUMERATOR
          / AdaptiveResourceLimitConstants.CONTRACT_RATE_DENOMINATOR;
      // logger.info(totalEnergyAverageUsage + ">" + targetTotalEnergyLimit + "\n" + result);
    } else {
      result = totalEnergyCurrentLimit * AdaptiveResourceLimitConstants.EXPAND_RATE_NUMERATOR
          / AdaptiveResourceLimitConstants.EXPAND_RATE_DENOMINATOR;
      // logger.info(totalEnergyAverageUsage + "<" + targetTotalEnergyLimit + "\n" + result);
    }

    result = Math.min(
        Math.max(result, totalEnergyLimit),
        totalEnergyLimit * AdaptiveResourceLimitConstants.LIMIT_MULTIPLIER
    );

    dbManager.getDynamicPropertiesStore().saveTotalEnergyCurrentLimit(result);
    logger.debug(
        "adjust totalEnergyCurrentLimit, old[" + totalEnergyCurrentLimit + "], new[" + result
            + "]");
  }

  @Override
  public void consume(TransactionCapsule trx,
      TransactionTrace trace)
      throws ContractValidateException, AccountResourceInsufficientException {
    throw new RuntimeException("Not support");
  }


  public boolean useEnergy(AccountCapsule accountCapsule, long energy, long now) {

    long energyUsage = accountCapsule.getEnergyUsage();
    long latestConsumeTime = accountCapsule.getAccountResource().getLatestConsumeTimeForEnergy();
    long energyLimit = calculateGlobalEnergyLimit(accountCapsule);

    long newEnergyUsage = increase(energyUsage, 0, latestConsumeTime, now);

    if (energy > (energyLimit - newEnergyUsage)) {
      return false;
    }

    latestConsumeTime = now;
    long latestOperationTime = dbManager.getHeadBlockTimeStamp();
    newEnergyUsage = increase(newEnergyUsage, energy, latestConsumeTime, now);
    accountCapsule.setEnergyUsage(newEnergyUsage);
    accountCapsule.setLatestOperationTime(latestOperationTime);
    accountCapsule.setLatestConsumeTimeForEnergy(latestConsumeTime);

    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);

    if (dbManager.getDynamicPropertiesStore().getAllowAdaptiveEnergy() == 1) {
      long blockEnergyUsage = dbManager.getDynamicPropertiesStore().getBlockEnergyUsage() + energy;
      dbManager.getDynamicPropertiesStore().saveBlockEnergyUsage(blockEnergyUsage);
    }

    return true;
  }


  public long calculateGlobalEnergyLimit(AccountCapsule accountCapsule) {
    long frozeBalance = accountCapsule.getAllFrozenBalanceForEnergy();
    if (frozeBalance < 1000_000L) {
      return 0;
    }

    long energyWeight = frozeBalance / 1000_000L;
    long totalEnergyLimit = dbManager.getDynamicPropertiesStore().getTotalEnergyCurrentLimit();
    long totalEnergyWeight = dbManager.getDynamicPropertiesStore().getTotalEnergyWeight();

    assert totalEnergyWeight > 0;

    return (long) (energyWeight * ((double) totalEnergyLimit / totalEnergyWeight));
  }

  public long getAccountLeftEnergyFromFreeze(AccountCapsule accountCapsule) {

    long now = dbManager.getWitnessController().getHeadSlot();

    long energyUsage = accountCapsule.getEnergyUsage();
    long latestConsumeTime = accountCapsule.getAccountResource().getLatestConsumeTimeForEnergy();
    long energyLimit = calculateGlobalEnergyLimit(accountCapsule);

    long newEnergyUsage = increase(energyUsage, 0, latestConsumeTime, now);

    return max(energyLimit - newEnergyUsage, 0); // us
  }

  public void bandwidthEnergyConsume(TransactionCapsule trx, TransactionTrace trace)
      throws TooBigTransactionResultException, ContractValidateException, AccountResourceInsufficientException {
    List<Contract> contracts = trx.getInstance().getRawData().getContractList();
    if (trx.getResultSerializedSize() > Constant.MAX_RESULT_SIZE_IN_TX * contracts.size()) {
      throw new TooBigTransactionResultException();
    }

    long bytesSize;

    bytesSize = trx.getInstance().toBuilder().clearRet().build().getSerializedSize();

    for (Contract contract : contracts) {

      bytesSize += Constant.MAX_RESULT_SIZE_IN_TX;

      logger.debug("trxId {},bandwidth cost :{}", trx.getTransactionId(), bytesSize);
      trace.setNetBill(bytesSize, 0);
      byte[] address = TransactionCapsule.getOwner(contract);
      AccountCapsule accountCapsule = dbManager.getAccountStore().get(address);
      if (accountCapsule == null) {
        throw new ContractValidateException("account not exists");
      }

      long now = dbManager.getWitnessController().getHeadSlot();

      if (contractCreateNewAccount(contract)) {
        consumeEnergyForCreateNewAccount(accountCapsule, bytesSize, now, trace);
        continue;
      }

      if (useAccountFrozenEnergy(accountCapsule, bytesSize, now)) {
        continue;
      }

      if (useTransactionFee(accountCapsule, bytesSize, trace)) {
        continue;
      }

      long fee = dbManager.getDynamicPropertiesStore().getTransactionFee() * bytesSize;
      throw new AccountResourceInsufficientException(
          "Account Insufficient energy[" + bytesSize + "] and balance["
              + fee + "] to create new account");

    }

  }


  public boolean contractCreateNewAccount(Contract contract) {
    switch (contract.getType()) {
      case AccountCreateContract:
        return true;
      default:
        return false;
    }
  }

  public void consumeEnergyForCreateNewAccount(AccountCapsule accountCapsule, long bytes,
      long now, TransactionTrace trace) throws AccountResourceInsufficientException {
    boolean ret = consumeFreezeEnergyForCreateNewAccount(accountCapsule, bytes,now);

    if (!ret) {
      ret =consumeFeeForCreateNewAccount(accountCapsule, trace);
      if (!ret) {
        throw new AccountResourceInsufficientException();
      }
    }

    long fee = dbManager.getDynamicPropertiesStore().getCreateAccountFee();
    long energyForCreateNewAccount = fee * dbManager.getDynamicPropertiesStore().getEnergyFee();
    trace.setNetBill(0, energyForCreateNewAccount);
    dbManager.getDynamicPropertiesStore().addTotalCreateAccountCost(fee);
  }


  public boolean consumeFreezeEnergyForCreateNewAccount(AccountCapsule accountCapsule, long bytes,
      long now) {

    long createNewAccountEnergyRatio = 1 / dbManager.getDynamicPropertiesStore().
        getCreateNewAccountEnergyRate();

    long energyLeftFromFreeze = getAccountLeftEnergyFromFreeze(accountCapsule);

    if (bytes * createNewAccountEnergyRatio <= energyLeftFromFreeze) {
      long latestConsumeTime = now;
      long latestOperationTime = dbManager.getHeadBlockTimeStamp();
      energyLeftFromFreeze = increase(energyLeftFromFreeze, bytes * createNewAccountEnergyRatio, latestConsumeTime,
          now);
      accountCapsule.setLatestConsumeTime(latestConsumeTime);
      accountCapsule.setLatestOperationTime(latestOperationTime);
      accountCapsule.setEnergyUsage(energyLeftFromFreeze);
      dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
      return true;
    }
    return false;
  }

  public boolean consumeFeeForCreateNewAccount(AccountCapsule accountCapsule,
      TransactionTrace trace) {
    long fee = dbManager.getDynamicPropertiesStore().getCreateAccountFee();
    if (consumeFee(accountCapsule, fee)) {
      trace.setNetBill(0, fee);
      dbManager.getDynamicPropertiesStore().addTotalCreateAccountCost(fee);
      return true;
    } else {
      return false;
    }
  }

  private boolean useTransactionFee(AccountCapsule accountCapsule, long bytes,
      TransactionTrace trace) {
    long fee = dbManager.getDynamicPropertiesStore().getTransactionFee() * bytes;
    if (consumeFee(accountCapsule, fee)) {
      trace.setNetBill(0, fee);
      dbManager.getDynamicPropertiesStore().addTotalTransactionCost(fee);
      return true;
    } else {
      return false;
    }
  }

  private boolean useAccountFrozenEnergy(AccountCapsule accountCapsule, long bytes, long now) {

    long energyLeftFromFreeze = getAccountLeftEnergyFromFreeze(accountCapsule);

    long rate = dbManager.getDynamicPropertiesStore().getCreateNewAccountEnergyRate();
    if (bytes * 1 / rate > energyLeftFromFreeze) {
      logger.debug("Energy is running out. now use TRX");
      return false;
    }

    long latestConsumeTime = now;
    long latestOperationTime = dbManager.getHeadBlockTimeStamp();
    energyLeftFromFreeze = increase(energyLeftFromFreeze, bytes, latestConsumeTime, now);
    accountCapsule.setNetUsage(energyLeftFromFreeze);
    accountCapsule.setLatestOperationTime(latestOperationTime);
    accountCapsule.setLatestConsumeTime(latestConsumeTime);

    dbManager.getAccountStore().put(accountCapsule.createDbKey(), accountCapsule);
    return true;
  }
}


