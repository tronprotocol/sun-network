package org.tron.tinydice;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.DataWord;
import org.tron.sunapi.SunNetwork;
import org.tron.sunapi.SunNetworkResponse;
import org.tron.sunapi.request.TriggerContractRequest;
import org.tron.sunapi.response.TransactionResponse;

@Slf4j
public class Server {

  @Getter
  private SunNetwork sdk;
  private String contractAddress;

  public Server() {
    ConfigImpl config = new ConfigImpl("config.conf");
    contractAddress = config.getContractAddress();
    sdk = new SunNetwork();

    SunNetworkResponse<Integer> ret = sdk.init(config, null);
    if (ret.getData() != 0) {
      System.out.println("Failed to init sdk");
    }
    sdk.setPrivateKey(config.getRtuPriKey());
  }

  private boolean rtu(){
    TriggerContractRequest request = new TriggerContractRequest();
    request.setContractAddrStr(contractAddress);
    request.setMethodStr("rtu()");
    SunNetworkResponse<TransactionResponse> sunNetworkresp = sdk.sideChainService.triggerContract(request);
    logger.info("SUN Network response code is: " + sunNetworkresp.getDesc());

    TransactionResponse resp = sunNetworkresp.getData();
    return resp != null && resp.getResult();
  }

  private byte[] stat() {
    TriggerContractRequest request = new TriggerContractRequest();
    request.setContractAddrStr(contractAddress);
    request.setMethodStr("stat()");
    SunNetworkResponse<TransactionResponse> sunNetworkresp = sdk.sideChainService.triggerContract(request);
    logger.info("SUN Network response code is: " + sunNetworkresp.getDesc());

    TransactionResponse resp = sunNetworkresp.getData();
    return (resp != null && resp.getResult()) ? ByteArray.fromHexString(resp.constantResult) : null;
  }

  public void start() {
    while (true) {
      try {
        Thread.sleep(500);
        byte[] result = stat();
        if (!ArrayUtils.isEmpty(result)) {
          long currIdx = DataWord.getDataWord(result, 0).longValue();
          long curBatchIdx = DataWord.getDataWord(result, 1).longValue();
          logger.info("currIdx:" + currIdx + " curBatchIdx:" + curBatchIdx);

          for (long i = curBatchIdx + 1; i <= currIdx; i++) {
            rtu();
          }
        }
      } catch (Throwable e) {
        logger.error("Error occur in rtu service, try next fullNode", e);
      }
    }
  }

  public static void main(String[] args) {
    Server server = new Server();
    server.start();
  }
}
