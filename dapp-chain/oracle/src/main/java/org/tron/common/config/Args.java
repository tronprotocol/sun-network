package org.tron.common.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.WalletUtil;
import org.apache.commons.lang3.ArrayUtils;


@Slf4j
@Component
public class Args {

  public static Args instance;

  @Parameter(names = {"-c", "--config"}, description = "Config File")
  private String shellConfFileName = "";

  @Getter
  private List<String> mainchainFullNodeList;
  @Getter
  private String mainchainFullNode;
  @Getter
  private String mainchainSolidity;


  @Getter
  private List<String> sidechainFullNodeList;
  @Getter
  private String sidechainFullNode;
  @Getter
  private String sidechainSolidity;


  @Getter
  private byte[] mainchainGateway;


  @Getter
  private byte[] sidechainGateway;

  @Getter
  private String mainchainKafka;


  @Getter
  private String sidechainKafka;


  @Getter
  @Parameter(names = {"-p", "--private-key"}, description = "Oracle Private Key")
  private String oraclePrivateKeyStr;
  @Getter
  private byte[] oraclePrivateKey;

  @Getter
  private byte[] sunTokenAddress;

  @Getter
  private String alertDingWebhookToken;

  @Getter
  private Map<String, Properties> mysqlReadConfs = new HashMap<>();
  @Getter
  private Map<String, Properties> mysqlWriteConfs = new HashMap<>();


  /**
   * set parameters.
   */
  private Args() {
  }

  private void loadMysqlConf(Config config) {
    for (Map.Entry<String, ConfigValue> entry : config.getConfig("mysql").root().entrySet()) {
      String dbName = entry.getKey();
      for (Map.Entry<String, ConfigValue> e : ((ConfigObject) entry.getValue()).entrySet()) {

      }
      ConfigObject common = (ConfigObject) (((ConfigObject) entry.getValue()).get("common"));
      ConfigObject read = (ConfigObject) (((ConfigObject) entry.getValue()).get("read"));
      ConfigObject write = (ConfigObject) (((ConfigObject) entry.getValue()).get("write"));

      Properties readProperties = new Properties();
      Properties writeProperties = new Properties();

      if (common != null && common.entrySet().size() > 0) {
        for (Map.Entry<String, ConfigValue> e : common.entrySet()) {
          readProperties.put(e.getKey(), e.getValue().unwrapped().toString());
          writeProperties.put(e.getKey(), e.getValue().unwrapped().toString());
        }
      }

      if (write != null && write.entrySet().size() > 0) {
        for (Map.Entry<String, ConfigValue> e : write.entrySet()) {
          writeProperties.put(e.getKey(), e.getValue().unwrapped());
        }
      }
      // 默认会有写的库
      mysqlWriteConfs.put(dbName, writeProperties);

      if (read != null && read.entrySet().size() > 0) {
        for (Map.Entry<String, ConfigValue> e : read.entrySet()) {
          readProperties.put(e.getKey(), e.getValue().unwrapped());
        }
        mysqlReadConfs.put(dbName, readProperties);
      }
    }
  }

  public static Args getInstance() {
    // TODO: fix singleton
    if (instance == null) {
      instance = new Args();
    }
    return instance;
  }

  public void setParam(String[] args) throws RpcConnectException {
    JCommander.newBuilder().addObject(instance).build().parse(args);
    loadConf(shellConfFileName);
  }

  public void loadSunTokenAddress() throws RpcConnectException {
    this.sunTokenAddress = WalletUtil
        .decodeFromBase58Check(SideChainGatewayApi.getSunTokenAddress());
  }

  public void loadConf(String confName) throws RpcConnectException {
    if (StringUtils.isEmpty(confName)) {
      confName = "config-sample.conf";
    }
    Config config = Configuration.getByPath(confName);
    this.mainchainFullNodeList = config.getStringList("mainchain.fullnode.ip.list");
    this.mainchainFullNode = this.mainchainFullNodeList.get(0);
    this.mainchainSolidity = config.getStringList("mainchain.solidity.ip.list").get(0);

    this.sidechainFullNodeList = config.getStringList("sidechain.fullnode.ip.list");
    this.sidechainFullNode = this.sidechainFullNodeList.get(0);
    this.sidechainSolidity = config.getStringList("sidechain.solidity.ip.list").get(0);

    this.mainchainGateway = WalletUtil
        .decodeFromBase58Check(config.getString("gateway.mainchain.address"));
    this.sidechainGateway = WalletUtil
        .decodeFromBase58Check(config.getString("gateway.sidechain.address"));
    if (StringUtils.isEmpty(this.oraclePrivateKeyStr)) {
      this.oraclePrivateKey = Hex.decode(config.getString("oracle.private.key"));
    }else{
      this.oraclePrivateKey = Hex.decode(this.oraclePrivateKeyStr);
    }

    this.mainchainKafka = config.getString("kafka.mainchain.server");

    if (config.hasPath("alert.dingding.webhook.token")) {
      this.alertDingWebhookToken = config.getString("alert.dingding.webhook.token");
    }

    // loadMysqlConf(config);

    // loadSunTokenAddress();
  }

  public static void main(String[] args) {
    try {
      Args.getInstance().setParam(args);
    } catch (RpcConnectException e) {
      e.printStackTrace();
    }
  }

}
