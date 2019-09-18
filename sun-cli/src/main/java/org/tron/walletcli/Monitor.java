package org.tron.walletcli;


import com.typesafe.config.Config;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tron.api.GrpcAPI.AccountResourceMessage;
import org.tron.core.config.Configuration;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Account.AccountResource;

public class Monitor{
  private static final Logger logger = LoggerFactory.getLogger("monitor");
  private WalletApiWrapper walletApiWrapper = new WalletApiWrapper();
  public static List<String> oracleAddress = Configuration.getByPath("config.conf").getStringList("oracleList");
  public String alertUrl = Configuration.getByPath("config.conf").getString("alert.webhook.url");
  public long checkTimeInterval = Configuration.getByPath("config.conf").getLong("checkTimeInterval");



  public void checkOracleResources(String chainName){
    oracleAddress.stream().forEach(address-> {
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
      logger.info(msg);
      sendAlert(msg);
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

  public  void sendAlert(String msg) {
    if (StringUtils.isEmpty(alertUrl)) {
      return;
    }
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httppost = new HttpPost(alertUrl);
      httppost.addHeader("Content-Type", "application/json; charset=utf-8");

      String MSG_BASE = "{ \"msgtype\": \"text\", \"text\": {\"content\": \"%s\"}}";
      String textMsg = String.format(MSG_BASE, msg);
      StringEntity se = new StringEntity(textMsg, "utf-8");
      httppost.setEntity(se);

      HttpResponse response = httpclient.execute(httppost);
      if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
        String result = EntityUtils.toString(response.getEntity(), "utf-8");
        System.out.println(result);
      }
    } catch (ClientProtocolException e) {
      logger.error("ClientProtocolException {}", e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      logger.error("IOException {}", e.getMessage());
      e.printStackTrace();
    }
  }


  public static void main(String[] s) {
    Monitor monitor = new Monitor();
    Runnable runnable = new Runnable(){
      public void run() {
        monitor.checkOracleResourcesTask();
      }
    };
    ScheduledExecutorService service = Executors
        .newSingleThreadScheduledExecutor();

    service.scheduleAtFixedRate(runnable, 0, monitor.checkTimeInterval, TimeUnit.SECONDS);
  }

}
