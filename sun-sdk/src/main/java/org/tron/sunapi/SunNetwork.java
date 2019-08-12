package org.tron.sunapi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.sunserver.IMultiTransactionSign;

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
   * @return the result of initialize
   * @author sun-network
   */
  public SunNetworkResponse<Integer> init(IServerConfig config,
      IMultiTransactionSign multiTransactionSign) {
    SunNetworkResponse<Integer> ret;

    this.mainChainService = new MainchainApi();
    ret = this.mainChainService.init(config, multiTransactionSign);
    if (!ret.getCode().equals(ErrorCodeEnum.SUCCESS.getCode())) {
      return ret;
    }

    this.sideChainService = new SidechainApi();
    ret = this.sideChainService.init(config, multiTransactionSign);
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
}
