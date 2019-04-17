package org.tron.common.config;

import java.util.LinkedList;
import java.util.List;
import lombok.Data;

public class SystemSetting {

  public static final SystemSetting INSTANCE = new SystemSetting();
  public static volatile double mineRate;

  public static final long YI = 100000000;
  public static final long TRX = 1_000_000;
  public static final long WAITING_FOR_PAY_COUNTDOWN = 9_000L; // ms
  public static final long CONFIRMING_COUNTDOWN = 1_000L; // ms
  public static final long GAMING_COUNTDOWN = 7_000L; // ms
  public static final long FAIL_COUNTDOWN = 3_000L; // ms
  public static final long KICK_OUT_THRESHOLD = 300_000L; // ms
  public static final long PAID_PLAYER_AT_LEAST = 2;
  public static final long PLAYER_AT_LEAST = 2;
  public static final String DEFAULT_ROOM_NAME = "Lucky Room";
  public static final String DEFAULT_PLAYER_NAME = "Justin";
  public static final int ONE = 1;
  public static final int WAITING_FOR_PLAYER_AT_LEAST = 1;
  public static final int GAME_LOOP_THREADPOOL_CORE_SIZE = 10_000;
  public static final int GAME_LOOP_RETRY_MAX = 10;
  public static final long GAME_LOOP_RETRY_SLEEP = 2_000L; // ms
  public static final long GAME_LOOP_RETRY_RESET_INTERVAL = 120_000L; // ms

  // "https://apilist.tronscan.org/api/account/list";
  // public static final String PAGED_ACCOUNT_URL = "http://127.0.0.1:9084/api/account/list";

  public static final long FEE_LIMIT = 100 * TRX;
  public static final int TOKEN_CALL_VALUE = 0;
  public static final long TOKEN_ID = 0;

  public static final long DAPP_ID_DICE = 1;

  public static final int DB_STATUS_VALID = 1; // 数据库项 status 的 取值,  0: 初始, 1:有效, 2:失效


  private SystemSetting() {
    range = new Range();
  }

  @Data
  public class Range {

    int min = 5;
    int max = 100;
    int duration = 0;
  }

  private List<Range> ranges = new LinkedList<>();
  private Range range;  // 默认的 range

//  private String rtuPswd;

  public synchronized void addRange(int min, int max, int duration) {
    Range range = new Range();
    range.setDuration(duration <= 0 ? 1 : duration);
    range.setMax(max);
    range.setMin(min);
    ranges.add(range);
  }

  public synchronized List<Range> getAllRanges() {
    if (ranges.size() > 0) {
      return ranges;
    }
    List<Range> list = new LinkedList<>();
    list.add(range);
    return list;
  }

  public synchronized void clearRange() {
    ranges.clear();
  }

  public synchronized Range getRange() {
    if (ranges.size() > 1) {
      long minutes = (System.currentTimeMillis() / 60000);
      long allDurations = 0;
      for (Range r : ranges) {
        allDurations += r.duration;
      }
      minutes = minutes % allDurations;
      for (Range r : ranges) {
        if (minutes < r.duration) {
          return r;
        }
        minutes -= r.duration;
      }
    }
    if (ranges.size() == 1) {
      return ranges.get(0);
    }
    return range;
  }

//  public synchronized String getRtuPswd() {
//    return rtuPswd;
//  }
//
//  public synchronized void setRtuPswd(String pswd){
//    rtuPswd = pswd;
//  }
}
