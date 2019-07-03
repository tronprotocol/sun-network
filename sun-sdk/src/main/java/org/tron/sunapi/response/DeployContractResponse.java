package org.tron.sunapi.response;

import lombok.Data;

@Data
public class DeployContractResponse {
  public String contractAddress;

  public DeployContractResponse(String address) {
    contractAddress = address;
  }

}
