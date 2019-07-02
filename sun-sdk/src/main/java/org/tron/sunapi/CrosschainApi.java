package org.tron.sunapi;

import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.tron.common.utils.AbiUtil;
import org.tron.core.exception.EncodingException;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunapi.response.TransactionResponse.ResponseType;
import org.tron.sunserver.WalletApi;

public class CrosschainApi {
  public MainchainApi mainchainApi;
  public SidechainApi sidechainApi;
  public WalletApi mainchainServer;
  public WalletApi sidechainServer;

  public CrosschainApi(MainchainApi mainchainApi, SidechainApi sidechainApi) {
    this.mainchainApi = mainchainApi;
    this.sidechainApi = sidechainApi;

    this.mainchainServer = mainchainApi.getServerApi();
    this.sidechainServer = sidechainApi.getServerApi();
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrx(long trxNum, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    byte[] sideGatewayAddress = WalletApi.getSideGatewayAddress();
    if (sideGatewayAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_SIDE_CHAIN_INVALID_GATEWAY);
    }

    String methodStr = "withdrawTRX()";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

      TransactionResponse result = sidechainServer
          .triggerContract(sideGatewayAddress, trxNum, input, feeLimit,0, "0");
      resp.setData(result);
      if (result.getResponseType() == ResponseType.TRANSACTION_NORMAL && result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch(EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc10(String tokenId, long tokenValue, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    byte[] sideGatewayAddress = WalletApi.getSideGatewayAddress();
    if (sideGatewayAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_SIDE_CHAIN_INVALID_GATEWAY);
    }

    if(StringUtils.isEmpty(tokenId)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "withdrawTRC10()";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr,  "", false));
      TransactionResponse result = sidechainServer.
          triggerContract(sideGatewayAddress, 0, input, feeLimit, tokenValue, tokenId);
      resp.setData(result);
      if (result.getResponseType() == ResponseType.TRANSACTION_NORMAL && result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc20(String contractAddrStr, String value,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    //sidechain contract address
    byte[] sideAddress = WalletApi.decodeFromBase58Check(contractAddrStr);
    if (sideAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    String methodStr = "withdrawal(uint256)";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, value, false));

      TransactionResponse result = sidechainServer
          .triggerContract(sideAddress, 0, input, feeLimit, 0, "0");
      resp.setData(result);
      if (result.getResponseType() == ResponseType.TRANSACTION_NORMAL && result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc721(String contractAddrStr,
      String value, long feeLimit) {
    return withdrawTrc20(contractAddrStr, value, feeLimit);
  }

  //  private void mapingTrc(String contractAddrStr, String methodStr, String argsStr, String trxHash, long feeLimit)
//      throws  CipherException, IOException, CancelException, EncodingException{
//    long callValue = 0;
//    long tokenCallValue = 0;
//    String tokenId = "";
//
//    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
//    byte[] contractAddress = WalletApi.decodeFromBase58Check(contractAddrStr);
//
//    boolean result = walletApiWrapper.callContractAndCheck(contractAddress, callValue, input, feeLimit, tokenCallValue, tokenId);
//    if (result) {
//      System.out.println("mappingTrc successfully.\n");
//
//      String mainContractAddress = walletApiWrapper.calcMaincontractAddress(trxHash);
//
//      walletApiWrapper.sideGetMappingAddress(contractAddrStr, mainContractAddress);
//    } else {
//      System.out.println("please confirm the result in side chain after 60s.");
//    }
//  }
//
//  private void mappingTrc20(String[] parameters)
//      throws IOException, CipherException, CancelException, EncodingException {
//    if (parameters == null ||
//        parameters.length != 7) {
//      System.out.println("mapping trc20 needs 6 parameters like following: ");
//      System.out.println("mapping trc20 sideGatewayAddress trxHash name symbol decimal feelmit");
//      return;
//    }
//
//    String contractAddrStr = parameters[1];  //side gateway
//    String methodStr = "deployDAppTRC20AndMapping(bytes,string,string,uint8)";
//    String trxHash = parameters[2];
//    String name = parameters[3];
//    String symbol = parameters[4];
//    String decimal = parameters[5];
//    String argsStr =  "\""+ trxHash + "\",\"" + name + "\",\"" + symbol + "\",\"" + decimal +  "\"";
//    long feeLimit = Long.valueOf(parameters[6]);
//
//    mapingTrc(contractAddrStr, methodStr, argsStr, trxHash, feeLimit);
//  }
//
//
//
//  private void mappingTrc721(String[] parameters)
//      throws IOException, CipherException, CancelException, EncodingException {
//    if (parameters == null || parameters.length != 6) {
//      System.out.println("mapping trc721 needs 5 parameters like following: ");
//      System.out.println("mapping trc721 contractAddress trxHash name symbol feelmit");
//      return;
//    }
//
//    String contractAddrStr = parameters[1];  //side gateway
//    String methodStr = "deployDAppTRC721AndMapping(bytes,string,string)";
//    String trxHash = parameters[2];
//    String name = parameters[3];
//    String symbol = parameters[4];
//    String argsStr = "\"" + trxHash + "\",\"" + name + "\",\"" + symbol + "\"";
//    long feeLimit = Long.valueOf(parameters[5]);
//
//    mapingTrc(contractAddrStr, methodStr, argsStr, trxHash, feeLimit);
//  }
//
//
  public SunNetworkResponse<TransactionResponse> depositTrx(String mainChainGateway, long trxNum,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String methodStr = "depositTRX()";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));
      byte[] contractAddress = WalletApi.decodeFromBase58Check(mainChainGateway);

      TransactionResponse result = mainchainServer
          .triggerContract(contractAddress, trxNum, input, feeLimit, 0, "");
      resp.setData(result);
      if (result.getResponseType() == ResponseType.TRANSACTION_NORMAL && result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> depositTrc10(String mainChainGateway,
      String tokenId, long tokenValue, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String methodStr = "depositTRC10()";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));
      byte[] contractAddress = WalletApi.decodeFromBase58Check(mainChainGateway);

      TransactionResponse result = mainchainServer
          .triggerContract(contractAddress, 0, input, feeLimit, tokenValue, tokenId);
      resp.setData(result);
      if (result.getResponseType() == ResponseType.TRANSACTION_NORMAL && result.getResult()) {
        resp.success(result);
      } else {
        resp.failed(ErrorCodeEnum.FAILED);
      }
    } catch (EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

//  private void depositTrc(
//      String contractAddrStr, String mainGatewayAddr, String methodStr,
//      String depositMethodStr, String num, long feeLimit)
//      throws  CipherException, IOException, CancelException, EncodingException{
//    long callValue = 0;
//    long tokenCallValue = 0;
//    String tokenId = "";
//    String argsStr = "\"" + mainGatewayAddr + "\",\"" + num + "\"";
//
//    byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
//    byte[] contractAddress = WalletApi.decodeFromBase58Check(contractAddrStr);
//
//    boolean result = walletApiWrapper.callContractAndCheck(contractAddress, callValue, input, feeLimit, tokenCallValue, tokenId);
//    if (result) {
//      System.out.println("approve successfully.\n");
//
//      byte[] depositContractAddr =  WalletApi.decodeFromBase58Check(mainGatewayAddr);
//      String depositArgStr = num + ",\"" + contractAddrStr + "\"";
//      byte[] depositInput = Hex.decode(AbiUtil.parseMethod(depositMethodStr, depositArgStr , false));
//
//      boolean ret =  walletApiWrapper.callContract(depositContractAddr, callValue, depositInput, feeLimit, tokenCallValue, tokenId);
//      if (ret) {
//        System.out.println("Broadcast the depositTrc successfully.\n"
//            + "Please check the given transaction id to get the result on blockchain using getTransactionInfoById command");
//      } else {
//        System.out.println("Broadcast the depositTrc failed");
//      }
//    } else {
//      System.out.println("approve failed!!");
//    }
//  }
//
//  private void depositTrc20(String[] parameters)
//      throws IOException, CipherException, CancelException, EncodingException {
//    if (parameters == null || parameters.length != 5) {
//      System.out.println("deposit trc20 needs 4 parameters like following: ");
//      System.out.println("deposit trc20 trc20ContractAddress mainGatewayAddress num feelmit");
//      return;
//    }
//
//    String contractAddrStr = parameters[1];  //main trc20 contract address
//    String methodStr = "approve(address,uint256)";
//    String mainGatewayAddr = parameters[2]; //main gateway contract address
//    String num = parameters[3];
//    String depositMethodStr = "depositTRC20(uint256,address)";
//
//    long feeLimit = Long.valueOf(parameters[4]);
//
//    depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit);
//  }
//
//  private void depositTrc721(String[] parameters)
//      throws IOException, CipherException, CancelException, EncodingException {
//    if (parameters == null || parameters.length != 5) {
//      System.out.println("deposit trc721 needs 4 parameters like following: ");
//      System.out.println("deposit trc721 trc721ContractAddress mainGatewayAddress num feelmit");
//      return;
//    }
//
//    String contractAddrStr = parameters[1];  //main trc20 contract address
//    String methodStr = "approve(address,uint256)";
//    String mainGatewayAddr = parameters[2]; //main gateway contract address
//    String num = parameters[3];
//    String depositMethodStr = "depositTRC721(uint256,address)";
//
//    long feeLimit = Long.valueOf(parameters[4]);
//
//    depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit);
//
//  }


}
