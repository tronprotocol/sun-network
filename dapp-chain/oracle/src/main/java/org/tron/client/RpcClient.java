package org.tron.client;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.Optional;
import org.tron.api.GrpcAPI.BytesMessage;
import org.tron.api.GrpcAPI.Return;
import org.tron.api.GrpcAPI.TransactionExtention;
import org.tron.api.WalletGrpc;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Contract;
import org.tron.protos.Contract.AssetIssueContract;
import org.tron.protos.Protocol.SmartContract;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Protocol.TransactionInfo;

class RpcClient {


  private WalletGrpc.WalletBlockingStub blockingStub;

  RpcClient(String target) {
    ManagedChannel channel = ManagedChannelBuilder.forTarget(target).usePlaintext(true).build();
    blockingStub = WalletGrpc.newBlockingStub(channel);
  }

  TransactionExtention triggerContract(Contract.TriggerSmartContract request) {
    return blockingStub.triggerContract(request);
  }

  Optional<TransactionInfo> getTransactionInfoById(String txID) {
    BytesMessage request = BytesMessage.newBuilder()
        .setValue(ByteString.copyFrom(ByteArray.fromHexString(txID))).build();
    TransactionInfo transactionInfo = blockingStub.getTransactionInfoById(request);
    return Optional.ofNullable(transactionInfo);
  }

  Optional<Return> broadcastTransaction(Transaction signaturedTransaction) {
    Return response = blockingStub.broadcastTransaction(signaturedTransaction);
    return Optional.ofNullable(response);
  }

  AssetIssueContract getAssetIssueById(String assetId) {
    ByteString assetIdBs = ByteString.copyFrom(assetId.getBytes());
    BytesMessage request = BytesMessage.newBuilder().setValue(assetIdBs).build();
    return blockingStub.getAssetIssueById(request);
  }

  SmartContract getContract(byte[] contractAddress) {
    ByteString assetIdBs = ByteString.copyFrom(contractAddress);
    BytesMessage request = BytesMessage.newBuilder().setValue(assetIdBs).build();
    return blockingStub.getContract(request);
  }
}
