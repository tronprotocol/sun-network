package org.tron.common.application;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class ApplicationHandler implements ApplicationContextAware {

  private static TronApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = (TronApplicationContext) applicationContext;
  }

  public static void closeSelf() {
    try {
      applicationContext.destroy();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    try {
      applicationContext.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }

    System.exit(1);
  }
}
