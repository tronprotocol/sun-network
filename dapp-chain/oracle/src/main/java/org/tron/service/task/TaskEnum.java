package org.tron.service.task;

public enum TaskEnum {
  MAIN_CHAIN("mainChain"),
  SIDE_CHAIN("sideChain");
  private String name;

  TaskEnum(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
