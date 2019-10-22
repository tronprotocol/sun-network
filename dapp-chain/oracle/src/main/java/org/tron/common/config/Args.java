package org.tron.common.config;

import static java.lang.System.exit;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.Config;
import java.io.File;
import java.io.IOException;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.spongycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.common.crypto.ECKey;
import org.tron.common.utils.ByteArray;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.args.SeedNode;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.overlay.discover.node.Node;
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

  @Getter
  @Parameter(description = "--seed-nodes")
  private List<String> seedNodes = new ArrayList<>();

  @Getter
  private SeedNode seedNode;

  @Getter
  @Setter
  private boolean nodeDiscoveryEnable;

  @Getter
  @Setter
  private boolean nodeDiscoveryPersist;

  @Getter
  @Setter
  private int nodeListenPort;


  @Getter
  @Setter
  private String nodeDiscoveryBindIp;

  @Getter
  @Setter
  private String nodeExternalIp;

  @Getter
  @Setter
  private int tcpNettyWorkThreadNum;

  @Getter
  @Setter
  private int udpNettyWorkThreadNum;

  @Getter
  @Setter
  private int nodeP2pVersion;

  @Getter
  @Setter
  private int nodeConnectionTimeout;

  @Getter
  @Setter
  private List<Node> activeNodes;

  @Getter
  @Setter
  private List<Node> passiveNodes;

  @Getter
  @Setter
  private List<Node> fastForwardNodes;

  @Getter
  @Setter
  private int nodeMaxActiveNodes;

  @Getter
  @Setter
  private int nodeMaxActiveNodesWithSameIp;

  @Getter
  @Setter
  private double connectFactor;

  @Getter
  @Setter
  private double activeConnectFactor;

  @Getter
  @Setter
  private double disconnectNumberFactor;

  @Getter
  @Setter
  private double maxConnectNumberFactor;

  @Getter
  @Setter
  private long receiveTcpMinDataLength;

  @Getter
  @Setter
  private boolean isOpenFullTcpDisconnect;

  @Getter
  @Parameter(names = {"--fast-forward"})
  private boolean fastForward = false;


  @Getter
  @Setter
  private int validContractProtoThreadNum;

  @Getter
  @Setter
  @Parameter(names = {"--validate-sign-thread"}, description = "Num of validate thread")
  private int validateSignThreadNum;


  @Getter
  @Setter
  private int minEffectiveConnection;

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

    this.seedNode = new SeedNode();
    this.seedNode.setIpList(Optional.ofNullable(this.seedNodes)
        .filter(seedNode -> 0 != seedNode.size())
        .orElse(config.getStringList("seed.node.ip.list")));

    this.nodeDiscoveryEnable =
        config.hasPath("node.discovery.enable") && config.getBoolean("node.discovery.enable");
    this.nodeDiscoveryPersist =
        config.hasPath("node.discovery.persist") && config.getBoolean("node.discovery.persist");
    this.nodeListenPort =
        config.hasPath("node.listen.port") ? config.getInt("node.listen.port") : 0;

    externalIp(config,this);

    this.tcpNettyWorkThreadNum = config.hasPath("node.tcpNettyWorkThreadNum") ? config
        .getInt("node.tcpNettyWorkThreadNum") : 0;

    this.udpNettyWorkThreadNum = config.hasPath("node.udpNettyWorkThreadNum") ? config
        .getInt("node.udpNettyWorkThreadNum") : 1;
    this.nodeP2pVersion =
        config.hasPath("node.p2p.version") ? config.getInt("node.p2p.version") : 0;

    this.nodeConnectionTimeout =
        config.hasPath("node.connection.timeout") ? config.getInt("node.connection.timeout") * 1000
            : 0;

    this.activeNodes = getNodes(config, "node.active");

    this.passiveNodes = getNodes(config, "node.passive");

    this.fastForwardNodes = getNodes(config, "node.fastForward");

    this.nodeMaxActiveNodes =
        config.hasPath("node.maxActiveNodes") ? config.getInt("node.maxActiveNodes") : 30;

    this.nodeMaxActiveNodesWithSameIp =
        config.hasPath("node.maxActiveNodesWithSameIp") ? config
            .getInt("node.maxActiveNodesWithSameIp") : 2;
    this.connectFactor =
        config.hasPath("node.connectFactor") ? config.getDouble("node.connectFactor") : 0.3;

    this.activeConnectFactor = config.hasPath("node.activeConnectFactor") ?
        config.getDouble("node.activeConnectFactor") : 0.1;
    this.disconnectNumberFactor = config.hasPath("node.disconnectNumberFactor") ?
        config.getDouble("node.disconnectNumberFactor") : 0.4;
    this.maxConnectNumberFactor = config.hasPath("node.maxConnectNumberFactor") ?
        config.getDouble("node.maxConnectNumberFactor") : 0.8;
    this.receiveTcpMinDataLength = config.hasPath("node.receiveTcpMinDataLength") ?
        config.getLong("node.receiveTcpMinDataLength") : 2048;
    this.isOpenFullTcpDisconnect = config.hasPath("node.isOpenFullTcpDisconnect") && config
        .getBoolean("node.isOpenFullTcpDisconnect");
    this.validContractProtoThreadNum =
        config.hasPath("node.validContractProto.threads") ? config
            .getInt("node.validContractProto.threads")
            : Runtime.getRuntime().availableProcessors();

    this.validateSignThreadNum = config.hasPath("node.validateSignThreadNum") ? config
        .getInt("node.validateSignThreadNum") : Runtime.getRuntime().availableProcessors() / 2;
    // loadMysqlConf(config);

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

    minEffectiveConnection = config.hasPath("node.rpc.minEffectiveConnection") ?
        config.getInt("node.rpc.minEffectiveConnection") : 1;
  }

  public String getOracleAddress() {
    return WalletUtil.encode58Check(
        ECKey.fromPrivate(getOraclePrivateKey()).getAddress());
  }

  private static void externalIp(final com.typesafe.config.Config config, Args args) {
    if (!config.hasPath("node.discovery.external.ip") || config
        .getString("node.discovery.external.ip").trim().isEmpty()) {
      if (args.nodeExternalIp == null) {
        logger.info("External IP wasn't set, using checkip.amazonaws.com to identify it...");
        BufferedReader in = null;
        try {
          in = new BufferedReader(new InputStreamReader(
              new URL("http://checkip.amazonaws.com").openStream()));
          args.nodeExternalIp = in.readLine();
          if (args.nodeExternalIp == null || args.nodeExternalIp.trim().isEmpty()) {
            throw new IOException("Invalid address: '" + args.nodeExternalIp + "'");
          }
          try {
            InetAddress.getByName(args.nodeExternalIp);
          } catch (Exception e) {
            throw new IOException("Invalid address: '" + args.nodeExternalIp + "'");
          }
          logger.info("External address identified: {}", args.nodeExternalIp);
        } catch (IOException e) {
          args.nodeExternalIp = args.nodeDiscoveryBindIp;
          logger.warn(
              "Can't get external IP. Fall back to peer.bind.ip: " + args.nodeExternalIp + " :"
                  + e);
        } finally {
          if (in != null) {
            try {
              in.close();
            } catch (IOException e) {
              //ignore
            }
          }

        }
      }
    } else {
      args.nodeExternalIp = config.getString("node.discovery.external.ip").trim();
    }
  }

  private static List<Node> getNodes(final com.typesafe.config.Config config, String path) {
    if (!config.hasPath(path)) {
      return Collections.emptyList();
    }
    List<Node> ret = new ArrayList<>();
    List<String> list = config.getStringList(path);
    for (String configString : list) {
      Node n = Node.instanceOf(configString);
      ret.add(n);
    }
    return ret;
  }

  public static void main(String[] args) {
    Args.getInstance().setParam(args);
  }

}
