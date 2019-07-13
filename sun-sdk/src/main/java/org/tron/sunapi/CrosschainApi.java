package org.tron.sunapi;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.tron.common.utils.AbiUtil;
import org.tron.core.exception.EncodingException;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunapi.response.TransactionResponse.ResponseType;
import org.tron.sunserver.ServerApi;

public class CrosschainApi {

  public MainchainApi mainchainApi;
  public SidechainApi sidechainApi;
  public ServerApi mainchainServer;
  public ServerApi sidechainServer;

  /**
   * @param mainchainApi the main chain
   * @param sidechainApi the side chain
   * @author sun-network
   */
  public CrosschainApi(MainchainApi mainchainApi, SidechainApi sidechainApi) {
    this.mainchainApi = mainchainApi;
    this.sidechainApi = sidechainApi;

    this.mainchainServer = mainchainApi.getServerApi();
    this.sidechainServer = sidechainApi.getServerApi();
  }

  /**
   * @param trxNum the number of trx
   * @param feeLimit the fee limit
   * @return the result of withdraw trx
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrx(long trxNum, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    byte[] sideGatewayAddress = ServerApi.getSideGatewayAddress();
    if (sideGatewayAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_SIDE_CHAIN_INVALID_GATEWAY);
    }

    String methodStr = "withdrawTRX()";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));

      TransactionResponse result = sidechainServer
          .triggerContract(sideGatewayAddress, trxNum, input, feeLimit, 0, "0");
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

  /**
   * @param tokenId the token ID of trc10
   * @param tokenValue the token value of trc10
   * @param feeLimit the fee limit
   * @return the result of withdraw trc10
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrc10(String tokenId, long tokenValue,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    byte[] sideGatewayAddress = ServerApi.getSideGatewayAddress();
    if (sideGatewayAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_SIDE_CHAIN_INVALID_GATEWAY);
    }

    if (StringUtils.isEmpty(tokenId)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "withdrawTRC10(uint256,uint256)";
    try {
      String inputParam = tokenId + "," + tokenValue;
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
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

  /**
   * @param contractAddrStr the trc20 contract address in side chain
   * @param value the token value of trc20
   * @param feeLimit the fee limit
   * @return the result of withdraw trc20
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrc20(String contractAddrStr, String value,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    //side chain contract address
    byte[] sideAddress = ServerApi.decodeFromBase58Check(contractAddrStr);
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

  /**
   * @param contractAddrStr the trc721 contract address in side chain
   * @param value the token value of trc721
   * @param feeLimit the fee limit
   * @return the result of withdraw trc721
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrc721(String contractAddrStr,
      String value, long feeLimit) {
    return withdrawTrc20(contractAddrStr, value, feeLimit);
  }

  private SunNetworkResponse<TransactionResponse> mappingTrc(String mainGateway, String methodStr,
      String argsStr, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
      byte[] contractAddress = ServerApi.decodeFromBase58Check(mainGateway);

      TransactionResponse result = mainchainServer
          .triggerContract(contractAddress, 0, input, feeLimit, 0, "");
      resp.setData(result);
      if (result.getResult()) {
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

  public SunNetworkResponse<TransactionResponse> mappingTrc20(String mainGateway, String trxHash,
      long feeLimit) {
    if (StringUtils.isEmpty(mainGateway) || StringUtils.isEmpty(trxHash)) {
      SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "mappingTRC20(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    return mappingTrc(mainGateway, methodStr, argsStr, feeLimit);
  }

  public SunNetworkResponse<TransactionResponse> mappingTrc721(String mainGateway, String trxHash,
      long feeLimit) {
    if (StringUtils.isEmpty(mainGateway) || StringUtils.isEmpty(trxHash)) {
      SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "mappingTRC721(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    return mappingTrc(mainGateway, methodStr, argsStr, feeLimit);
  }

  /**
   * @param mainChainGateway the gateway address of main chain
   * @param trxNum the number of trx
   * @param feeLimit the fee limit
   * @return the result of deposit trx
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> depositTrx(String mainChainGateway, long trxNum,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String methodStr = "depositTRX()";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, "", false));
      byte[] contractAddress = ServerApi.decodeFromBase58Check(mainChainGateway);

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

  /**
   * @param mainChainGateway the gateway address of main chain
   * @param tokenId the token id of trc10
   * @param feeLimit the fee limit
   * @return the result of deposit trc10
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> depositTrc10(String mainChainGateway,
      String tokenId, long tokenValue, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String methodStr = "depositTRC10(uint256,uint256)";
    try {
      String inputParam = tokenId + "," + tokenValue;
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, inputParam, false));
      byte[] contractAddress = ServerApi.decodeFromBase58Check(mainChainGateway);

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

  private SunNetworkResponse<TransactionResponse> depositTrc(String contractAddrStr,
      String mainGatewayAddr,
      String methodStr, String depositMethodStr, String num, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
    List<TransactionResponse> dataList = new ArrayList<TransactionResponse>();

    String argsStr = "\"" + mainGatewayAddr + "\",\"" + num + "\"";
    try {
      byte[] input = Hex.decode(AbiUtil.parseMethod(methodStr, argsStr, false));
      byte[] contractAddress = ServerApi.decodeFromBase58Check(contractAddrStr);

      TransactionResponse result = mainchainServer
          .triggerContract(contractAddress, 0, input, feeLimit, 0, "");
      dataList.add(result);
      resp.setDataList(dataList);
      String trxId = result.getTrxId();

      if (org.apache.commons.lang3.StringUtils.isEmpty(trxId) || !result.getResult()) {
        return resp.failed(ErrorCodeEnum.FAILED);
      }

      boolean check = mainchainServer.checkTxInfo(trxId);
      if (check) {
        byte[] depositContractAddr = ServerApi.decodeFromBase58Check(mainGatewayAddr);
        String depositArgStr = "\"" + contractAddrStr + "\"," + num;
        byte[] depositInput = Hex
            .decode(AbiUtil.parseMethod(depositMethodStr, depositArgStr, false));

        result = mainchainServer
            .triggerContract(depositContractAddr, 0, depositInput, feeLimit, 0, "");
        dataList.add(result);
        resp.setDataList(dataList);
        if (result.getResult()) {
          resp.success(dataList);
        } else {
          resp.failed(ErrorCodeEnum.FAILED);
        }
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

  public SunNetworkResponse<TransactionResponse> depositTrc20(String contractAddrStr,
      String mainGatewayAddr, String num, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    if (StringUtils.isEmpty(contractAddrStr) || StringUtils.isEmpty(mainGatewayAddr)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "approve(address,uint256)";
    String depositMethodStr = "depositTRC20(address,uint256)";

    return depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit);
  }

  public SunNetworkResponse<TransactionResponse> depositTrc721(String contractAddrStr,
      String mainGatewayAddr, String num, long feeLimit) {

    if (StringUtils.isEmpty(contractAddrStr) || StringUtils.isEmpty(mainGatewayAddr)) {
      SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "approve(address,uint256)";
    String depositMethodStr = "depositTRC721(address,uint256)";

    return depositTrc(contractAddrStr, mainGatewayAddr, methodStr, depositMethodStr, num, feeLimit);
  }


}
