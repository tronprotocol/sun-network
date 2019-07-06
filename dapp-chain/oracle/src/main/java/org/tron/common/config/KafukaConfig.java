package org.tron.common.config;

import lombok.Getter;

public class KafukaConfig {

  @Getter
  private String username;
  @Getter
  private String password;

  public KafukaConfig(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
