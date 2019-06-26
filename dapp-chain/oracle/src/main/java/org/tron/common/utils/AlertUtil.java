package org.tron.common.utils;

import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.tron.common.config.Args;
import org.tron.common.logger.LoggerOracle;

@Slf4j(topic = "alert")
public class AlertUtil {

  private static final LoggerOracle loggerOracle = new LoggerOracle(logger);

  public static void sendAlert(String msg) {
    loggerOracle.error("sendAlert: {} ", msg);
    if (StringUtils.isEmpty(Args.getInstance().getAlertDingWebhookToken())) {
      return;
    }
    try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
      HttpPost httppost = new HttpPost(Args.getInstance().getAlertDingWebhookToken());
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
      loggerOracle.error("ClientProtocolException {}", e.getMessage());
      e.printStackTrace();
    } catch (IOException e) {
      loggerOracle.error("IOException {}", e.getMessage());
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Args.getInstance().setParam(args);
  }
}
