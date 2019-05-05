package org.tron.service.check;

public class CheckTrans {

  private static CheckTrans instance = new CheckTrans();

  public static CheckTrans getInstance() {
    return instance;
  }

  private CheckTrans() {
  }
}
