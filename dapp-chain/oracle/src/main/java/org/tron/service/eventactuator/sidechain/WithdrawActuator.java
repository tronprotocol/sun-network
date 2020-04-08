package org.tron.service.eventactuator.sidechain;

import lombok.extern.slf4j.Slf4j;
import org.tron.client.MainChainGatewayApi;
import org.tron.common.utils.ByteArray;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public abstract class WithdrawActuator extends Actuator {
  public Actuator getNextActuator() {
    return null;
  }
}
