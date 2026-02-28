package org.tron.walletcli;

import com.typesafe.config.Config;
import lombok.Data;
import org.tron.common.utils.AddressUtil;
import org.tron.common.utils.ByteArray;
import org.tron.core.config.Configuration;
import org.tron.sunapi.IServerConfig;

@Data
public class ServerConfigImpl implements IServerConfig {

  private String mainSolidityNode;
  private String mainFullNode;
  private String mainNetType;
  private int mainRPCVersion;
  private byte[] mainGatewayAddress;
  private String sideSolidityNode;
  private String sideFullNode;
  private String sideNetType;
  private int sideRPCVersion;
  private byte[] sideGatewayAddress;
  private byte[] sideChainId;

  ServerConfigImpl(String file) {
    Config config = Configuration.getByPath(file);
//mainchain config
    if (config.hasPath("mainchain.soliditynode.ip.list")) {
      setMainSolidityNode(config.getStringList("mainchain.soliditynode.ip.list").get(0));
    }
    if (config.hasPath("mainchain.fullnode.ip.list")) {
      setMainFullNode(config.getStringList("mainchain.fullnode.ip.list").get(0));
    }
    if (config.hasPath("mainchain.net.type")) {
      setMainNetType(config.getString("mainchain.net.type"));
    }
    if (config.hasPath("mainchain.RPC_version")) {
      setMainRPCVersion(config.getInt("mainchain.RPC_version"));
    }
    if (config.hasPath("mainchain.gateway_address")) {
      setMainGatewayAddress(AddressUtil
          .decode58Check(config.getString("mainchain.gateway_address")));
    }

//sidechain config
    if (config.hasPath("sidechain.soliditynode.ip.list")) {
      setSideSolidityNode(config.getStringList("sidechain.soliditynode.ip.list").get(0));
    }
    if (config.hasPath("sidechain.fullnode.ip.list")) {
      setSideFullNode(config.getStringList("sidechain.fullnode.ip.list").get(0));
    }
    if (config.hasPath("sidechain.net.type")) {
      setSideNetType(config.getString("sidechain.net.type"));
    }
    if (config.hasPath("sidechain.RPC_version")) {
      setSideRPCVersion(config.getInt("sidechain.RPC_version"));
    }
    if (config.hasPath("sidechain.gateway_address")) {
      setSideGatewayAddress(AddressUtil
          .decode58Check(config.getString("sidechain.gateway_address")));
    }
    setSideChainId(ByteArray.fromHexString(config.getString("sidechain.sideChainId")));
  }
}
