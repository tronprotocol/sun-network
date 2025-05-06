package org.tron.common.logsfilter.capsule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.tron.common.logsfilter.EventPluginLoader;
import org.tron.common.logsfilter.TRC20Utils;
import org.tron.common.logsfilter.trigger.BalanceTrackerTrigger;
import org.tron.common.logsfilter.trigger.BalanceTrackerTrigger.AssetStatusPojo;
import org.tron.common.runtime.vm.LogInfo;
import org.tron.core.capsule.BlockCapsule;
import org.tron.core.capsule.TransactionCapsule;
import org.tron.core.db.accountchange.AccountChangeRecord;

@Slf4j
public class BalanceTrackerCapsule extends TriggerCapsule {


  @Getter
  @Setter
  private BalanceTrackerTrigger trc20TrackerTrigger;

  public BalanceTrackerCapsule(BlockCapsule block, Map<String, AccountChangeRecord.AccountInfo> accountInfoMap) {
    trc20TrackerTrigger = new BalanceTrackerTrigger();
    trc20TrackerTrigger.setBlockHash(block.getBlockId().toString());
    trc20TrackerTrigger.setParentHash(block.getParentHash().toString());
    trc20TrackerTrigger.setBlockNumber(block.getNum());
    trc20TrackerTrigger.setTimeStamp(block.getTimeStamp());
    List<TransactionCapsule> transactionCapsules = block.getTransactions();
    List<LogInfo> logInfos = new ArrayList<>();
    for (TransactionCapsule transactionCapsule : transactionCapsules) {
      List<LogInfo> innerList = transactionCapsule.getTrxTrace().getTransactionContext()
          .getProgramResult().getLogInfoList();
      if (innerList != null && innerList.size() > 0) {
        logInfos.addAll(innerList);
      }
    }
    if (logInfos.size() > 0) {
      List<AssetStatusPojo> assetStatusPojos = TRC20Utils
          .parseTrc20AssetStatusPojo(block, logInfos);
      trc20TrackerTrigger.setAssetStatusList(assetStatusPojos);
    }

    List<BalanceTrackerTrigger.Trc10StatusPojo> trc10StatusList = new LinkedList<>();
    List<BalanceTrackerTrigger.TrxStatusPojo> trxStatusList = new LinkedList<>();
    handlerTrxAndTrc10(accountInfoMap, trxStatusList, trc10StatusList);
    trc20TrackerTrigger.setTrxStatusList(trxStatusList);
    trc20TrackerTrigger.setTrc10StatusList(trc10StatusList);

  }

  private void handlerTrxAndTrc10(Map<String, AccountChangeRecord.AccountInfo> accountInfoMap,
      List<BalanceTrackerTrigger.TrxStatusPojo> trxStatusList,
      List<BalanceTrackerTrigger.Trc10StatusPojo> trc10StatusList) {
    if (CollectionUtils.isEmpty(accountInfoMap)) {
      return;
    }

    accountInfoMap.values().stream().forEach(info -> {
      final BalanceTrackerTrigger.TrxStatusPojo trxStatusPojo = converterTrx(info);
      trxStatusList.add(trxStatusPojo);
      final List<BalanceTrackerTrigger.Trc10StatusPojo> trc10List = converterTrc10(info.getAccountAddress(), info.getTrc10Map());
      trc10StatusList.addAll(trc10List);
    });
  }

  private BalanceTrackerTrigger.TrxStatusPojo converterTrx(AccountChangeRecord.AccountInfo info) {
    BalanceTrackerTrigger.TrxStatusPojo trx = new BalanceTrackerTrigger.TrxStatusPojo();
    trx.setAccountAddress(info.getAccountAddress());
    trx.setActions(info.getActions());

    trx.setBalance(String.valueOf(info.getBalance()));
    trx.setFrozenBalance(String.valueOf(info.getFrozenBalance()));
    trx.setEnergyFrozenBalance(String.valueOf(info.getEnergyFrozenBalance()));
    trx.setDelegatedFrozenBalanceForEnergy(String.valueOf(info.getDelegatedFrozenBalanceForEnergy()));
    trx.setDelegatedFrozenBalanceForBandwidth(String.valueOf(info.getDelegatedFrozenBalanceForBandwidth()));
    trx.setFrozenSupplyBalance(String.valueOf(info.getFrozenSupplyBalance()));
    trx.setAcquiredDelegatedFrozenBalanceForEnergy(String.valueOf(info.getAcquiredDelegatedFrozenBalanceForEnergy()));
    trx.setAcquiredDelegatedFrozenBalanceForBandwidth(String.valueOf(info.getAcquiredDelegatedFrozenBalanceForBandwidth()));

    trx.setIncrementBalance(String.valueOf(info.getIncrementBalance()));
    trx.setIncrementFrozenBalance(String.valueOf(info.getIncrementFrozenBalance()));
    trx.setIncrementEnergyFrozenBalance(String.valueOf(info.getIncrementEnergyFrozenBalance()));
    trx.setIncrementDelegatedFrozenBalanceForEnergy(String.valueOf(info.getIncrementDelegatedFrozenBalanceForEnergy()));
    trx.setIncrementDelegatedFrozenBalanceForBandwidth(String.valueOf(info.getIncrementDelegatedFrozenBalanceForBandwidth()));
    trx.setIncrementFrozenSupplyBalance(String.valueOf(info.getIncrementFrozenSupplyBalance()));
    trx.setIncrementAcquiredDelegatedFrozenBalanceForEnergy(String.valueOf(info.getIncrementAcquiredDelegatedFrozenBalanceForEnergy()));
    trx.setIncrementAcquiredDelegatedFrozenBalanceForBandwidth(String.valueOf(info.getIncrementAcquiredDelegatedFrozenBalanceForBandwidth()));

    return trx;
  }

  private List<BalanceTrackerTrigger.Trc10StatusPojo> converterTrc10(String accountAddress,
      Map<String, AccountChangeRecord.Trc10Info> trc10Map) {
    List<BalanceTrackerTrigger.Trc10StatusPojo> list = new LinkedList<>();
    if (CollectionUtils.isEmpty(trc10Map)) {
      return list;
    }

    trc10Map.forEach((key, info) -> {
      BalanceTrackerTrigger.Trc10StatusPojo trc10Info = new BalanceTrackerTrigger.Trc10StatusPojo();
      trc10Info.setAccountAddress(accountAddress);
      trc10Info.setTokenAddress(info.getTokenId());
      trc10Info.setBalance(String.valueOf(info.getBalance()));
      trc10Info.setIncrementBalance(String.valueOf(info.getIncrementBalance()));
      list.add(trc10Info);
    });
    return list;
  }

  @Override
  public void processTrigger() {
    EventPluginLoader.getInstance().postTRC20TrackerTrigger(trc20TrackerTrigger);
  }


}
