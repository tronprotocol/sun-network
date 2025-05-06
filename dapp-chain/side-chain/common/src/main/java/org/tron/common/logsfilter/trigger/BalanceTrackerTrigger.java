package org.tron.common.logsfilter.trigger;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Data
@EqualsAndHashCode(callSuper = false)
public class BalanceTrackerTrigger extends Trigger {

  @Data
  public static class AssetStatusPojo {

    private String accountAddress;
    private String tokenAddress;
    private String balance;
    private String incrementBalance;
    private String decimals;
  }

  @Data
  public static class Trc10StatusPojo {

    private String accountAddress;
    private String tokenAddress;
    private String balance;
    private String incrementBalance;
  }

  @Data
  public static class TrxStatusPojo {

    private String accountAddress;
    private List<Integer> actions;

    private String balance;
    private String frozenBalance;
    private String energyFrozenBalance;
    private String delegatedFrozenBalanceForEnergy;
    private String delegatedFrozenBalanceForBandwidth;
    private String frozenSupplyBalance;
    private String acquiredDelegatedFrozenBalanceForEnergy;
    private String acquiredDelegatedFrozenBalanceForBandwidth;

    private String incrementBalance;
    private String incrementFrozenBalance;
    private String incrementEnergyFrozenBalance;
    private String incrementDelegatedFrozenBalanceForEnergy;
    private String incrementDelegatedFrozenBalanceForBandwidth;
    private String incrementFrozenSupplyBalance;
    private String incrementAcquiredDelegatedFrozenBalanceForEnergy;
    private String incrementAcquiredDelegatedFrozenBalanceForBandwidth;
  }

  public enum ConcernTopics {
    TRANSFER(
        "Transfer(address,address,uint256)",
        "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"),
    Withdrawal(
        "Withdrawal(address,uint256)",
        "7fcf532c15f0a6db0bd6d0e038bea71d30d808c7d98cb3bf7268a95bf5081b65"),
    Deposit(
        "Deposit(address,uint256)",
        "e1fffcc4923d04b559f4d29a8bfc6cda04eb5b0d3c460751c2402c5c5cc9109c"),
    UNKNOWN("UNKNOWN()", "0c78932dd210147f42a4ec6c5a353697626c4043d49be5f063518e57f3399e61");

    @Getter private String sign;
    @Getter private String signHash;

    ConcernTopics(String sign, String signHash) {
      this.sign = sign;
      this.signHash = signHash;
    }

    public static Boolean MatchSignHash(String dist) {
      for (ConcernTopics value : ConcernTopics.values()) {
        if (value.signHash.equals(dist)) {
          return true;
        }
      }
      return false;
    }

    public static ConcernTopics getBySH(String signHa) {
      for (ConcernTopics value : ConcernTopics.values()) {
        if (value.signHash.equals(signHa)) {
          return value;
        }
      }
      return UNKNOWN;
    }
  }

  private long blockNumber;

  private String parentHash;

  private String blockHash;

  private Boolean solidity = false;

  private List<AssetStatusPojo> assetStatusList = new ArrayList<>();

  private List<Trc10StatusPojo> trc10StatusList = new ArrayList<>();

  private List<TrxStatusPojo> trxStatusList = new ArrayList<>();

  public BalanceTrackerTrigger() {
    super();
    setTriggerName(Trigger.TRC20TRACKER_TRIGGER_NAME);
  }

  public void solidityType() {
    setTriggerName(Trigger.TRC20TRACKER_SOLIDITY_TRIGGER_NAME);
  }
}
