package org.tron.sunapi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.tron.common.utils.AbiUtil;
import org.tron.common.utils.AddressUtil;
import org.tron.common.utils.ByteArray;
import org.tron.core.exception.EncodingException;
import org.tron.sunapi.response.TransactionResponse;
import org.tron.sunapi.response.TransactionResponse.ResponseType;
import org.tron.sunserver.ServerApi;

public class CrosschainApi {

  public ServerApi mainchainServer;
  public ServerApi sidechainServer;

  /**
   * @param mainchainApi the main chain
   * @param sidechainApi the side chain
   * @author sun-network
   */
  public CrosschainApi(MainchainApi mainchainApi, SidechainApi sidechainApi) {

    this.mainchainServer = mainchainApi.getServerApi();
    this.sidechainServer = sidechainApi.getServerApi();
  }

  /**
   * @param trxNum   the number of trx
   * @param feeLimit the fee limit
   * @return the result of withdraw trx
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrx(long trxNum, long withdrawFee,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    byte[] sideGatewayAddress = ServerApi.getSideGatewayAddress();
    if (sideGatewayAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_SIDE_CHAIN_INVALID_GATEWAY);
    }

    String methodStr = "withdrawTRX()";
    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, "", false));

      TransactionResponse result = sidechainServer
          .triggerContract(sideGatewayAddress, trxNum + withdrawFee, input, feeLimit, 0, "0");
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
   * @param tokenId    the token ID of trc10
   * @param tokenValue the token value of trc10
   * @param feeLimit   the fee limit
   * @return the result of withdraw trc10
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrc10(String tokenId, long tokenValue,
      long withdrawFee, long feeLimit) {
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
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, inputParam, false));
      TransactionResponse result = sidechainServer.
          triggerContract(sideGatewayAddress, withdrawFee, input, feeLimit, tokenValue, tokenId);
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
   * @param value           the token value of trc20
   * @param feeLimit        the fee limit
   * @return the result of withdraw trc20
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrc20(String contractAddrStr, String value,
      long withdrawFee, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    //side chain contract address
    byte[] sideAddress = AddressUtil.decodeFromBase58Check(contractAddrStr);
    if (sideAddress == null) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_ERROR);
    }

    String methodStr = "withdrawal(uint256)";
    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, value, false));

      TransactionResponse result = sidechainServer
          .triggerContract(sideAddress, withdrawFee, input, feeLimit, 0, "0");
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
   * @param value           the token value of trc721
   * @param feeLimit        the fee limit
   * @return the result of withdraw trc721
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> withdrawTrc721(String contractAddrStr,
      String value, long withdrawFee, long feeLimit) {
    return withdrawTrc20(contractAddrStr, value, withdrawFee, feeLimit);
  }

  private SunNetworkResponse<TransactionResponse> mappingTrc(String methodStr,
      String argsStr, long mappingFee, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, argsStr, false));

      TransactionResponse result = mainchainServer
          .triggerContract(ServerApi.getMainGatewayAddress(), mappingFee, input, feeLimit, 0, "");
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

  public SunNetworkResponse<TransactionResponse> mappingTrc20(String trxHash, long mappingFee,
      long feeLimit) {
    if (StringUtils.isEmpty(trxHash)) {
      SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "mappingTRC20(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    return mappingTrc(methodStr, argsStr, mappingFee, feeLimit);
  }

  public SunNetworkResponse<TransactionResponse> mappingTrc721(String trxHash, long mappingFee,
      long feeLimit) {
    if (StringUtils.isEmpty(trxHash)) {
      SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "mappingTRC721(bytes)";
    String argsStr = "\"" + trxHash + "\"";

    return mappingTrc(methodStr, argsStr, mappingFee, feeLimit);
  }

  /**
   * @param trxNum   the number of trx
   * @param feeLimit the fee limit
   * @return the result of deposit trx
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> depositTrx(long trxNum, long depositFee,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String methodStr = "depositTRX()";
    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, "", false));

      TransactionResponse result = mainchainServer
          .triggerContract(ServerApi.getMainGatewayAddress(), trxNum + depositFee, input, feeLimit,
              0, "");
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
   * @param tokenId  the token id of trc10
   * @param feeLimit the fee limit
   * @return the result of deposit trc10
   * @author sun-network
   */
  public SunNetworkResponse<TransactionResponse> depositTrc10(String tokenId, long tokenValue,
      long depositFee,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    String methodStr = "depositTRC10(uint64,uint64)";
    try {
      String inputParam = tokenId + "," + tokenValue;
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, inputParam, false));

      TransactionResponse result = mainchainServer
          .triggerContract(ServerApi.getMainGatewayAddress(), depositFee, input, feeLimit,
              tokenValue,
              tokenId);
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
      String methodStr, String depositMethodStr, String num, long depositFee, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
    List<TransactionResponse> dataList = new ArrayList<TransactionResponse>();

    String mainGatewayAddr = AddressUtil.encode58Check(ServerApi.getMainGatewayAddress());
    String argsStr = "\"" + mainGatewayAddr + "\",\"" + num + "\"";
    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(methodStr, argsStr, false));
      byte[] contractAddress = AddressUtil.decodeFromBase58Check(contractAddrStr);

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
        String depositArgStr = "\"" + contractAddrStr + "\"," + num;
        byte[] depositInput = ByteArray
            .fromHexString(AbiUtil.parseMethod(depositMethodStr, depositArgStr, false));

        result = mainchainServer
            .triggerContract(ServerApi.getMainGatewayAddress(), depositFee, depositInput, feeLimit,
                0, "");
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

  public SunNetworkResponse<TransactionResponse> depositTrc20(String contractAddrStr, String num,
      long depositFee, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();

    if (StringUtils.isEmpty(contractAddrStr)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "approve(address,uint256)";
    String depositMethodStr = "depositTRC20(address,uint64)";
    BigInteger amount = new BigInteger(num);
    if (amount.compareTo(new BigInteger("18446744073709551615")) > 0) {
      depositMethodStr = "depositTRC20(address,uint256)";
    }
    return depositTrc(contractAddrStr, methodStr, depositMethodStr, num, depositFee, feeLimit);
  }

  public SunNetworkResponse<TransactionResponse> depositTrc721(String contractAddrStr, String num,
      long depositFee, long feeLimit) {

    if (StringUtils.isEmpty(contractAddrStr)) {
      SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String methodStr = "approve(address,uint256)";
    String depositMethodStr = "depositTRC721(address,uint256)";

    return depositTrc(contractAddrStr, methodStr, depositMethodStr, num, depositFee, feeLimit);
  }

  public SunNetworkResponse<TransactionResponse> retryDeposit(String nonce, long retryFee,
      long feeLimit) {

    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
    if (StringUtils.isEmpty(nonce)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }
    String retryMethodStr = "retryDeposit(uint256)";

    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(retryMethodStr, nonce, false));

      TransactionResponse result = mainchainServer
          .triggerContract(ServerApi.getMainGatewayAddress(), retryFee, input, feeLimit, 0, null);
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

  public SunNetworkResponse<TransactionResponse> retryWithdraw(String nonce, long retryFee,
      long feeLimit) {

    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
    if (StringUtils.isEmpty(nonce)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String retryMethodStr = "retryWithdraw(uint256)";

    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(retryMethodStr, nonce, false));

      TransactionResponse result = sidechainServer
          .triggerContract(ServerApi.getSideGatewayAddress(), retryFee, input, feeLimit, 0, null);
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

  public SunNetworkResponse<TransactionResponse> retryMapping(String nonce, long retryFee,
      long feeLimit) {

    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse<TransactionResponse>();
    if (StringUtils.isEmpty(nonce)) {
      return resp.failed(ErrorCodeEnum.COMMON_PARAM_EMPTY);
    }

    String retryMethodStr = "retryMapping(uint256)";

    try {
      byte[] input = ByteArray.fromHexString(AbiUtil.parseMethod(retryMethodStr, nonce, false));

      TransactionResponse result = mainchainServer
          .triggerContract(ServerApi.getMainGatewayAddress(), retryFee, input, feeLimit, 0, null);
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

}
