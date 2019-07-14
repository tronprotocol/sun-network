package org.tron.sunapi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SunNetwork {
  @Getter
  public MainchainApi mainChainService;
  @Getter
  public SidechainApi sideChainService;
  @Getter
  public CrosschainApi crossChainService;

  /**
   * @param config the configuration path
   * @param priKey the private key
   * @return the result of initialize
   * @author sun-network
   */
//  public SunNetworkResponse<Integer> init(String config, String priKey) {
//    SunNetworkResponse<Integer> ret;
//
//    this.mainChainService = new MainchainApi();
//    ret = this.mainChainService.init(config, priKey);
//    if(!ret.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
//      return ret;
//    }
//
//    this.sideChainService = new SidechainApi();
//    ret = this.sideChainService.init(config, priKey);
//    if(!ret.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
//      return ret;
//    }
//
//    crossChainService = new CrosschainApi(mainChainService, sideChainService);
//
//    return ret;
//  }

  /**
   * @param config the configuration path
   * @return the result of initialize
   * @author sun-network
   */
  public SunNetworkResponse<Integer> init(String config) {
    SunNetworkResponse<Integer> ret;

    this.mainChainService = new MainchainApi();
    ret = this.mainChainService.init(config);
    if (!ret.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      return ret;
    }

    this.sideChainService = new SidechainApi();
    ret = this.sideChainService.init(config);
    if (!ret.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      return ret;
    }

    crossChainService = new CrosschainApi(mainChainService, sideChainService);

    return ret;
  }

  public SunNetworkResponse<Integer> setPrivateKey(String priKey) {
    SunNetworkResponse<Integer> ret;

    ret = this.mainChainService.setPrivateKey(priKey);
    if (!ret.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      return ret;
    }

    ret = this.sideChainService.setPrivateKey(priKey);

    return ret;
  }


  public static void main(String[] args) {
    String priKey = "e901ef62b241b6f1577fd6ea34ef8b1c4b3ddee1e3c051b9e63f5ff729ad47a1";

    SunNetwork sdk = new SunNetwork();
    sdk.init("config.conf");
    sdk.setPrivateKey(priKey);

    System.out.println("\r\n===================== balance before deposit ========================");
    {
      SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        System.out.println("main chain balance is:" + resp.getData());
      }

      resp = sdk.getSideChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        System.out.println("side chain balance is:" + resp.getData());
      }
    }


  }
}
