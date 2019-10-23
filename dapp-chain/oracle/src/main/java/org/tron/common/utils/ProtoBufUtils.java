package org.tron.common.utils;

import com.google.protobuf.Any;
import org.tron.protos.Sidechain.DepositTRC10Event;
import org.tron.protos.Sidechain.DepositTRC20Event;
import org.tron.protos.Sidechain.DepositTRC721Event;
import org.tron.protos.Sidechain.DepositTRXEvent;
import org.tron.protos.Sidechain.EventMsg;
import org.tron.protos.Sidechain.EventMsg.EventType;
import org.tron.protos.Sidechain.MappingTRC20Event;
import org.tron.protos.Sidechain.MappingTRC721Event;
import org.tron.protos.Sidechain.WithdrawTRC10Event;
import org.tron.protos.Sidechain.WithdrawTRC20Event;
import org.tron.protos.Sidechain.WithdrawTRC721Event;
import org.tron.protos.Sidechain.WithdrawTRXEvent;

public class ProtoBufUtils {

  public static String unpackNonce(EventMsg eventMsg) {
    Any any = eventMsg.getParameter();
    try {
      switch (eventMsg.getType().getNumber()) {
        case EventType.DEPOSIT_TRC10_EVENT_VALUE:
          DepositTRC10Event depositTRC10Event = any.unpack(DepositTRC10Event.class);
          return new String(depositTRC10Event.getNonce().toByteArray());
        case EventType.DEPOSIT_TRC20_EVENT_VALUE:
          DepositTRC20Event depositTRC20Event = any.unpack(DepositTRC20Event.class);
          return new String(depositTRC20Event.getNonce().toByteArray());
        case EventType.DEPOSIT_TRC721_EVENT_VALUE:
          DepositTRC721Event depositTRC721Event = any.unpack(DepositTRC721Event.class);
          return new String(depositTRC721Event.getNonce().toByteArray());
        case EventType.DEPOSIT_TRX_EVENT_VALUE:
          DepositTRXEvent depositTRXEvent = any.unpack(DepositTRXEvent.class);
          return new String(depositTRXEvent.getNonce().toByteArray());
        case EventType.MAPPING_TRC20_VALUE:
          MappingTRC20Event mappingTRC20Event = any.unpack(MappingTRC20Event.class);
          return new String(mappingTRC20Event.getNonce().toByteArray());
        case EventType.MAPPING_TRC721_VALUE:
          MappingTRC721Event mappingTRC721Event = any.unpack(MappingTRC721Event.class);
          return new String(mappingTRC721Event.getNonce().toByteArray());
        case EventType.WITHDRAW_TRC10_EVENT_VALUE:
          WithdrawTRC10Event withdrawTRC10Event = any.unpack(WithdrawTRC10Event.class);
          return new String(withdrawTRC10Event.getNonce().toByteArray());
        case EventType.WITHDRAW_TRC20_EVENT_VALUE:
          WithdrawTRC20Event withdrawTRC20Event = any.unpack(WithdrawTRC20Event.class);
          return new String(withdrawTRC20Event.getNonce().toByteArray());
        case EventType.WITHDRAW_TRC721_EVENT_VALUE:
          WithdrawTRC721Event withdrawTRC721Event = any.unpack(WithdrawTRC721Event.class);
          return new String(withdrawTRC721Event.getNonce().toByteArray());
        case EventType.WITHDRAW_TRX_EVENT_VALUE:
          WithdrawTRXEvent withdrawTRXEvent = any.unpack(WithdrawTRXEvent.class);
          return new String(withdrawTRXEvent.getNonce().toByteArray());
        default:
          return null;
      }
    } catch (Exception e) {
      return null;
    }
  }
}
