package org.tron.common.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.tron.common.exception.RpcConnectException;
import org.tron.common.utils.WalletUtil;
import org.tron.service.task.EventTask;
import org.tron.service.task.InitTask;

@Slf4j(topic = "app")
@Configuration
@Import(CommonConfig.class)
public class DefaultConfig {


  @Autowired
  public ApplicationContext appCtx;

  @Autowired
  public CommonConfig commonConfig;

  @Bean
  public InitTask InitTask() {
    return new InitTask(10);
  }

  @Bean
  public EventTask EventTask() {
    Args arg = Args.getInstance();
    String mainGateway = WalletUtil.encode58Check(arg.getMainchainGateway());
    String sideGateway = WalletUtil.encode58Check(arg.getSidechainGateway());
    return new EventTask(mainGateway,sideGateway);

  }

  public DefaultConfig() {
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception", e));
  }
}
