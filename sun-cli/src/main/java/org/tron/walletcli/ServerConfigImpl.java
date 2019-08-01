package org.tron.walletcli;

import lombok.Data;
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

  }
}
