package org.tron.service.task.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcException;
import org.tron.service.task.EventTask;

@Slf4j(topic = "mainChainTask")
public class DepositTRC721Task implements EventTask {

  private String from;
  private String uid;
  private String contractAddress;

  public DepositTRC721Task(String from, String uid, String contractAddress) {
    this.from = from;
    this.uid = uid;
    this.contractAddress = contractAddress;
  }

  @Override
  public void run() {
    logger
        .info("from:{},uid:{},contractAddress{}", this.from, this.uid, this.contractAddress);
    try {
      String sideContractAddress = SideChainGatewayApi
          .getMainToSideContractMap(this.contractAddress);
      String trxId = SideChainGatewayApi
          .mintToken(sideContractAddress, this.from, Long.parseLong(this.uid));
      SideChainGatewayApi.chackTxInfo(trxId);
    } catch (RpcException e) {
      logger
          .error("from:{},uid:{},contractAddress{}", this.from, this.uid, this.contractAddress);
      e.printStackTrace();
    }
  }
}
