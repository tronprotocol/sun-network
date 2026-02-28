package org.tron.client;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.WalletSolidityGrpc;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol.TransactionInfo;

class SolidityRpcClient {

  private WalletSolidityGrpc.WalletSolidityBlockingStub solidityBlockingStub;

  SolidityRpcClient(String target) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
    solidityBlockingStub = WalletSolidityGrpc.newBlockingStub(channel);
  }

  Optional<TransactionInfo> getTransactionInfoById(String txID) {
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(ByteString.copyFrom(ByteArray.fromHexString(txID))).build();
    TransactionInfo transactionInfo = solidityBlockingStub.getTransactionInfoById(request);
    return Optional.ofNullable(transactionInfo);
  }
}
