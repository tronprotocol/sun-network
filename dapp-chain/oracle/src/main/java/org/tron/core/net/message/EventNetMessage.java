package org.tron.core.net.message;

import lombok.Getter;
import lombok.Setter;
import org.tron.protos.Sidechain.EventMsg;

public class EventNetMessage extends TronMessage {

  protected EventMsg eventMsg;
  @Getter
  @Setter
  private long timestamp;

  @Getter
  @Setter
  private byte[] signature;

  public EventNetMessage(byte[] data) throws Exception {
    super(data);
    this.type = MessageTypes.EVENT.asByte();
    this.eventMsg = EventMsg.parseFrom(data);
  }

  @Override
  public Class<?> getAnswerMessage() {
    return null;
  }
}
