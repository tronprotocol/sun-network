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
import org.tron.protos.Sidechain.MappingTRC20Event;
import org.tron.service.capsule.TransactionExtensionCapsule;

@Slf4j(topic = "mainChainTask")
public class MappingTRC20Actuator extends MappingActuator {

  private static final String NONCE_TAG = "mapping_";

  private MappingTRC20Event event;
  @Getter
  private EventType type = EventType.MAPPING_TRC20;
  @Getter
  private TaskEnum taskEnum = TaskEnum.SIDE_CHAIN;

  public MappingTRC20Actuator(String contractAddress, String nonce) {
    ByteString contractAddressBS = ByteString
        .copyFrom(WalletUtil.decodeFromBase58Check(contractAddress));
    ByteString nonceBS = ByteString.copyFrom(ByteArray.fromString(nonce));
    this.event = MappingTRC20Event.newBuilder().setContractAddress(contractAddressBS)
        .setNonce(nonceBS).build();
  }

  public MappingTRC20Actuator(EventMsg eventMsg) throws InvalidProtocolBufferException {
    this.event = eventMsg.getParameter().unpack(MappingTRC20Event.class);
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

      long trcDecimals = MainChainGatewayApi.getTRCDecimals(contractAddressStr);
      String trcName = MainChainGatewayApi.getTRCName(contractAddressStr);
      String trcSymbol = MainChainGatewayApi.getTRCSymbol(contractAddressStr);
      logger.info(
          "MappingTRC20Event, contractAddress: {}, trcName: {}, trcSymbol: {}, trcDecimals: {}, nonce: {}.",
          contractAddressStr, trcName, trcSymbol, trcDecimals, nonceStr);
      String contractOwner = MainChainGatewayApi
          .getContractOwner(event.getContractAddress().toByteArray());
      Transaction tx = SideChainGatewayApi
          .multiSignForMappingTRC20(contractAddressStr, trcName, trcSymbol, trcDecimals,
              contractOwner, nonceStr);
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