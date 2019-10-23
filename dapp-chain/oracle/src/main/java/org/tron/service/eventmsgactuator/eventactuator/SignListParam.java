package org.tron.service.eventmsgactuator.eventactuator;

import java.util.List;
import lombok.Getter;

public class SignListParam {

  @Getter
  private List<String> oracleSigns;
  @Getter
  private List<String> oracleAddresses;

  public SignListParam(List<String> oracleSigns, List<String> oracleAddresses) {
    this.oracleSigns = oracleSigns;
    this.oracleAddresses = oracleAddresses;
  }
}
