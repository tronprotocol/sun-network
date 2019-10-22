package org.tron.core.net.message;

import org.tron.protos.Sidechain.EventNetMsg;

public class EventNetMessage extends TronMessage {

  protected EventNetMsg eventNetMsg;

  public EventNetMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.EVENT.asByte();
    this.eventNetMsg = EventNetMsg.parseFrom(data);
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }
}
