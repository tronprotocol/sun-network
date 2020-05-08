package org.tron.service.eventactuator.mainchain;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Objects;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.client.SideChainGatewayApi;
import org.tron.common.utils.ByteArray;
import org.tron.common.utils.WalletUtil;
import org.tron.protos.Protocol.Transaction;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.EventMsg.TaskEnum;
import org.tron.protos.Sidechain.MappingTRC721Event;
import org.tron.service.capsule.TransactionExtensionCapsule;

@Slf4j(topic = "mainChainTask")
public class MappingTRC721Actuator extends MappingActuator {

  private static final String NONCE_TAG = "mapping_";

  private MappingTRC721Event event;
  @Getter
  private EventType type = EventType.MAPPING_TRC721;
  @Getter
  private TaskEnum taskEnum = TaskEnum.SIDE_CHAIN;

  public MappingTRC721Actuator(String contractAddress, String nonce) {
    ByteString contractAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(contractAddress));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = MappingTRC721Event.newBuilder().setContractAddress(contractAddressBS)
        .setContractAddress(contractAddressBS).setNonce(nonceBS).build();
  }

  public MappingTRC721Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MappingTRC721Event.class);
  }

  @Override
  public CreateRet createTransactionExtensionCapsule() {
    if (Objects.nonNull(transactionExtensionCapsule)) {
      return CreateRet.SUCCESS;
    }
    try {
      String contractAddressStr = WalletUtil
          .encode58Check(event.getContractAddress().toByteArray());
      String nonceStr = event.getNonce().toStringUtf8();

      String trcName = MainChainGatewayApi.getTRCName(contractAddressStr);
      String trcSymbol = MainChainGatewayApi.getTRCSymbol(contractAddressStr);
      String contractOwner = MainChainGatewayApi
          .getContractOwner(event.getContractAddress().toByteArray());
      logger.info(
          "MappingTRC721Event, contractAddress: {}, trcName: {}, trcSymbol: {}, contractOwner: {}, nonce: {}.",
          contractAddressStr, trcName, trcSymbol, contractOwner, nonceStr);

      Transaction tx = SideChainGatewayApi
          .multiSignForMappingTRC721(contractAddressStr, trcName, trcSymbol, contractOwner,
              nonceStr);
      this.transactionExtensionCapsule = new TransactionExtensionCapsule(NONCE_TAG + nonceStr, tx,
          0);
      return CreateRet.SUCCESS;
    } catch (Exception e) {
      logger.error("when create transaction extension capsule", e);
      return CreateRet.FAIL;
    }
  }

  @Override
  public EventMsg getMessage() {
    return EventMsg.newBuilder().setParameter(Any.pack(this.event)).setType(getType())
        .setTaskEnum(getTaskEnum()).build();
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
