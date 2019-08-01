package org.tron.sunapi;

public interface IServerConfig {

  String getMainSolidityNode();

  String getMainFullNode();

  String getMainNetType();

  int getMainRPCVersion();

  byte[] getMainGatewayAddress();

  String getSideSolidityNode();

  String getSideFullNode();

  String getSideNetType();

  int getSideRPCVersion();

  byte[] getSideGatewayAddress();

  byte[] getSideChainId();
}
