package stest.tron.wallet.dailybuild.exceptionfee;

import java.util.HashMap;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.Test;
import stest.tron.wallet.common.client.Configuration;

@Slf4j
public class Test1 {

  @Test
  public void testSendCoin() {
    logger.info("1111");
    String filePath = "/Users/tron/dev/git/20190225Tron/java-tron/src/test/resources/soliditycode/contractTrcToken036.sol";

    final String compile = Configuration.getByPath("testng.conf")
        .getString("defaultParameter.solidityCompile");

    String outputPath = "src/test/resources/soliditycode/output";

    HashMap<String, String> retMap = new HashMap<>();
    String absolutePath = System.getProperty("user.dir");

    String cmd =
        compile + " --optimize --bin --abi --overwrite " + filePath + " -o "
            + absolutePath + "/" + outputPath;
    logger.debug("cmd: " + cmd);
    String contractName = "IllegalDecorate";


  }

}
