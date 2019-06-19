package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MappingTRC721Event;
import org.tron.protos.Sidechain.TaskEnum;
import org.tron.service.check.TransactionExtensionCapsule;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "mainChainTask")
public class MappingTRC721Actuator extends Actuator {

  private static final String NONCE_TAG = "mapping_";

  private MappingTRC721Event event;
  @Getter
  private EventType type = EventType.MAPPING_TRC721;

  public MappingTRC721Actuator(String contractAddress, String nonce) {
    ByteString contractAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(contractAddress));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromHexString(nonce));
    this.event = MappingTRC721Event.newBuilder().setContractAddress(contractAddressBS)
        .setContractAddress(contractAddressBS).setNonce(nonceBS).build();
  }

  public MappingTRC721Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MappingTRC721Event.class);
  }

  @Override
  public TransactionExtensionCapsule createTransactionExtensionCapsule()
      throws RpcConnectException {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return transactionExtensionCapsule;
    }
    String contractAddressStr = WalletUtil.encode58Check(event.getContractAddress().toByteArray());
    String nonceStr = ByteArray.toHexString(event.getNonce().toByteArray());

    String trcName = MainChainGatewayApi.getTRCName(contractAddressStr);
    String trcSymbol = MainChainGatewayApi.getTRCSymbol(contractAddressStr);
    logger.info(
        "MappingTRC721Event, contractAddress: {}, trcName: {}, trcSymbol: {}, nonce: {}.",
        contractAddressStr, trcName, trcSymbol, nonceStr);

    Transaction tx = SideChainGatewayApi
        .multiSignForMappingTRC721(contractAddressStr, trcName, trcSymbol,
            nonceStr);
    this.transactionExtensionCapsule = new TransactionExtensionCapsule(TaskEnum.SIDE_CHAIN,
        NONCE_TAG + nonceStr, tx);
    return this.transactionExtensionCapsule;
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType()).build();
  }

  @Override
  public byte[] getNonceKey() {
    return ByteArray.fromString(NONCE_TAG + event.getNonce().toStringUtf8());
  }

  @Override
  public byte[] getNonce() {
    return event.getNonce().toByteArray();
  }

}
