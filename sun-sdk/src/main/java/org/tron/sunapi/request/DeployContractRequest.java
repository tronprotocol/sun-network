package org.tron.sunapi.request;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class DeployContractRequest {
  public String contractName;

  public String abiStr;

  public String codeStr;

  public String constructorStr;

  public String argsStr;

  public boolean Hex;

  public long feeLimit;

  public long consumeUserResourcePercent;

  public long originEnergyLimit;

  public long value;

  public long tokenValue;

  public String tokenId;

  public String libraryAddressPair;

  public String compilerVersion;

  public DeployContractRequest() {
    this.contractName = null;
    this.abiStr = null;
    this.codeStr = null;
    this.constructorStr = null;
    this.argsStr = null;
    this.Hex = false;
    this.feeLimit = 0;
    this.consumeUserResourcePercent = 0;
    this.originEnergyLimit = 0;
    this.value = 0;
    this.tokenValue = 0;
    this.tokenId = null;
    this.libraryAddressPair = null;
    this.compilerVersion = null;
  }

}
