package org.tron.core.net.message;

import com.google.protobuf.ByteString;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventNetMsg;
import org.tron.protos.Sidechain.EventNetMsg.Raw;

public class EventNetMessage extends TronMessage {

  protected EventNetMsg eventNetMsg;

  public EventNetMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.EVENT.asByte();
    this.eventNetMsg = EventNetMsg.parseFrom(data);
  }

  public EventNetMessage(EventMsg event, Raw raw, byte[] rawSignature) {

    eventNetMsg = EventNetMsg.newBuilder().setRaw(
        raw).setRawSignature(ByteString.copyFrom(rawSignature))
        .build();
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }
}
