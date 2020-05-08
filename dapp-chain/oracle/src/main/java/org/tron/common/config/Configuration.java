package org.tron.common.config;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Configuration {

  private static Config config;

  private static final Logger logger = LoggerFactory.getLogger("Configuration");

  /**
   * Get configuration by a given path.
   *
   * @param configurationPath path to configuration file
   * @return loaded configuration
   */
  static Config getByPath(final String configurationPath) {
    if (Strings.isNullOrEmpty(configurationPath)) {
      throw new IllegalArgumentException("Configuration path is required!");
    }

    if (config == null) {
      File configFile = new File(configurationPath);
      logger.info("config file: " + configFile.getAbsolutePath());
      if (configFile.exists()) {
        try {
          config = ConfigFactory
              .parseReader(new InputStreamReader(new FileInputStream(configFile)));
          logger.info("use user defined config file in current dir");
        } catch (FileNotFoundException e) {
          logger.error("load user defined config file exception: " + e.getMessage());
        }
      } else {
        config = ConfigFactory.load(configurationPath);
        logger.info("user defined config file doesn't exists, use default config file in jar");
      }
    }
    return config;
  }
}
