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
    ByteString bsTxid = ByteString.copyFrom(ByteArray.fromHexString(txID));
    BytesMessage request = BytesMessage.newBuilder().setValue(bsTxid).build();
    TransactionInfo transactionInfo = blockingStub.getTransactionInfoById(request);
    return Optional.ofNullable(transactionInfo);
  }

  public boolean broadcastTransaction(Transaction signaturedTransaction) {
    int i = 10;
    Return response = blockingStub.broadcastTransaction(signaturedTransaction);
    while (response.getResult() == false && response.getCode() == response_code.SERVER_BUSY
        && i > 0) {
      i--;
      response = blockingStub.broadcastTransaction(signaturedTransaction);
      logger.info("repeate times = " + (11 - i));
      try {
        Thread.sleep(300);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    if (!response.getResult()) {
      logger.info("Code = " + response.getCode());
      logger.info("Message = " + response.getMessage().toStringUtf8());
    }
    return response.getResult();
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
