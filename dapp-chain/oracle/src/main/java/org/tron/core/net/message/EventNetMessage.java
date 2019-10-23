package org.tron.core.net.message;

import com.google.protobuf.ByteString;
import lombok.Getter;
import org.tron.protos.Sidechain.EventNetMsg;
import org.tron.protos.Sidechain.EventNetMsg.Raw;

public class EventNetMessage extends TronMessage {

  @Getter
  protected EventNetMsg eventNetMsg;

  public EventNetMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.EVENT.asByte();
    this.eventNetMsg = EventNetMsg.parseFrom(data);
  }

  public EventNetMessage(Raw raw, byte[] rawSignature) {

    this.eventNetMsg = EventNetMsg.newBuilder().setRaw(raw)
        .setRawSignature(ByteString.copyFrom(rawSignature)).build();
    this.type = MessageTypes.EVENT.asByte();
    this.data = eventNetMsg.toByteArray();
  }

  public EventNetMessage(EventNetMsg msg) {
    this.eventNetMsg = msg;
    this.type = MessageTypes.EVENT.asByte();
    this.data = msg.toByteArray();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }
}
