package org.tron.common.config.args;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

public class SeedNode {

  @Getter
  @Setter
  private List<String> ipList;
}
