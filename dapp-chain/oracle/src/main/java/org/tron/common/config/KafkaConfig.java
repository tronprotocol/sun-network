package org.tron.common.config;

import lombok.Getter;

public class KafkaConfig {

  @Getter
  private String username;
  @Getter
  private String password;

  public KafkaConfig(String username, String password) {
    this.username = username;
    this.password = password;
  }
}
