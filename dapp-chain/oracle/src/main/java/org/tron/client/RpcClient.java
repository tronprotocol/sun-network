package org.tron.client;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.tron.api.GrpcAPI.AddressPrKeyPairMessage;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.EmptyMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.Return.response_code;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.common.exception.RpcException;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Protocol.Account;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;

@Slf4j
public class RpcClient {

  private WalletGrpc.WalletBlockingStub blockingStub;

  public RpcClient(String target) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
  }

  public TransactionExtention triggerContract(Contract.TriggerSmartContract request) {
    return blockingStub.triggerContract(request);
  }

  public Optional<TransactionInfo> getTransactionInfoById(String txID) {
    BytesMessage request = BytesMessage.newBuilder()
      .setValue(ByteString.copyFrom(ByteArray.fromHexString(txID))).build();
    TransactionInfo transactionInfo = blockingStub.getTransactionInfoById(request);
    return Optional.ofNullable(transactionInfo);
  }

  public boolean broadcastTransaction(Transaction signaturedTransaction) throws RpcException {

    int maxRetry = 10;
    for (int i = 0; i < maxRetry; i++) {

      Return response = blockingStub.broadcastTransaction(signaturedTransaction);
      if (response.getResult()) {
        // true is success
        return response.getResult();
      } else {
        // false is fail
        if (response.getCode() == response_code.SERVER_BUSY) {
          // when SERVER_BUSY, retry
          logger.info("will retry {} time(s)", i + 1);
          try {
            Thread.sleep(300);
          } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
          }
        } else {
          logger.info("server error, fail, code: {}, message", response.getCode(),
            response.getMessage().toStringUtf8());
          // fail, not retry
          throw new RpcException("server error, fail");
        }
      }
    }
    logger.error("broadcast transaction, exceed max retry, fail");
    throw new RpcException("broadcast transaction, exceed max retry, fail");

  }

  public TransactionExtention createTransaction2(Contract.TransferContract contract) {
    return blockingStub.createTransaction2(contract);
  }

  public Account queryAccount(byte[] address) {
    ByteString addressBS = ByteString.copyFrom(address);
    Account request = Account.newBuilder().setAddress(addressBS).build();
    return blockingStub.getAccount(request);
  }

  public AddressPrKeyPairMessage generateAddress(EmptyMessage emptyMessage) {
    return blockingStub.generateAddress(emptyMessage);
  }
}
