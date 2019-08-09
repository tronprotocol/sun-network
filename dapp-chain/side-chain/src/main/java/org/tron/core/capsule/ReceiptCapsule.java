package org.tron.core.capsule;

import static org.tron.core.Constant.SUN_TOKEN_ID;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.tron.common.utils.Sha256Hash;
import org.tron.common.utils.StringUtil;
import org.tron.core.Constant;
import org.tron.core.db.EnergyProcessor;
import org.tron.core.db.Manager;
import org.tron.core.exception.BalanceInsufficientException;
import org.tron.protos.Protocol.ResourceReceipt;
import org.tron.protos.Protocol.Transaction.Result.contractResult;

public class ReceiptCapsule {

  private ResourceReceipt receipt;
  @Getter
  @Setter
  private long multiSignFee;

  private Sha256Hash receiptAddress;

  public ReceiptCapsule(ResourceReceipt data, Sha256Hash receiptAddress) {
    this.receipt = data;
    this.receiptAddress = receiptAddress;
  }

  public ReceiptCapsule(Sha256Hash receiptAddress) {
    this.receipt = ResourceReceipt.newBuilder().build();
    this.receiptAddress = receiptAddress;
  }

  public void setReceipt(ResourceReceipt receipt) {
    this.receipt = receipt;
  }

  public ResourceReceipt getReceipt() {
    return this.receipt;
  }

  public Sha256Hash getReceiptAddress() {
    return this.receiptAddress;
  }

  public void setNetUsage(long netUsage) {
    this.receipt = this.receipt.toBuilder().setNetUsage(netUsage).build();
  }

  public void setNetFee(long netFee) {
    this.receipt = this.receipt.toBuilder().setNetFee(netFee).build();
  }

  public void addNetFee(long netFee) {
    this.receipt = this.receipt.toBuilder().setNetFee(getNetFee() + netFee).build();
  }

  public long getEnergyUsage() {
    return this.receipt.getEnergyUsage();
  }

  public long getEnergyFee() {
    return this.receipt.getEnergyFee();
  }

  public void setEnergyUsage(long energyUsage) {
    this.receipt = this.receipt.toBuilder().setEnergyUsage(energyUsage).build();
  }

  public void setEnergyFee(long energyFee) {
    this.receipt = this.receipt.toBuilder().setEnergyFee(energyFee).build();
  }

  public long getOriginEnergyUsage() {
    return this.receipt.getOriginEnergyUsage();
  }

  public long getEnergyUsageTotal() {
    return this.receipt.getEnergyUsageTotal();
  }

  public void setOriginEnergyUsage(long energyUsage) {
    this.receipt = this.receipt.toBuilder().setOriginEnergyUsage(energyUsage).build();
  }

  public void setEnergyUsageTotal(long energyUsage) {
    this.receipt = this.receipt.toBuilder().setEnergyUsageTotal(energyUsage).build();
  }

  public long getNetUsage() {
    return this.receipt.getNetUsage();
  }

  public long getNetFee() {
    return this.receipt.getNetFee();
  }

  /**
   * payEnergyBill pay receipt energy bill by energy processor.
   */
  public void payEnergyBill(Manager manager, AccountCapsule origin, AccountCapsule caller,
      long percent, long originEnergyLimit, EnergyProcessor energyProcessor, long now)
      throws BalanceInsufficientException {
    if (receipt.getEnergyUsageTotal() <= 0) {
      return;
    }

    if (Objects.isNull(origin)) {
      payEnergyBill(manager, caller, receipt.getEnergyUsageTotal(), energyProcessor, now);
      return;
    }

    if (caller.getAddress().equals(origin.getAddress())) {
      payEnergyBill(manager, caller, receipt.getEnergyUsageTotal(), energyProcessor, now);
    } else {
      long originUsage = Math.multiplyExact(receipt.getEnergyUsageTotal(), percent) / 100;
      originUsage = getOriginUsage(manager, origin, originEnergyLimit, energyProcessor,
          originUsage);

      long callerUsage = receipt.getEnergyUsageTotal() - originUsage;
      energyProcessor.useEnergy(origin, originUsage, now);
      this.setOriginEnergyUsage(originUsage);
      payEnergyBill(manager, caller, callerUsage, energyProcessor, now);
    }
  }

  private long getOriginUsage(Manager manager, AccountCapsule origin,
      long originEnergyLimit,
      EnergyProcessor energyProcessor, long originUsage) {
    return Math.min(originUsage,
        Math.min(energyProcessor.getAccountLeftEnergyFromFreeze(origin), originEnergyLimit));
  }

  private void payEnergyBill(
      Manager manager,
      AccountCapsule account,
      long usage,
      EnergyProcessor energyProcessor,
      long now) throws BalanceInsufficientException {
    long accountEnergyLeft = energyProcessor.getAccountLeftEnergyFromFreeze(account);
    if (accountEnergyLeft >= usage) {
      energyProcessor.useEnergy(account, usage, now);
      this.setEnergyUsage(usage);
    } else {
      energyProcessor.useEnergy(account, accountEnergyLeft, now);

      long energyFee;
      int chargingType = manager.getDynamicPropertiesStore().getSideChainChargingType();
      long dynamicEnergyFee = manager.getDynamicPropertiesStore().getEnergyFee(chargingType);
      if(chargingType == 0) {
        long sunPerEnergy = Constant.SUN_PER_ENERGY;
        if (dynamicEnergyFee > 0) {
          sunPerEnergy = dynamicEnergyFee;
        }
        energyFee = (usage - accountEnergyLeft) * sunPerEnergy;
        this.setEnergyUsage(accountEnergyLeft);
        this.setEnergyFee(energyFee);

        long balance = account.getBalance();
        if (balance < energyFee) {
          throw new BalanceInsufficientException(
                  StringUtil.createReadableString(account.createDbKey()) + " insufficient balance");
        }
      } else {
        long sunPerEnergy = Constant.MICRO_SUN_TOKEN_PER_ENERGY;
        if (dynamicEnergyFee > 0) {
          sunPerEnergy = dynamicEnergyFee;
        }
        energyFee = (usage - accountEnergyLeft) * sunPerEnergy;
        this.setEnergyUsage(accountEnergyLeft);
        this.setEnergyFee(energyFee);

        long balance = account.getAssetMapV2().getOrDefault(SUN_TOKEN_ID, 0L);
        if (balance < energyFee) {
          throw new BalanceInsufficientException(
                  StringUtil.createReadableString(account.createDbKey()) + " insufficient token");
        }
      }

      manager.adjustBalance(account, -energyFee, chargingType);
      manager.adjustFund(energyFee);
    }

    manager.getAccountStore().put(account.getAddress().toByteArray(), account);
  }

  public static ResourceReceipt copyReceipt(ReceiptCapsule origin) {
    return origin.getReceipt().toBuilder().build();
  }

  public void setResult(contractResult success) {
    this.receipt = receipt.toBuilder().setResult(success).build();
  }

  public contractResult getResult() {
    return this.receipt.getResult();
  }
}
