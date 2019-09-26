package org.tron.walletcli.checker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.protos.Protocol.Account;

public class OracleChecker extends AbstractChecker {

  public static List<String> oracleAddress = config.getStringList("oracleList");
  public long checkTimeInterval = config.getLong("checkTimeInterval");

  public void checkOracleResources(String chainName) {
    Date minute = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(minute);
    int min = calendar.get(Calendar.MINUTE);

    boolean needPrint = ((min % 60) < 6);
    List<String> msgList = new ArrayList<>();
    oracleAddress.stream().forEach(address -> {
      Account account = walletApiWrapper.getAccount(address).getData();
      long balance = account.getBalance();
      AccountResourceMessage resource = walletApiWrapper.getAccountResource(address);
      long bandwidthLimit = resource.getNetLimit();
      long bandwidthUsed = resource.getNetUsed();
      long energyLimit = resource.getEnergyLimit();
      long energyUsed = resource.getEnergyUsed();
      String msg = "\nOracle " + address + "\nbalance " + balance + "\nbandwidthLimit "
          + bandwidthLimit + "\nbandwidthUsed " + bandwidthUsed + "\nenergyLimit " + energyLimit +
          "\nenergyUsed " + energyUsed + "\n";
      msgList.add(msg);

      if (bandwidthLimit - bandwidthUsed < 2000) {
        msg = chainName + "\nOracle " + address + " low bandwidth";
        sendAlert(msg);
      }

      if (energyLimit - energyUsed < 80000) {
        msg = chainName + "\nOracle " + address + " low energy";
        sendAlert(msg);
      }
    });
    String totalMsg = msgList.stream().reduce(chainName,(acc, element) ->{
      acc = acc + element + "\n";
      return acc;
    });
    if (needPrint) {
      logger.info(totalMsg);
      sendAlert(totalMsg);
    }
  }

  public void checkOracleResourcesTask() {
    walletApiWrapper.switch2Main();
    checkOracleResources("MainChain");
    walletApiWrapper.switch2Side();
    checkOracleResources("SideChain");
  }
}
