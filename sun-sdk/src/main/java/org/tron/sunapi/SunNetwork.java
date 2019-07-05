package org.tron.sunapi;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.tron.sunapi.response.TransactionResponse;

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
    long balanceMain1 = 0;
    long balanceMain2 = 0;
    long balanceMain3 = 0;
    long balanceSide1 = 0;
    long balanceSide2 = 0;
    long balanceSide3 = 0;

    System.out.println("\r\n===================== balance before deposit ========================");
    {
      SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceMain1 = resp.getData();
        System.out.println("main chain balance is:" + resp.getData());
      }

      resp = sdk.getSideChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceSide1 = resp.getData();
        System.out.println("side chain balance is:" + resp.getData());
      }
    }

    System.out.println("\r\n===================== deposit 124 trx ===============================");
    {
      SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService()
          .depositTrx("TTGhuSDKr561gzHFjkZ1V4ZtMgUEFLa7ct", 124, 1000000);

      System.out.println("Error code desc: " + resp.getDesc());
      System.out.println("transaction result: " + resp.getData().getResult());
      System.out.println("transaction id: " + resp.getData().getTrxId());
    }

    System.out.println("\r\n===================== balance after deposit, sleep 6s ===============");
    {
      try {
        Thread.sleep(6000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceMain2 = resp.getData();
        System.out.println("main chain balance is:" + resp.getData());
      }

      Assert.assertEquals(balanceMain1, balanceMain2 + 124);

      resp = sdk.getSideChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceSide2 = resp.getData();
        System.out.println("side chain balance is:" + resp.getData());
      }

      Assert.assertEquals(balanceSide2, balanceSide1 + 124);
    }

    System.out.println("\r\n===================== withdraw 123 trx ==============================");
    {
      SunNetworkResponse<TransactionResponse> resp = sdk.getCrossChainService()
          .withdrawTrx(123, 1000000);

      System.out.println("Error code desc: " + resp.getDesc());
      System.out.println("transaction result: " + resp.getData().getResult());
      System.out.println("transaction id: " + resp.getData().getTrxId());
    }

    System.out.println("\r\n===================== balance after withdraw ========================");
    {
      SunNetworkResponse<Long> resp = sdk.getMainChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceMain3 = resp.getData();
        System.out.println("main chain balance is:" + resp.getData());
      }

      resp = sdk.getSideChainService().getBalance();
      if (resp.getCode() == ErrorCodeEnum.SUCCESS.getCode()) {
        balanceSide3 = resp.getData();
        System.out.println("side chain balance is:" + resp.getData());
      }

      Assert.assertEquals(balanceMain3, balanceMain2 + 123);
      Assert.assertEquals(balanceSide3, balanceSide2 - 123);
    }

//    DeployContractRequest request = new DeployContractRequest();
//
//    request.setContractName("HelloWorld");
//    request.setAbiStr("[{\"constant\":false,\"inputs\":[],\"name\":\"SayHello\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"string\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"constant\":false,\"inputs\":[{\"name\":\"amount\",\"type\":\"uint256\"}],\"name\":\"update\",\"outputs\":[{\"name\":\"\",\"type\":\"address\"},{\"name\":\"\",\"type\":\"uint256\"}],\"payable\":false,\"stateMutability\":\"nonpayable\",\"type\":\"function\"},{\"anonymous\":false,\"inputs\":[{\"indexed\":false,\"name\":\"addr\",\"type\":\"address\"},{\"indexed\":false,\"name\":\"balance\",\"type\":\"uint256\"}],\"name\":\"test\",\"type\":\"event\"}] ");
//    request.setbyteStr("608060405234801561001057600080fd5b50610293806100206000396000f30060806040526004361061004c576000357c0100000000000000000000000000000000000000000000000000000000900463ffffffff1680633acb31501461005157806382ab890a1461011b575b600080fd5b34801561005d57600080fd5b5061006661018f565b604051808473ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff16815260200180602001838152602001828103825284818151815260200191508051906020019080838360005b838110156100de5780820151818401526020810190506100c3565b50505050905090810190601f16801561010b5780820380516001836020036101000a031916815260200191505b5094505050505060405180910390f35b34801561012757600080fd5b5061014660048036038101908080359060200190929190505050610248565b604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390f35b6000606060007fba14d60620c94b24c2dcf27d840342d0e5dd1d2cd37517005a602a5e75287ef333600054604051808373ffffffffffffffffffffffffffffffffffffffff1673ffffffffffffffffffffffffffffffffffffffff1681526020018281526020019250505060405180910390a1336000546040805190810160405280600a81526020017f48656c6c6f576f726c640000000000000000000000000000000000000000000081525090925092509250909192565b60008082600080828254019250508190555033600054915091509150915600a165627a7a72305820472ddccb68b074fc731403228a7379c49f166b19075e172c1300323c0d4843ee0029");
//    request.setFeeLimit(1000000000);
//    request.setTrx(10000000);
//
//    SunNetworkResponse<DeployContractResponse> result = sdk.getMainChainService().deployContract(request);
//
//    System.out.println("Error num:" + result.getData());
//    System.out.println("result:" + new Gson().toJson(result));

  }
}
