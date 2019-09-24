package org.tron.walletcli.checker;

public class ContractChecker extends AbstractChecker {

  protected long checkTimeInterval = config.getLong("checkTimeInterval");
  protected String mainChainGateway = config.getString("mainchain.gateway_address");
  protected String sideChainGateway = config.getString("sidechain.gateway_address");
}
