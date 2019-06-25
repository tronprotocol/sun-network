package org.tron.common.logger;

import org.slf4j.Logger;

public class LoggerOracle {

  private final Logger logger;

  public LoggerOracle(Logger logger) {
    this.logger = logger;
  }

  public void trace(String s) {
    if (logger.isTraceEnabled()) {
      logger.trace(s);
    }
  }

  public void trace(String s, Object... arguments) {
    if (logger.isTraceEnabled()) {
      logger.trace(s, arguments);
    }
  }

  public void debug(String s) {
    if (logger.isDebugEnabled()) {
      logger.debug(s);
    }
  }

  public void debug(String s, Object... arguments) {
    if (logger.isDebugEnabled()) {
      logger.debug(s, arguments);
    }
  }

  public void info(String s) {
    if (logger.isInfoEnabled()) {
      logger.info(s);
    }
  }

  public void info(String s, Object... arguments) {
    if (logger.isInfoEnabled()) {
      logger.info(s, arguments);
    }
  }

  public void warn(String s) {
    if (logger.isWarnEnabled()) {
      logger.warn(s);
    }
  }

  public void warn(String s, Object... arguments) {
    if (logger.isWarnEnabled()) {
      logger.warn(s, arguments);
    }
  }

  public void error(String s) {
    if (logger.isErrorEnabled()) {
      logger.error(s);
    }
  }

  public void error(String s, Object... arguments) {
    if (logger.isErrorEnabled()) {
      logger.error(s, arguments);
    }
  }
}
