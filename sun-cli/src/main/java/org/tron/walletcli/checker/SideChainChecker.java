package org.tron.walletcli.checker;

import java.util.List;
import org.tron.protos.Protocol;
import org.tron.protos.Protocol.SideChainParameters;
import org.tron.protos.Protocol.SideChainParameters.SideChainParameter;

public class SideChainChecker extends AbstractChecker {
  public long checkTimeInterval = config.getLong("checkTimeInterval");

  public String checkFund(){
    this.walletApiWrapper.switch2Side();
    SideChainParameters sideChainParameters = walletApiWrapper.getSideChainParameters().get();
    for(SideChainParameter para : sideChainParameters.getChainParameterList()){
      if(para.getKey().toLowerCase().contains("getfund")) {
        sendAlert("Current Fund: " + para.getValue() + " sun");
        return para.getValue() + " sun";
      }
    }
    return "";
  }
}
