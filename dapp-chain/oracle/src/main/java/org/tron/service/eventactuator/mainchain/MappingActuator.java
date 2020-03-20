package org.tron.service.eventactuator.mainchain;

import lombok.extern.slf4j.Slf4j;
import org.tron.service.eventactuator.Actuator;

@Slf4j(topic = "sideChainTask")
public abstract class MappingActuator extends Actuator {
  public Actuator getNextActuator() {
    return null;
  }
}
