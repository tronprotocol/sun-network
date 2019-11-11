package org.tron.program;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.google.protobuf.ByteString;
import java.io.File;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.tron.common.application.Application;
import org.tron.common.application.ApplicationFactory;
import org.tron.common.application.TronApplicationContext;
import org.tron.core.Constant;
import org.tron.core.Wallet;
import org.tron.core.capsule.AccountCapsule;
import org.tron.core.config.DefaultConfig;
import org.tron.core.config.args.Args;
import org.tron.core.db.Manager;
import org.tron.core.services.RpcApiService;
import org.tron.core.services.WitnessService;
import org.tron.core.services.http.FullNodeHttpApiService;
import org.tron.core.services.interfaceOnSolidity.RpcApiServiceOnSolidity;
import org.tron.core.services.interfaceOnSolidity.http.solidity.HttpApiOnSolidityService;
import org.tron.protos.Protocol;

@Slf4j(topic = "app")
public class FullNode {

  public static void load(String path) {
    try {
      File file = new File(path);
      if (!file.exists() || !file.isFile() || !file.canRead()) {
        return;
      }
      LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
      JoranConfigurator configurator = new JoranConfigurator();
      configurator.setContext(lc);
      lc.reset();
      configurator.doConfigure(file);
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  /**
   * Start the FullNode.
   */
  public static void main(String[] args) {
    logger.info("Full node running.");
    Args.setParam(args, Constant.TESTNET_CONF);
    Args cfgArgs = Args.getInstance();

    load(cfgArgs.getLogbackPath());

    if (cfgArgs.isHelp()) {
      logger.info("Here is the help message.");
      return;
    }

    if (Args.getInstance().isDebug()) {
      logger.info("in debug mode, it won't check energy time");
    } else {
      logger.info("not in debug mode, it will check energy time");
    }

    DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
    beanFactory.setAllowCircularReferences(false);
    TronApplicationContext context =
        new TronApplicationContext(beanFactory);
    context.register(DefaultConfig.class);

    context.refresh();
    Application appT = ApplicationFactory.create(context);
    Manager manager = context.getBean(Manager.class);
    AccountCapsule existAccount = manager.getAccountStore()
        .get(Wallet.decodeFromBase58Check("TJEuSMoC7tbs99XkbGhSDk7cM1xnxR931s"));
    if (existAccount == null) {
      long start = 1547532000000L;
      int interval = 300000;
      long next = start;
      while (next < System.currentTimeMillis()) {
        next += interval;
      }
      manager.getDynamicPropertiesStore().saveMaintenanceTimeInterval(interval);
      manager.getDynamicPropertiesStore().saveNextMaintenanceTime(next);
      int ProposalExpireTime = 300000;
      manager.getDynamicPropertiesStore().saveProposalExpireTime(ProposalExpireTime);
    }
    shutdown(appT);

    mockWitness(context);

    RpcApiService rpcApiService = context.getBean(RpcApiService.class);
    appT.addService(rpcApiService);
    if (cfgArgs.isWitness()) {
      appT.addService(new WitnessService(appT, context));
    }

    FullNodeHttpApiService httpApiService = context.getBean(FullNodeHttpApiService.class);
    appT.addService(httpApiService);

    if (Args.getInstance().getStorage().getDbVersion() == 2) {
      RpcApiServiceOnSolidity rpcApiServiceOnSolidity = context
          .getBean(RpcApiServiceOnSolidity.class);
      appT.addService(rpcApiServiceOnSolidity);
      HttpApiOnSolidityService httpApiOnSolidityService = context
          .getBean(HttpApiOnSolidityService.class);
      appT.addService(httpApiOnSolidityService);
    }

    appT.initServices(cfgArgs);
    appT.startServices();
    appT.startup();

    rpcApiService.blockUntilShutdown();
  }

  public static void shutdown(final Application app) {
    logger.info("********register application shutdown hook********");
    Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));
  }

  private static void mockWitness(TronApplicationContext context) {
    Manager manager = context.getBean(Manager.class);
    String[] localWitnesses = {
        "TJEuSMoC7tbs99XkbGhSDk7cM1xnxR931s",
        "TSYvXycFYNvtyLWciaoqmCHBZFsSNBoATJ",
        "TAf7g6tTJyhAycXhd72DyCREn8YnjZdXdB",
        "TUiLejEL9F5ih7MeuuqPL7c5WPBUYyFYiR",
        "TYbR1QfaKAM2avMjQuwqytsEL7c6d3fhQh"

    };

    AccountCapsule existAccount = manager.getAccountStore()
        .get(Wallet.decodeFromBase58Check(localWitnesses[4]));
    if (existAccount != null) {
      logger.info("Not mock witness, not the first time to kill");
      return;
    }

    logger.info("Try to mock witness");

    manager.getWitnessStore().getAllWitnesses().forEach(witnessCapsule -> {
      manager.getWitnessStore().delete(witnessCapsule.getAddress().toByteArray());
    });

    int idx = 0;
    for (String acc : localWitnesses) {
      byte[] address = Wallet.decodeFromBase58Check(acc);

      AccountCapsule account = new AccountCapsule(ByteString.copyFrom(address),
          Protocol.AccountType.Normal);
      account.setBalance(1000000000000000000L);

      long voteCount = 1000_000;
      if (idx == 0) {
        voteCount = 3000_000;
      } else if (idx == 4) {
        voteCount = 5000_000;
      }
      voteCount = 5000_000 + voteCount * 4000;

      account.addVotes(ByteString.copyFrom(address), voteCount);
      context.getBean(Manager.class).getAccountStore().put(address, account);
      manager.insertWitness(address, voteCount, idx++);
    }

    manager.getWitnessController().initWits();
  }
}