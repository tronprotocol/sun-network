package org.tron.common.config;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import java.io.BufferedReader;
import java.io.File;
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
import org.tron.client.SideChainGatewayApi;
import org.tron.common.config.args.SeedNode;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.overlay.discover.node.Node;
import org.tron.common.utils.WalletUtil;


@Slf4j
@Component
public class Args {

  public static Args instance;

  @Parameter(names = {"-c", "--config"}, description = "Config File")
  private String shellConfFileName = "";

  @Parameter(names = {"-d", "--database"}, description = "Directory")
  private String databaseDirectory = "database";

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
  private String chainId;

  @Getter
  private GenesisBlock genesisBlock;
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
      confName = "config.conf";
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
    } else {
      this.oraclePrivateKey = Hex.decode(this.oraclePrivateKeyStr);
    }

    this.mainchainKafka = config.getString("kafka.mainchain.server");

    if (config.hasPath("alert.dingding.webhook.token")) {
      this.alertDingWebhookToken = config.getString("alert.dingding.webhook.token");
    }


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


    if (config.hasPath("genesis.block")) {
      this.genesisBlock = new GenesisBlock();

      this.genesisBlock.setTimestamp(config.getString("genesis.block.timestamp"));
      this.genesisBlock.setParentHash(config.getString("genesis.block.parentHash"));

//      if (config.hasPath("genesis.block.witnesses")) {
//        this.genesisBlock.setWitnesses(getWitnessesFromConfig(config));
//      }
    } else {
      this.genesisBlock = GenesisBlock.getDefault();
    }
    // loadMysqlConf(config);

    // loadSunTokenAddress();
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

  /**
   * get output directory.
   */
  public String getDatabaseDirectory() {
    if (!this.databaseDirectory.equals("") && !this.databaseDirectory.endsWith(File.separator)) {
      return this.databaseDirectory + File.separator;
    }
    return this.databaseDirectory;
  }
  public static void main(String[] args) {
    try {
      Args.getInstance().setParam(args);
    } catch (RpcConnectException e) {
      e.printStackTrace();
    }
  }

}
