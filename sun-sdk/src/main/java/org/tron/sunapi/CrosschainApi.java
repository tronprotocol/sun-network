package org.tron.sunapi;

import java.io.IOException;
import org.apache.commons.lang.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.tron.common.utils.AbiUtil;
import org.tron.core.exception.CancelException;
import org.tron.core.exception.CipherException;
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
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse();

    byte[] sideGatewayAddress = sidechainServer.getSideGatewayAddress();
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
    } catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    } catch(EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc10(String tokenId, long tokenValue, long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse();

    byte[] sideGatewayAddress = sidechainServer.getSideGatewayAddress();
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
    } catch(IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch(CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
    } catch(EncodingException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_ENCODING);
    } catch (Exception e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_UNKNOWN);
    }

    return resp;
  }

  public SunNetworkResponse<TransactionResponse> withdrawTrc20(String contractAddrStr, String value,
      long feeLimit) {
    SunNetworkResponse<TransactionResponse> resp = new SunNetworkResponse();

    //sidechain contract address
    byte[] sideAddress = sidechainServer.decodeFromBase58Check(contractAddrStr);
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
    } catch (IOException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_IO);
    } catch (CipherException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CIPHER);
    } catch (CancelException e) {
      resp.failed(ErrorCodeEnum.EXCEPTION_CANCEL);
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


}
