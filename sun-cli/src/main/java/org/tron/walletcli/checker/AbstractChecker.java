package org.tron.walletcli.checker;

import java.io.IOException;
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
import org.tron.core.config.Configuration;
import org.tron.walletcli.WalletApiWrapper;

public class AbstractChecker implements Checkable {

  protected static final Logger logger = LoggerFactory.getLogger("monitor");
  protected static com.typesafe.config.Config config = Configuration.getByPath("config.conf");
  public static String alertUrl = config.getString("alert.webhook.url");

  protected WalletApiWrapper walletApiWrapper = new WalletApiWrapper(true);

  public void sendAlert(String msg) {
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


}
