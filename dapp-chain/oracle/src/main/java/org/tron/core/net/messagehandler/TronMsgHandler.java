package org.tron.core.net.messagehandler;

import org.tron.common.exception.P2pException;
import org.tron.core.net.message.TronMessage;
import org.tron.core.net.peer.PeerConnection;

public interface TronMsgHandler {

  void processMessage(PeerConnection peer, TronMessage msg) throws P2pException;

}
