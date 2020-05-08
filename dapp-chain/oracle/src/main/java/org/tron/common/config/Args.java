package org.tron.common.config;

import static java.lang.System.exit;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.Config;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.keystore.CipherException;
import org.tron.keystore.Credentials;


@Slf4j(topic = "args")
@Component
public class Args {

  public static Args instance;

  @Parameter(names = {"-c", "--config"}, description = "Config File")
  private String shellConfFileName = "";

  @Getter
  @Parameter(names = {"-i",
      "--init-task"}, help = true, description = "exe init task before event task")
  private boolean initTask = false;

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
  private String mainchainGatewayStr;

  @Getter
  private byte[] refMainchainGateway;
  @Getter
  private String refMainchainGatewayStr;

  @Getter
  private byte[] chainId;

  @Getter
  private byte[] sidechainGateway;
  @Getter
  private String sidechainGatewayStr;

  @Getter
  private String mainchainKafka;

  @Parameter(names = {"-p", "--private-key"}, description = "Oracle Private Key")
  private String oraclePrivateKeyStr;
  @Getter
  private byte[] oraclePrivateKey;

  @Getter
  private int oracleRetryTimes;

  @Parameter(names = {"-pw", "--password"}, description = "Oracle keystore password")
  private String password;

  @Getter
  private byte[] sunTokenAddress;

  @Getter
  private String alertDingWebhookToken;

  @Getter
  private Map<String, Properties> mysqlReadConfs = new HashMap<>();
  @Getter
  private Map<String, Properties> mysqlWriteConfs = new HashMap<>();

  @Getter
  KafkaConfig kafkaConfig = null;
  @Getter
  String kafkaGroupId;

  /**
   * set parameters.
   */
  private Args() {
  }

  public static Args getInstance() {
    if (instance == null) {
      instance = new Args();
    }
    return instance;
  }

  public void setParam(String[] args) {
    JCommander.newBuilder().addObject(instance).build().parse(args);
    loadConf(shellConfFileName);
  }

  private void loadConf(String confName) {
    if (StringUtils.isEmpty(confName)) {
      confName = "config.conf";
    }
    Config config = Configuration.getByPath(confName);
    this.mainchainFullNodeList = config.getStringList("mainchain.fullnode.ip.list");
    this.mainchainFullNode = this.mainchainFullNodeList.get(0);
    if (config.hasPath("mainchain.solidity.ip.list")) {
      this.mainchainSolidity = config.getStringList("mainchain.solidity.ip.list").get(0);
    }

    this.sidechainFullNodeList = config.getStringList("sidechain.fullnode.ip.list");
    this.sidechainFullNode = this.sidechainFullNodeList.get(0);
    if (config.hasPath("sidechain.solidity.ip.list")) {
      this.sidechainSolidity = config.getStringList("sidechain.solidity.ip.list").get(0);
    }

    this.mainchainGatewayStr = config.getString("gateway.mainchain.address");
    this.mainchainGateway = WalletUtil
        .decodeFromBase58Check(this.mainchainGatewayStr);
    this.refMainchainGatewayStr = config.getString("gateway.referenceMainchain.address");
    this.refMainchainGateway = WalletUtil
        .decodeFromBase58Check(this.refMainchainGatewayStr);

    this.chainId = ByteArray.fromHexString(config.getString("sidechain.chain.id"));

    this.sidechainGatewayStr = config.getString("gateway.sidechain.address");
    this.sidechainGateway = WalletUtil
        .decodeFromBase58Check(sidechainGatewayStr);

    this.oracleRetryTimes = config.getInt("oracle.retryTimes");
    if (oracleRetryTimes >= SystemSetting.RETRY_TIMES_EPOCH_OFFSET || oracleRetryTimes < 1) {
      logger.error("oracle retryTimes should < " + SystemSetting.RETRY_TIMES_EPOCH_OFFSET + " and >= 1");
      exit(-1);
    }

    if (StringUtils.isNotEmpty(this.oraclePrivateKeyStr)) {
      this.oraclePrivateKey = Hex.decode(this.oraclePrivateKeyStr);
    } else if (config.hasPath("oracle.private.key")) {
      this.oraclePrivateKeyStr = config.getString("oracle.private.key");
      this.oraclePrivateKey = Hex.decode(this.oraclePrivateKeyStr);
    } else if (config.hasPath("oracle.keystore")) {

      String fileName = System.getProperty("user.dir") + "/" + config.getString("oracle.keystore");
      String password;
      if (StringUtils.isEmpty(instance.password)) {
        System.out.println("Please input your password.");
        password = WalletUtil.inputPassword();
      } else {
        password = instance.password;
        instance.password = null;
      }

      try {
        Credentials credentials = WalletUtil
            .loadCredentials(password, new File(fileName));
        ECKey ecKeyPair = credentials.getEcKeyPair();
        oraclePrivateKey = ecKeyPair.getPrivKeyBytes();
        oraclePrivateKeyStr = ByteArray.toHexString(oraclePrivateKey);
      } catch (IOException e) {
        logger.error(e.getMessage());
        logger.error("Witness node start faild!");
        exit(-1);
      } catch (CipherException e) {
        logger.error(e.getMessage());
        logger.error("Witness node start faild!");
        exit(-1);
      }
    }

    this.mainchainKafka = config.getString("kafka.server");

    if (config.hasPath("alert.webhook.url")) {
      this.alertDingWebhookToken = config.getString("alert.webhook.url");
    }
    if (config.hasPath("initTaskSwitch") && config.getBoolean("initTaskSwitch")) {
      this.initTask = true;
    }
    if (config.hasPath("kafka.authorization.user") && config
        .hasPath("kafka.authorization.passwd")) {
      kafkaConfig = new KafkaConfig(
          config.getString("kafka.authorization.user"),
          config.getString("kafka.authorization.passwd"));
    }
    if (config.hasPath("kafka.group.id")) {
      kafkaGroupId = config.getString("kafka.group.id");
    } else {
      kafkaGroupId = "Oracle_" + getOracleAddress();
    }
  }

  public String getOracleAddress() {
    return WalletUtil.encode58Check(
        ECKey.fromPrivate(getOraclePrivateKey()).getAddress());
  }

  public static void main(String[] args) {
    Args.getInstance().setParam(args);
  }

}
