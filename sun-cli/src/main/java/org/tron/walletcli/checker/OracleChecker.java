package org.tron.walletcli.checker;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.core.config.Configuration;
import org.tron.protos.Protocol.Account;

public class OracleChecker extends AbstractChecker {
  public static List<String> oracleAddress = config.getStringList("oracleList");
  public long checkTimeInterval = config.getLong("checkTimeInterval");

  public void checkOracleResources(String chainName){
    oracleAddress.stream().forEach(address-> {
      Date minute = new Date();
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(minute);
      int min = calendar.get(Calendar.MINUTE);

      boolean needPrint = ((min % 60) < 6) ;
      Account account = walletApiWrapper.getAccount(address).getData();
      long balance = account.getBalance();
      AccountResourceMessage resource = walletApiWrapper.getAccountResource(address);
      long bandwidthLimit = resource.getNetLimit();
      long bandwidthUsed = resource.getNetUsed();
      long energyLimit = resource.getEnergyLimit();
      long energyUsed = resource.getEnergyUsed();
      String msg = chainName + " Oracle " + address + " balance " + balance + " bandwidthLimit "
          + bandwidthLimit + " bandwidthUsed " + bandwidthUsed + " energyLimit " +  energyLimit +
          " energyUsed " + energyUsed;

      if(needPrint) {
        logger.info(msg);
        sendAlert(msg);
      }
      if(bandwidthLimit - bandwidthUsed < 2000) {
        msg = chainName + " Oracle " + address + "low bandwidth";
        sendAlert(msg);
      }

      if(energyLimit - energyUsed < 80000) {
        msg = chainName + " Oracle " + address + "low energy";
        sendAlert(msg);
      }
    });
  }

  public void checkOracleResourcesTask() {
    checkOracleResources("MainChain");
    walletApiWrapper.switch2Side();
    checkOracleResources("SideChain");
    walletApiWrapper.switch2Main();
  }
}
