package org.tron.common.utils;

import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "signUtils")
public class SignUtils {

  public static long getDelay(String ownSign, List<String> oracleSigns) {
    int sleepCnt = 0;
    for (String signs : oracleSigns) {
      if (signs.equalsIgnoreCase(ownSign)) {
        break;
      }
      sleepCnt++;
    }
    return sleepCnt * 3_000L;
  }
}
