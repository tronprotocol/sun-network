package org.tron.walletcli;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.tron.walletcli.checker.OracleChecker;

public class Monitor{



  public static void main(String[] s) {
    OracleChecker oc = new OracleChecker();
    Runnable runnable = new Runnable(){
      public void run() {
        oc.checkOracleResourcesTask();
      }
    };
    ScheduledExecutorService service = Executors
        .newSingleThreadScheduledExecutor();

    service.scheduleAtFixedRate(runnable, 0, oc.checkTimeInterval, TimeUnit.SECONDS);
  }

}
