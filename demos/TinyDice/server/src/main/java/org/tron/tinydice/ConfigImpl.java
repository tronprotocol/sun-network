package org.tron.tinydice;

import com.typesafe.config.Config;
import lombok.Data;
import org.bouncycastle.util.encoders.Hex;
import org.tron.common.utils.AddressUtil;
import org.tron.common.utils.ByteArray;
import org.tron.core.config.Configuration;
import org.tron.sunapi.IServerConfig;

@Data
public class ConfigImpl implements IServerConfig {

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

  private String contractAddress;
  private String rtuPriKey;

  ConfigImpl(String file) {
    Config config = Configuration.getByPath(file);

    //mainchain config
    if (config.hasPath("mainchain.soliditynode.ip.list")){
      mainSolidityNode = config.getStringList("mainchain.soliditynode.ip.list").get(0);
    }
    if (config.hasPath("mainchain.fullnode.ip.list")) {
      mainFullNode = config.getStringList("mainchain.fullnode.ip.list").get(0);
    }
    mainNetType = config.getString("mainchain.net.type");
    mainRPCVersion = config.getInt("mainchain.RPC_version");

    mainGatewayAddress = AddressUtil
        .decode58Check(config.getString("mainchain.gateway_address"));

    //sidechain config
    if (config.hasPath("sidechain.soliditynode.ip.list")) {
      sideSolidityNode = config.getStringList("sidechain.soliditynode.ip.list").get(0);
    }
    if (config.hasPath("sidechain.fullnode.ip.list")) {
      sideFullNode = config.getStringList("sidechain.fullnode.ip.list").get(0);
    }
    sideNetType = config.getString("sidechain.net.type");
    sideRPCVersion = config.getInt("sidechain.RPC_version");
    sideGatewayAddress = AddressUtil
        .decode58Check(config.getString("sidechain.gateway_address"));
    sideChainId = ByteArray.fromHexString(config.getString("sidechain.sideChainId"));

    // game config
    contractAddress = config.getString("game.contractAddress");
    rtuPriKey = config.getString("game.rtuPriKey");
  }

}
